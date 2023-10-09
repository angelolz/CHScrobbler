package methods;

import main.CHScrobbler;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class Utils
{
    private static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

    public static String escapeRegex(String input)
    {
        return SPECIAL_REGEX_CHARS.matcher(input).replaceAll("\\\\$0");
    }

    public static String readURL(String string)
    {
        try
        {
            URL url = new URL(string);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream())))
            {
                StringBuilder buffer = new StringBuilder();
                int read;
                char[] chars = new char[1024];
                while((read = reader.read(chars)) != -1)
                {
                    buffer.append(chars, 0, read);
                }

                return buffer.toString();
            }
        }

        catch(IOException e)
        {
            CHScrobbler.getLogger().error("Unable to check for new updates!", e);
        }

        //if it fails
        return null;
    }

    public static boolean isNullOrEmpty(String str)
    {
        if(str == null)
            return true;
        else return str.isEmpty();
    }

    public static boolean isNumber(String str)
    {
        try
        {
            Integer.parseInt(str);
            return true;
        }

        catch(NumberFormatException e)
        {
            return false;
        }
    }

    public static String getDefaultCloneHeroDataFolder()
    {
        String os = System.getProperty(Statics.OS_NAME);
        String defaultUserDirectory = FileSystemView.getFileSystemView().getDefaultDirectory().getPath().replaceAll("\\\\", "/");

        if(os.toLowerCase().contains("linux"))
            return defaultUserDirectory + "/.clonehero";
        else
            return defaultUserDirectory + "/Clone Hero";
    }

    public static void showErrorAndExit(String title, String message)
    {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    public static String getDefaultScoreSpyDataFolder()
    {
        String os = System.getProperty(Statics.OS_NAME);

        if(os.toLowerCase().contains("linux"))
        {
            String defaultUserDirectory = FileSystemView.getFileSystemView().getDefaultDirectory().getPath().replaceAll("\\\\", "/");
            return defaultUserDirectory + "/ScoreSpy/100";
        }


        String defaultInstallDir = System.getenv("ProgramFiles") + "/ScoreSpy Launcher/GameData/100";
        defaultInstallDir = defaultInstallDir.replaceAll("\\\\", "/");

        return defaultInstallDir;
    }

    public static boolean isMac()
    {
        String os = System.getProperty(Statics.OS_NAME);
        return os.toLowerCase().contains("mac os x");
    }
}
