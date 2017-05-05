package net.wespot.utils;

import javax.servlet.http.Cookie;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

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

    public static boolean validEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
