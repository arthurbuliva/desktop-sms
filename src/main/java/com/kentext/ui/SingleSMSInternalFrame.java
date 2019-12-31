package com.kentext.ui;

import com.kentext.common.Common;
import com.kentext.security.Enigma;
import com.kentext.service.SMS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

class SingleSMSInternalFrame extends JInternalFrame implements Common
{
    private JPanel contentPanel, messageActions;
    private JLabel destinationLabel, statusLabel;
    private JTextField destinationField;
    private JLabel destinationFieldMessage;
    private JLabel senderIdLabel;
    private JComboBox availableSenderIds;
    private JTextArea messageField;
    private JScrollPane dataScrollPane;
    private JButton sendNow, sendLater;
    private Enigma enigma;
    private SMS sms;

    SingleSMSInternalFrame()
    {
        super(
                "New Single SMS",
                true,
                true,
                true,
                true
        );

        setLayout(new BorderLayout());
        setSize(600, 300);

        JPanel destinationPanel = new JPanel();
        destinationPanel.setLayout(new BorderLayout());

        destinationLabel = new JLabel("Destination:     ");
        destinationField = new JTextField();
        destinationFieldMessage = new JLabel("", SwingConstants.RIGHT);

        destinationPanel.add(destinationLabel, BorderLayout.WEST);
        destinationPanel.add(destinationField, BorderLayout.CENTER);
        destinationPanel.add(destinationFieldMessage, BorderLayout.SOUTH);

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

        destinationField.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                Matcher matcher = NUMBER_PATTERN.matcher(destinationField.getText());

                if (matcher.matches())
                {
                    destinationFieldMessage.setText(null);
                }
                else
                {
                    destinationField.setBackground(ERROR_COLOR);
                    destinationFieldMessage.setText("<html><small>Invalid destination. Should be 12 digits starting with 2547</small></html>");
                }
            }

            @Override
            public void focusGained(FocusEvent e)
            {
                destinationField.setBackground(DEFAULT_COLOR);
                destinationField.setForeground(DEFAULT_FOREGROUND_COLOR);
                destinationFieldMessage.setText(null);
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

        sendNow.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Matcher matcher = NUMBER_PATTERN.matcher(destinationField.getText());

                if (matcher.matches())
                {
                    try
                    {
                        sms = new SMS();

                        String origin = (String) availableSenderIds.getItemAt(availableSenderIds.getSelectedIndex());

                        boolean sendStatus = sms.sendMessage(
                                origin,
                                destinationField.getText(),
                                messageField.getText(),
                                LocalDateTime.now(),
                                0
                        );

                        if (sendStatus)
                        {
                            JOptionPane.showMessageDialog(null,
                                    "Message queued to be sent",
                                    "Kentext SMS Manager",
                                    JOptionPane.INFORMATION_MESSAGE);

                            destinationField.setText(null);
                            messageField.setText(null);
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(null,
                                    "Message could not be sent. Please try again",
                                    "Kentext SMS Manager",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    catch (Exception ex)
                    {
                        JOptionPane.showMessageDialog(null,
                                ex.getMessage(),
                                "Kentext SMS Manager",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(null,
                            "Invalid destination. Should be 12 digits starting with 2547 ",
                            "Kentext SMS Manager",
                            JOptionPane.WARNING_MESSAGE);

                    destinationField.setBackground(ERROR_COLOR);
                    destinationFieldMessage.setText("<html><small>Invalid destination. Should be 12 digits starting with 2547</small></html>");
                }
            }
        });

        sendLater.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Matcher matcher = NUMBER_PATTERN.matcher(destinationField.getText());

                if (matcher.matches())
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

                            String origin = (String) availableSenderIds.getItemAt(availableSenderIds.getSelectedIndex());

                            boolean sendStatus = sms.sendMessage(
                                    origin,
                                    destinationField.getText(),
                                    messageField.getText(),
                                    dateTime,
                                    0
                            );

                            if (sendStatus)
                            {
                                JOptionPane.showMessageDialog(null,
                                        "Message queued to be sent",
                                        "Kentext SMS Manager",
                                        JOptionPane.INFORMATION_MESSAGE);

                                destinationField.setText(null);
                                messageField.setText(null);
                            }
                            else
                            {
                                JOptionPane.showMessageDialog(null,
                                        "Message could not be sent. Please try again",
                                        "Kentext SMS Manager",
                                        JOptionPane.WARNING_MESSAGE);
                            }
                        }
                        catch (HeadlessException | IOException | InvalidKeyException
                                | NoSuchAlgorithmException | InvalidKeySpecException
                                | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex)
                        {
                            LOGGER.severe(ex.getMessage());
                        }
                    }

                }
                else
                {
                    JOptionPane.showMessageDialog(null,
                            "Invalid destination. Should be 12 digits starting with 2547 ",
                            "Kentext SMS Manager",
                            JOptionPane.WARNING_MESSAGE);

                    destinationField.setBackground(ERROR_COLOR);
                    destinationFieldMessage.setText("<html><small>Invalid destination. Should be 12 digits starting with 2547</small></html>");
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
