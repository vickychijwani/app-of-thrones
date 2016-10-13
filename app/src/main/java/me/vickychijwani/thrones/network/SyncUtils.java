package me.vickychijwani.thrones.network;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.data.ThronesContract;
import me.vickychijwani.thrones.util.CrashLedger;

public class SyncUtils {

    private static final String TAG = "SyncUtils";

    // frequency for periodic sync, in seconds
    private static final long SYNC_FREQUENCY = 24 * 60 * 60;

    public static void setupSyncAdapter(@NonNull Context context) {
        String accountType = context.getString(R.string.sync_account_type);
        // get a handle to the existing account, if it exists
        Account account = StubAuthenticatorService.getAccount(accountType);
        // try to create the account (no-op if it already exists)
        AccountManager accountManager = (AccountManager) context
                .getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            CrashLedger.Log.i(TAG, "Periodic sync will occur every " + SYNC_FREQUENCY + " seconds");
            ContentResolver.addPeriodicSync(account, ThronesContract.CONTENT_AUTHORITY,
                    Bundle.EMPTY, SYNC_FREQUENCY);
        }
    }

    /**
     * Helper method to trigger an immediate sync ("refresh"). Note, the {@link SyncAdapter} is
     * implemented in such a way that it may not actually sync if it thinks the data is fresh enough.
     */
    public static void syncNowIfNeeded(@NonNull Context context) {
        CrashLedger.Log.i(TAG, "Sync requested");
        String accountType = context.getString(R.string.sync_account_type);
        Bundle extras = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(StubAuthenticatorService.getAccount(accountType),
                ThronesContract.CONTENT_AUTHORITY, extras);
    }

}
