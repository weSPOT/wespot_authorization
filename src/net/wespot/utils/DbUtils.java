package net.wespot.utils;

import net.wespot.db.*;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

public final class DbUtils {
    static {
        ObjectifyService.register(Account.class);
        ObjectifyService.register(AccountReset.class);
        ObjectifyService.register(CodeToAccount.class);
        ObjectifyService.register(AccessToken.class);
        ObjectifyService.register(ApplicationRegistry.class);
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
