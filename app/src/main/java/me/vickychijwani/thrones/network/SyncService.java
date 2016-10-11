package me.vickychijwani.thrones.network;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

// Define a Service that returns an IBinder for the sync adapter class, allowing the sync adapter
// framework to call onPerformSync().
// Source: https://developer.android.com/training/sync-adapters/creating-sync-adapter.html#CreateSyncAdapterService
public class SyncService extends Service {

    private static SyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(),
                        true /* autoInitialize */, false /* allowParallelSyncs */);
            }
        }
    }

    // Return an object that allows the system to invoke the sync adapter
    @Override
    public IBinder onBind(Intent intent) {
        // Get the object that allows external processes to call onPerformSync(). The object is
        // created in the base class code when the SyncAdapter constructors call super()
        return sSyncAdapter.getSyncAdapterBinder();
    }

}
