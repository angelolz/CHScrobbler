package main;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.CallException;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import objects.Config;
import objects.ReleaseJson;
import lombok.Getter;
import methods.Setup;
import methods.Statics;
import methods.Utils;
import methods.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;

public class CHScrobbler
{
    private static final String VERSION = "v1.6";
    @Getter
    private static Logger logger;
    @Getter
    private static Config config;

    public static void main(String[] args)
    {
        //logger for CHScrobbler
        logger = LoggerFactory.getLogger(CHScrobbler.class);

        //logger for last.fm api
        Caller.getInstance().getLogger().setLevel(Level.WARNING);

        logger.info("Thanks for using CHScrobbler {} by angelolz :) | {}", VERSION, Statics.REPO_URL);
        logger.info("If you like this program, I would appreciate a donation! {}", Statics.DONATE_URL);
        checkVersion();

        try
        {
            File configFile = new File(Statics.CONFIG_FILE);

            //start setup if config.json isn't found
            if(!configFile.exists())
                config = initSetup();
            else
            {
                FileReader fr = new FileReader(configFile);
                config = new Gson().fromJson(fr, Config.class);
                fr.close();

                //start setup if config.json is empty
                if(config == null)
                    config = initSetup();
            }

            Validator validator = new Validator(config);
            config = validator.validateSettings();
            printSettings();

            //logs in last.fm with provided info
            Config.LastFmCredentials lastFmCredentials = config.getLastFmCredentials();
            Session session = Authenticator.getMobileSession(lastFmCredentials.getLastfmUserName(), lastFmCredentials.getLastFmPassword(), lastFmCredentials.getLastFmApiKey(), lastFmCredentials.getLastFmSecret());

            if(session == null)
                Utils.showErrorAndExit(Statics.LAST_FM_INIT_ERROR, "Unable to establish connection with last.fm! Please make sure your last.fm details are correct!");

            logger.info("Successfully logged in with last.fm!");
            ScrobblerManager.init(session);
        }

        catch(CallException e)
        {
            if(e.getCause() instanceof UnknownHostException)
                Utils.showErrorAndExit(Statics.LAST_FM_INIT_ERROR, "Unable to establish connection with last.fm! Please check your internet connection.");

            else
            {
                logger.error("Something went wrong when establishing a connection with last.fm! " + Statics.REPORT_MESSAGE, e);
                Utils.showErrorAndExit(Statics.LAST_FM_INIT_ERROR, "Something went wrong when establishing a connection with last.fm!\n" + Statics.REPORT_MESSAGE);
            }
        }

        catch(JsonSyntaxException e)
        {
            Utils.showErrorAndExit(Statics.CONFIG_ERROR, String.format("Your config is formatted incorrectly. %nPlease delete your %s file and restart CHScrobbler to go through the setup.", Statics.CONFIG_FILE));
        }

        catch(Exception e)
        {
            logger.error("Something went wrong reading the config file! " + Statics.REPORT_MESSAGE, e);
        }
    }

    private static void checkVersion()
    {
        String json = Utils.readURL("https://api.github.com/repos/angelolz/CHScrobbler/releases/latest");

        if(json == null)
        {
            CHScrobbler.getLogger().error("Unable to check for new updates! This is probably due to no internet connection or GitHub is down.");
            return;
        }

        Gson gson = new Gson();
        ReleaseJson r = gson.fromJson(json, ReleaseJson.class);

        boolean latestVersion = VERSION.equalsIgnoreCase(r.getTagName());

        if(!latestVersion) {
            logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.warn("!! A new CHScrobbler version is available. !!");
            logger.warn("!! Please update using the link above.     !!");
            logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private static Config initSetup()
    {
        try
        {
            return Setup.init();
        }
        catch(IOException e)
        {
            logger.error("There was an error making the config file. " + Statics.REPORT_MESSAGE, e);
            Utils.showErrorAndExit(Statics.CONFIG_ERROR, "There was an error making the config file. " + Statics.REPORT_MESSAGE);
            return null; //shouldn't reach here cause app will exit above
        }
    }

    private static void printSettings()
    {
        logger.info("------- CHScrobbler Settings -------");
        logger.info("last.fm user: {}", config.getLastFmCredentials().getLastfmUserName());
        logger.info("Game Mode: {}", config.getGameMode().getGameName());

        String folderPath = Utils.getFolderPath(config.getGameMode(), config.getDataFolders());
        logger.info("Data Folder: {}", folderPath.isEmpty() ? "[same as CHScrobbler]" : folderPath);
        logger.info("Scrobble threshold: {} seconds", config.getScrobbleThreshold());
        logger.info("------------------------------------");
    }
}
