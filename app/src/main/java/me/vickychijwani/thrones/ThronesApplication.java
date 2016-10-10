package me.vickychijwani.thrones;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import com.squareup.picasso.Picasso;

import me.vickychijwani.thrones.network.HboApi;
import me.vickychijwani.thrones.network.TvdbApi;

public class ThronesApplication extends Application {

    private static ThronesApplication sInstance;

    protected Picasso mPicasso = null;

    protected HboApi mHboApi = null;
    protected TvdbApi mTvdbApi = null;

    public static ThronesApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        setupPicasso();
        setupHboApi();
        setupTvdbApi();
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

    protected void setupHboApi() {
        if (mHboApi != null) {
            return;
        }
        mHboApi = new HboApi();
    }

    protected void setupTvdbApi() {
        if (mTvdbApi != null) {
            return;
        }
        mTvdbApi = new TvdbApi(getString(R.string.tvdb_api_key));
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

    public HboApi getHboApi() {
        return mHboApi;
    }

    public TvdbApi getTvdbApi() {
        return mTvdbApi;
    }

}
