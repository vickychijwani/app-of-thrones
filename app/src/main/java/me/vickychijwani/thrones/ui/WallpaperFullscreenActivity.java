package me.vickychijwani.thrones.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.vickychijwani.thrones.R;

public class WallpaperFullscreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_full);

        WallpaperFullscreenFragment fragment = (WallpaperFullscreenFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            String url = getIntent().getStringExtra(WallpaperFullscreenFragment.KEY_URL);
            fragment = WallpaperFullscreenFragment.newInstance(url);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

}
