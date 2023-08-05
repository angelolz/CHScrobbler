package methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReadURL
{
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
}
