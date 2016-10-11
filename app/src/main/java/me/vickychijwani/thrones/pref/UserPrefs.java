package me.vickychijwani.thrones.pref;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Utility class to persist user preferences. Do NOT use this for persisting application state, that
 * is managed separately by {@link AppState}.
 */
public class UserPrefs extends Prefs<UserPrefs.Key> {

    private static final String PREFS_FILE_NAME = "user_prefs";
    private static UserPrefs sInstance = null;

    // keys
    public static class Key extends BaseKey {

        /* Example:
         * public static final Key SOME_KEY = new Key("key_string", ValueType.class, "default value");
         */

        /* package */ <T> Key(String str, Class<T> type, T defaultValue) {
            super(str, type, defaultValue);
        }

    }

    private UserPrefs(@NonNull Context context) {
        super(context.getApplicationContext(), PREFS_FILE_NAME);
    }

    public static UserPrefs getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new UserPrefs(context);
        }

        return sInstance;
    }

}
