package com.kentext.main;

import com.kentext.common.Common;
import static com.kentext.common.Common.DATA_FILE;
import com.kentext.service.Daemon;
import com.kentext.ui.MainWindow;
import java.io.File;
import java.util.Timer;

public class Kentext implements Common
{
    public Kentext()
    {

        File vaultLocation = new File(DATA_FILE);

        if (!vaultLocation.exists())
        {
            /*
             * Create the necessary folders to hold the SQLite database
             */
            vaultLocation.getParentFile().mkdirs();
        }
        
        MainWindow mainWindow = new MainWindow();
        mainWindow.setVisible(true);
    }
    
    private void startService()
    {
        Daemon service = new Daemon();

        java.util.Timer timer = new Timer();
        timer.schedule(service, 0, 1000);
    }
    
    public static void main(String[] args)
    {
        Kentext kentext = new Kentext();
        kentext.startService();
    }
}
