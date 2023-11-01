package methods;

import main.CHScrobbler;
import objects.Game;
import objects.Config;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Setup
{
    public static Config init() throws IOException
    {
        Config config = new Config();
        Scanner scanner = new Scanner(System.in);

        CHScrobbler.getLogger().info("--------- Running setup... ---------");
        System.out.println();
        System.out.println("Hello! Thank you for using CHScrobbler. This seems like your first time using the program! I'll get you started.\n");

        Config.LastFmCredentials lastFmCredentials = new Config.LastFmCredentials();
        lastFmCredentials.setLastFmApiKey(getUserInput("What is your last.fm api key?", scanner));
        lastFmCredentials.setLastFmSecret(getUserInput("What is your last.fm shared secret?", scanner));
        lastFmCredentials.setLastfmUserName(getUserInput("What is your last.fm username?", scanner));
        lastFmCredentials.setLastFmPassword(getUserInput("What is your last.fm password?", scanner));

        Game gameMode = getGameInput(scanner);
        System.out.println();

        System.out.println("--------- Finding your data folders... ---------");
        Config.DataFolders dataFolders = getFolders(scanner);
        scanner.close();

        config.setLastFmCredentials(lastFmCredentials)
              .setDataFolders(dataFolders)
              .setGameMode(gameMode)
              .setScrobbleThreshold(String.valueOf(Statics.DEFAULT_SCROBBLE_THRESHOLD));

        Utils.writeSettings(config);

        System.out.printf("Excellent! All your details are saved in the \"%s\" file, so you can change that if you've made a mistake.\n" +
            "The program will now proceed with logging in.\n\n", Statics.CONFIG_FILE);

        return config;
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

    private static Config.DataFolders getFolders(Scanner scanner)
    {
        Config.DataFolders dataFolders = new Config.DataFolders();

        for(Game game : Game.values())
        {
            String defaultFolderPath = getDefaultFolderPath(game);

            if(game == Game.SCORESPY && Utils.isMac())
            {
                System.out.println("ScoreSpy data folder setting will be empty due to being unsupported on Mac devices.");
                dataFolders.setScoreSpyDataFolder("");
                continue;
            }

            if(new File(defaultFolderPath).exists())
            {
                Utils.setFolderPath(dataFolders, game, defaultFolderPath);
                System.out.printf("Automatically found your %s folder: %s%n", game.getGameName(), defaultFolderPath);
            }

            else
            {
                System.out.printf("Couldn't find your %s data folder. Put the directory below or hit [Enter] to use the directory CHScrobbler is in.%n", game.getGameName());
                String answer = scanner.nextLine();
                answer = answer.trim().isEmpty() ? "" : answer.replaceAll("\\\\", "/");
                Utils.setFolderPath(dataFolders, game, answer);
            }
        }

        System.out.println("If any of these folders aren't correct, you can change it in your config.\n");
        return dataFolders;
    }

    private static Game getGameInput(Scanner scanner)
    {
        while(true)
        {
            System.out.print("What game do you want to use with CHScrobbler? [1 = Clone Hero | 2 = ScoreSpy | 3 = YARG] ");
            String input = scanner.nextLine();
            switch(input)
            {
                case "1":
                    System.out.println("You've chosen Clone Hero.");
                    return Game.CLONE_HERO;
                case "2":
                    System.out.println("You've chosen ScoreSpy.");
                    return Game.SCORESPY;
                case "3":
                    System.out.println("You've chosen YARG.");
                    return Game.YARG;
                default:
                    System.out.println("Invalid answer. Please enter 1, 2, or 3.");
            }
        }
    }

    private static String getDefaultFolderPath(Game game)
    {
        switch(game)
        {
            case CLONE_HERO:
                return Utils.getDefaultCloneHeroDataFolder();
            case SCORESPY:
                return Utils.getDefaultScoreSpyDataFolder();
            case YARG:
                return Utils.getDefaultYARGFolder();
            default:
                return "";
        }
    }
}
