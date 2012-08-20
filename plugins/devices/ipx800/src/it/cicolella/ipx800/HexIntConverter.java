package it.cicolella.ipx800;

/**
 *
 * @author Enrico
 */
public class HexIntConverter {


    /**
     * Takes in input an int and returns a String representing its hex value
     * Eg: input= 42 returns "2a". Alpha characters are lower case
     * @param integer
     * @return its corresponding int
     */
    public static String convert(int integer) {
        String hex = null;
        try {
            hex = Integer.toHexString(integer);
        } catch (Exception e) {
            return null;
        }
        return hex;
    }

    /**
     * Takes in input an hex string eg: "2A" or "2a" and returns the int 42.
     * If the String in input is not a valid hex a NumberFormatException is throwed.
     * @param hex
     * @return the input value converted to int
     */
    public static int convert(String hex) {
        int intValue;
        try {
            intValue = Integer.parseInt(hex, 16);
        } catch (NumberFormatException numberFormatException) {
            return -1;
        }
        return intValue;
    }

    private HexIntConverter() {
    }
}
