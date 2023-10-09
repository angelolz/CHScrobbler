package methods;

import main.CHScrobbler;
import objects.Config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Setup
{
    public static void init(File configFile) throws IOException
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Hello! Thank you for using CHScrobbler. This seems like your first time using the program! I'll get you started.\n");

        String apiKey = getUserInput("What is your last.fm api key?", scanner);
        String secret = getUserInput("What is your last.fm shared secret?", scanner);
        String user = getUserInput("What is your last.fm username?", scanner);
        String pass = getUserInput("What is your last.fm password?", scanner);

        System.out.println();

        String cloneHeroDataFolder = getDefaultOrCustomFolder("Clone Hero", Utils.getDefaultCloneHeroDataFolder(), scanner);
        String scoreSpyDataFolder = "";
        boolean scoreSpyMode = false;
        if(!Utils.isMac())
        {
            scoreSpyDataFolder = getDefaultOrCustomFolder("ScoreSpy", Utils.getDefaultScoreSpyDataFolder(), scanner);
            scoreSpyMode = getYesOrNoInput("Do you want to use ScoreSpy mode? [y/n]", scanner);
            System.out.printf("ScoreSpy mode will be %s. This can be changed in your %s file.\n\n",
                scoreSpyMode ? "ENABLED" : "DISABLED", Statics.CONFIG_FILE);
        }

        else
            System.out.println("CHScrobbler detected that you are on a Mac, so ScoreSpy questions will be skipped.\n");

        scanner.close();

        CHScrobbler.getConfig()
                   .setLastFmApiKey(apiKey)
                   .setLastFmSecret(secret)
                   .setUsername(user)
                   .setPassword(pass)
                   .setCloneHeroDataFolder(cloneHeroDataFolder)
                   .setScorespyDataFolder(scoreSpyDataFolder)
                   .setScrobbleThreshold(Statics.DEFAULT_SCROBBLE_THRESHOLD)
                   .setScoreSpyMode(scoreSpyMode);

        saveConfigToFile(configFile, CHScrobbler.getConfig());

        System.out.printf("Excellent! All your details are saved in the \"%s\" file, so you can change that if you've made a mistake.\n" +
            "The program will now proceed with logging in.\n\n", Statics.CONFIG_FILE);
    }

    private static String getUserInput(String prompt, Scanner scanner)
    {
        while(true)
        {
            System.out.print(prompt + " ");
            String answer = scanner.nextLine();

            if(answer.isEmpty())
                System.out.println("Your answer cannot be blank.\n");
            else
                return answer;
        }
    }

    private static String getDefaultOrCustomFolder(String folderName, String defaultFolder, Scanner scanner)
    {
        File folder = new File(defaultFolder);
        if(!folder.exists())
        {
            System.out.println("Couldn't find your " + folderName + " data folder. Enter the directory link below or hit Enter to use the directory CHScrobbler is in.");
            String answer = scanner.nextLine();
            return answer.trim().isEmpty() ? defaultFolder : answer.replaceAll("\\\\", "/");
        }
        else
        {
            System.out.println("Automatically found your " + folderName + " folder:");
            System.out.println(folder.getPath());
            System.out.println("If this isn't correct, you can change it in your config.\n");
            return defaultFolder;
        }
    }

    private static boolean getYesOrNoInput(String prompt, Scanner scanner)
    {
        while(true)
        {
            System.out.print(prompt + " ");
            String input = scanner.nextLine().toLowerCase();
            if(input.equals("y"))
                return true;
            else if(input.equals("n"))
                return false;
            else
                System.out.println("Invalid answer. Please enter 'y' for yes or 'n' for no.");
        }
    }

    private static void saveConfigToFile(File configFile, Config config) throws
        IOException
    {
        try(FileWriter fw = new FileWriter(configFile))
        {
            fw.write(config.toString());
        }
    }
}
