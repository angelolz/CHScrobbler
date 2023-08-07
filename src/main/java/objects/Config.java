package objects;

public class Config
{
    String lastFmApiKey;
    String lastFmSecret;
    String username;
    String password;
    String dataFolder;
    Integer scrobbleThreshold;

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

    public String getDataFolder()
    {
        return dataFolder;
    }

    public Config setDataFolder(String dataFolder)
    {
        this.dataFolder = dataFolder;
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
}
