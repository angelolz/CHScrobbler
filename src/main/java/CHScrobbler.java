import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class CHScrobbler
{
    private static String trackTitle = "", trackArtist = "";
    private static Session session;

    public static void main(String[] args)
    {
        try
        {
            //get api and auth info
            Properties prop = new Properties();
            FileInputStream propFile = new FileInputStream("config.properties");
            prop.load(propFile);
            String lastFmApiKey = prop.getProperty("lastfm_apikey");
            String lastFmSecret = prop.getProperty("lastfm_secret");
            String user = prop.getProperty("lastfm_username");
            String pass = prop.getProperty("lastfm_password");

            Caller.getInstance().getLogger().setLevel(Level.WARNING);

            if(user.isEmpty() || pass.isEmpty())
            {
                System.out.println("Username or password cannot be blank! Please put them in config.properties!");
                Executors.newSingleThreadScheduledExecutor().schedule(() -> System.exit(0), 5, TimeUnit.SECONDS);
            }
            //logs in last.fm with provided info
            session = Authenticator.getMobileSession(user, pass, lastFmApiKey, lastFmSecret);

            //logged in, start scrobbling
            if(session == null)
            {
                System.out.println("Unable to establish connection with last.fm! Please make sure your username and password are correct!");
                Executors.newSingleThreadScheduledExecutor().schedule(() -> System.exit(0), 5, TimeUnit.SECONDS);
            }

            else
                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(CHScrobbler::scrobble, 0, 1, TimeUnit.SECONDS);
        }

        catch(IOException e)
        {
            System.out.println("Sorry, couldn't find or read the 'config.properties' file. " +
                "Make sure that it is in the same directory as Clone Hero and it is not a corrupted file!");
            Executors.newSingleThreadScheduledExecutor().schedule(() -> System.exit(0), 6, TimeUnit.SECONDS);
        }

        catch(Exception e)
        {
            System.out.println("Something went wrong! Please send a screenshot of this error log to Angel.");
            e.printStackTrace();
        }
    }

    private static void scrobble()
    {
        try
        {

            File file = new File("currentsong.txt");
            if(!file.exists())
            {
                System.out.print("\rUnable to find 'currentsong.txt'! Please make sure you have \"Export Current Song\" enabled in Settings.");
            }

            else
            {
                List<String> trackInfo = Files.readAllLines(Paths.get("currentsong.txt"));
                if(trackInfo.size() > 0)
                {
                    if(!trackArtist.equalsIgnoreCase(trackInfo.get(1)) && !trackTitle.equalsIgnoreCase(trackInfo.get(0)))
                    {
                        int now = (int) (System.currentTimeMillis() / 1000);
                        trackArtist = trackInfo.get(1);
                        trackTitle = trackInfo.get(0);

                        ScrobbleResult result = Track.scrobble(trackArtist, trackTitle, now, session);

                        String str = "\r" +
                            "Now Playing: " + trackInfo.get(1) + " - " + trackInfo.get(0) +
                            " | " +
                            (result.isSuccessful() && !result.isIgnored() ? "Scrobbling!" : "Not scrobbling!") + "     ";
                        System.out.print(str);
                    }
                }

                else
                {
                    trackArtist = "";
                    trackTitle = "";
                    System.out.print("\r" + "Currently not playing anything!     ");
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
