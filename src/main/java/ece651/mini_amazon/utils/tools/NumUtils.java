package ece651.mini_amazon.utils.tools;


import ece651.mini_amazon.exceptions.utilsException.IntFormatException;

public class NumUtils {

    // convert string to int
    // currently does not support non-digit (e.g. '+', '-', etc)
    public static int strToInt(String str) throws IntFormatException {
        if (str.length() == 0) {
            throw new IntFormatException("ERROR: Cannot parse Empty String to Integer!");
        }
        if (str.length() > 7) {
            throw new IntFormatException("ERROR: Too long to be parsed as an Integer: " + str);
        }

        int res = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (!Character.isDigit(ch)) {
                throw new IntFormatException("ERROR: found non-digit character in " + str);
            }
            int digit = ch - '0';
            if (i == 0 && str.length() != 1 && digit == 0) { // no leading zeroes!
                throw new IntFormatException("ERROR: there are leading zero(es) in " + str);
            }
            res = res * 10 + digit;
        }

        return res;
    }
}
