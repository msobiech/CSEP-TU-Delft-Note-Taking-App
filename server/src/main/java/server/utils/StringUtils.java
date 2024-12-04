package server.utils;

public class StringUtils {
    private StringUtils() {} // private constructor to prevent instantiation

    /**
     * Checks if a given string is null or empty.
     * @param s the string to check
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
