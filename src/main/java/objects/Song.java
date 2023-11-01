package objects;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class Song
{
    @SerializedName("Artist")
    private final String artist;

    @SerializedName("Name")
    private final String track;

    @SerializedName("Album")
    private final String album;

    public Song(String artist, String track, String album)
    {
        this.artist = artist;
        this.track = track;
        this.album = album;
    }
}
