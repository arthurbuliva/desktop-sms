package com.kentext.service;

import com.kentext.common.Common;
import com.kentext.db.Users;
import com.kentext.security.Enigma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;
import javax.crypto.NoSuchPaddingException;

public class Authenticate implements Common
{
    private Users users;
    private SMS sms;

    public Authenticate()
    {
        users = new Users();

        try
        {
            sms = new SMS();
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException ex)
        {
            LOGGER.severe(ex.getMessage());
        }
    }

    public boolean authenticateUser(String username, char[] password)
    {
        Users userData = users.retrieveUser(username);

        try (Enigma enigma = new Enigma())
        {
            char[] storedPassword = userData.getPassword().toCharArray();
            char[] userPassword = (enigma.encryptText(String.valueOf(password))).toCharArray();

            if (Arrays.equals(storedPassword, userPassword))
            {
                System.setProperty("com.kentext.desktop.mynumber", enigma.encryptText(username));
                System.setProperty("com.kentext.desktop.authkey", enigma.encryptText(String.valueOf(password)));

                return true;
            }
            else
            {
                return false;
            }
        }
        catch (java.lang.NullPointerException ex)
        {
            LOGGER.severe(ex.getMessage());
            
            return false;
        }

        catch (Exception ex)
        {
            LOGGER.severe(ex.getMessage());
            
            return false;
        }
    }

    private String generateToken()
    {
        final char[] ALLOWED_CHARACTERS =
                "ABCDEFGHJKLMNPRTUVWXYZ2346789".toCharArray();

        StringBuilder randomStringBuilder = new StringBuilder();

        int TOKEN_LENGTH = 7;
        while (randomStringBuilder.length() < TOKEN_LENGTH)
        {
            randomStringBuilder.append(
                    ALLOWED_CHARACTERS[
                            new Random().nextInt(ALLOWED_CHARACTERS.length)
                            ]
            );
        }

        return randomStringBuilder.toString();
    }

    public boolean processNewUser(String username, String password)
    {
        //Step 1: Generate a random authentication token and encrypt it
        String unencryptedToken = generateToken();

        try (Enigma enigma = new Enigma())
        {

            //Step 2: Insert this user as new in the database with active flag 0
            users.createNewUser(
                    username,
                    password,
                    enigma.encryptText(unencryptedToken),
                    null,
                    0
            );

            // Step 3: SMS this user with the token and wait for validation
            sms.sendMessage(username, username, unencryptedToken, LocalDateTime.now(), 1);

            String expected = unencryptedToken;
            String given = "";

            JTextField verificationField = new JTextField(20);
            JLabel errorLabel = new JLabel();

            String[] options =
                    {
                            "Resend Verification Code",
                            "Verify",
                            "Cancel",
                    };

            String message = "Enter the verification code you received on your phone";

            Object[] params = {
                    "<html><h3>" + message + "</h3></html>",
                    verificationField,
                    errorLabel
            };

            KeyboardFocusManager
                    .getCurrentKeyboardFocusManager()
                    .addKeyEventDispatcher(
                            new KeyEventDispatcher()
                            {
                                @Override
                                public boolean dispatchKeyEvent(KeyEvent e) {
                                    boolean keyHandled = false;
                                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                                        verificationField.setBackground(DEFAULT_COLOR);
                                        verificationField.setForeground(DEFAULT_FOREGROUND_COLOR);
                                        errorLabel.setText(null);
                                        if (e.getKeyCode() == KeyEvent.VK_ENTER)
                                        {
                                            //                                ok();
                                            keyHandled = true;
                                        }
                                        else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                                        {
                                            //                                cancel();
                                            keyHandled = true;
                                        }
                                    }
                                    return keyHandled;
                                }
                            }
                    );

            while (!given.equals(expected))
            {
                int answer = JOptionPane.showOptionDialog(
                        null,
                        params,
                        "Kentext SMS Desktop",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,     //do not use a custom Icon
                        options,  //the titles of buttons
                        options[JOptionPane.NO_OPTION]); //d

                switch (answer)
                {
                    case JOptionPane.CANCEL_OPTION:
                    {
                        System.exit(0);
                    }
                    break;
                    case JOptionPane.NO_OPTION:
                    {
                        given = verificationField.getText();
                        verificationField.setBackground(ERROR_COLOR);
                        errorLabel.setText("<html><small>Invalid token. Please try again</small></html>");
                    }
                    break;
                    case JOptionPane.YES_OPTION: // Resend
                    {
                        sms.sendMessage(username, username, unencryptedToken, LocalDateTime.now(), 1);

                        verificationField.setText(null);
                        verificationField.setBackground(DEFAULT_COLOR);
                        verificationField.setForeground(DEFAULT_FOREGROUND_COLOR);
                    }
                    break;
                    default:
                    {
                        verificationField.setBackground(DEFAULT_COLOR);
                        verificationField.setForeground(DEFAULT_FOREGROUND_COLOR);
                    }
                }
            }

            //Step 5:: If all valid, delete the token and update user as active

            return users.activateUser(username);
        }
        catch (Exception ex)
        {
            LOGGER.severe(ex.getMessage());
        }

        return false;
    }
}
