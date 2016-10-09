package me.vickychijwani.thrones.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.ThronesApplication;

public class WallpaperFullscreenFragment extends Fragment {

    public static final String KEY_URL = "key:url";
    private static final String TAG = "WallpaperFullscreenFragment";

    public static WallpaperFullscreenFragment newInstance(@NonNull String url) {
        Bundle args = new Bundle();
        args.putString(KEY_URL, url);
        WallpaperFullscreenFragment fragment = new WallpaperFullscreenFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallpaper_full, container, false);
        ImageView wallpaperView = (ImageView) view.findViewById(R.id.wallpaper);

        String url = getArguments().getString(KEY_URL);
        if (url == null) {
            Log.wtf(TAG, "This isn't supposed to happen!");
            return view;
        }
        ThronesApplication.getInstance().getPicasso()
                .load(url)
                .fit()
                .centerInside()
                .into(wallpaperView);

        return view;
    }
}
