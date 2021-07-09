import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Setup
{
    public static void init(File file) throws IOException
    {
        System.out.println("Hello! Thank you for using CHScrobbler. This seems like your first time using the program! " +
            "I'll get you started.\n");

        Scanner kbd = new Scanner(System.in);

        System.out.println("First off, you would need a last.fm api key! You can get one here: https://www.last.fm/api/account/create\n" +
            "You only need to fill in the email and Application name. You can just put 'CHScrobbler' or whatever." +
            "After signing up, you should get an api key and a shared secret key. **DO NOT SHARE THIS WITH ANYONE**\n\n" +
            "What is your api key?");
        String apiKey = kbd.nextLine();

        System.out.println("What is your last.fm shared secret?");
        String secret = kbd.nextLine();
        System.out.println();

        System.out.println("What is your last.fm username?");
        String user = kbd.nextLine();
        System.out.println();

        System.out.println("What is your last.fm password?");
        String pass = kbd.nextLine();
        System.out.println();

        kbd.close();

        FileWriter fw = new FileWriter(file);
        fw.write(String.format(
            "lastfm_apikey=%s\n" +
                "lastfm_secret=%s\n" +
                "lastfm_username=%s\n" +
                "lastfm_password=%s",
            apiKey, secret, user, pass));
        fw.close();

        System.out.println("Excellent! All your details are saved in the 'config.txt' file, so you can change that if you've made a mistake.\n" +
            "The program will now proceed with logging in.\n\n");
    }
}
