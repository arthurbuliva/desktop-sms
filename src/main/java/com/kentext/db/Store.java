package com.kentext.db;

import com.kentext.common.Common;
import com.kentext.security.Enigma;

import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;

/**
 * Safekeeping of the coins into the database
 */
public final class Store implements Common
{
    private Enigma enigma;

    /**
     * Initialize the Store
     */
    public Store()
    {
        File vaultLocation = new File(DATA_FILE);

        if (!vaultLocation.exists())
        {
            /*
             * Create the necessary folders to hold the SQLite database
             */
            vaultLocation.getParentFile().mkdirs();

            try
            {
                constructStore();
            }
            catch (Exception ex)
            {
                LOGGER.severe(ex.getMessage());
                
                // DELETE The silos
                File dataDirectory = new File(DATA_DIRECTORY);
                dataDirectory.delete();
                
                System.exit(0);
            }
        }

        try
        {
            enigma = new Enigma();
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            LOGGER.severe(e.getMessage());
        }
    }

    /**
     * Store an SMS message to database
     * 
     * @param origin From whom the message originates
     * @param destination The destination of the message
     * @param message The message itself, encrypted
     * @param sent Flag denoting whether the message has been sent or not
     * @param status The status of the sending
     * @param sendDate The date when the message is to be sent
     * @param isToken Flags if this message is the token used to authenticate a user
     * @param sentBy The sender id
     * 
     * @return TRUE if message successfully stored, FALSE otherwise 
     */
    public boolean saveMessage(String origin, String destination, String message, int sent, String status, LocalDateTime sendDate, int isToken, String sentBy)
    {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH);

        try (Connection connection = DriverManager.getConnection(VAULT))
        {
            String sql = "INSERT INTO outbox(origin, destination, message, sent, status, send_date, is_token, sent_by) " +
                         "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, origin);
            statement.setString(2, destination);
            statement.setString(3, message);
            statement.setInt(4, sent);
            statement.setString(5, status);
            statement.setString(6, format.format(sendDate));
            statement.setInt(7, isToken);
            statement.setString(8, sentBy);

            statement.executeUpdate();

            connection.close();

            return true;
        }
        catch (SQLException se)
        {
            LOGGER.severe(se.getMessage());

            return false;
        }
    }

    /**
     * Delete an SMS message from the database
     * 
     * @param id The id of the message to be deleted
     * 
     * @return TRUE if successfully deleted, FALSE otherwise
     */
    public boolean deleteMessage(String id)
    {
        try (Connection connection = DriverManager.getConnection(VAULT))
        {
            String sql = "DELETE FROM outbox WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, id);

            statement.executeUpdate();

            connection.close();

            return true;
        }
        catch (SQLException se)
        {
            LOGGER.severe(se.getMessage());

            return false;
        }
    }

    /**
     * Read all data in outbox table belonging to a particular user
     * 
     * @param sender The user we are interested in
     * 
     * @return HashMap of the messages
     */
    public HashMap<Integer, HashMap<String, String>> retrieveSentMessages(String sender)
    {
        HashMap<Integer, HashMap<String, String>> sentMessages = new HashMap();

        try (Connection connection = DriverManager.getConnection(VAULT))
        {
            String sql = "SELECT id, origin, destination, message, send_date, sent, status FROM outbox WHERE sent_by = ? ORDER BY id DESC";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, sender);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                HashMap<String, String> dataMap = new HashMap();

                dataMap.put("id", resultSet.getString("id"));
                dataMap.put("origin", resultSet.getString("origin"));
                dataMap.put("message", resultSet.getString("message"));
                dataMap.put("destination", resultSet.getString("destination"));
                dataMap.put("send_date", resultSet.getString("send_date"));
                dataMap.put("sent", resultSet.getString("sent"));
                dataMap.put("status", resultSet.getString("status"));

                sentMessages.put(resultSet.getInt("id"), dataMap);
            }

            connection.close();

            return sentMessages;
        }
        catch (SQLException se)
        {
            LOGGER.severe(se.getMessage());

            return null;
        }
    }

    /**
     * Fetch details of a particular user
     * 
     * @param username The user that we are interested in
     * 
     * @return HashMap of the details of the user
     */
    public HashMap<String, String> retrieveUser(String username)
    {
        HashMap<String, String> dataMap = new HashMap();

        try (Connection connection = DriverManager.getConnection(VAULT))
        {
            String sql = "SELECT id, username, token, token_active_until, active, password, active FROM users WHERE username = ?";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                dataMap.put("username", resultSet.getString("username"));
                dataMap.put("password", resultSet.getString("password"));
                dataMap.put("active", resultSet.getString("active"));
                dataMap.put("id", resultSet.getString("id"));
                dataMap.put("token", resultSet.getString("token"));
                dataMap.put("token_active_until", resultSet.getString("token_active_until"));
            }

            connection.close();

            return dataMap;
        }
        catch (SQLException se)
        {
            LOGGER.severe(se.getMessage());

            return null;
        }
    }

    /**
     * Add a new user into the database
     * 
     * @param username The username of the user, usually an MSISDN (international phone number)
     * @param password Password, encrypted
     * @param authToken The token against which we will verify this user
     * @param authTokenValidDate The validity of the authentication token
     * @param active Whether the user his active or not
     * 
     * @return TRUE if user added successfully, FALSE otherwise
     */
    public boolean addUser(String username, String password, String authToken, String authTokenValidDate, int active)
    {
        try (Connection connection = DriverManager.getConnection(VAULT))
        {
            String sql = "INSERT OR REPLACE INTO users(username, password, token, token_active_until, active) VALUES(?, ?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, username);
            statement.setString(2, enigma.encryptText(password));
            statement.setString(3, authToken);
            statement.setString(4, authTokenValidDate);
            statement.setInt(5, active);

            statement.executeUpdate();

            connection.close();

            return true;
        }
        catch (Exception se)
        {
            LOGGER.severe(se.getMessage());
            
            return false;
        }
    }

    /**
     * Activate a given user
     * 
     * @param username The user in which we are interested
     * 
     * @return TRUE if successfully activated, FALSE otherwise.
     */
    public boolean activateUser(String username)
    {
        try (Connection connection = DriverManager.getConnection(VAULT))
        {
            String sql = "UPDATE users SET active = ?, token = ? WHERE username = ?";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, 1);
            statement.setString(2, null);
            statement.setString(3, username);

            statement.executeUpdate();

            connection.close();

            return true;
        }
        catch (Exception se)
        {
            LOGGER.severe(se.getMessage());
            
            return false;
        }
    }

    /**
     * Initialize the storage tables
     */
    private void constructStore()
    {
        try (Connection connection = DriverManager.getConnection(VAULT))
        {
            String phonebookSQL = "CREATE TABLE phonebook(" +
                                  "id INTEGER PRIMARY KEY," +
                                  "name TEXT," +
                                  "number TEXT," +
                                  "user INT DEFAULT 0" +
                                  ")";

            String outboxSQL = "CREATE TABLE outbox(" +
                               "id INTEGER PRIMARY KEY," +
                               "origin TEXT," +
                               "destination TEXT," +
                               "send_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                               "message TEXT," +
                               "sent INT," +
                               "is_token INT," +
                               "sent_by INT DEFAULT 0," +
                               "status TEXT" +
                               ")";

            String userManagementSQL = "CREATE TABLE users(" +
                                       "id INTEGER PRIMARY KEY," +
                                       "username TEXT NOT NULL UNIQUE," +
                                       "password TEXT," +
                                       "token TEXT," +
                                       "token_active_until DATETIME DEFAULT CURRENT_TIMESTAMP," +
                                       "active INT DEFAULT 0" +
                                       ")";

            PreparedStatement phonebookStatement = connection.prepareStatement(phonebookSQL);
            phonebookStatement.executeUpdate();

            PreparedStatement outboxStatement = connection.prepareStatement(outboxSQL);
            outboxStatement.executeUpdate();

            PreparedStatement usersStatement = connection.prepareStatement(userManagementSQL);
            usersStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            LOGGER.severe(e.getMessage());
            
            // DELETE The silos
            File dataDirectory = new File(DATA_DIRECTORY);
            dataDirectory.delete();
        }
    }
}
