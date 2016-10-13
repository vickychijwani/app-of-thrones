package me.vickychijwani.thrones;

import android.app.Application;

import com.google.firebase.FirebaseApp;

import me.vickychijwani.thrones.util.Analytics;

public class ThronesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * If you want to initialize something in ALL processes (sync process, Firebase crash
         * reporter process, etc), do it here. If you want it ONLY in the main UI process, do it in
         * {@link me.vickychijwani.thrones.data.ThronesProvider#INIT_APP(android.content.Context)}.
         */
        FirebaseApp.initializeApp(this);
        Analytics.initialize(this);
    }

}
