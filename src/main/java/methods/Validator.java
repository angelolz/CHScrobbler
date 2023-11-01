package methods;

import objects.Config;
import objects.Game;
import main.CHScrobbler;

import java.io.File;
import java.io.IOException;

public class Validator
{
    private final Config config;
    private boolean updated;

    public Validator(Config config)
    {
        this.config = config;
        this.updated = false;
    }

    public Config validateSettings() throws IOException
    {
        validateLastFmCredentials();
        validateMode();
        validateDataFolder();
        validateScrobbleThreshold();

        if(updated) Utils.writeSettings(config);

        return config;
    }

    private void validateLastFmCredentials() throws IOException
    {
        Config.LastFmCredentials lastFmCredentials = config.getLastFmCredentials();
        String fillInText = String.format("Please fill them in the %s file.", Statics.CONFIG_FILE);

        if(lastFmCredentials == null)
        {
            lastFmCredentials = new Config.LastFmCredentials();
            lastFmCredentials.setLastFmApiKey("");
            lastFmCredentials.setLastFmSecret("");
            lastFmCredentials.setLastfmUserName("");
            lastFmCredentials.setLastFmPassword("");
            config.setLastFmCredentials(lastFmCredentials);
            Utils.writeSettings(config);

//            Utils.showErrorAndExit(Statics.LAST_FM_INIT_ERROR, "Couldn't find last.fm credentials! " + fillInText);
        }

        String apiKey = lastFmCredentials.getLastFmApiKey();
        String secret = lastFmCredentials.getLastFmSecret();
        String username = lastFmCredentials.getLastfmUserName();
        String password = lastFmCredentials.getLastFmPassword();

        if(Utils.isNullOrEmpty(apiKey))
            Utils.showErrorAndExit(Statics.LAST_FM_INIT_ERROR, "last.fm API Key cannot be blank! " + fillInText);
        if(Utils.isNullOrEmpty(secret))
            Utils.showErrorAndExit(Statics.LAST_FM_INIT_ERROR, "last.fm shared secret key cannot be blank! " + fillInText);
        if(Utils.isNullOrEmpty(username))
            Utils.showErrorAndExit(Statics.LAST_FM_INIT_ERROR, "last.fm username cannot be blank! " + fillInText);
        if(Utils.isNullOrEmpty(password))
            Utils.showErrorAndExit(Statics.LAST_FM_INIT_ERROR, "last.fm password cannot be blank! " + fillInText);
    }

    private void validateMode()
    {
        if(config.getGameMode() == null)
        {
            config.setGameMode(Game.CLONE_HERO);
            updated = true;
            CHScrobbler.getLogger().warn("Game mode setting is not set or is incorrect, defaulting to Clone Hero.");
        }
    }

    private void validateDataFolder()
    {
        Config.DataFolders dataFolders = new Config.DataFolders();
        if(config.getDataFolders() != null)
            dataFolders = config.getDataFolders();

        for(Game game : Game.values())
        {
            String defaultFolder = null;
            String setFolder = null;
            switch(game)
            {
                case CLONE_HERO:
                    defaultFolder = Utils.getDefaultCloneHeroDataFolder();
                    setFolder = dataFolders.getCloneHeroDataFolder();
                    break;
                case SCORESPY:
                    defaultFolder = Utils.getDefaultScoreSpyDataFolder();
                    setFolder = dataFolders.getScoreSpyDataFolder();
                    break;
                case YARG:
                    defaultFolder = Utils.getDefaultYARGFolder();
                    setFolder = dataFolders.getYARGDataFolder();
                    break;
            }

            if(setFolder == null)
            {
                if(!new File(defaultFolder).exists())
                {
                    switch(game)
                    {
                        case CLONE_HERO:
                            dataFolders.setCloneHeroDataFolder("");
                            break;
                        case SCORESPY:
                            dataFolders.setScoreSpyDataFolder("");
                            break;
                        case YARG:
                            dataFolders.setYARGDataFolder("");
                            break;
                    }

                    updated = true;
                    CHScrobbler.getLogger().warn("Couldn't find data folder setting or default data folder for {}, setting to empty.", game.getGameName());
                }

                else
                {
                    Utils.setFolderPath(dataFolders, game, defaultFolder);
                    updated = true;
                    CHScrobbler.getLogger().warn("Couldn't find data folder setting for {}, using default data folder: {}", game.getGameName(), defaultFolder);
                }
            }
        }

        if(updated) config.setDataFolders(dataFolders);
    }

    private void validateScrobbleThreshold()
    {
        if(config.getScrobbleThreshold() == null)
        {
            config.setScrobbleThreshold(String.valueOf(Statics.DEFAULT_SCROBBLE_THRESHOLD));
            updated = true;
            CHScrobbler.getLogger().warn("Couldn't find scrobble threshold setting, using default threshold of {} seconds.", Statics.DEFAULT_SCROBBLE_THRESHOLD);
        }

        if(!Utils.isNumber(config.getScrobbleThreshold()))
        {
            config.setScrobbleThreshold(String.valueOf(Statics.DEFAULT_SCROBBLE_THRESHOLD));
            updated = true;
            CHScrobbler.getLogger().warn("Scrobble threshold setting is not a valid number, using default threshold of {} seconds.", Statics.DEFAULT_SCROBBLE_THRESHOLD);
        }

        else
        {
            int threshold = Integer.parseInt(config.getScrobbleThreshold());

            if(threshold < Statics.DEFAULT_SCROBBLE_THRESHOLD || threshold > 240)
            {
                config.setScrobbleThreshold(String.valueOf(Statics.DEFAULT_SCROBBLE_THRESHOLD));
                updated = true;
                CHScrobbler.getLogger().warn("Scrobble threshold must be between {} and 240 seconds (4 minutes). Using default of {} seconds.", Statics.DEFAULT_SCROBBLE_THRESHOLD, Statics.DEFAULT_SCROBBLE_THRESHOLD);
            }
        }
    }
}
