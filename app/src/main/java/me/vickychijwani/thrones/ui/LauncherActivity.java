package me.vickychijwani.thrones.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.util.CrashLedger;

public class LauncherActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "LauncherActivity";

    private EpisodesFragment mEpisodesFragment = null;
    private WallpapersFragment mWallpapersFragment = null;

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episodes);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView =  (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        navView.setCheckedItem(R.id.action_episode_recaps);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (mEpisodesFragment == null) {
                CrashLedger.Log.d(TAG, "LauncherActivity#onCreate: Creating new EpisodesFragment");
                mEpisodesFragment = EpisodesFragment.newInstance();
                ft.replace(R.id.fragment_container, mEpisodesFragment);
            } else {
                CrashLedger.Log.d(TAG, "LauncherActivity#onCreate: EpisodesFragment already exists, attaching it");
                ft.attach(mEpisodesFragment);
            }
            ft.commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        Fragment newFragment;
        switch (item.getItemId()) {
            case R.id.action_episode_recaps:
                CrashLedger.navigationEvent("Nav drawer", "Episode recaps");
                if (currentFragment instanceof EpisodesFragment) {
                    return true;
                }
                if (mEpisodesFragment == null) {
                    mEpisodesFragment = EpisodesFragment.newInstance();
                }
                newFragment = mEpisodesFragment;
                break;
            case R.id.action_wallpapers:
                CrashLedger.navigationEvent("Nav drawer", "Wallpapers");
                if (currentFragment instanceof WallpapersFragment) {
                    return true;
                }
                if (mWallpapersFragment == null) {
                    mWallpapersFragment = WallpapersFragment.newInstance();
                }
                newFragment = mWallpapersFragment;
                break;
            default:
                // fall back to handling for non-navigation actions
                return handleNonNavigationAction(item);
        }
        //noinspection ConstantConditions
        if (newFragment == null) {
            Log.wtf(TAG, "You forgot to assign to newFragment!");
        }
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction()
                .detach(currentFragment);
        if (newFragment.isDetached()) {
            ft.attach(newFragment);
        } else {
            ft.add(R.id.fragment_container, newFragment);
        }
        ft.commit();
        return true;
    }

    private boolean handleNonNavigationAction(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            default:
                return false;
        }
    }

}
