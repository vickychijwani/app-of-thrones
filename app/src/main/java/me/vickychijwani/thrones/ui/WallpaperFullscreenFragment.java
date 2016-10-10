package me.vickychijwani.thrones.ui;

import android.Manifest;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.ThronesApplication;

public class WallpaperFullscreenFragment extends Fragment
        implements Toolbar.OnMenuItemClickListener {

    public static final String KEY_URL = "key:url";
    private static final String TAG = "WallpaperFullscreenFragment";

    private Target mBitmapTarget = null;
    private String mUrl;

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
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ImageView wallpaperView = (ImageView) view.findViewById(R.id.wallpaper);

        toolbar.setNavigationIcon(VectorDrawableCompat.create(getResources(), R.drawable.close,
                getActivity() != null ? getActivity().getTheme() : null));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });
        toolbar.inflateMenu(R.menu.wallpaper);
        toolbar.setOnMenuItemClickListener(this);

        mUrl = getArguments().getString(KEY_URL);
        if (mUrl == null) {
            Log.wtf(TAG, "This isn't supposed to happen!");
            return view;
        }
        ThronesApplication.getInstance().getPicasso()
                .load(mUrl)
                .fit()
                .centerInside()
                .into(wallpaperView);

        return view;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set_wallpaper:
                setWallpaper();
                return true;
            default:
                return false;
        }
    }

    @RequiresPermission(Manifest.permission.SET_WALLPAPER)
    private void setWallpaper() {
        final WallpaperManager wallpaperMgr = WallpaperManager.getInstance(getActivity());
        boolean missingPermission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !wallpaperMgr.isWallpaperSupported())
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !wallpaperMgr.isSetWallpaperAllowed());
        if (missingPermission) {
            showMessage(R.string.set_wallpaper_no_permission, Snackbar.LENGTH_LONG);
            return;
        }
        if (mBitmapTarget == null) {
            // hold a strong reference to the Target to prevent GC
            // http://stackoverflow.com/questions/20181491/use-picasso-to-get-a-callback-with-a-bitmap#comment30114541_20181629
            mBitmapTarget = new BitmapTarget() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    try {
                        wallpaperMgr.setBitmap(bitmap);
                        showMessage(R.string.set_wallpaper_succeeded, Snackbar.LENGTH_SHORT);
                    } catch (IOException e) {
                        showMessage(R.string.set_wallpaper_failed, Snackbar.LENGTH_LONG);
                    }
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    showMessage(R.string.set_wallpaper_failed, Snackbar.LENGTH_LONG);
                }
            };
        }
        ThronesApplication.getInstance().getPicasso()
                .load(mUrl)
                .into(mBitmapTarget);
    }

    private void showMessage(@StringRes int message, int duration) {
        if (getView() != null) {
            Snackbar.make(getView(), message, duration).show();
        }
    }

    private static abstract class BitmapTarget implements Target {
        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    }

}
