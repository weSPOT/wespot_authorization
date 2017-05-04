package net.wespot.utils;

public final class Utils {
    public static boolean isEmpty(String value) {
        return value == null || "".equals(value.trim());
    }
}
