package com.kentext.ui;

import com.kentext.common.Common;
import com.kentext.db.Outbox;
import com.kentext.security.Enigma;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

class SentMessagesInternalFrame extends JInternalFrame implements Common
{
    private Outbox outbox;
    private Enigma enigma;
    private JTable sentMessageTable;

    SentMessagesInternalFrame()
    {
        super(
                "Sent Messages",
                true,
                true,
                true,
                true
        );

        initComponents();

        setSize(600, 300);
    }

    private void initComponents()
    {
        setLayout(new BorderLayout());

        try
        {
            outbox = new Outbox();
            enigma = new Enigma();
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException ex)
        {
            LOGGER.severe(ex.getMessage());
        }

        JButton deleteButton = new JButton("Delete");

        JPanel bulkSMSMessageActions = new JPanel();
        bulkSMSMessageActions.setLayout(new FlowLayout(FlowLayout.TRAILING));

        bulkSMSMessageActions.add(deleteButton);

        TableModel model = new DefaultTableModel(getSentMessagesData(), getTableHeaders()){
            @Override
            public boolean isCellEditable(int row, int col){
                return false;
            }
        };

        sentMessageTable = new JTable(model);
        sentMessageTable.removeColumn(sentMessageTable.getColumn(""));
        sentMessageTable.removeColumn(sentMessageTable.getColumn("Status"));

        sentMessageTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(
                        sentMessageTable.getModel().getValueAt(row, 5).equals("") ? OK_COLOR : ERROR_COLOR
                );

                return c;
            }
        });

        sentMessageTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        sentMessageTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JScrollPane scrollPane = new JScrollPane(sentMessageTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane, BorderLayout.CENTER);
        add(bulkSMSMessageActions, BorderLayout.SOUTH);

        sentMessageTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                TableModel model = new DefaultTableModel(getSentMessagesData(), getTableHeaders()){
                    @Override
                    public boolean isCellEditable(int row, int col){
                        return false;
                    }
                };

                sentMessageTable.setModel(model);
                sentMessageTable.removeColumn(sentMessageTable.getColumn(""));
                sentMessageTable.removeColumn(sentMessageTable.getColumn("Status"));
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    Object errorMessage = sentMessageTable.getModel().getValueAt(sentMessageTable.getSelectedRow(), 5);

                    if(errorMessage.equals(""))
                    {
                        JOptionPane.showMessageDialog(null,
                            String.format("Message was sent successfully"),
                            "Kentext SMS Manager",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                    else
                    {
                        if(errorMessage.equals(String.valueOf(SCHEDULED)))
                        {
                            JOptionPane.showMessageDialog(null,
                                String.format("Message scheduled to be sent at a future date or time"),
                                "Kentext SMS Manager",
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(null,
                                String.format("Message not sent: %s", errorMessage),
                                "Kentext SMS Manager",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                else
                {
                    sentMessageTable.setSelectionBackground(Color.BLUE);
                }
            }
        });

        deleteButton.addActionListener(e ->
        {
            for(int selected : sentMessageTable.getSelectedRows())
            {
                String value = sentMessageTable.getModel().getValueAt(selected, 0).toString();
                
                outbox.deleteMessage(value);

                TableModel newModel = new DefaultTableModel(getSentMessagesData(), getTableHeaders()){
                    @Override
                    public boolean isCellEditable(int row, int col){
                        return false;
                    }
                };

                sentMessageTable.setModel(newModel);
                sentMessageTable.removeColumn(sentMessageTable.getColumn(""));
                sentMessageTable.removeColumn(sentMessageTable.getColumn("Status"));
            }
        });
    }

    private Vector<Vector<String>> getSentMessagesData()
    {
        try
        {
            HashMap<String, HashMap<String, String>> sentMessages = outbox.getMessagesSentBy(
                    enigma.decryptText(System.getProperty("com.kentext.desktop.mynumber"))
            );

            assert sentMessages != null;

            List<String> sortedKeys = new ArrayList<>(sentMessages.keySet());

            sortedKeys.sort(Collections.reverseOrder());

            Vector<Vector<String>> tableData = new Vector<>();

            for (String index : sortedKeys)
            {
                HashMap<String, String> messageMap = sentMessages.get(index);

                String id = messageMap.get("id");
                String destination = messageMap.get("destination");
                String date = messageMap.get("send_date");
                String encryptedMessage = messageMap.get("message");
                String decryptedMessage = enigma.decryptText(encryptedMessage);
                String origin = messageMap.get("origin");
                String status = messageMap.get("status");

                Vector<String> smsData = new Vector<>();

                smsData.add(id);
                smsData.add(date);
                smsData.add(destination);
                smsData.add(origin);
                smsData.add(decryptedMessage);
                smsData.add(status);

                tableData.add(smsData);
            }

            return tableData;
        }
        catch (IOException | InvalidKeyException | NoSuchAlgorithmException
                | InvalidKeySpecException | BadPaddingException
                | IllegalBlockSizeException ex)
        {
            LOGGER.severe(ex.getMessage());
            return null;
        }
    }

    private Vector getTableHeaders()
    {
        Vector<String> headers = new Vector<>();

        headers.add("");
        headers.add("Date");
        headers.add("Destination");
        headers.add("Sent As");
        headers.add("Message");
        headers.add("Status");

        return headers;
    }
}
