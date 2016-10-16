package me.vickychijwani.thrones.util;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

import me.vickychijwani.thrones.data.entity.Episode;

public final class Analytics {

    private static FirebaseAnalytics sFirebaseAnalytics = null;

    private static final String EVENT_VIEW_SYNOPSIS = "view_synopsis";

    private static final String EVENT_VIEW_WALLPAPER = "view_wallpaper";
    private static final String EVENT_SET_WALLPAPER = "set_wallpaper";

    private static final String EVENT_ENABLE_APPWIDGET = "enable_appwidget";
    private static final String EVENT_DISABLE_APPWIDGET = "disable_appwidget";
    private static final String EVENT_UPDATE_APPWIDGET = "update_appwidget";

    public static void initialize(@NonNull Context context) {
        sFirebaseAnalytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
    }

    public static void viewSynopsis(@NonNull Episode episode) {
        Bundle params = new Bundle();
        params.putString("episode", episode.toString());
        sFirebaseAnalytics.logEvent(EVENT_VIEW_SYNOPSIS, params);
    }

    public static void viewWallpaper(@NonNull String url) {
        Bundle params = new Bundle();
        params.putString("url", url);
        sFirebaseAnalytics.logEvent(EVENT_VIEW_WALLPAPER, params);
    }

    public static void setWallpaper(@NonNull String url) {
        Bundle params = new Bundle();
        params.putString("url", url);
        sFirebaseAnalytics.logEvent(EVENT_SET_WALLPAPER, params);
    }

    public static void enableAppWidget() {
        Bundle params = new Bundle();
        sFirebaseAnalytics.logEvent(EVENT_ENABLE_APPWIDGET, params);
    }

    public static void disableAppWidget() {
        Bundle params = new Bundle();
        sFirebaseAnalytics.logEvent(EVENT_DISABLE_APPWIDGET, params);
    }

    public static void updateAppWidget() {
        Bundle params = new Bundle();
        sFirebaseAnalytics.logEvent(EVENT_UPDATE_APPWIDGET, params);
    }

}
