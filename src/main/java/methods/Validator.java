package methods;

import main.CHScrobbler;
import objects.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Validator
{
    public static void validateSettings(File configFile, Config config, Properties prop) throws IOException
    {
        validateLastFmCredentials(prop, config);
        validateDataFolder(prop, config, configFile, Statics.CH_DATA_FOLDER_PROP_NAME, Utils.getDefaultCloneHeroDataFolder());
        validateDataFolder(prop, config, configFile, Statics.SS_DATA_FOLDER_PROP_NAME, Utils.getDefaultScoreSpyDataFolder());
        validateScrobbleThreshold(prop, config, configFile);
    }

    private static void validateLastFmCredentials(Properties prop, Config config)
    {
        String fillInText = String.format("Please fill them in with the %s file.", Statics.CONFIG_FILE);
        String apiKey = prop.getProperty("lastfm_apikey");
        String secret = prop.getProperty("lastfm_secret");
        String username = prop.getProperty("lastfm_username");
        String password = prop.getProperty("lastfm_password");

        if(Utils.isNullOrEmpty(apiKey))
            Utils.showErrorAndExit(Statics.LAST_FM_INIT_ERROR, "last.fm API Key cannot be blank! " + fillInText);
        if(Utils.isNullOrEmpty(secret))
            Utils.showErrorAndExit(Statics.LAST_FM_INIT_ERROR, "last.fm shared secret key cannot be blank! " + fillInText);
        if(Utils.isNullOrEmpty(username))
            Utils.showErrorAndExit(Statics.LAST_FM_INIT_ERROR, "last.fm username cannot be blank! " + fillInText);
        if(Utils.isNullOrEmpty(password))
            Utils.showErrorAndExit(Statics.LAST_FM_INIT_ERROR, "last.fm password cannot be blank! " + fillInText);

        config.setLastFmApiKey(apiKey)
              .setLastFmSecret(secret)
              .setUsername(username)
              .setPassword(password);
    }

    private static void validateDataFolder(Properties prop, Config config, File configFile, String propertyName, String defaultFolder) throws
        IOException
    {
        String dataFolder = prop.getProperty(propertyName);

        if(dataFolder == null)
        {
            dataFolder = defaultFolder;
            writeSetting(configFile, prop, propertyName, dataFolder);
            CHScrobbler.getLogger().info("Couldn't find data folder setting, using default data folder: {}", dataFolder);
        }

        if(propertyName.equals(Statics.CH_DATA_FOLDER_PROP_NAME))
            config.setCloneHeroDataFolder(dataFolder);
        else if(propertyName.equals(Statics.SS_DATA_FOLDER_PROP_NAME))
            config.setScorespyDataFolder(dataFolder);
    }

    private static void validateScrobbleThreshold(Properties prop, Config config, File configFile) throws IOException
    {
        String thresholdString = prop.getProperty(Statics.SCROBBLE_THRESHOLD_PROP_NAME);
        int threshold = Statics.DEFAULT_SCROBBLE_THRESHOLD;

        if(Utils.isNullOrEmpty(thresholdString))
        {
            thresholdString = String.valueOf(Statics.DEFAULT_SCROBBLE_THRESHOLD);
            writeSetting(configFile, prop, Statics.SCROBBLE_THRESHOLD_PROP_NAME, thresholdString);
            CHScrobbler.getLogger().info("Couldn't find scrobble threshold setting, using default threshold of {} seconds.", Statics.DEFAULT_SCROBBLE_THRESHOLD);
        }

        if(Utils.isNumber(thresholdString))
        {
            threshold = Integer.parseInt(thresholdString);

            if(threshold < Statics.DEFAULT_SCROBBLE_THRESHOLD || threshold > 240)
            {
                CHScrobbler.getLogger().warn("scrobble_threshold_seconds must be between {} and 240 seconds (4 minutes). Using default of {} seconds.", Statics.DEFAULT_SCROBBLE_THRESHOLD, Statics.DEFAULT_SCROBBLE_THRESHOLD);
                threshold = Statics.DEFAULT_SCROBBLE_THRESHOLD;
            }
        }
        else
            CHScrobbler.getLogger().warn("scrobble_threshold_seconds is not a valid number. Using default of {} seconds.", Statics.DEFAULT_SCROBBLE_THRESHOLD);

        config.setScrobbleThreshold(threshold);
    }

    private static void writeSetting(File configFile, Properties prop, String setting, String value) throws
        IOException
    {
        FileOutputStream fr = new FileOutputStream(configFile);
        prop.setProperty(setting, value);
        prop.store(fr, null);
    }
}
