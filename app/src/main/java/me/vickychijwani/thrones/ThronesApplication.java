package me.vickychijwani.thrones;

import android.app.Application;

public class ThronesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * DO NOT INITIALIZE SINGLETONS HERE, do that in:
         * {@link me.vickychijwani.thrones.data.ThronesProvider#INIT_APP(android.content.Context)}
         */
    }

}
