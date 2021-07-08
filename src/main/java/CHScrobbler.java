import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class CHScrobbler
{
    private static Logger logger;
    private static final String version = "v1.1";

    public static void main(String[] args)
    {
        logger = LoggerFactory.getLogger(CHScrobbler.class);

        try
        {
            //get api and auth info
            Properties prop = new Properties();
            FileInputStream propFile = new FileInputStream("config.properties");
            prop.load(propFile);
            String lastFmApiKey = prop.getProperty("lastfm_apikey");
            String lastFmSecret = prop.getProperty("lastfm_secret");
            String user = prop.getProperty("lastfm_username");
            String pass = prop.getProperty("lastfm_password");

            Caller.getInstance().getLogger().setLevel(Level.WARNING);

            if(user.isEmpty() || pass.isEmpty())
            {
                System.out.println("Username or password cannot be blank! Please put them in config.properties!");
                Executors.newSingleThreadScheduledExecutor().schedule(() -> System.exit(0), 5, TimeUnit.SECONDS);
            }
            //logs in last.fm with provided info
            Session session = Authenticator.getMobileSession(user, pass, lastFmApiKey, lastFmSecret);

            //logged in, start scrobbling
            if(session == null)
            {
                System.out.println("Unable to establish connection with last.fm! Please make sure your username and password are correct!");
                Executors.newSingleThreadScheduledExecutor().schedule(() -> System.exit(0), 10, TimeUnit.SECONDS);
            }

            else
            {
                System.out.println("Thanks for using CHScrobbler " + version + " by angelolz1 :)\n\n");
                logger.info("Successfully logged in with last.fm!");
                ScrobblerManager.init(session);
            }
        }

        catch(IOException e)
        {
            System.out.println("Sorry, couldn't find or read the 'config.properties' file. " +
                "Make sure that it is in the same directory as Clone Hero and it is not a corrupted file!");
            Executors.newSingleThreadScheduledExecutor().schedule(() -> System.exit(0), 7, TimeUnit.SECONDS);
        }

        catch(Exception e)
        {
            System.out.println("Something went wrong! Please send a screenshot of this error log to Angel.");
            e.printStackTrace();
        }
    }

    public static Logger getLogger()
    {
        return logger;
    }
}
