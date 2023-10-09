package objects;

public class Config
{
    private String lastFmApiKey;
    private String lastFmSecret;
    private String username;
    private String password;
    private String cloneHeroDataFolder;
    private String scorespyDataFolder;
    private Integer scrobbleThreshold;

    boolean scoreSpyMode;

    public String getLastFmApiKey()
    {
        return lastFmApiKey;
    }

    public Config setLastFmApiKey(String lastFmApiKey)
    {
        this.lastFmApiKey = lastFmApiKey;
        return this;
    }

    public String getLastFmSecret()
    {
        return lastFmSecret;
    }

    public Config setLastFmSecret(String lastFmSecret)
    {
        this.lastFmSecret = lastFmSecret;
        return this;
    }

    public String getUsername()
    {
        return username;
    }

    public Config setUsername(String username)
    {
        this.username = username;
        return this;
    }

    public String getPassword()
    {
        return password;
    }

    public Config setPassword(String password)
    {
        this.password = password;
        return this;
    }

    public String getCloneHeroDataFolder()
    {
        return cloneHeroDataFolder;
    }

    public Config setCloneHeroDataFolder(String cloneHeroDataFolder)
    {
        this.cloneHeroDataFolder = cloneHeroDataFolder;
        return this;
    }

    public String getScorespyDataFolder()
    {
        return scorespyDataFolder;
    }

    public Config setScorespyDataFolder(String scorespyDataFolder)
    {
        this.scorespyDataFolder = scorespyDataFolder;
        return this;
    }

    public Integer getScrobbleThreshold()
    {
        return scrobbleThreshold;
    }

    public Config setScrobbleThreshold(Integer scrobbleThreshold)
    {
        this.scrobbleThreshold = scrobbleThreshold;
        return this;
    }

    public boolean isScoreSpyMode()
    {
        return scoreSpyMode;
    }

    public Config setScoreSpyMode(boolean scoreSpyMode)
    {
        this.scoreSpyMode = scoreSpyMode;
        return this;
    }

    @Override
    public String toString()
    {
        return String.format("lastfm_username=%s\n" +
                "lastfm_password=%s\n" +
                "lastfm_apikey=%s\n" +
                "lastfm_secret=%s\n" +
                "clonehero_data_folder=%s\n" +
                "scorespy_data_folder=%s\n" +
                "scrobble_threshold_seconds=%s\n" +
                "scorespy_mode=%s",
            username, password, lastFmApiKey, lastFmSecret, cloneHeroDataFolder, scorespyDataFolder, scrobbleThreshold, scoreSpyMode);
    }
}
