package me.vickychijwani.thrones.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import me.vickychijwani.thrones.BuildConfig;
import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.network.SyncAdapter;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "AppCompatActivity";

    private SyncStatusReceiver mSyncStatusReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSyncStatusReceiver = new SyncStatusReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter syncStatusFilter = new IntentFilter(SyncAdapter.INTENT_ACTION_SYNC_STATUS);
        registerReceiver(mSyncStatusReceiver, syncStatusFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mSyncStatusReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // to prevent leaking Activity
        mSyncStatusReceiver = null;
    }

    private void handleSyncStatus(String syncStatus) {
        switch (syncStatus) {
            case SyncAdapter.SYNC_STATUS_RUNNING:
                Toast.makeText(this, R.string.sync_running, Toast.LENGTH_SHORT).show();
                break;
            case SyncAdapter.SYNC_STATUS_SUCCESS:
                Toast.makeText(this, R.string.sync_success, Toast.LENGTH_SHORT).show();
                break;
            case SyncAdapter.SYNC_STATUS_FAILED_WILL_RETRY:
            case SyncAdapter.SYNC_STATUS_FAILED_WONT_RETRY:
                Toast.makeText(this, R.string.sync_error, Toast.LENGTH_LONG).show();
                break;
            case SyncAdapter.SYNC_STATUS_SKIPPED:
                // no-op
                break;
            default:
                if (BuildConfig.DEBUG) {
                    Log.wtf(TAG, "Unknown sync status received: '" + syncStatus + "'");
                }
        }
    }

    // safe to hold a reference to the Activity since it won't outlive the Activity
    private /* non-static */ class SyncStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String syncStatus = intent.getStringExtra(SyncAdapter.KEY_SYNC_STATUS);
            if (BuildConfig.DEBUG && syncStatus == null) {
                Log.wtf(TAG, "Sync status is null! How is this possible?!");
                return;
            }
            handleSyncStatus(syncStatus);
        }
    }

}
