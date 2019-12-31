package com.kentext.ui;

import com.kentext.common.Common;
import com.kentext.security.Enigma;
import com.kentext.service.Authenticate;
import com.kentext.service.SMS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class NewUserPanel extends JPanel implements Common
{
    private JPanel loginFieldsPanel;
    private JPanel usernamePanel;
    private JPanel passwordPanel;
    private JPanel confirmPasswordPanel;
    private JButton createAccountButton;
    private JButton cancelLoginButton;
    private JLabel usernameLabel, passwordLabel, confirmPasswordLabel;
    private JTextField usernameField;
    private JPasswordField passwordField, confirmPasswordField;
    private JPanel loginButtonsPanel;
    private Authenticate authenticate;
    private SMS sms;
    private Enigma enigma;

    public NewUserPanel()
    {
        initComponents();
    }

    private void initComponents()
    {
        authenticate = new Authenticate();

        try
        {
            sms = new SMS();
            enigma = new Enigma();
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException ex)
        {
            LOGGER.severe(ex.getMessage());
        }

        setLayout(new BorderLayout());

        final String usernameFieldPlaceholderText = "Phone number without + eg 254722000001";

        createAccountButton = new JButton("Add User");
        cancelLoginButton = new JButton("Cancel");

        usernameLabel = new JLabel("Phone:", SwingConstants.RIGHT);
        usernameField = new JTextField("", 15);
        usernameField.setToolTipText(usernameFieldPlaceholderText);

        passwordLabel = new JLabel("Password:", SwingConstants.RIGHT);
        passwordField = new JPasswordField("", 15);
        confirmPasswordLabel = new JLabel("Repeat password:", SwingConstants.RIGHT);
        confirmPasswordField = new JPasswordField("", 15);

        loginFieldsPanel = new JPanel();
        loginFieldsPanel.setLayout(new BorderLayout());

        usernamePanel = new JPanel();
        usernamePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);

        passwordPanel = new JPanel();
        passwordPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);

        confirmPasswordPanel = new JPanel();
        confirmPasswordPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        confirmPasswordPanel.add(confirmPasswordLabel);
        confirmPasswordPanel.add(confirmPasswordField);

        loginFieldsPanel.add(usernamePanel, BorderLayout.NORTH);
        loginFieldsPanel.add(passwordPanel, BorderLayout.CENTER);
        loginFieldsPanel.add(confirmPasswordPanel, BorderLayout.SOUTH);

        loginButtonsPanel = new JPanel();
        loginButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        loginButtonsPanel.add(createAccountButton);
        loginButtonsPanel.add(cancelLoginButton);

        add(loginFieldsPanel, BorderLayout.CENTER);
        add(loginButtonsPanel, BorderLayout.SOUTH);

        createAccountButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(Arrays.equals(passwordField.getPassword(), confirmPasswordField.getPassword()))
                {
                    try
                    {
                        System.setProperty("com.kentext.desktop.mynumber", enigma.encryptText(
                            loadConfigurationFile().getProperty("DEFAULT_KENTEXT_NUMBER")
                        ));
                        System.setProperty("com.kentext.desktop.authkey", enigma.encryptText(String.valueOf(confirmPasswordField.getPassword())));

                        boolean processed = authenticate.processNewUser(usernameField.getText(),
                                new String(passwordField.getPassword()));

                        if (processed)
                        {
                            String welcomeMessage =
                                    "<html>Congratulations! Your local account has been successfully created!<br><br>" +
                                    "In order to complete the registration, Kentext needs to allocate SMS credits to you.<br>" +
                                    "Please go to <a href='http://kentext.com/user/register'>http://kentext.com/user/register</a> " +
                                    "and create an account.<br><br>" +
                                    "After successfully creating an account you will be able to use Kentext Desktop.<br><br>" +
                                    "<strong>Please note</strong>: use the same username and password that you have just used<br><br></html>";

                            JOptionPane.showMessageDialog(
                                    usernamePanel,
                                    welcomeMessage,
                                    "Kentext Desktop",
                                    JOptionPane.INFORMATION_MESSAGE
                            );

                            final Container loginParent = NewUserPanel.this.getParent();
                            loginParent.remove(NewUserPanel.this);

                            loginParent.repaint();
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(
                                    usernamePanel,
                                    "Error creating new user. Try again.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                    catch (HeadlessException | IOException | InvalidKeyException
                            | NoSuchAlgorithmException | InvalidKeySpecException
                            | BadPaddingException | IllegalBlockSizeException ex)
                    {
                        LOGGER.severe(ex.getMessage());
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(
                            usernamePanel,
                            "Passwords do not match. Try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });


        cancelLoginButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });
    }
}
