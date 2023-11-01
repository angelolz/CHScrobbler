package methods;

import com.google.gson.GsonBuilder;
import objects.Config;
import objects.Game;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            return null;
        }
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
        String defaultUserDirectory = FileSystemView.getFileSystemView().getDefaultDirectory().getPath().replaceAll("\\\\", "/");

        if(isLinux())
            return defaultUserDirectory + "/.clonehero";
        else
            return defaultUserDirectory + "/Clone Hero";
    }

    public static String getDefaultScoreSpyDataFolder()
    {
        if(isLinux())
            return FileSystemView.getFileSystemView().getDefaultDirectory().getPath().replaceAll("\\\\", "/") + "/ScoreSpy/100";
        else if(isMac()) //scorespy isn't supported for macs
            return "";
        else
            return System.getenv("ProgramFiles").replaceAll("\\\\", "/") + "/ScoreSpy Launcher/GameData/100";
    }

    public static String getDefaultYARGFolder()
    {
        if(isLinux())
            return FileSystemView.getFileSystemView().getDefaultDirectory().getPath().replaceAll("\\\\", "/") + "/.config/unity3d/YARC/YARG/";
        else if(isMac())
            return System.getProperty("user.home").replaceAll("\\\\", "/") + "/Library/Application Support/com.UnityTechnologies.com.unity.template-starter-kit";
        else
            return System.getProperty("user.home").replaceAll("\\\\", "/") + "/AppData/LocalLow/YARC/YARG";
    }

    public static boolean isMac()
    {
        String os = System.getProperty(Statics.OS_NAME);
        return os.toLowerCase().contains("mac os x");
    }

    public static boolean isLinux()
    {
        String os = System.getProperty(Statics.OS_NAME);
        return os.toLowerCase().contains("linux");
    }

    public static void showErrorAndExit(String title, String message)
    {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    public static void setFolderPath(Config.DataFolders dataFolders, Game game, String folderPath)
    {
        switch(game)
        {
            case CLONE_HERO:
                dataFolders.setCloneHeroDataFolder(folderPath);
                break;
            case SCORESPY:
                dataFolders.setScoreSpyDataFolder(folderPath);
                break;
            case YARG:
                dataFolders.setYARGDataFolder(folderPath);
                break;
        }
    }

    public static String getFolderPath(Game gameMode, Config.DataFolders dataFolders)
    {
        switch(gameMode)
        {
            case CLONE_HERO:
                return dataFolders.getCloneHeroDataFolder();
            case SCORESPY:
                return dataFolders.getScoreSpyDataFolder();
            case YARG:
                return dataFolders.getYARGDataFolder();
            default:
                return null;
        }
    }

    public static Path getFilePath(Game gameMode, Config.DataFolders dataFolders, String fileName)
    {
        return Paths.get(getFolderPath(gameMode, dataFolders), fileName);
    }

    public static void writeSettings(Config config) throws IOException
    {
        FileWriter fw = new FileWriter(Statics.CONFIG_FILE);
        new GsonBuilder().setPrettyPrinting().create().toJson(config, fw);
        fw.close();
    }
}
