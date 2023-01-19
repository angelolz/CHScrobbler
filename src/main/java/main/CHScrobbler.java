package main;

import com.google.gson.Gson;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import jsonObjects.ReleaseJson;
import methods.ReadURL;
import methods.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

public class CHScrobbler
{
    private static Logger logger;
    private static final String VERSION = "v1.4";
    private static final String LAST_FM_INIT_ERROR = "last.fm init error!";
    private static String dataFolder;

    public static void main(String[] args)
    {

        logger = LoggerFactory.getLogger(CHScrobbler.class);

        try
        {
            System.out.println("Thanks for using CHScrobbler " + VERSION + " by angelolz1 :)");
            System.out.println("https://github.com/angelolz1/CHScrobbler\n\n");

            boolean latestVersion = checkVersion();
            if(!latestVersion)
            {
                System.out.println("You're currently on an old version of CHScrobbler. Please update CHScrobbler using the link above as soon as possible.\n\n");
            }

            File file = new File("config.txt");

            //start setup if config.txt isn't found
            if(!file.exists())
            {
                initSetup(file);
            }

            //get api and auth info
            FileInputStream propFile = new FileInputStream(file);

            Properties prop = new Properties();
            prop.load(propFile);
            String lastFmApiKey = prop.getProperty("lastfm_apikey");
            String lastFmSecret = prop.getProperty("lastfm_secret");
            String user = prop.getProperty("lastfm_username");
            String pass = prop.getProperty("lastfm_password");
            dataFolder = prop.getProperty("clonehero_data_folder");

            //set last.fm api to show only warnings
            Caller.getInstance().getLogger().setLevel(Level.WARNING);

            if(lastFmApiKey.isEmpty() || lastFmSecret.isEmpty())
            {
                JOptionPane.showMessageDialog(null,"last.fm API Key or shared secret key cannot be blank! Please fill them in with the config.txt file.",
                    LAST_FM_INIT_ERROR, JOptionPane.ERROR_MESSAGE);
            }

            if(user.isEmpty() || pass.isEmpty())
            {
                JOptionPane.showMessageDialog(null,"last.fm username or password cannot be blank! Please fill them in with the config.txt file.",
                    LAST_FM_INIT_ERROR, JOptionPane.ERROR_MESSAGE);
            }

            //logs in last.fm with provided info
            Session session = Authenticator.getMobileSession(user, pass, lastFmApiKey, lastFmSecret);

            //logged in, start scrobbling
            if(session == null)
            {
                JOptionPane.showMessageDialog(null,"Unable to establish connection with last.fm! Please make sure your config.txt details are correct!",
                    LAST_FM_INIT_ERROR, JOptionPane.ERROR_MESSAGE);
            }

            else
            {
                logger.info("Successfully logged in with last.fm!");
                ScrobblerManager.init(session);
            }
        }

        catch(IOException e)
        {
            JOptionPane.showMessageDialog(null,"Sorry, there was a problem reading the config file! Please report this if you can!",
                LAST_FM_INIT_ERROR, JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        catch(Exception e)
        {
            System.out.println("Something went wrong! Please send a screenshot of this error log.");
            e.printStackTrace();
        }
    }

    public static Logger getLogger()
    {
        return logger;
    }

    private static boolean checkVersion()
    {
        String json = ReadURL.readURL("https://api.github.com/repos/angelolz1/CHScrobbler/releases/latest");
        Gson gson = new Gson();
        ReleaseJson r = gson.fromJson(json, ReleaseJson.class);

        return VERSION.equalsIgnoreCase(r.getTagName());
    }

    private static void initSetup(File file) {
        try
        {
            Setup.init(file);
        }

        catch(IOException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,"There was an error making the config file. Please let the dev know.",
                "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static String getDataDirectory() {
        return dataFolder;
    }

    public static void setDataDirectory(String newDir) {
        dataFolder = newDir;
    }
}
