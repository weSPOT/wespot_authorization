package net.wespot.utils;

import javax.servlet.http.Cookie;

public final class Utils {
    public static boolean isEmpty(String value) {
        return value == null || "".equals(value.trim());
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
