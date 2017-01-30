package io.dropwizard.maxmind.geoip2;

public class Characters {

    public static String toAscii(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.replaceAll("[^\\x20-\\x7e]", "");
    }

}
