package methods;

import java.util.regex.Pattern;

public class EscapeRegex
{
    private static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

    public static String escapeRegex(String input)
    {
        return SPECIAL_REGEX_CHARS.matcher(input).replaceAll("\\\\$0");
    }
}
