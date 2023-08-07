package methods;

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
                while ((read = reader.read(chars)) != -1)
                {
                    buffer.append(chars, 0, read);
                }

                return buffer.toString();
            }
        }

        catch(IOException e)
        {
            System.out.println("Unable to check for new updates!");
            e.printStackTrace();
        }

        //if it fails
        return null;
    }

    public static boolean isNullOrEmpty(String str) {
        if(str == null)
            return true;
        else return str.isEmpty();
    }
}
