package com.kentext.common;

import java.awt.Color;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public interface Common
{
    /**
     * Directory where the user data will be stored
     */
    public final String DATA_DIRECTORY = String.format(
        "%s%s.kentext",
        System.getProperty("user.home"), java.io.File.separator
    );

    public final String DATA_FILE = String.format(
            "%s%s%s%s%s",
            DATA_DIRECTORY, java.io.File.separator, "silo", java.io.File.separator, "kentext.db"
    );
    
    public final String CONFIG_FILE = String.format(
            "%s%s%s%s%s",
            DATA_DIRECTORY, java.io.File.separator, "config", java.io.File.separator, "kentext.conf"
    );

    // The database in which to store our data
    public final String VAULT = String.format(
            "jdbc:sqlite:%s",
            DATA_FILE
    );
    
    public final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public final int ERROR = -1;
    public final int DRAFT = 0;
    public final int SENT = 1;
    public final int SCHEDULED = 2;

    public final Pattern NUMBER_PATTERN = Pattern.compile("\\+?2547\\d{8}");

    public final Color ERROR_COLOR = Color.PINK;
    public final Color DEFAULT_COLOR = Color.WHITE;
    public final Color OK_COLOR = new Color(242, 250, 245);
    public final Color DEFAULT_FOREGROUND_COLOR = Color.BLACK;

    public final int SMS_SENT_STATUS_OK = 1701;
    public final int SMS_SENT_STATUS_INTERNAL_ERROR = 1710;
    public final int SMS_SENT_STATUS_INSUFFICIENT_CREDIT = 1025;
    
    default Properties loadConfigurationFile()
    {
        Properties myProperties = new Properties();
        
        try (FileInputStream fileInputStream = new FileInputStream(CONFIG_FILE))
        {
            // load a properties file
            myProperties.load(fileInputStream);

            return myProperties;
        }
        catch (Exception ex)
        {
            LOGGER.severe(ex.getMessage());
            
            return null;
        }
    };
}
