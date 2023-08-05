package main;

import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleData;
import de.umass.lastfm.scrobble.ScrobbleResult;
import methods.EscapeRegex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ScrobblerManager
{
    private static Session session;
    private static String dataDir;
    private static int scrobbleThresholdSeconds;
    private static ScrobbleData scrobbleData;
    private static boolean attemptedScrobble;
    private static boolean warnedNotFound;
    private static boolean loggedException;
    private static Pattern customSongPattern;
    private static boolean customSongPatternContainsAlbum;

    public static void init(Session session, String dataDir, int scrobbleThresholdSeconds)
    {
        ScrobblerManager.session = session;
        ScrobblerManager.dataDir = dataDir;
        ScrobblerManager.scrobbleThresholdSeconds = scrobbleThresholdSeconds;

        ScrobblerManager.initCustomSongPattern();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ScrobblerManager::scrobble, 0, 1, TimeUnit.SECONDS);
    }

    private static void initCustomSongPattern()
    {
        Optional<String> customSongExportSetting = getCustomSongExportSetting();
        boolean containsTrackAndArtist = customSongExportSetting.map(x -> x.contains("%s") && x.contains("%a")).orElse(true);
        boolean containsAlbum = customSongExportSetting.map(x -> x.contains("%b")).orElse(false);

        if (customSongExportSetting.isPresent())
            CHScrobbler.getLogger().info("The Clone Hero setting \"custom_song_export\" is currently set to \"{}\".", customSongExportSetting.get());
        else
            CHScrobbler.getLogger().info("The Clone Hero setting \"custom_song_export\" is currently not set.");

        if(!containsTrackAndArtist)
            CHScrobbler.getLogger().error("\"custom_song_export\" must contain \"%s\" and \"%a\". This enables CHScrobbler to function correctly.");

        if(!containsAlbum)
            CHScrobbler.getLogger().warn("\"custom_song_export\" should contain \"%b\". This enables CHScrobbler to include the album name when scrobbling.");
        else
            CHScrobbler.getLogger().info("\"custom_song_export\" contains \"%b\". This enables CHScrobbler to include the album name when scrobbling.");

        //no need for regex if custom_song_export is unchanged
        if(customSongExportSetting.map(x -> x.startsWith("%s%n%a") && !containsAlbum).orElse(true))
            return;

        String value = customSongExportSetting.get();


        String regex = EscapeRegex.escapeRegex(value)
                .replace("%n", "\\R")
                .replaceAll("%(\\w)", "(?<$1>.*)");

        ScrobblerManager.customSongPattern = Pattern.compile(regex);
        ScrobblerManager.customSongPatternContainsAlbum = containsAlbum;
    }

    private static Optional<String> getCustomSongExportSetting()
    {
        Path settingsFilePath = Paths.get(dataDir, "settings.ini");

        if(!Files.exists(settingsFilePath))
            return Optional.empty();

        try(Stream<String> lines = Files.lines(settingsFilePath, StandardCharsets.UTF_8))
        {
            String settingPrefix = "custom_song_export =";

            return lines
                    .map(String::trim)
                    .filter(x -> x.startsWith(settingPrefix))
                    .findFirst()
                    .map(x -> x.substring(settingPrefix.length()).trim());
        }
        catch(IOException e)
        {
            CHScrobbler.getLogger().error("Couldn't read the \"settings.ini\" file.", e);

            return Optional.empty();
        }
    }

    private static void scrobble()
    {
        try
        {
            Path currentSongFilePath = Paths.get(dataDir, "currentsong.txt");

            if(!Files.exists(currentSongFilePath))
            {
                if(!warnedNotFound)
                {
                    warnedNotFound = true;
                    CHScrobbler.getLogger().warn("Unable to find \"currentsong.txt\"! Please make sure you have \"Export Current Song\" " +
                            "enabled in Settings and your Clone Hero data folder is set correctly.");
                }

                return;
            }

            byte[] currentSongBytes;

            try
            {
                currentSongBytes = Files.readAllBytes(currentSongFilePath);
            }

            catch(IOException e)
            {
                CHScrobbler.getLogger().error("Sorry, couldn't find or read the \"currentsong.txt\" file! Please try opening this app again.", e);
                Executors.newSingleThreadScheduledExecutor().schedule(() -> System.exit(0), 5, TimeUnit.SECONDS);

                return;
            }

            if(currentSongBytes.length == 0)
            {
                clearScrobbleData();
                return;
            }

            String currentSong = new String(currentSongBytes, StandardCharsets.UTF_8);
            if(currentSong.isEmpty())
            {
                clearScrobbleData();
                return;
            }

            String artist, track, album;

            if(customSongPattern != null)
            {
                Matcher matcher = customSongPattern.matcher(currentSong);
                if(!matcher.find())
                {
                    clearScrobbleData();
                    return;
                }

                artist = matcher.group("a").trim();
                track = matcher.group("s").trim();
                album = customSongPatternContainsAlbum ? matcher.group("b").trim() : "";
            }

            else
            {
                String[] lines = currentSong.split("\\R");

                artist = lines[1];
                track = lines[0];
                album = "";
            }

            //removes the song speed modifier from the title
            track = track.replaceAll("(\\(\\d+%\\))", "").trim();

            if(scrobbleData == null
                    || !scrobbleData.getArtist().equalsIgnoreCase(artist)
                    || !scrobbleData.getTrack().equalsIgnoreCase(track)
                    || !scrobbleData.getAlbum().equalsIgnoreCase(album))
            {
                scrobbleData = new ScrobbleData(artist, track, (int) (System.currentTimeMillis() / 1000));

                if(!album.isEmpty())
                {
                    scrobbleData.setAlbum(album);
                    scrobbleData.setAlbumArtist(artist);
                }

                Track.updateNowPlaying(scrobbleData, session);

                CHScrobbler.getLogger().info("Now playing \"{}\" by {}{}", track, artist, album.isEmpty() ? "" : ", from the album \"" + album + "\".");

                attemptedScrobble = false;
            }

            else if(!attemptedScrobble && (System.currentTimeMillis() / 1000) - scrobbleData.getTimestamp() >= scrobbleThresholdSeconds)
            {
                attemptedScrobble = true;
                ScrobbleResult result = Track.scrobble(scrobbleData, session);

                if(result.isSuccessful() && !result.isIgnored())
                    CHScrobbler.getLogger().info("Scrobbled the currently playing song!");
                else
                    CHScrobbler.getLogger().warn("Couldn't scrobble the currently playing song!");
            }
        }

        catch(Exception e)
        {
            if (!loggedException)
            {
                loggedException = true;
                CHScrobbler.getLogger().error("Something went wrong! Please send a screenshot of this error log to @angelolz1 on GitHub or Twitter.", e);
            }
        }
    }

    private static void clearScrobbleData()
    {
        if(scrobbleData == null)
            return;

        scrobbleData = null;
        attemptedScrobble = false;

        CHScrobbler.getLogger().info("Currently not playing anything!");
    }
}
