package me.vickychijwani.thrones.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.ThronesApplication;
import me.vickychijwani.thrones.data.ThronesContract;
import me.vickychijwani.thrones.data.entity.Episode;
import me.vickychijwani.thrones.data.entity.Season;
import me.vickychijwani.thrones.network.HboApi;

public class EpisodesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int EPISODES_LOADER_ID = 0;

    private SeasonsAdapter mSeasonsAdapter;
    private ViewPager mViewPager;
    private boolean mDidInitLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episodes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabbar);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mSeasonsAdapter = new SeasonsAdapter(this, getSupportFragmentManager(), new ArrayList<Season>());
        mViewPager.setAdapter(mSeasonsAdapter);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(mViewPager);

        getSupportLoaderManager().initLoader(EPISODES_LOADER_ID, null, this);
        mDidInitLoader = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // courtesy http://stackoverflow.com/a/16703452/504611
        if (!mDidInitLoader) {
            getSupportLoaderManager().restartLoader(EPISODES_LOADER_ID, null, this);
        }
        mDidInitLoader = false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == EPISODES_LOADER_ID) {
            Uri uri = ThronesContract.EpisodeTable.CONTENT_URI_LIST;
            return new CursorLoader(this, uri, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == EPISODES_LOADER_ID) {
            if (cursor.getCount() == 0) {
                // start a network request chain
                SyncStatusCallback syncCallback = new SyncStatusCallback(new WeakReference<>(this));
                ThronesApplication.getInstance().getHboApi().fetchAllSeasons(syncCallback);
                return;
            }

            @SuppressLint("UseSparseArrays")
            Map<Integer, Season> seasons = new TreeMap<>();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                Episode episode = new Episode(cursor);
                int seasonNumber = episode.seasonNumber;
                if (!seasons.containsKey(seasonNumber)) {
                    seasons.put(seasonNumber, new Season(seasonNumber,
                            new ArrayList<Episode>()));
                }
                seasons.get(seasonNumber).episodes.add(episode);
            }
            // no need to close the cursor, Loader does that

            mSeasonsAdapter.setSeasons(new ArrayList<>(seasons.values()));
            mSeasonsAdapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(seasons.size()-1);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSeasonsAdapter.setSeasons(new ArrayList<Season>());
        mSeasonsAdapter.notifyDataSetChanged();
    }

    public void showSyncError() {
        Snackbar.make(findViewById(R.id.root_view), R.string.sync_error, Snackbar.LENGTH_LONG).show();
    }



    private static class SeasonsAdapter extends FragmentStatePagerAdapter {
        private final List<Season> mSeasons;
        private final Map<Integer, EpisodesFragment> mSeasonNumToFragment;
        private final String mTitleFormatString;

        SeasonsAdapter(Context context, FragmentManager fm, List<Season> seasons) {
            super(fm);
            mSeasons = seasons;
            mSeasonNumToFragment = new TreeMap<>();
            mTitleFormatString = context.getString(R.string.season_title);
        }

        void setSeasons(List<Season> seasons) {
            mSeasons.clear();
            mSeasons.addAll(seasons);
            for (Season season : seasons) {
                EpisodesFragment fragment = mSeasonNumToFragment.get(season.number);
                if (fragment != null) {
                    fragment.setEpisodes(season.episodes);
                }
            }
        }

        @Override
        public Fragment getItem(int position) {
            Season season = mSeasons.get(position);
            List<Episode> episodes = season.episodes;
            EpisodesFragment fragment = EpisodesFragment.newInstance(episodes);
            mSeasonNumToFragment.put(season.number, fragment);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.format(mTitleFormatString, mSeasons.get(position).number);
        }

        @Override
        public int getCount() {
            return mSeasons.size();
        }
    }


    private static class SyncStatusCallback implements HboApi.SyncStatusCallback {
        private final WeakReference<EpisodesActivity> mActivityRef;

        SyncStatusCallback(WeakReference<EpisodesActivity> activityRef) {
            this.mActivityRef = activityRef;
        }

        @Override
        public void onSuccess() {
            EpisodesActivity activity = mActivityRef.get();
            if (activity != null) {
                activity.getSupportLoaderManager().restartLoader(EPISODES_LOADER_ID, null, activity);
            }
        }

        @Override
        public void onError(@Nullable String message) {
            EpisodesActivity fragment = mActivityRef.get();
            if (fragment != null) {
                fragment.showSyncError();
            }
        }
    }

}
