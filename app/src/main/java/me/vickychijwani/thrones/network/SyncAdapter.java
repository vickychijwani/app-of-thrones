package me.vickychijwani.thrones.network;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.ParseException;
import android.os.Bundle;
import android.support.annotation.StringDef;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import me.vickychijwani.thrones.BuildConfig;
import me.vickychijwani.thrones.pref.AppState;

// Handles data transfer between server and app, using the Android syncadapter framework.
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    // intent action must be unique to avoid conflicts with other apps' intents
    public static final String INTENT_ACTION_SYNC_STATUS = "me.vickychijwani.thrones.intent.ACTION_SYNC_STATUS";
    public static final String KEY_SYNC_STATUS = "key:SYNC_STATUS";

    @StringDef({ SYNC_STATUS_RUNNING, SYNC_STATUS_SUCCESS, SYNC_STATUS_SKIPPED,
            SYNC_STATUS_FAILED_WILL_RETRY, SYNC_STATUS_FAILED_WONT_RETRY })
    @Retention(RetentionPolicy.SOURCE)
    public @interface SyncStatus {}
    public static final String SYNC_STATUS_RUNNING = "sync_status:running";
    public static final String SYNC_STATUS_SUCCESS = "sync_status:success";
    public static final String SYNC_STATUS_SKIPPED = "sync_status:skipped";
    public static final String SYNC_STATUS_FAILED_WILL_RETRY = "sync_status:failed_will_retry";
    public static final String SYNC_STATUS_FAILED_WONT_RETRY = "sync_status:failed_wont_retry";

    private static final String TAG = "SyncAdapter";

    // in case we encounter an unknown failure, wait at least these many seconds before retrying
    private static final int SYNC_DELAY_AFTER_UNKNOWN_FAILURE = 60;

    // NEVER sync more frequently than this (in seconds)
    private static final long MIN_SYNC_INTERVAL = 60 * 60;

    private ContentResolver mContentResolver;
    private HboApi mHboApi = null;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        init(context);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        init(context);
    }

    private void init(Context context) {
        mContentResolver = context.getContentResolver();
    }

    @WorkerThread
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        AppState appState = AppState.getInstance(getContext());
        long lastSyncTime = appState.getLong(AppState.Key.LAST_SYNC_TIME);
        if (System.currentTimeMillis() < lastSyncTime + MIN_SYNC_INTERVAL*1000) {
            Log.w(TAG, "Skipping sync, last sync was at epoch time = " + lastSyncTime);
            Log.w(TAG, "Next sync cannot happen before epoch time = " + (lastSyncTime + MIN_SYNC_INTERVAL*1000));
            // set nothing on syncResult; we can consider this sync "successful" since the data is fresh
            broadcastStatus(SYNC_STATUS_SKIPPED);
            return;
        }

        if (mHboApi == null) {
            mHboApi = new HboApi(getContext());
        }

        Log.i(TAG, "SyncAdapter#onPerformSync()");
        try {
            broadcastStatus(SYNC_STATUS_RUNNING);
            mHboApi.fetchAllSeasonsSync();
            appState.setLong(AppState.Key.LAST_SYNC_TIME, System.currentTimeMillis());
            broadcastStatus(SYNC_STATUS_SUCCESS);
        } catch (WrappedSyncException wrapped) {
            Throwable e = wrapped.getCause();
            Log.e(TAG, "Sync failed", e);
            if (e instanceof IOException) {
                // soft error, will be retried
                syncResult.stats.numIoExceptions++;
                broadcastStatus(SYNC_STATUS_FAILED_WILL_RETRY);
            } else if (e instanceof JSONException || e instanceof ParseException) {
                // hard error, will NOT be retried
                syncResult.stats.numParseExceptions++;
                broadcastStatus(SYNC_STATUS_FAILED_WONT_RETRY);
            } else {
                String msg = "Unknown exception type encountered during sync! Handle this!";
                if (BuildConfig.DEBUG) {
                    Log.wtf(TAG, msg);
                } else {
                    Log.e(TAG, msg);
                }
                syncResult.delayUntil = SYNC_DELAY_AFTER_UNKNOWN_FAILURE;
                broadcastStatus(SYNC_STATUS_FAILED_WILL_RETRY);
            }
        }
    }

    private void broadcastStatus(@SyncStatus String syncStatus) {
        Log.i(TAG, "Sync status changed to '" + syncStatus + "'");
        Intent intent = new Intent();
        intent.setAction(INTENT_ACTION_SYNC_STATUS);
        intent.putExtra(KEY_SYNC_STATUS, syncStatus);
        // make sure no other app receives this broadcast
        intent.setPackage(getContext().getPackageName());
        getContext().sendBroadcast(intent);
    }

}
