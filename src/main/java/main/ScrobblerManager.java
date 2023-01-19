package main;

import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScrobblerManager
{
    private static Session session;
    private static boolean playing = false, attemptedScrobble = false;
    private static String trackArtist = "", trackTitle = "";
    private static int timestamp;

    public static void init(Session s)
    {
        session = s;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ScrobblerManager::scrobble, 0, 1, TimeUnit.SECONDS);
    }

    private static void scrobble()
    {
        try
        {
            File file = new File(CHScrobbler.getDataDirectory() + "/currentsong.txt");
            if(!file.exists())
            {
                System.out.print("\rUnable to find 'currentsong.txt'! Please make sure you have \"Export Current Song\" " +
                    "enabled in Settings and your Clone Hero data folder is set correctly.");
            }

            else
            {
                List<String> trackInfo = Files.readAllLines(Paths.get(CHScrobbler.getDataDirectory() + "/currentsong.txt"));
                if(trackInfo.size() > 0)
                {
                    //removes the song speed modifier from the title
                    String correctedTitle = trackInfo.get(0).replaceAll("(\\(\\d+%\\))", "").trim();

                    if(!trackArtist.equalsIgnoreCase(trackInfo.get(1)) && !trackTitle.equalsIgnoreCase(correctedTitle))
                    {
                        timestamp = (int) (System.currentTimeMillis() / 1000);
                        trackArtist = trackInfo.get(1);
                        trackTitle = correctedTitle;

                        if(!playing)
                        {
                            CHScrobbler.getLogger().info("Now Playing: " + trackInfo.get(1) + " - " + correctedTitle);
                            playing = true;
                        }
                    }

                    else
                    {
                        if(!attemptedScrobble)
                        {
                            if(System.currentTimeMillis()/1000 - timestamp >= 25)
                            {
                                ScrobbleResult result = Track.scrobble(trackArtist, correctedTitle, timestamp, session);

                                if(result.isSuccessful() && !result.isIgnored())
                                {
                                    CHScrobbler.getLogger().info("Scrobbled the currently playing song!");
                                }

                                else
                                {
                                    CHScrobbler.getLogger().warn("Couldn't scrobble the currently playing song!");
                                }

                                attemptedScrobble = true;
                            }
                        }
                    }
                }

                else
                {
                    if(playing)
                    {
                        playing = false;
                        attemptedScrobble = false;
                        trackArtist = "";
                        trackTitle = "";
                        timestamp = 0;

                        System.out.print("Currently not playing anything!\r");
                    }
                }
            }
        }

        catch(IOException e)
        {
            System.out.println("Sorry, couldn't find or read the 'currentsong.txt' file! Please try opening this app again.");
            Executors.newSingleThreadScheduledExecutor().schedule(() -> System.exit(0), 5, TimeUnit.SECONDS);
        }
    }
}
