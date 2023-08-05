package main;

import com.google.gson.Gson;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import jsonObjects.ReleaseJson;
import methods.ReadURL;
import methods.Setup;
import methods.Statics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

public class CHScrobbler
{
    private static final String VERSION = "v1.5";
    private static Logger logger;

    public static void main(String[] args)
    {
        logger = LoggerFactory.getLogger(CHScrobbler.class);

        System.out.println("Thanks for using CHScrobbler " + VERSION + " by angelolz1 :)");
        System.out.println("https://github.com/angelolz/CHScrobbler\n\n");
        checkVersion();

        try
        {
            File file = new File("config.txt");

            //start setup if config.txt isn't found
            if(!file.exists()) initSetup(file);

            //get properties
            FileInputStream propFile = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(propFile);

            //last.fm api details
            String lastFmApiKey = prop.getProperty("lastfm_apikey");
            String lastFmSecret = prop.getProperty("lastfm_secret");
            String user = prop.getProperty("lastfm_username");
            String pass = prop.getProperty("lastfm_password");

            //clone hero data
            String dataFolder = FileSystemView.getFileSystemView().getDefaultDirectory().getPath().replaceAll("\\\\", "/") + "/Clone Hero";
            if(prop.getProperty(Statics.DATA_FOLDER_PROP_NAME) == null)
            {
                FileOutputStream fr = new FileOutputStream(file);
                prop.setProperty(Statics.DATA_FOLDER_PROP_NAME, dataFolder);
                prop.store(fr, Statics.DATA_FOLDER_PROP_NAME);
                fr.close();
            }

            else
                dataFolder = prop.getProperty(Statics.DATA_FOLDER_PROP_NAME);

            //validate scrobble threshold seconds
            int scrobbleThresholdSeconds = validateThreshold(prop.getProperty("scrobble_threshold_seconds"));

            //set last.fm api to show only warnings
            Caller.getInstance().getLogger().setLevel(Level.WARNING);

            if(lastFmApiKey.isEmpty() || lastFmSecret.isEmpty())
            {
                JOptionPane.showMessageDialog(null,"last.fm API Key or shared secret key cannot be blank! Please fill them in with the config.txt file.",
                    Statics.LAST_FM_INIT_ERROR, JOptionPane.ERROR_MESSAGE);
            }

            if(user.isEmpty() || pass.isEmpty())
            {
                JOptionPane.showMessageDialog(null,"last.fm username or password cannot be blank! Please fill them in with the config.txt file.",
                    Statics.LAST_FM_INIT_ERROR, JOptionPane.ERROR_MESSAGE);
            }

            //logs in last.fm with provided info
            Session session = Authenticator.getMobileSession(user, pass, lastFmApiKey, lastFmSecret);

            if(session == null)
            {
                JOptionPane.showMessageDialog(null,"Unable to establish connection with last.fm! Please make sure your config.txt details are correct!",
                    Statics.LAST_FM_INIT_ERROR, JOptionPane.ERROR_MESSAGE);
            }

            else
            {
                logger.info("Successfully logged in with last.fm!");
                ScrobblerManager.init(session, dataFolder, scrobbleThresholdSeconds);
            }
        }

        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null,"Sorry, there was a problem reading the config file! Please report this error to @angelolz1 on GitHub/Twitter!",
                Statics.LAST_FM_INIT_ERROR, JOptionPane.ERROR_MESSAGE);
            System.out.println("Something went wrong! Please send a screenshot of this error log to @angelolz1 on GitHub or Twitter.");
            e.printStackTrace();
        }
    }

    public static Logger getLogger()
    {
        return logger;
    }

    private static void checkVersion()
    {
        String json = ReadURL.readURL("https://api.github.com/repos/angelolz/CHScrobbler/releases/latest");
        Gson gson = new Gson();
        ReleaseJson r = gson.fromJson(json, ReleaseJson.class);

        boolean latestVersion = VERSION.equalsIgnoreCase(r.getTagName());

        if(!latestVersion)
            System.out.println("You're currently on an old version of CHScrobbler. Please update CHScrobbler using the link above as soon as possible.\n\n");
    }

    private static void initSetup(File file)
    {
        try
        {
            Setup.init(file);
        }

        catch(IOException e)
        {
            JOptionPane.showMessageDialog(null,"There was an error making the config file. Please report this to @angelolz1 on Github or Twitter.",
                "Error!", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static int validateThreshold(String thresholdSecondsString)
    {
        int scrobbleThresholdSeconds = Statics.DEFAULT_SCROBBLE_THRESHOLD;

        if (thresholdSecondsString != null && !thresholdSecondsString.isEmpty())
        {
            try
            {
                scrobbleThresholdSeconds = Integer.parseInt(thresholdSecondsString);
            }
            catch(NumberFormatException e)
            {
                logger.warn("Invalid scrobble_threshold_seconds given. Please fix this setting in your config.txt file.");
                logger.info("scrobble_threshold_seconds set to default of {} seconds.", Statics.DEFAULT_SCROBBLE_THRESHOLD);
            }

            if (scrobbleThresholdSeconds < Statics.DEFAULT_SCROBBLE_THRESHOLD || scrobbleThresholdSeconds > 240)
            {
                scrobbleThresholdSeconds = Statics.DEFAULT_SCROBBLE_THRESHOLD;
                logger.warn("scrobble_threshold_seconds must be a valid number between {} and 240 seconds (4 minutes).", Statics.DEFAULT_SCROBBLE_THRESHOLD);
                logger.info("scrobble_threshold_seconds set to default of {} seconds.", Statics.DEFAULT_SCROBBLE_THRESHOLD);
            }
        }

        return scrobbleThresholdSeconds;
    }
}
