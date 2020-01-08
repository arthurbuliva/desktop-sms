package kentext.test;


import com.kentext.common.Common;

public class Workshop implements Common
{
    public Workshop()
    {
        System.out.println(CONFIG_FILE);

        System.out.println(loadConfigurationFile().getProperty("SMS_ROUTE"));
    }

    public static void main(String[] args)
    {
        Workshop workshop = new Workshop();
    }
}
