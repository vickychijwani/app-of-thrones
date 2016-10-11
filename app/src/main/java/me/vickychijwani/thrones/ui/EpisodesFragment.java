package me.vickychijwani.thrones.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.data.ThronesContract;
import me.vickychijwani.thrones.data.entity.Episode;
import me.vickychijwani.thrones.data.entity.Season;
import me.vickychijwani.thrones.network.SyncUtils;

public class EpisodesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "EpisodesFragment";
    private static final int EPISODES_LOADER_ID = 0;

    private SeasonsAdapter mSeasonsAdapter;
    private ViewPager mViewPager;
    private boolean mDidInitLoader;
    private boolean mActivateLastSeason = true;

    public static EpisodesFragment newInstance() {
        return new EpisodesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_episodes, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(VectorDrawableCompat.create(getResources(), R.drawable.menu,
                getActivity() != null ? getActivity().getTheme() : null));
        final DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabbar);
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(mViewPager);
        if (mViewPager != null && getActivity() != null) {
            mSeasonsAdapter = new SeasonsAdapter(getActivity(), getChildFragmentManager(), new ArrayList<Season>());
            mViewPager.setAdapter(mSeasonsAdapter);
        } else {
            Log.wtf(TAG, "onAttach() was supposed to have been called by now, but it wasn't!");
        }

        if (!mDidInitLoader) {
            getLoaderManager().initLoader(EPISODES_LOADER_ID, null, this);
            mDidInitLoader = true;
        } else {
            getLoaderManager().restartLoader(EPISODES_LOADER_ID, null, this);
        }

        // initiate a sync
        SyncUtils.syncNowIfNeeded(getActivity());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // courtesy http://stackoverflow.com/a/16703452/504611
        if (!mDidInitLoader) {
            getLoaderManager().restartLoader(EPISODES_LOADER_ID, null, this);
        }
        mDidInitLoader = false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == EPISODES_LOADER_ID) {
            Uri uri = ThronesContract.EpisodeTable.CONTENT_URI_LIST;
            return new CursorLoader(getActivity(), uri, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == EPISODES_LOADER_ID) {
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
            if (mActivateLastSeason) {
                mViewPager.setCurrentItem(seasons.size() - 1);
                mActivateLastSeason = false;    // only do this once
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSeasonsAdapter.setSeasons(new ArrayList<Season>());
        mSeasonsAdapter.notifyDataSetChanged();
    }



    private static class SeasonsAdapter extends FragmentStatePagerAdapter {
        private final List<Season> mSeasons;
        private final Map<Integer, EpisodesInSeasonFragment> mSeasonNumToFragment;
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
                EpisodesInSeasonFragment fragment = mSeasonNumToFragment.get(season.number);
                if (fragment != null) {
                    fragment.setEpisodes(season.episodes);
                }
            }
        }

        @Override
        public Fragment getItem(int position) {
            Season season = mSeasons.get(position);
            List<Episode> episodes = season.episodes;
            EpisodesInSeasonFragment fragment = EpisodesInSeasonFragment.newInstance(episodes);
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

}
