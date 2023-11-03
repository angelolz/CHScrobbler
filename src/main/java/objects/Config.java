package objects;

import com.google.gson.annotations.SerializedName;
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
    @SerializedName("__comment")
    private String comment;

    public Config()
    {
        this.comment = "The gamemode options are clonehero, scorespy, or yarg. Must be lowercase.";
    }

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
        private String scoreSpyDataFolder;
        private String YARGDataFolder;
    }
}
