package net.wespot.utils;

import net.wespot.db.*;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

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
public final class DbUtils {
    static {
        ObjectifyService.register(AccessToken.class);
        ObjectifyService.register(Account.class);
        ObjectifyService.register(AccountReset.class);
        ObjectifyService.register(ApplicationRegistry.class);
        ObjectifyService.register(CodeToAccount.class);
        ObjectifyService.register(School.class);
    }

    private static class Loader<ClassType> {
        public ClassType load(Class<ClassType> classToLoad, String id) {
            if (!Utils.isEmpty(id)) {
                return ObjectifyService.ofy().load().key(Key.create(classToLoad, id)).now();
            }
            return null;
        }
    }

    public static Account getAccount(String username) {
        return new Loader<Account>().load(Account.class, username);
    }

    public static AccessToken getAccessToken(String accessToken) {
        return new Loader<AccessToken>().load(AccessToken.class, accessToken);
    }

    public static AccountReset getAccountReset(String code) {
        return new Loader<AccountReset>().load(AccountReset.class, code);
    }

    public static ApplicationRegistry getApplication(String name) {
        return new Loader<ApplicationRegistry>().load(ApplicationRegistry.class, name);
    }

    public static CodeToAccount getCodeToAccount(String code) {
        return new Loader<CodeToAccount>().load(CodeToAccount.class, code);
    }

    public static School getSchool(String name) {
        return new Loader<School>().load(School.class, name);
    }
}
