package objects;

public class Song
{
    private final String artist;
    private final String track;
    private final String album;

    public Song(String artist, String track, String album)
    {
        this.artist = artist;
        this.track = track;
        this.album = album;
    }

    public String getArtist()
    {
        return artist;
    }

    public String getTrack()
    {
        return track;
    }

    public String getAlbum()
    {
        return album;
    }
}
