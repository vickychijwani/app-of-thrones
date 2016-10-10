package me.vickychijwani.thrones.network;

import android.support.annotation.NonNull;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public final class BeautifulDeathApi {

    private static final String TAG = "BeautifulDeathApi";
    private static final String URL = "http://www.robertmball.com/Beautiful-Death";

    private static boolean mIsSyncOngoing = false;

    public static void fetchAllPosters(@NonNull final DataCallback dataCallback) {
        if (mIsSyncOngoing) {
            return;
        }
        mIsSyncOngoing = true;
        Observable.just(1)
                .map(new Func1<Integer, List<String>>() {
                    @Override
                    public List<String> call(Integer integer) {
                        return fetchAllPostersSync();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> posterUrls) {
                        mIsSyncOngoing = false;
                        dataCallback.onSuccess(posterUrls);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        try {
                            mIsSyncOngoing = false;
                            if (throwable instanceof WrappedSyncException) {
                                throwable = throwable.getCause();
                            }
                            Log.e(TAG, throwable.getMessage());
                            Log.e(TAG, Log.getStackTraceString(throwable));
                            dataCallback.onError();
                        } catch (Exception e) {
                            Log.e(TAG, "Error thrown in onError! FIX THIS!!!");
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                });
    }

    private static List<String> fetchAllPostersSync() {
        try {
            List<String> posterUrls = new ArrayList<>();
            Document doc = Jsoup.connect(URL).get();
            Elements imgs = doc.select("#project .project_content .slideshow img");
            for (Element img : imgs) {
                posterUrls.add(img.attr("data-hi-res"));
            }
            return posterUrls;
        } catch (IOException e) {
            throw new WrappedSyncException("Failed to get posters", e);
        }
    }


    public interface DataCallback {
        void onSuccess(List<String> posterUrls);
        void onError();
    }

}
