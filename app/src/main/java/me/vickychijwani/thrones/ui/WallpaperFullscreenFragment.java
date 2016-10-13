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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.util.Analytics;
import me.vickychijwani.thrones.util.CrashLedger;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class WallpaperFullscreenFragment extends Fragment
        implements Toolbar.OnMenuItemClickListener {

    public static final String KEY_URL = "key:url";
    private static final String TAG = "WallpaperFullFragment";

    private Target mBitmapTargetToSetWallpaper = null;
    private String mUrl;

    private Subscription mRxSubscription = null;

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
                    ActivityCompat.finishAfterTransition(getActivity());
                }
            }
        });
        toolbar.inflateMenu(R.menu.wallpaper);
        toolbar.setOnMenuItemClickListener(this);

        mUrl = getArguments().getString(KEY_URL);
        if (mUrl == null) {
            throw new IllegalArgumentException("Received null URL");
        }
        Analytics.viewWallpaper(mUrl);

        Picasso.with(getActivity())
                .load(mUrl)
                .fit()
                .centerInside()
                .into(wallpaperView);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mRxSubscription != null && !mRxSubscription.isUnsubscribed()) {
            mRxSubscription.unsubscribe();
        }
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
            CrashLedger.reportNonFatal(new SetWallpaperFailedException(mUrl, "insufficient permissions"));
            showMessage(R.string.set_wallpaper_no_permission, Snackbar.LENGTH_LONG);
            return;
        }
        if (mBitmapTargetToSetWallpaper == null) {
            // hold a strong reference to the Target to prevent GC
            // http://stackoverflow.com/questions/20181491/use-picasso-to-get-a-callback-with-a-bitmap#comment30114541_20181629
            mBitmapTargetToSetWallpaper = new BitmapTargetToSetWallpaper(wallpaperMgr);
        }
        showMessage(R.string.set_wallpaper_progress, Snackbar.LENGTH_INDEFINITE);
        Picasso.with(getActivity())
                .load(mUrl)
                .into(mBitmapTargetToSetWallpaper);
    }

    private void showMessage(@StringRes int message, int duration) {
        if (getView() != null) {
            Snackbar.make(getView(), message, duration).show();
        }
    }



    private class BitmapTargetToSetWallpaper implements Target {

        private final WallpaperManager mWallpaperManager;

        BitmapTargetToSetWallpaper(@NonNull WallpaperManager wallpaperManager) {
            mWallpaperManager = wallpaperManager;
        }

        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            if (mRxSubscription != null && !mRxSubscription.isUnsubscribed()) {
                mRxSubscription.unsubscribe();
            }
            mRxSubscription = Observable.just(1)
                    .map(new Func1<Integer, Boolean>() {
                        @Override
                        public Boolean call(Integer integer) {
                            try {
                                mWallpaperManager.setBitmap(bitmap);
                                return true;
                            } catch (Exception e) {
                                throw new SetWallpaperFailedException(mUrl, e);
                            }
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean success) {
                            Analytics.setWallpaper(mUrl);
                            showMessage(R.string.set_wallpaper_succeeded, Snackbar.LENGTH_SHORT);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            if (!(throwable instanceof SetWallpaperFailedException)) {
                                throwable = new SetWallpaperFailedException(mUrl, throwable);
                            }
                            CrashLedger.reportNonFatal(throwable);
                            showMessage(R.string.set_wallpaper_failed, Snackbar.LENGTH_LONG);
                        }
                    }, new Action0() {
                        @Override
                        public void call() {
                            mRxSubscription = null;
                        }
                    });
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            CrashLedger.reportNonFatal(new SetWallpaperFailedException(mUrl, "onBitmapFailed"));
            showMessage(R.string.set_wallpaper_failed, Snackbar.LENGTH_LONG);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}

    }

}
