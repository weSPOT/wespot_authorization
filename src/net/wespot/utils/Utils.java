package net.wespot.utils;

import javax.servlet.http.Cookie;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * ****************************************************************************
 * Copyright (C) 2013-2017 Open Universiteit Nederland
 * <p/>
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Contributors: Rafael Klaessen
 * ****************************************************************************
 */
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

    public static boolean isValidEmail(String email) {
        final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        final Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidUri(String uriString) {
        try {
            final URI uri = new URI(uriString);
            return true;
        } catch(URISyntaxException e) {
            return false;
        }
    }
}
