package kentext.test;


import com.kentext.common.Common;

public class Workshop implements Common
{
    
    public static void main(String[] args)
    {
//        SpringApplication.run(DemoApplication.class, args);
        Outbox outbox = new Outbox();

        for (int i = 20; i < 25; i++)
        {
            outbox.create(i);
        }
        
        outbox.readAll().forEach((o) ->
        {
            System.out.println(((Outbox)o).getMessage());
        });
        
        System.exit(0);
    }
}
