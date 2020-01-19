package com.kentext.ui;

import com.kentext.common.Common;
import java.sql.SQLException;
import org.h2.tools.Console;

public class H2DBViewer implements Common
{
    public static void main(String[] args) throws SQLException, ClassNotFoundException
    {
        String[] options = 
        {
            "-url", "jdbc:h2:~/.kentext/silo/kentext.db",
            "-driver", "org.h2.Driver",
            "-user", "sa",
            "-password", "sa",
        };
        
        Console console = new Console();
        console.runTool(options);
    }
}
