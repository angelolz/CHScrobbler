package objects;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class ReleaseJson
{
    @SerializedName("tag_name")
    private String tagName;
}
