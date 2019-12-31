package com.kentext.main;

import com.kentext.common.Common;
import com.kentext.service.Daemon;
import com.kentext.ui.MainWindow;
import java.util.Timer;

public class Kentext implements Common
{
    public Kentext()
    {
//        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        
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
