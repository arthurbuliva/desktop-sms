package com.kentext.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kentext.common.Common;
import com.kentext.security.Enigma;
import java.net.URI;
import java.util.HashMap;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class KentextService implements Common
{
    public KentextService()
    {

    }

    public HashMap<String, Object> getAvailableCreditsFromKentext() throws Exception
    {
        HashMap<String, Object> availableCreditsFromKentext = new HashMap<>();

        CloseableHttpClient httpclient = HttpClients.custom().build();

        HttpPost httpPost = new HttpPost(loadConfigurationFile().getProperty("KENTEXT_TOKEN_URL"));

        httpPost.setHeader("Content-Type" , "application/json");

        HttpResponse userTokenPostResponse = httpclient.execute(httpPost);

        if(userTokenPostResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
        {
            JsonElement userTokenPostResponseJSON = new JsonParser().parse(
                    EntityUtils.toString(userTokenPostResponse.getEntity())
            );

            // Now we log in
            httpPost.setURI(
                    new URI(loadConfigurationFile().getProperty("KENTEXT_LOGIN_URL"))
            );

            httpPost.setHeader("X-CSRF-Token" , userTokenPostResponseJSON.getAsJsonObject().get("token").getAsString());

            HashMap<String, String> credentials = new HashMap();

            try (Enigma enigma = new Enigma())
            {
                credentials.put("username", enigma.decryptText(System.getProperty("com.kentext.desktop.mynumber")));
                credentials.put("password", enigma.decryptText(System.getProperty("com.kentext.desktop.authkey")));
            }
            catch (Exception ex)
            {
                LOGGER.severe(ex.getMessage());
            }

            Gson gson = new Gson();
            String credentialsJSON = gson.toJson(credentials);

            StringEntity requestEntity = new StringEntity(
                    credentialsJSON,
                    ContentType.APPLICATION_JSON);

            httpPost.setEntity(requestEntity);

            HttpResponse userLoginPostResponse = httpclient.execute(httpPost);

            if(userLoginPostResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                JsonElement userLoginPostResponseJSON = new JsonParser().parse(
                        EntityUtils.toString(userLoginPostResponse.getEntity())
                );

                String currentCredits = userLoginPostResponseJSON
                        .getAsJsonObject().get("user")
                        .getAsJsonObject().get("field_sms_credits")
                        .getAsJsonObject().get("und")
                        .getAsJsonArray().get(0)
                        .getAsJsonObject().get("value")
                        .getAsString();

                JsonArray senderIds = userLoginPostResponseJSON
                        .getAsJsonObject().get("user")
                        .getAsJsonObject().get("field_sender_ids")
                        .getAsJsonObject().get("und")
                        .getAsJsonArray();

                availableCreditsFromKentext.put("credits", currentCredits);
                availableCreditsFromKentext.put("sender_ids", senderIds);

                return (availableCreditsFromKentext);

//                httpPost.setHeader("Cookie" , userLoginPostResponseJSON.getAsJsonObject().get("sessid").getAsString());
//                System.out.println(httpPost.getLastHeader("Cookie"));
//                System.out.println(getAvailableSMSCredits(httpPost, httpclient));

            }
            else
            {
                throw new Exception(userLoginPostResponse.getStatusLine().getReasonPhrase());
            }
        }
        else
        {
            throw new Exception(userTokenPostResponse.getStatusLine().getReasonPhrase());
        }
    }

    private static String getAvailableSMSCredits(HttpPost userLoginPost, CloseableHttpClient httpclient) throws Exception
    {
        HttpResponse userLoginPostResponse = httpclient.execute(userLoginPost);

        if(userLoginPostResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
        {
            JsonElement userLoginPostResponseJSON = new JsonParser().parse(
                    EntityUtils.toString(userLoginPostResponse.getEntity())
            );

            return userLoginPostResponseJSON
                    .getAsJsonObject().get("user")
                    .getAsJsonObject().get("field_sms_credits")
                    .getAsJsonObject().get("und")
                    .getAsJsonArray().get(0)
                    .getAsJsonObject().get("value")
                    .getAsString();
        }
        else
        {
            return (userLoginPostResponse.getStatusLine().getReasonPhrase());
        }
    }
}
