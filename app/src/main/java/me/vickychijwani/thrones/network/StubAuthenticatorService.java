package me.vickychijwani.thrones.network;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;

// A bound Service that instantiates the authenticator when started.
// source: https://developer.android.com/training/sync-adapters/creating-authenticator.html#CreateAuthenticatorService
public class StubAuthenticatorService extends Service {

    public static final String ACCOUNT_NAME = "Account";

    private StubAuthenticator mAuthenticator;

    /**
     * Obtain a handle to the {@link android.accounts.Account} used for sync in this application.
     *
     * <p>It is important that the accountType specified here matches the value in your sync adapter
     * configuration XML file for android.accounts.AccountAuthenticator (often saved in
     * res/xml/syncadapter.xml). If this is not set correctly, you'll receive an error indicating
     * that "caller uid XXXXX is different than the authenticator's uid".
     *
     * @param accountType AccountType defined in the configuration XML file for
     *                    android.accounts.AccountAuthenticator (e.g. res/xml/syncadapter.xml).
     * @return Handle to application's account (not guaranteed to resolve unless the account
     *         has been added with {@link android.accounts.AccountManager#addAccountExplicitly(Account, String, Bundle)})
     */
    public static Account getAccount(@NonNull String accountType) {
        // Note: Normally the account name is set to the user's identity (username or email
        // address). However, since we aren't actually using any user accounts, it makes more sense
        // to use a generic string in this case.
        return new Account(ACCOUNT_NAME, accountType);
    }

    @Override
    public void onCreate() {
        mAuthenticator = new StubAuthenticator(this);
    }

    // When the system binds to this Service to make the RPC call,
    // return the authenticator's IBinder
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
