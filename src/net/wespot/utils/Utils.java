package net.wespot.utils;

import javax.servlet.http.Cookie;
import java.util.Set;

public final class Utils {
    public static boolean isEmpty(String value) {
        return value == null || "".equals(value.trim());
    }

    public static boolean hasEmpty(Set<String> set) {
        boolean hasEmpty = false;
        for (String key : set) {
            if (!hasEmpty) hasEmpty = Utils.isEmpty(key);
        }
        return hasEmpty;
    }

    public static String getTokenFromCookies(Cookie[] cookies) {
        if (cookies == null) return null;
        String token = null;
        for (Cookie cookie : cookies) {
            if ("net.wespot.authToken".equals(cookie.getName())) {
                token = cookie.getValue();
            }
        }
        return token;
    }
}
