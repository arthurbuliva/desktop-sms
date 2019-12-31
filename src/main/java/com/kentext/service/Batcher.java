package com.kentext.service;

import com.kentext.common.Common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;

public class Batcher implements Common
{
    final int BATCH_SIZE = 100;

    public Batcher()
    {
    }

    public ArrayList<ArrayList<String>> batchRecipients(ArrayList<String> contacts)
    {
        ArrayList<ArrayList<String>> cleanedRecipientBatches = new ArrayList();

        ArrayList<String> cleanedRecipients = new ArrayList();

        Iterator<String> iterator = contacts.iterator();

        while (iterator.hasNext())
        {
            String number = iterator.next();

            Matcher matcher = NUMBER_PATTERN.matcher(number);

            if (matcher.matches())
            {
                cleanedRecipients.add(number.replaceAll("\\+", ""));

                if (cleanedRecipients.size() >= BATCH_SIZE)
                {
                    cleanedRecipientBatches.add(cleanedRecipients);
                    cleanedRecipients = new ArrayList();
                }

            }
            if (!iterator.hasNext())
            {
                if (!cleanedRecipients.isEmpty())
                {
                    cleanedRecipientBatches.add(cleanedRecipients);
                }
            }
        }

        return cleanedRecipientBatches;
    }
}
