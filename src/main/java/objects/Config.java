package objects;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Config
{
    private LastFmCredentials lastFmCredentials;
    private DataFolders dataFolders;
    private String scrobbleThreshold;
    private Game gameMode;

    @Data
    public static class LastFmCredentials
    {
        private String lastfmUserName;
        private String lastFmPassword;
        private String lastFmApiKey;
        private String lastFmSecret;
    }

    @Data
    public static class DataFolders
    {
        private String cloneHeroDataFolder;
        private String scorespyDataFolder;
        private String YARGDataFolder;
    }
}
