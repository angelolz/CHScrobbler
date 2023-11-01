package objects;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public enum Game
{
    @SerializedName("clonehero")
    CLONE_HERO("Clone Hero", "clonehero"),

    @SerializedName("scorespy")
    SCORESPY("ScoreSpy","scorespy"),

    @SerializedName("yarg")
    YARG("YARG","yarg");

    private final String gameName;
    private final String settingName;

    Game(String gameName, String settingName)
    {
        this.gameName = gameName;
        this.settingName = settingName;
    }
}
