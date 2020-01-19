package com.kentext.service;

import com.kentext.common.Common;
import com.kentext.db.Outbox;
import com.kentext.security.Enigma;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private Outbox outbox;

    public Daemon()
    {
        try
        {
            enigma = new Enigma();
            sms = new SMS();
            outbox = new Outbox();
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
        HashMap<String, HashMap<String, String>> pendingMessages = outbox.getPendingMessages();

        for (Map.Entry<String, HashMap<String, String>> entry : pendingMessages.entrySet())
        {
            String index = entry.getKey();

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
                    
                    outbox.setSendStatus(id, status.get("send_status"), status.get("error_message"));
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
}
