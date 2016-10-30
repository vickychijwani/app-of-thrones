package me.vickychijwani.thrones.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.network.SyncAdapter;
import me.vickychijwani.thrones.util.CrashLedger;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    private SyncStatusReceiver mSyncStatusReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashLedger.lifecycleEvent(this.getClass(), "onCreate");
        mSyncStatusReceiver = new SyncStatusReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        CrashLedger.lifecycleEvent(this.getClass(), "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        CrashLedger.lifecycleEvent(this.getClass(), "onResume");
        IntentFilter syncStatusFilter = new IntentFilter(SyncAdapter.INTENT_ACTION_SYNC_STATUS);
        registerReceiver(mSyncStatusReceiver, syncStatusFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CrashLedger.lifecycleEvent(this.getClass(), "onPause");
        unregisterReceiver(mSyncStatusReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        CrashLedger.lifecycleEvent(this.getClass(), "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CrashLedger.lifecycleEvent(this.getClass(), "onDestroy");
        // to prevent leaking Activity
        mSyncStatusReceiver = null;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        CrashLedger.lifecycleEvent(this.getClass(), "onTrimMemory");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        CrashLedger.lifecycleEvent(this.getClass(), "onLowMemory");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        CrashLedger.lifecycleEvent(this.getClass(), "onBackPressed");
    }


    /* Utility methods for derived classes */
    protected void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }



    private void handleSyncStatus(@NonNull String syncStatus) {
        CrashLedger.Log.i(TAG, "BaseActivity received sync status = " + syncStatus);
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
            case SyncAdapter.SYNC_STATUS_CANCELLED:
                // no-op
                break;
            default:
                CrashLedger.reportNonFatal(new Exception("Unknown sync status received: '" + syncStatus + "'"));
        }
    }

    // safe to hold a reference to the Activity since it won't outlive the Activity
    private /* non-static */ class SyncStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String syncStatus = intent.getStringExtra(SyncAdapter.KEY_SYNC_STATUS);
            if (syncStatus == null) {
                CrashLedger.reportNonFatal(new Exception("Null sync status received"));
                return;
            }
            handleSyncStatus(syncStatus);
        }
    }

}
