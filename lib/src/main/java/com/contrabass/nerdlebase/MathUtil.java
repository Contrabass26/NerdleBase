package com.contrabass.nerdlebase;

public class MathUtil {

    public static String truncateDecimals(String s) {
        if (!s.contains(".")) {
            return s;
        }
        if (Double.parseDouble(s) == Math.round(Double.parseDouble(s))) {
            return s.substring(0, s.indexOf("."));
        }
        return s;
    }

    public static double log(double n, double base) {
        return Math.log(n) / Math.log(base);
    }

    public static double getInformation(double probability) {
        return 0 - log(probability, 2);
    }
}
