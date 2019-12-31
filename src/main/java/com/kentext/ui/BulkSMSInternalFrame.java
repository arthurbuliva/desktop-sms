package com.kentext.ui;

import com.kentext.common.Common;
import com.kentext.security.Enigma;
import com.kentext.service.SMS;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

class BulkSMSInternalFrame extends JInternalFrame implements Common
{
    private JPanel contentPanel, messageActions;
    private JLabel destinationLabel, statusLabel;
    private JTextField destinationField;
    private JButton invokeFileChooserButton;
    private JTextArea messageField;
    private JLabel senderIdLabel;
    private JComboBox availableSenderIds;
    private JScrollPane dataScrollPane;
    private JButton sendNow, sendLater;
    private Enigma enigma;
    private SMS sms;
    

    BulkSMSInternalFrame()
    {
        super(
                "New Bulk SMS",
                true,
                true,
                true,
                true
        );

        setLayout(new BorderLayout());
        setSize(600, 300);

        JPanel destinationPanel = new JPanel();
        destinationPanel.setLayout(new BorderLayout());

        JPanel filesPanel = new JPanel();
        filesPanel.setLayout(new FlowLayout());

        invokeFileChooserButton = new JButton("Choose recipients");

        destinationLabel = new JLabel("Recipients:     ");
        destinationField = new JTextField(30);
        destinationField.setEditable(false);

        destinationPanel.add(destinationLabel, BorderLayout.WEST);

        filesPanel.add(destinationField, BorderLayout.WEST);
        filesPanel.add(invokeFileChooserButton, BorderLayout.EAST);

        destinationPanel.add(filesPanel, BorderLayout.CENTER);

        add(destinationPanel, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout((new BorderLayout()));

        messageField = new JTextArea();
        messageField.setLineWrap(true);
        messageField.setWrapStyleWord(true);

        messageField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                statusLabel.setText(
                        String.format(
                                "%s characters",
                                messageField.getText().length()
                        )
                );
            }
        });

        String decryptedSenderIds = null;

        try
        {
            enigma = new Enigma();
            decryptedSenderIds = enigma.decryptText(System.getProperty("com.kentext.desktop.senderids"));
        }
        catch (IOException | InvalidKeyException | NoSuchAlgorithmException
                | InvalidKeySpecException | BadPaddingException
                | IllegalBlockSizeException | NoSuchPaddingException ex)
        {
             LOGGER.severe(ex.getMessage());
        }

        String[] senderIds = decryptedSenderIds.replace("[", "").replace("]", "").split(", ");

        senderIdLabel = new JLabel("Sender ID:");
        availableSenderIds = new JComboBox(senderIds);

        dataScrollPane = new JScrollPane(messageField);
        contentPanel.add(new JLabel("Message:"), BorderLayout.NORTH);
        contentPanel.add(dataScrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        statusLabel = new JLabel();
        contentPanel.add(statusLabel, BorderLayout.SOUTH);

        messageActions = new JPanel();
        messageActions.setLayout(new FlowLayout(FlowLayout.TRAILING));

        sendNow = new JButton("Send Now");
        sendLater = new JButton("Send Later");

        invokeFileChooserButton.addActionListener(
                new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        JFileChooser contactsChooser = new JFileChooser();
                        contactsChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                        int option = contactsChooser.showOpenDialog(null);

                        if (option == JFileChooser.APPROVE_OPTION)
                        {
                            File file = contactsChooser.getSelectedFile();

                            try
                            {
                                destinationField.setBackground(destinationField.getDisabledTextColor());
                                destinationField.setText(file.getCanonicalPath());
                            }
                            catch (IOException ex)
                            {
                                LOGGER.severe(ex.getMessage());
                            }
                        }
                        else
                        {
                            destinationField.setText(null);
                        }
                    }
                }
        );

        sendNow.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(destinationField.getText().isEmpty())
                {
                    destinationField.setBackground(ERROR_COLOR);
                }
                else
                {
                    try
                    {
                        sms = new SMS();

                        ArrayList<String> contacts = new ArrayList<>();

                        try (BufferedReader br = new BufferedReader(new FileReader(destinationField.getText())))
                        {
                            while (br.ready())
                            {
                                String number = br.readLine();

                                Matcher matcher = NUMBER_PATTERN.matcher(number);

                                if (matcher.matches())
                                {
                                    contacts.add(number);
                                }
                            }
                        }
                        catch (Exception ex)
                        {
                            JOptionPane.showMessageDialog(null,
                                    ex.getMessage(),
                                    "Kentext SMS Manager",
                                    JOptionPane.ERROR_MESSAGE);
                        }

                        String origin = (String) availableSenderIds.getItemAt(availableSenderIds.getSelectedIndex());

                        for (String contact : contacts)
                        {
                            sms.sendMessage(
                                origin,
                                contact,
                                messageField.getText(),
                                LocalDateTime.now(),
                                0
                            );
                        }

                        JOptionPane.showMessageDialog(null,
                                "Messages queued to be sent",
                                "Kentext SMS Manager",
                                JOptionPane.INFORMATION_MESSAGE);

                        destinationField.setText(null);
                        messageField.setText(null);
                    }
                    catch (Exception ex)
                    {
                        LOGGER.severe(ex.getMessage());
                    }
                }
            }
        });

        sendLater.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(destinationField.getText().isEmpty())
                {
                    destinationField.setBackground(ERROR_COLOR);
                }
                else
                {
                    DateTimeSelector dateTimeSelector = new DateTimeSelector();
                    String sendDate = dateTimeSelector.getDateTime();

                    if (sendDate != null)
                    {
                        try
                        {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                            LocalDateTime dateTime = LocalDateTime.parse(sendDate, formatter);

                            sms = new SMS();

                            ArrayList<String> contacts = new ArrayList<>();

                            try (BufferedReader br = new BufferedReader(new FileReader(destinationField.getText())))
                            {
                                while (br.ready())
                                {
                                    String number = br.readLine();

                                    Matcher matcher = NUMBER_PATTERN.matcher(number);

                                    if (matcher.matches())
                                    {
                                        contacts.add(number);
                                    }
                                }
                            }
                            catch (Exception ex)
                            {
                                JOptionPane.showMessageDialog(null,
                                        ex.getMessage(),
                                        "Kentext SMS Manager",
                                        JOptionPane.ERROR_MESSAGE);
                            }

                            String origin = (String) availableSenderIds.getItemAt(availableSenderIds.getSelectedIndex());

                            for (String contact : contacts)
                            {
                                sms.sendMessage(
                                    origin,
                                    contact,
                                    messageField.getText(),
                                    dateTime,
                                    0
                                );
                            }

                            JOptionPane.showMessageDialog(null,
                                    "Messages queued to be sent",
                                    "Kentext SMS Manager",
                                    JOptionPane.INFORMATION_MESSAGE);

                            destinationField.setText(null);
                            messageField.setText(null);
                        }
                        catch (Exception ex)
                        {
                            JOptionPane.showMessageDialog(null,
                                    ex.getMessage(),
                                    "Kentext SMS Manager",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        messageActions.add(senderIdLabel);
        messageActions.add(availableSenderIds);

        messageActions.add(sendNow);
        messageActions.add(sendLater);

        add(messageActions, BorderLayout.SOUTH);
    }
}
