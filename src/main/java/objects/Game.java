package objects;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public enum Game
{
    @SerializedName("clonehero")
    CLONE_HERO("Clone Hero"),

    @SerializedName("scorespy")
    SCORESPY("ScoreSpy"),

    @SerializedName("yarg")
    YARG("YARG");

    private final String gameName;

    Game(String gameName)
    {
        this.gameName = gameName;
    }
}
