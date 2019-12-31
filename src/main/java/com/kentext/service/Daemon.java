package com.kentext.service;

import com.kentext.common.Common;
import com.kentext.security.Enigma;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Daemon extends TimerTask implements Common
{
    private Enigma enigma;
    private SMS sms;


    public Daemon()
    {
        try
        {
            enigma = new Enigma();
            sms = new SMS();
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException ex)
        {
            LOGGER.severe(ex.getMessage());
            
            System.exit(1);
        }
    }

    @Override
    public void run()
    {
        HashMap<Integer, HashMap<String, String>> pendingMessages = getPendingMessages();

        for (Map.Entry<Integer, HashMap<String, String>> entry : pendingMessages.entrySet())
        {
            Integer index = entry.getKey();

            HashMap<String, String> messageMap = entry.getValue();

            String destination = messageMap.get("destination");
            String date = messageMap.get("send_date");
            String encryptedMessage = messageMap.get("message");
            String id = messageMap.get("id");
            int isToken = Integer.parseInt(messageMap.get("is_token"));
            String origin = isToken == 1 ?
                    loadConfigurationFile().getProperty("DEFAULT_KENTEXT_NUMBER")
                    : messageMap.get("origin");


            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            try
            {
                Date send_date = format.parse(date);

                if (send_date.compareTo(new Date()) <= 0)
                {
                    String decryptedMessage = enigma.decryptText(encryptedMessage);

                    LOGGER.info(String.format("Sending message to %s", destination));

                    HashMap<String, String> status = sms.callSMSApi(
                            origin,
                            destination,
                            decryptedMessage,
                            isToken == 1
                    );

                    try (Connection connection = DriverManager.getConnection(VAULT))
                    {
                        String sql = "UPDATE outbox SET sent = ?, status = ? WHERE id = ?";

                        PreparedStatement statement = connection.prepareStatement(sql);

                        statement.setString(1, status.get("send_status"));
                        statement.setString(2, status.get("error_message"));
                        statement.setObject(3, id);

                        statement.executeUpdate();

                        connection.close();
                    }
                    catch (SQLException se)
                    {
                        LOGGER.severe(se.getMessage());
                    }
                }
                else
                {
                    LOGGER.fine("No new messages to send");
                }
            }
            catch (IOException | InvalidKeyException | NoSuchAlgorithmException
                    | InvalidKeySpecException | ParseException | BadPaddingException
                    | IllegalBlockSizeException ex)
            {
                LOGGER.severe(ex.getMessage());
            }
        }
    }

    private HashMap<Integer, HashMap<String, String>> getPendingMessages()
    {
        HashMap<Integer, HashMap<String, String>> pendingMessages = new HashMap();

        try (Connection connection = DriverManager.getConnection(VAULT))
        {
            String sql = "SELECT * FROM outbox WHERE sent = ? AND send_date <= DATETIME(?)";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, SCHEDULED);
            statement.setObject(2, LocalDateTime.now());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                HashMap<String, String> dataMap = new HashMap();

                dataMap.put("id", resultSet.getString("id"));
                dataMap.put("origin", resultSet.getString("origin"));
                dataMap.put("message", resultSet.getString("message"));
                dataMap.put("destination", resultSet.getString("destination"));
                dataMap.put("send_date", resultSet.getString("send_date"));
                dataMap.put("is_token", resultSet.getString("is_token"));

                pendingMessages.put(resultSet.getInt("id"), dataMap);
            }

            connection.close();

            return pendingMessages;
        }
        catch (SQLException se)
        {
            LOGGER.severe(se.getMessage());

            return null;
        }
    }
}
