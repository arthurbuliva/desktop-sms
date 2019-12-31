package com.kentext.ui;

import com.kentext.common.Common;
import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;

class AuthenticationWindow extends JInternalFrame implements Common
{
    private JTabbedPane tabbedPane;

    private LoginPanel loginPanel;
    private NewUserPanel newUserPanel;

    AuthenticationWindow()
    {
        super(
                "Log in to Kentext Desktop",
                false,
                false,
                false,
                false
        );

        loginPanel = new LoginPanel();
        newUserPanel = new NewUserPanel();

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Log in", loginPanel);
        tabbedPane.addTab("Create Account/Reset Password", newUserPanel);

        getContentPane().add(tabbedPane);

        pack();
    }
}
