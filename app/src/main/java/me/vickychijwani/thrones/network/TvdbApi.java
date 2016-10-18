package me.vickychijwani.thrones.network;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import me.vickychijwani.thrones.BuildConfig;
import me.vickychijwani.thrones.network.entity.TvdbApiKey;
import me.vickychijwani.thrones.network.entity.TvdbImage;
import me.vickychijwani.thrones.network.entity.TvdbImageList;
import me.vickychijwani.thrones.network.entity.TvdbToken;
import me.vickychijwani.thrones.util.CrashLedger;
import me.vickychijwani.thrones.util.NetworkUtils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class TvdbApi {

    private static final String TAG = "TvdbApi";
    private static final String API_BASE_URL = "https://api.thetvdb.com/";
    private static final String ASSETS_BASE_URL = "http://thetvdb.com/banners/";
    private static final int GAME_OF_THRONES_SERIES_ID = 121361;

    // singleton
    private static TvdbApi sApi = null;

    private enum ImageKeyType {
        POSTER("poster"),
        FANART("fanart"),
        SEASON("season"),
        SEASONWIDE("seasonwide"),
        SERIES("series");

        private String str;
        ImageKeyType(String str) {
            this.str = str;
        }
        public String toString() {
            return str;
        }
    }

    private final String mApiKey;
    private TvdbApiService mApiService;
    private TvdbToken mToken;

    private AtomicBoolean mIsLoginOngoing = new AtomicBoolean(false);
    private Map<ImageKeyType, Boolean> mIsSyncOngoing = new HashMap<>();
    private Map<ImageKeyType, WallpaperDataCallback> mDeferredRequests = new TreeMap<>();

    public static TvdbApi getInstance() {
        if (sApi == null) {
            //noinspection ConstantConditions
            if (BuildConfig.TVDB_API_KEY == null) {
                throw new IllegalArgumentException("TVDB API key is null!");
            }
            sApi = new TvdbApi(BuildConfig.TVDB_API_KEY);
        }
        return sApi;
    }

    private TvdbApi(@NonNull String apiKey) {
        mApiKey = apiKey;
        setupApiService();
    }

    private void setupApiService() {
        Gson gson = new GsonBuilder().create();
        OkHttpClient httpClient;
        if (mToken != null) {
            httpClient = NetworkUtils.makeHttpClientForBearerAuth(mToken.getToken());
        } else {
            httpClient = NetworkUtils.makeHttpClient();
        }
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(API_BASE_URL)
                .build();
        mApiService = retrofit.create(TvdbApiService.class);
    }

    public void fetchAllPosters(@NonNull final WallpaperDataCallback dataCallback) {
        fetchAllImages(ImageKeyType.POSTER, dataCallback);
    }

    public void fetchAllFanart(@NonNull final WallpaperDataCallback dataCallback) {
        fetchAllImages(ImageKeyType.FANART, dataCallback);
    }

    private void fetchAllImages(@NonNull final ImageKeyType imageKeyType,
                               @NonNull final WallpaperDataCallback dataCallback) {
        if (!mIsSyncOngoing.containsKey(imageKeyType)) {
            mIsSyncOngoing.put(imageKeyType, false);
        }
        if (mIsSyncOngoing.get(imageKeyType)) {
            return;
        }
        mIsSyncOngoing.put(imageKeyType, true);
        Observable.just(1)
                .map(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return tryLoginIfNeeded(imageKeyType, dataCallback);
                    }
                })
                // proceed only if the request was NOT deferred
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean deferred) {
                        return !deferred;
                    }
                })
                .map(new Func1<Boolean, List<String>>() {
                    @Override
                    public List<String> call(Boolean isLoginOngoing) {
                        return fetchAllImagesSync(imageKeyType);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> urls) {
                        CrashLedger.Log.i(TAG, "TVDB request succeeded, got " + urls.size() + " URLs");
                        dataCallback.onSuccess(urls);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof WrappedSyncException) {
                            throwable = throwable.getCause();
                        }
                        CrashLedger.reportNonFatal(throwable);
                        dataCallback.onError();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mIsSyncOngoing.put(imageKeyType, false);
                    }
                });
    }

    private boolean tryLoginIfNeeded(ImageKeyType imageKeyType, WallpaperDataCallback dataCallback) {
        // if we're trying to login, wait for it (even if the token is not null, still
        // wait because we must be retrying the login for a good reason)
        if (mIsLoginOngoing.get()) {
            CrashLedger.Log.d(TAG, "Deferring request for key type = " + imageKeyType.toString());
            mDeferredRequests.put(imageKeyType, dataCallback);
            return true;
        }

        // if there's no login attempt ongoing and we have a token, we're good to go
        if (mToken != null) {
            return false;
        }

        // try to login
        mIsLoginOngoing.set(true);
        try {
            // actual login attempt
            login();
            mIsLoginOngoing.set(false);
            CrashLedger.Log.d(TAG, "Login done, firing " + mDeferredRequests.size() + " deferred requests now");
            // kick-off deferred requests (in separate threads)
            for (Map.Entry<ImageKeyType, WallpaperDataCallback> deferredRequest : mDeferredRequests.entrySet()) {
                fetchAllImages(deferredRequest.getKey(), deferredRequest.getValue());
            }
            // now move on to the current request
            return false;
        } catch (IOException e) {
            mIsLoginOngoing.set(false);
            // throw an unchecked exception, the Observable will relay it on
            throw new WrappedSyncException("Failed to login", e);
        }
    }

    @SuppressLint("DefaultLocale")
    @WorkerThread
    private List<String> fetchAllImagesSync(ImageKeyType imageKeyType)
            throws WrappedSyncException {
        try {
            List<String> urls = new ArrayList<>();
            TvdbImageList imageList = handleImagesResponse(mApiService.getImages(
                    GAME_OF_THRONES_SERIES_ID, imageKeyType.toString()));
            for (TvdbImage image : imageList.getData()) {
                urls.add(getImageUrl(image.getFileName()));
            }
            return urls;
        } catch (Throwable e) {
            // throw an unchecked exception, the Observable will relay it on
            throw new WrappedSyncException("Failed to sync data", e);
        }
    }

    @WorkerThread
    private void login() throws IOException {
        mToken = handleLoginResponse(mApiService.login(new TvdbApiKey(mApiKey)).execute());
        setupApiService();
    }

    private TvdbImageList handleImagesResponse(Call<TvdbImageList> call)
            throws IOException {
        Response<TvdbImageList> response = call.execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            // try to refresh the current token
            mApiService.refreshToken().execute();
            // retry the original request
            return handleImagesResponseDontRetry(call.clone());
        } else {
            throw new RuntimeException("Failed to fetch images from TVDB API");
        }
    }

    private TvdbImageList handleImagesResponseDontRetry(Call<TvdbImageList> call)
            throws IOException {
        Response<TvdbImageList> response = call.execute();
        if (!response.isSuccessful()) {
            throw new RuntimeException("Failed to fetch images from TVDB API");
        }
        return response.body();
    }

    private TvdbToken handleLoginResponse(Response<TvdbToken> response) {
        if (!response.isSuccessful()) {
            throw new RuntimeException("Failed to login to TVDB API");
        }
        return response.body();
    }

    private static String getImageUrl(@NonNull String image) {
        return ASSETS_BASE_URL + image;
    }

}
