package me.vickychijwani.thrones;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.util.Log;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;

import me.vickychijwani.thrones.network.HboApi;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ThronesApplication extends Application {

    private static ThronesApplication sInstance;

    private static final String IMAGE_CACHE_PATH = "images";
    private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024;     // in bytes
    private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024;    // in bytes

    protected OkHttpClient mOkHttpClient = null;
    protected Picasso mPicasso = null;
    protected HboApi mHboApi = null;

    public static ThronesApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        setupOkHttpClient();
        setupPicasso();
        setupHboApiService();
    }

    private void setupOkHttpClient() {
        if (mOkHttpClient != null) {
            return;
        }
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        File cacheDir = createCacheDir(this, IMAGE_CACHE_PATH);
        long size = calculateDiskCacheSize(cacheDir);
        Cache cache = new Cache(cacheDir, size);
        mOkHttpClient = new OkHttpClient.Builder()
                .cache(cache)
                // add your other interceptors …
                // add logging as last interceptor
                .addInterceptor(loggingInterceptor)
                .build();
    }

    protected void setupPicasso() {
        if (mPicasso != null) {
            return;
        }
        mPicasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(mOkHttpClient))
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        Log.e("Picasso", "Failed to load image: " + uri + "\n"
                                + Log.getStackTraceString(exception));
                    }
                })
                .build();
    }

    protected void setupHboApiService() {
        if (mHboApi != null) {
            return;
        }
        mHboApi = new HboApi(mOkHttpClient);
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

    public HboApi getHboApi() {
        return mHboApi;
    }

    private static long calculateDiskCacheSize(File dir) {
        long size = MIN_DISK_CACHE_SIZE;
        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long available;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                available = statFs.getBlockCountLong() * statFs.getBlockSizeLong();
            } else {
                // checked at runtime
                //noinspection deprecation
                available = statFs.getBlockCount() * statFs.getBlockSize();
            }
            // Target 2% of the total space.
            size = available / 50;
        } catch (IllegalArgumentException ignored) {
        }
        // Bound inside min/max size for disk cache.
        return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE);
    }

    private static File createCacheDir(Context context, String path) {
        File cacheDir = context.getApplicationContext().getExternalCacheDir();
        if (cacheDir == null)
            cacheDir = context.getApplicationContext().getCacheDir();
        File cache = new File(cacheDir, path);
        if (!cache.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cache.mkdirs();
        }
        return cache;
    }

}
