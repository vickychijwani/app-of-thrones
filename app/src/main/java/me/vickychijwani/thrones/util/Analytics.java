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

}
