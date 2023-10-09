package main;

import com.google.gson.Gson;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import json.ReleaseJson;
import methods.Setup;
import methods.Statics;
import methods.Utils;
import methods.Validator;
import objects.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;

public class CHScrobbler
{
    private static final String VERSION = "v1.6";
    private static final String REPO_URL = "https://github.com/angelolz/CHScrobbler";
    private static Logger logger;
    private static Config config;

    public static void main(String[] args)
    {
        //logger for CHScrobbler
        logger = LoggerFactory.getLogger(CHScrobbler.class);

        //logger for last.fm api
        Caller.getInstance().getLogger().setLevel(Level.WARNING);

        logger.info("Thanks for using CHScrobbler {} by angelolz1 :) | {}", VERSION, REPO_URL);
        checkVersion();

        config = new Config();

        //start setup if config.txt isn't found
        if(!new File(Statics.CONFIG_FILE).exists())
        {
            logger.info("--------- Running setup... ---------");
            System.out.println();
            initSetup(new File(Statics.CONFIG_FILE));
        }

        try
        {
            //get properties
            File file = new File(Statics.CONFIG_FILE);
            FileInputStream propFile = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(propFile);

            Validator.validateSettings(file, config, prop);
            printSettings();

            //logs in last.fm with provided info
            Session session = Authenticator.getMobileSession(config.getUsername(), config.getPassword(), config.getLastFmApiKey(), config.getLastFmSecret());

            if(session == null)
                Utils.showErrorAndExit(Statics.LAST_FM_INIT_ERROR, "Unable to establish connection with last.fm! Please make sure your last.fm details are correct!");
            else
            {
                logger.info("Successfully logged in with last.fm!");
                ScrobblerManager.init(session);
            }
        }

        catch(Exception e)
        {
            logger.error("Something went wrong reading the config file! Please send a screenshot of this error log to @angelolz1 on GitHub or Twitter.", e);
        }
    }

    public static Logger getLogger() { return logger; }

    public static Config getConfig() { return config; }

    private static void checkVersion()
    {
        String json = Utils.readURL("https://api.github.com/repos/angelolz/CHScrobbler/releases/latest");
        Gson gson = new Gson();
        ReleaseJson r = gson.fromJson(json, ReleaseJson.class);

        boolean latestVersion = VERSION.equalsIgnoreCase(r.getTagName());

        if(!latestVersion)
            logger.warn("!! You're currently on an old version of CHScrobbler. Please update CHScrobbler using the link above as soon as possible. !!");
    }

    private static void initSetup(File file)
    {
        try { Setup.init(file); }
        catch(IOException e)
        {
            logger.error("There was an error making the config file. Please report this error to @angelolz1 on Github or Twitter.", e);
        }
    }

    private static void printSettings()
    {
        logger.info("------- CHScrobbler Settings -------");
        logger.info("Clone Hero Data folder: {}", config.getCloneHeroDataFolder());
        logger.info("ScoreSpy Data Folder: {}", config.getScorespyDataFolder());
        logger.info("ScoreSpy Mode: {}", config.isScoreSpyMode());
        logger.info("Scrobble threshold: {} seconds", config.getScrobbleThreshold());
        logger.info("------------------------------------");
    }
}
