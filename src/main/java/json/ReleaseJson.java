package json;

import com.google.gson.annotations.SerializedName;

public class ReleaseJson
{
    @SerializedName("tag_name")
    private String tagName;

    public String getTagName()
    {
        return tagName;
    }
}
