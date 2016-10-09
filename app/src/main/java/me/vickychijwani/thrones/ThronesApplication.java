package me.vickychijwani.thrones;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import com.squareup.picasso.Picasso;

import me.vickychijwani.thrones.network.HboApi;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ThronesApplication extends Application {

    private static ThronesApplication sInstance;

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
        mOkHttpClient = new OkHttpClient.Builder()
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

}
