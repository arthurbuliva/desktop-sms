package com.kentext.ui;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.kentext.common.Common;
import com.kentext.security.Enigma;
import com.kentext.service.Authenticate;
import com.kentext.service.KentextService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class LoginPanel extends JPanel implements Common
{

    private JPanel loginFieldsPanel;
    private JPanel usernamePanel;
    private JPanel passwordPanel;
    private JButton loginButton;
    private JButton cancelLoginButton;
    private JLabel usernameLabel, passwordLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPanel loginButtonsPanel;
    private Authenticate authenticate;

    public LoginPanel()
    {
        initComponents();
    }

    private void initComponents()
    {
        authenticate = new Authenticate();

        setLayout(new BorderLayout());

        final String usernameFieldPlaceholderText = "Phone number without + eg 254722000001";

        loginButton = new JButton("Create Account");
        cancelLoginButton = new JButton("Cancel");

        usernameLabel = new JLabel("Phone:", SwingConstants.RIGHT);
        usernameField = new JTextField("", 15);
        usernameField.setToolTipText(usernameFieldPlaceholderText);

        passwordLabel = new JLabel("Password:", SwingConstants.RIGHT);
        passwordField = new JPasswordField("", 15);

        loginButton = new JButton("Log in");
        cancelLoginButton = new JButton("Cancel");

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

        loginFieldsPanel.add(usernamePanel, BorderLayout.NORTH);
        loginFieldsPanel.add(passwordPanel, BorderLayout.SOUTH);

        loginButtonsPanel = new JPanel();
        loginButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
//        loginButtonsPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        loginButtonsPanel.add(loginButton);
        loginButtonsPanel.add(cancelLoginButton);

        add(loginFieldsPanel, BorderLayout.NORTH);
        add(loginButtonsPanel, BorderLayout.CENTER);

        loginButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                authenticate();
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

        usernameField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    authenticate();
                }
            }
        });

        passwordField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    authenticate();
                }
            }
        });
    }

    private void authenticate()
    {
        loginButton.setEnabled(false);
        loginButton.setText("Please wait");
                
        try
        {
            boolean authenticated = authenticate.authenticateUser(usernameField.getText(), passwordField.getPassword());

            if (authenticated)
            {
                ((JInternalFrame) getRootPane().getParent()).dispose();

                KentextService kentextService = new KentextService();

                JsonArray senderIds = (JsonArray) (kentextService.getAvailableCreditsFromKentext()).get("sender_ids");

                Vector availableSenderIds = new Vector();
                availableSenderIds.add(loadConfigurationFile().getProperty("DEFAULT_KENTEXT_NUMBER"));

                Gson gson = new Gson();
                Enigma enigma = new Enigma();

                for (JsonElement senderId : senderIds)
                {
                    Map<String, String> senderIdMap = new HashMap<String, String>();
                    senderIdMap = (Map<String, String>) gson.fromJson(senderId, senderIdMap.getClass());

                    if (!availableSenderIds.contains(senderIdMap.get("value")))
                    {
                        availableSenderIds.add(senderIdMap.get("value"));
                    }
                }

                System.setProperty("com.kentext.desktop.senderids", enigma.encryptText(String.valueOf(availableSenderIds)));
            }
            else
            {
                JOptionPane.showMessageDialog(
                        usernamePanel,
                        "Invalid username or password. Try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                
                loginButton.setEnabled(true);
                loginButton.setText("Login");
            }
        }
        catch (Exception ex)
        {
            LOGGER.severe(ex.getMessage());
            
            System.exit(1);
        }
    }
}
