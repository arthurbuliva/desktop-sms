package com.kentext.ui;

import com.kentext.common.Common;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame implements Common, ActionListener
{
    private JMenuBar menuBar;
    private JMenu fileMenu, viewMenu, helpMenu;
    private JMenuItem
            fileQuit,
            helpHelp,
            viewSentItems, viewNewSMS, viewNewBulkSMS;

    private JDesktopPane desktop;

    private SentMessagesInternalFrame sentMessagesInternalFrame;
    private SingleSMSInternalFrame singleSMSInternalFrame;
    private BulkSMSInternalFrame bulkSMSInternalFrame;
    private AuthenticationWindow authenticationWindow;

    public MainWindow()
    {
        initComponents();
        setDefaults();
    }

    private void initComponents()
    {
        fileMenu = new JMenu("File");
        viewMenu = new JMenu("View");
        helpMenu = new JMenu("Help");

        fileQuit = new JMenuItem("Quit");
        fileQuit.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });

        fileMenu.add(fileQuit);

        viewSentItems = new JMenuItem("Sent Items");
        viewNewSMS = new JMenuItem("New SMS");
        viewNewBulkSMS = new JMenuItem("New Bulk SMS");

        viewSentItems.addActionListener(this);
        viewNewSMS.addActionListener(this);
        viewNewBulkSMS.addActionListener((ActionListener) this);

        viewMenu.add(viewNewSMS);
        viewMenu.add(viewNewBulkSMS);
        viewMenu.add(viewSentItems);

        helpHelp = new JMenuItem("About Kentext SMS Manager");

        helpMenu.add(helpHelp);

        menuBar = new JMenuBar();

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        desktop = new JDesktopPane();
        //Make dragging a little faster but perhaps uglier.
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        setContentPane(desktop);

        setSize(700, 400);

        authenticationWindow = new AuthenticationWindow();
        desktop.add(authenticationWindow);

        int x = (this.getWidth() - authenticationWindow.getWidth()) / 2;
        int y = (this.getHeight() - authenticationWindow.getHeight()) / 2;

        authenticationWindow.setLocation(x, y);
        authenticationWindow.setVisible(true);
    }

    private void setDefaults()
    {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setTitle("Kentext SMS Manager");
        setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        if (source instanceof JMenuItem)
        {
            JMenuItem menuItem = (JMenuItem) source;
            String sourceText = menuItem.getText();

            switch (sourceText)
            {
                case "New SMS":
                {
                    if (!authenticationWindow.isVisible())
                    {
                        singleSMSInternalFrame = new SingleSMSInternalFrame();
                        desktop.add(singleSMSInternalFrame);

                        singleSMSInternalFrame.setVisible(true);
                    }
                }
                break;
                case "New Bulk SMS":
                {
                    if (!authenticationWindow.isVisible())
                    {
                        bulkSMSInternalFrame = new BulkSMSInternalFrame();
                        desktop.add(bulkSMSInternalFrame);

                        bulkSMSInternalFrame.setVisible(true);
                    }
                }
                break;
                case "Sent Items":
                {
                    if (!authenticationWindow.isVisible())
                    {
                        sentMessagesInternalFrame = new SentMessagesInternalFrame();
                        desktop.add(sentMessagesInternalFrame);

                        sentMessagesInternalFrame.setVisible(true);
                    }
                }
                break;
                default:
                {
                    authenticationWindow.setVisible(true);
                }
                break;
            }
        }
    }
}
