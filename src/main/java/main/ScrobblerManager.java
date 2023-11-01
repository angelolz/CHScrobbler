package main;

import com.google.gson.Gson;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleData;
import de.umass.lastfm.scrobble.ScrobbleResult;
import methods.Statics;
import methods.Utils;
import objects.Game;
import objects.Song;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ScrobblerManager
{
    private static Session session;
    private static ScrobbleData scrobbleData;
    private static boolean attemptedScrobble;
    private static boolean warnedNotFound;
    private static boolean loggedException;
    private static Pattern customSongPattern;
    private static boolean customSongPatternContainsAlbum;

    public static void init(Session session)
    {
        ScrobblerManager.session = session;
        ScrobblerManager.initCustomSongPattern();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ScrobblerManager::scrobble, 0, 1, TimeUnit.SECONDS);
    }

    private static void initCustomSongPattern()
    {
        if(CHScrobbler.getConfig().getGameMode() == Game.YARG)
            return; //YARG doesn't have the same format as CH or ScoreSpy

        Optional<String> customSongExportSetting = getCustomSongExportSetting();
        boolean containsTrackAndArtist = customSongExportSetting.map(x -> x.contains("%s") && x.contains("%a")).orElse(true);
        boolean containsAlbum = customSongExportSetting.map(x -> x.contains("%b")).orElse(false);
        boolean correctAlbumTag = customSongExportSetting.map(x -> x.startsWith("%b%n") || x.endsWith("%n%b") || x.contains("%n%b%n")).orElse(false);

        if(customSongExportSetting.isPresent() && !customSongExportSetting.get().isEmpty())
            CHScrobbler.getLogger().info("{} is currently set to \"{}\".", Statics.CUSTOM_SONG_EXPORT, customSongExportSetting.get());
        else
            Utils.showErrorAndExit(Statics.CUSTOM_SONG_EXPORT_ERROR, Statics.CUSTOM_SONG_EXPORT + " setting does not exist or is blank! Please add it in the " + Statics.GAME_SETTINGS_FILE + " file found in the data folder.");

        if(!containsTrackAndArtist)
            Utils.showErrorAndExit(Statics.CUSTOM_SONG_EXPORT_ERROR, Statics.CUSTOM_SONG_EXPORT + " must contain \"%s\" and \"%a\". This is required for CHScrobbler to function correctly.");

        if(!containsAlbum)
        {
            CHScrobbler.getLogger().warn("--------------------------------------------");
            CHScrobbler.getLogger().warn("\"{}\" does NOT contain \"%b\". CHScrobbler will NOT include the album name when scrobbling.", Statics.CUSTOM_SONG_EXPORT);
            CHScrobbler.getLogger().warn("To include the album name in your scrobble, add \"%n%b\" at the end of your \"{}\".", Statics.CUSTOM_SONG_EXPORT);
            CHScrobbler.getLogger().warn("After changing \"{}\", restart Clone Hero and CHScrobbler.", Statics.CUSTOM_SONG_EXPORT);
            CHScrobbler.getLogger().warn("--------------------------------------------");
        }

        else
        {
            if(correctAlbumTag)
                CHScrobbler.getLogger().info("\"{}\" contains \"%b\". CHScrobbler will include the album name when scrobbling.", Statics.CUSTOM_SONG_EXPORT);
            else
                Utils.showErrorAndExit(Statics.CUSTOM_SONG_EXPORT_ERROR, Statics.CUSTOM_SONG_EXPORT + " contains \"%b\" but is improperly formatted. Please add \"%n\" before and/or after it, wherever necessary.");
        }

        //no need for regex if custom_song_export is unchanged
        if(customSongExportSetting.map(x -> x.equals("%s%n%a%n%c")).orElse(true))
            return;

        String value = customSongExportSetting.get();
        String regex = Utils.escapeRegex(value)
                            .replace("%n", "\\R")
                            .replaceAll("%(\\w)", "(?<$1>.*)");

        ScrobblerManager.customSongPattern = Pattern.compile(regex);
        ScrobblerManager.customSongPatternContainsAlbum = containsAlbum;
    }

    private static Optional<String> getCustomSongExportSetting()
    {
        Path settingsFilePath = Utils.getFilePath(CHScrobbler.getConfig().getGameMode(), CHScrobbler.getConfig().getDataFolders(), Statics.GAME_SETTINGS_FILE);

        if(CHScrobbler.getConfig().getGameMode() == Game.YARG || !Files.exists(settingsFilePath)) //YARG doesn't have a custom song setting
            return Optional.empty();

        try(Stream<String> lines = Files.lines(settingsFilePath, StandardCharsets.UTF_8))
        {
            String settingPrefix = Statics.CUSTOM_SONG_EXPORT + " =";

            return lines
                .map(String::trim)
                .filter(x -> x.startsWith(settingPrefix))
                .findFirst()
                .map(x -> x.substring(settingPrefix.length()).trim());
        }

        catch(IOException e)
        {
            CHScrobbler.getLogger().error("Couldn't read the " + Statics.GAME_SETTINGS_FILE + " file.", e);
            return Optional.empty();
        }
    }

    private static void scrobble()
    {
        try
        {
            String currentSong = getCurrentSong();
            if(Utils.isNullOrEmpty(currentSong))
            {
                clearScrobbleData();
                return;
            }

            Song songData = getSongData(currentSong);
            if(songData == null)
                return;

            if(needsScrobbleUpdate(songData))
            {
                updateScrobbleData(songData);
                Track.updateNowPlaying(scrobbleData, session);
                logNowPlaying(songData);
                attemptedScrobble = false;
            }

            else if(!attemptedScrobble && shouldAttemptScrobble())
            {
                attemptedScrobble = true;
                ScrobbleResult result = Track.scrobble(scrobbleData, session);

                if(getScrobbleResult(result))
                    CHScrobbler.getLogger().info("Scrobbled the currently playing song!");
                else
                    CHScrobbler.getLogger().warn("Couldn't scrobble the currently playing song!");
            }
        }
        catch(Exception e)
        {
            handleException(e);
        }
    }

    private static boolean needsScrobbleUpdate(Song songData)
    {
        return scrobbleData == null ||
            !songData.getArtist().equalsIgnoreCase(scrobbleData.getArtist()) ||
            !songData.getTrack().equalsIgnoreCase(scrobbleData.getTrack()) ||
            (customSongPatternContainsAlbum && !songData.getAlbum().equalsIgnoreCase(scrobbleData.getAlbum()));
    }

    private static boolean shouldAttemptScrobble()
    {
        return !attemptedScrobble && (System.currentTimeMillis() / 1000) - scrobbleData.getTimestamp() >= Long.parseLong(CHScrobbler.getConfig().getScrobbleThreshold());
    }

    private static void updateScrobbleData(Song songData)
    {
        scrobbleData = new ScrobbleData(songData.getArtist(), songData.getTrack(), (int) (System.currentTimeMillis() / 1000));

        if(!songData.getAlbum().isEmpty())
        {
            scrobbleData.setAlbum(songData.getAlbum());
            scrobbleData.setAlbumArtist(songData.getArtist());
        }
    }

    private static void logNowPlaying(Song songData)
    {
        CHScrobbler.getLogger().info("Now playing \"{}\" by {}{}", songData.getTrack(), songData.getArtist(), songData.getAlbum() == null || songData.getAlbum().isEmpty() ? "" : ", from the album \"" + songData.getAlbum() + "\".");
    }

    private static boolean getScrobbleResult(ScrobbleResult result)
    {
        return result.isSuccessful() && !result.isIgnored();
    }

    private static void handleException(Exception e)
    {
        if(!loggedException)
        {
            loggedException = true;
            CHScrobbler.getLogger().error("Something went wrong! " + Statics.REPORT_MESSAGE, e);
        }
    }

    private static String getCurrentSong()
    {
        Path currentSongFilePath;
        if(CHScrobbler.getConfig().getGameMode() == Game.YARG)
            currentSongFilePath = Utils.getFilePath(CHScrobbler.getConfig().getGameMode(), CHScrobbler.getConfig().getDataFolders(), Statics.CURRENT_SONG_JSON);
        else
            currentSongFilePath = Utils.getFilePath(CHScrobbler.getConfig().getGameMode(), CHScrobbler.getConfig().getDataFolders(), Statics.CURRENT_SONG_TXT);

        try
        {
            if(!Files.exists(currentSongFilePath))
            {
                if(!warnedNotFound)
                {
                    warnedNotFound = true;
                    CHScrobbler.getLogger().warn("Unable to find \"currentsong.txt\"! Please make sure you have \"Export Current Song\" " +
                        "enabled in Settings and your Clone Hero data folder is set correctly.");
                }

                return null;
            }

            else
            {
                warnedNotFound = false;
                return new String(Files.readAllBytes(currentSongFilePath), StandardCharsets.UTF_8);
            }
        }

        catch(IOException e)
        {
            CHScrobbler.getLogger().error("Sorry, couldn't find or read the \"currentsong.txt\" file! Please try opening this app again.", e);
            return null;
        }
    }

    private static Song getSongData(String currentSong)
    {
        if(CHScrobbler.getConfig().getGameMode() == Game.YARG)
            return new Gson().fromJson(currentSong, Song.class);

        //else clone hero and score spy processing
        String artist;
        String track;
        String album;

        if(customSongPattern != null)
        {
            Matcher matcher = customSongPattern.matcher(currentSong);
            if(!matcher.find())
            {
                clearScrobbleData();
                return null;
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
        return new Song(artist, track, album);
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
