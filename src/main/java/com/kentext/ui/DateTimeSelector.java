package com.kentext.ui;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.text.DateFormatter;

public class DateTimeSelector
{
    private JSpinner timeSpinner;
    private JOptionPane option;
    private final String DATE_TIME_PATTERN = "dd-MM-yyyy HH:mm";
    private JSpinner.DateEditor timeEditor;

    public DateTimeSelector()
    {
        timeSpinner = new JSpinner(new SpinnerDateModel());
        timeEditor = new JSpinner.DateEditor(timeSpinner, DATE_TIME_PATTERN);

        timeSpinner.setEditor(timeEditor);

        timeEditor.getTextField().setColumns(timeEditor.getTextField().getColumns() + 2);
        timeEditor.getTextField().setHorizontalAlignment(JTextField.RIGHT);

        DateFormatter formatter = (DateFormatter) timeEditor.getTextField().getFormatter();
        formatter.setAllowsInvalid(false);
        formatter.setOverwriteMode(true);

        option = new JOptionPane();
    }

    public String getDateTime()
    {
        String dateTime = null;

        String[] options =
                {
                        "YES",
                        "No"
                };

        String message = "Choose start date and time:\n";
        Object[] params = {message, timeSpinner};

        int selection = option.showOptionDialog(
                null,
                params,
                "Choose your poison",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,     //do not use a custom Icon
                options,  //the titles of buttons
                options[0]); //default button title

        switch (selection)
        {
            case JOptionPane.YES_OPTION :
            {
                dateTime = timeEditor.getFormat().format(timeSpinner.getValue());
            }
            break;
            default:
            {
                dateTime = null;
            }
            break;
        }

        return dateTime;
    }
}
