package main;

import com.google.gson.Gson;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import jsonObjects.ReleaseJson;
import methods.Setup;
import methods.Statics;
import methods.Utils;
import objects.Config;
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

        logger.info("Thanks for using CHScrobbler " + VERSION + " by angelolz1 :)");
        logger.info("https://github.com/angelolz/CHScrobbler");
        checkVersion();

        //start setup if config.txt isn't found
        if(!new File("config.txt").exists())
            initSetup(new File("config.txt"));

        try
        {
            Config config = new Config();

            //get properties
            File file = new File("config.txt");
            FileInputStream propFile = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(propFile);

            config.setLastFmApiKey(prop.getProperty("lastfm_apikey"))
                  .setLastFmSecret(prop.getProperty("lastfm_secret"))
                  .setUsername(prop.getProperty("lastfm_username"))
                  .setPassword(prop.getProperty("lastfm_password"))
                  .setDataFolder(prop.getProperty(Statics.DATA_FOLDER_PROP_NAME))
                  .setScrobbleThreshold(Integer.parseInt(prop.getProperty(Statics.SCROBBLE_THRESHOLD_PROP_NAME)));

            //clone hero data
            String dataFolder = FileSystemView.getFileSystemView().getDefaultDirectory().getPath().replaceAll("\\\\", "/") + "/Clone Hero";
            if(config.getDataFolder() == null)
            {
                FileOutputStream fr = new FileOutputStream(file);
                config.setDataFolder(dataFolder);
                prop.setProperty(Statics.DATA_FOLDER_PROP_NAME, dataFolder);
                prop.store(fr, null);
            }

            else
            {
                config.setDataFolder(prop.getProperty(Statics.DATA_FOLDER_PROP_NAME));
                logger.info("Data folder: {}", config.getDataFolder());
            }

            if(config.getScrobbleThreshold() == null)
            {
                FileOutputStream fr = new FileOutputStream(file);
                config.setScrobbleThreshold(Statics.DEFAULT_SCROBBLE_THRESHOLD);
                prop.setProperty(Statics.SCROBBLE_THRESHOLD_PROP_NAME, String.valueOf(Statics.DEFAULT_SCROBBLE_THRESHOLD));
                prop.store(fr, null);
            }

            else
            {
                config.setScrobbleThreshold(validateThreshold(prop.getProperty(Statics.SCROBBLE_THRESHOLD_PROP_NAME)));
                logger.info("Scrobble threshold: {} seconds", config.getScrobbleThreshold());
            }

            //set last.fm api to show only warnings
            Caller.getInstance().getLogger().setLevel(Level.WARNING);

            if(Utils.isNullOrEmpty(config.getLastFmApiKey()) || Utils.isNullOrEmpty(config.getLastFmSecret()))
            {
                JOptionPane.showMessageDialog(null, "last.fm API Key or shared secret key cannot be blank! Please fill them in with the config.txt file.",
                    Statics.LAST_FM_INIT_ERROR, JOptionPane.ERROR_MESSAGE);
                return;
            }

            if(Utils.isNullOrEmpty(config.getUsername()) || Utils.isNullOrEmpty(config.getPassword()))
            {
                JOptionPane.showMessageDialog(null, "last.fm username or password cannot be blank! Please fill them in with the config.txt file.",
                    Statics.LAST_FM_INIT_ERROR, JOptionPane.ERROR_MESSAGE);
                return;
            }

            //logs in last.fm with provided info
            Session session = Authenticator.getMobileSession(config.getUsername(), config.getPassword(), config.getLastFmApiKey(), config.getLastFmSecret());

            if(session == null)
            {
                JOptionPane.showMessageDialog(null, "Unable to establish connection with last.fm! Please make sure your config.txt details are correct!",
                    Statics.LAST_FM_INIT_ERROR, JOptionPane.ERROR_MESSAGE);
            }

            else
            {
                logger.info("Successfully logged in with last.fm!");
                ScrobblerManager.init(session, config.getDataFolder(), config.getScrobbleThreshold());
            }
        }

        catch(Exception e)
        {
            logger.error("Something went wrong reading the config file! Please send a screenshot of this error log to @angelolz1 on GitHub or Twitter.");
            e.printStackTrace();
        }
    }

    public static Logger getLogger()
    {
        return logger;
    }

    private static void checkVersion()
    {
        String json = Utils.readURL("https://api.github.com/repos/angelolz/CHScrobbler/releases/latest");
        Gson gson = new Gson();
        ReleaseJson r = gson.fromJson(json, ReleaseJson.class);

        boolean latestVersion = VERSION.equalsIgnoreCase(r.getTagName());

        if(!latestVersion)
            logger.warn("You're currently on an old version of CHScrobbler. Please update CHScrobbler using the link above as soon as possible.\n");
    }

    private static void initSetup(File file)
    {
        try
        {
            Setup.init(file);
        }

        catch(IOException e)
        {
            logger.error("There was an error making the config file. Please report this error to @angelolz1 on Github or Twitter");
            e.printStackTrace();
        }
    }

    private static int validateThreshold(String thresholdSecondsString)
    {
        int scrobbleThresholdSeconds = Statics.DEFAULT_SCROBBLE_THRESHOLD;

        if(thresholdSecondsString != null && !thresholdSecondsString.isEmpty())
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

            if(scrobbleThresholdSeconds < Statics.DEFAULT_SCROBBLE_THRESHOLD || scrobbleThresholdSeconds > 240)
            {
                scrobbleThresholdSeconds = Statics.DEFAULT_SCROBBLE_THRESHOLD;
                logger.warn("scrobble_threshold_seconds must be a valid number between {} and 240 seconds (4 minutes). Please fix this setting in your config.txt file.", Statics.DEFAULT_SCROBBLE_THRESHOLD);
                logger.info("scrobble_threshold_seconds set to default of {} seconds.", Statics.DEFAULT_SCROBBLE_THRESHOLD);
            }
        }

        return scrobbleThresholdSeconds;
    }
}
