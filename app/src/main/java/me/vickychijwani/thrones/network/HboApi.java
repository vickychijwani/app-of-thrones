package me.vickychijwani.thrones.network;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashSet;
import java.util.Set;

import me.vickychijwani.thrones.ThronesApplication;
import me.vickychijwani.thrones.data.ThronesContract.EpisodeTable;
import me.vickychijwani.thrones.network.entity.HboEpisode;
import me.vickychijwani.thrones.network.entity.HboSeason;
import me.vickychijwani.thrones.network.entity.HboSeasonList;
import me.vickychijwani.thrones.network.entity.HboSynopsis;
import me.vickychijwani.thrones.network.entity.HboSynopsisId;
import me.vickychijwani.thrones.util.NetworkUtils;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public final class HboApi {

    private static final String TAG = "HboApiService";
    private static final String API_BASE_URL = "http://api.viewers-guide.hbo.com/service/";
    private static final String ASSETS_BASE_URL = "http://assets.viewers-guide.hbo.com/";

    public enum ImageSize {
        SMALL("small"),
        MEDIUM("medium"),
        LARGE("large"),
        XLARGE("xlarge");

        private String str;
        ImageSize(String str) {
            this.str = str;
        }
        public String toString() {
            return this.str;
        }
    }

    private final HboViewerGuideService mHboApi;
    private boolean mIsSyncOngoing = false;

    public HboApi() {
        OkHttpClient httpClient = NetworkUtils.makeHttpClient();
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(HboSynopsis.class, new HboSynopsis.Deserializer())
                .registerTypeAdapter(HboSynopsisId.class, new HboSynopsisId.Deserializer())
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(API_BASE_URL)
                .build();
        mHboApi = retrofit.create(HboViewerGuideService.class);
    }

    public static String getImageUrl(String image, ImageSize imageSize) {
        return ASSETS_BASE_URL + imageSize.toString() + image;
    }

    public static String sanitizeHboSynopsisHtml(@NonNull String synopsisHtml) {
        return synopsisHtml
                // strip quotes
                .replaceAll("<q( [^>]*)?>.*?</q>", "")
                .replaceAll("<h[1-6]( [^>]*)?>.*?</h[1-6]>", "")
                // strip images
                .replaceAll("<img( [^>]*)?>(</img>)?", "")
                // strip empty paragraphs etc (leftover after above operations)
                .replaceAll("<em( [^>]*)?>[ ]*</em>", "")
                .replaceAll("<p( [^>]*)?>[ ]*</p>", "");
    }

    public void fetchAllSeasons(@NonNull final SyncStatusCallback syncCallback) {
        if (mIsSyncOngoing) {
            return;
        }
        mIsSyncOngoing = true;
        Observable.just(1)
                .map(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return fetchAllSeasonsSync();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean success) {
                        mIsSyncOngoing = false;
                        if (success) {
                            syncCallback.onSuccess();
                        } else {
                            String message = "Sync failed for unknown reason";
                            Log.e(TAG, message);
                            syncCallback.onError(message);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mIsSyncOngoing = false;
                        if (throwable instanceof WrappedSyncException) {
                            throwable = throwable.getCause();
                        }
                        Log.e(TAG, Log.getStackTraceString(throwable));
                        syncCallback.onError(throwable.getMessage());
                    }
                });
    }

    @SuppressLint("DefaultLocale")
    @WorkerThread
    private boolean fetchAllSeasonsSync() throws WrappedSyncException {
        try {
            Set<Long> haveEpisodeIds = getExistingEpisodeIds();
            HboSeasonList hboSeasonList = handleSeasonsResponse(mHboApi.getSeasons().execute());

            for (HboSeason hboSeason : hboSeasonList.getSeasons()) {
                for (HboEpisode hboEpisode : hboSeason.getEpisodes()) {
                    int episodeId = hboEpisode.getId();
                    if (haveEpisodeIds.contains((long) episodeId)) {
                        // we already have this episode, skip it
                        continue;
                    }

                    int synopsisId = handleSynopsisIdResponse(mHboApi
                            .getEpisodeSynopsisId(episodeId).execute(), episodeId);

                    HboSynopsis hboSynopsis = handleSynopsisResponse(mHboApi
                            .getEpisodeSynopsis(episodeId, synopsisId).execute(), episodeId);

                    ContentValues values = new ContentValues();
                    values.put(EpisodeTable.COL_HBO_ID, episodeId);
                    values.put(EpisodeTable.COL_NUMBER, hboEpisode.getEpisodeNumber());
                    values.put(EpisodeTable.COL_SEASON_NUMBER, hboEpisode.getSeasonNumber());
                    values.put(EpisodeTable.COL_TITLE, hboEpisode.getTitle());
                    values.put(EpisodeTable.COL_EXCERPT, hboEpisode.getExcerpt());
                    values.put(EpisodeTable.COL_IMAGE, hboEpisode.getImg());
                    values.put(EpisodeTable.COL_SYNOPSIS, hboSynopsis.getText());
                    values.put(EpisodeTable.COL_SYNOPSIS_IMAGE, hboSynopsis.getImage());
                    ThronesApplication.getInstance().getContentResolver().insert(
                            EpisodeTable.CONTENT_URI_LIST, values);
                }
            }
        } catch (Throwable e) {
            // throw an unchecked exception, the Observable will relay it on
            throw new WrappedSyncException("Failed to sync data", e);
        }
        return true;
    }

    private Set<Long> getExistingEpisodeIds() {
        Context context = ThronesApplication.getInstance().getApplicationContext();
        Cursor cursor = context.getContentResolver().query(EpisodeTable.CONTENT_URI_LIST,
                new String[] {EpisodeTable.COL_HBO_ID}, null, null, null);
        Set<Long> haveEpisodeIds = new HashSet<>(cursor != null ? cursor.getCount() : 0);
        if (cursor != null) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(EpisodeTable.COL_HBO_ID));
                haveEpisodeIds.add(id);
            }
            cursor.close();
        }
        return haveEpisodeIds;
    }

    @SuppressLint("DefaultLocale")
    private HboSeasonList handleSeasonsResponse(Response<HboSeasonList> seasonsResponse)
            throws RuntimeException {
        if (!seasonsResponse.isSuccessful()) {
            throw new RuntimeException(String.format("Failed to get seasons data " +
                    "(status code %d)", seasonsResponse.code()));
        }
        return seasonsResponse.body();
    }

    @SuppressLint("DefaultLocale")
    private int handleSynopsisIdResponse(Response<HboSynopsisId> synopsisIdResponse, int episodeId)
            throws RuntimeException {
        if (!synopsisIdResponse.isSuccessful()) {
            throw new RuntimeException(String.format("Failed to get synopsisId for " +
                            "episodeId %d (status code %d)", episodeId,
                    synopsisIdResponse.code()));
        }
        return synopsisIdResponse.body().getId();
    }

    @SuppressLint("DefaultLocale")
    private HboSynopsis handleSynopsisResponse(Response<HboSynopsis> synopsisResponse, int episodeId) {
        if (!synopsisResponse.isSuccessful()) {
            throw new RuntimeException(String.format("Failed to get synopsis for " +
                            "episodeId %d (status code %d)", episodeId,
                    synopsisResponse.code()));
        }
        return synopsisResponse.body();
    }


    public interface SyncStatusCallback {
        void onSuccess();
        void onError(@Nullable String message);
    }

}
