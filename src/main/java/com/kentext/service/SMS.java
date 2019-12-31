package com.kentext.service;

import com.kentext.common.Common;
import com.kentext.db.Store;
import com.kentext.security.Enigma;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class SMS implements Common
{
    private final Store store;
    private final Enigma enigma;
    private final KentextService kentextService;

    public SMS() throws NoSuchPaddingException, NoSuchAlgorithmException
    {
        enigma = new Enigma();
        store = new Store();
        kentextService = new KentextService();
    }

    public Vector getMessagesSentBy(String origin)
    {
        Vector data = new Vector();

        HashMap<Integer, HashMap<String, String>> sentMessages = store.retrieveSentMessages(origin);

        for (Map.Entry<Integer, HashMap<String, String>> entry : sentMessages.entrySet())
        {
            Integer index = entry.getKey();

            HashMap<String, String> messageMap = entry.getValue();

            String id = messageMap.get("id");
            String destination = messageMap.get("destination");
            String date = messageMap.get("send_date");
            String encryptedMessage = messageMap.get("message");
            String decryptedMessage = "";
            String sent = messageMap.get("sent");

            try
            {
                decryptedMessage = enigma.decryptText(encryptedMessage);
            }
            catch (IOException | InvalidKeyException | NoSuchAlgorithmException
                    | InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException ex)
            {
                decryptedMessage = ex.getMessage();
                
                LOGGER.severe(ex.getMessage());
                
            }
                int reviewLength = 20;

                StringBuilder smsStringBuilder = new StringBuilder();
                
                smsStringBuilder.append(
                        String.format(
                                "<html>%s<p style=\"text-align:left;\">" +
                                "    <b>%s</b>" +
                                "    <span style=\"float:right;\">" +
                                "        %s" +
                                "    </span>" +
                                "</p>%s<hr/></html>",
                                id, destination, date,
                                decryptedMessage
                        )
                );

                if(sent != null && Integer.parseInt(sent) == SENT)
                {
                    data.add(smsStringBuilder);
                }

        }
        return data;
    }

    public boolean sendMessage(
            String origin, String destination, String message, LocalDateTime whenToSend, int isToken) throws
            BadPaddingException, NoSuchAlgorithmException,
            IllegalBlockSizeException, IOException,
            InvalidKeyException, InvalidKeySpecException
    {
        // Queue message for sending.
        // Daemon.java will pick it up
        return store.saveMessage(
                isToken == 1 ? enigma.decryptText(System.getProperty("com.kentext.desktop.mynumber")) : origin,
                destination,
                enigma.encryptText(message),
                SCHEDULED,
                null,
                whenToSend,
                isToken,
                isToken == 1 ?
                        loadConfigurationFile().getProperty("DEFAULT_KENTEXT_NUMBER")
                        : enigma.decryptText(System.getProperty("com.kentext.desktop.mynumber"))
        );
    }

    protected HashMap<String, String> callSMSApi(String origin, String destination, String message, boolean newUser)
    {
        String response = "";

        try
        {
            if(!newUser)
            {
                String availableCredits = (String) (kentextService.getAvailableCreditsFromKentext()).get("credits");

                if (Integer.parseInt(availableCredits) <= 0)
                {
                    response = "1025|Insufficient balance to send SMS";
                    throw new Exception(response);
                }
            }

            URL url = new URL(String.format(
                    loadConfigurationFile().getProperty("SMS_ROUTE"),
                    URLEncoder.encode(destination, "UTF-8"),
                    URLEncoder.encode(origin, "UTF-8"),
                    URLEncoder.encode(message, "UTF-8")
            ));

            InputStream inputStream = url.openStream();

            response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (Exception ex)
        {
            LOGGER.severe(ex.getMessage());
        }

        String[] apiResponse = response.split("\\|", 2);

        int responseCode = Integer.parseInt(apiResponse[0]);

        HashMap<String, String> sendStatus = new HashMap();

        switch (responseCode)
        {
            case SMS_SENT_STATUS_OK:
            {
                sendStatus.put("code", String.valueOf(responseCode));
                sendStatus.put("send_status", String.valueOf(SENT));
                sendStatus.put("error_message", null);
            }
            break;
            case 1702:
            {
                sendStatus.put("code", String.valueOf(responseCode));
                sendStatus.put("send_status", String.valueOf(ERROR));
                sendStatus.put("error_message", "Invalid URL Error, This means that one of the parameters was not provided or left blank");
            }
            break;
            case 1703:
            {
                sendStatus.put("code", String.valueOf(responseCode));
                sendStatus.put("send_status", String.valueOf(ERROR));
                sendStatus.put("error_message", "Invalid value in username or password field");
            }
            break;
            case 1704:
            {
                sendStatus.put("code", String.valueOf(responseCode));
                sendStatus.put("send_status", String.valueOf(ERROR));
                sendStatus.put("error_message", "Invalid value in 'type' field");
            }
            break;
            case 1705:
            {
                sendStatus.put("code", String.valueOf(responseCode));
                sendStatus.put("send_status", String.valueOf(ERROR));
                sendStatus.put("error_message", "Invalid Message");
            }
            break;
            case 1706:
            {
                sendStatus.put("code", String.valueOf(responseCode));
                sendStatus.put("send_status", String.valueOf(ERROR));
                sendStatus.put("error_message", "Invalid Destination");
            }
            break;
            case 1707:
            {
                sendStatus.put("code", String.valueOf(responseCode));
                sendStatus.put("send_status", String.valueOf(ERROR));
                sendStatus.put("error_message", "Invalid Source (Sender)");
            }
            break;
            case 1708:
            {
                sendStatus.put("code", String.valueOf(responseCode));
                sendStatus.put("send_status", String.valueOf(ERROR));
                sendStatus.put("error_message", "Invalid value for 'dlr' field");
            }
            break;
            case 1709:
            {
                sendStatus.put("code", String.valueOf(responseCode));
                sendStatus.put("send_status", String.valueOf(ERROR));
                sendStatus.put("error_message", "User validation failed");
            }
            break;
            case SMS_SENT_STATUS_INTERNAL_ERROR:
            {
                sendStatus.put("code", String.valueOf(responseCode));
                sendStatus.put("send_status", String.valueOf(ERROR));
                sendStatus.put("error_message", "Internal Error");
            }
            break;
            case SMS_SENT_STATUS_INSUFFICIENT_CREDIT:
            {
                sendStatus.put("code", String.valueOf(responseCode));
                sendStatus.put("send_status", String.valueOf(ERROR));
                sendStatus.put("error_message", "Insufficient Credit");
            }
            break;
            default:
            {
                sendStatus.put("code", null);
                sendStatus.put("send_status", String.valueOf(ERROR));
                sendStatus.put("error_message", null);
            }
        }

        return sendStatus;
    }
}
