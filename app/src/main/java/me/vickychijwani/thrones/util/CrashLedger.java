package me.vickychijwani.thrones.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.crash.FirebaseCrash;

/**
 * Utility methods to log events and messages to be included in crash reports.
 */
public final class CrashLedger {

    private static final int TAG_LENGTH = 10;

    private static final String TAG_LIFECYCLE = "lifecycle";
    private static final String TAG_NAVIGATION = "navigation";

    /**
     * Logging methods that log to Firebase as well as Logcat
     */
    @SuppressWarnings("unused")
    public static final class Log {
        public static void v(@NonNull String tag, @NonNull String message) {
            log(android.util.Log.VERBOSE, tag, message);
        }

        public static void d(@NonNull String tag, @NonNull String message) {
            log(android.util.Log.DEBUG, tag, message);
        }

        public static void i(@NonNull String tag, @NonNull String message) {
            log(android.util.Log.INFO, tag, message);
        }

        public static void w(@NonNull String tag, @NonNull String message) {
            log(android.util.Log.WARN, tag, message);
        }

        public static void e(@NonNull String tag, @NonNull String message) {
            log(android.util.Log.ERROR, tag, message);
        }

        private static void log(int logLevel, String tag, String message) {
            FirebaseCrash.logcat(logLevel, tag, message);
        }
    }

    /**
     * Logs a specific lifecycle event, e.g., Activity lifecycle. Included in crash reports.
     *
     * @param klass     the class whose event this is
     * @param method    the method that was triggered as part of the lifecycle
     */
    public static void lifecycleEvent(@NonNull Class klass, @NonNull String method) {
        event(TAG_LIFECYCLE, klass.getSimpleName() + " " + method + "()");
    }

    /**
     * Logs a navigation event. Included in crash reports. There's no need to log the source
     * because that was ideally logged in the previous nav event.
     *
     * @param via            the UI element that was used to perform this navigation
     * @param destination    the target end point of the navigation
     */
    public static void navigationEvent(@NonNull String via, @NonNull String destination) {
        event(TAG_NAVIGATION, via + " → " + destination);
    }

    /**
     * Reports a non-fatal error as a distinct "crash" event. If error is null, this is a no-op.
     * @param error    the non-fatal error to report
     */
    public static void reportNonFatal(@Nullable Throwable error) {
        if (error == null) {
            return;
        }
        Log.e("CrashLedgerNonFatal", ((error.getMessage() != null) ? (error.getMessage()+"\n") : "")
                + android.util.Log.getStackTraceString(error));
        FirebaseCrash.report(error);
    }

    private static void event(@NonNull String tag, @NonNull String message) {
        // tag is padded with spaces on the right
        FirebaseCrash.log(String.format("[%1$-" + TAG_LENGTH + "s] %2$s", tag.toUpperCase(), message));
    }

}
