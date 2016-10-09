package me.vickychijwani.thrones.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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

import me.vickychijwani.thrones.R;

public class WallpapersFragment extends Fragment {

    private static final String TAG = "WallpapersFragment";

    public static WallpapersFragment newInstance() {
        return new WallpapersFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallpapers, container, false);

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
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        tabLayout.setupWithViewPager(viewPager);
        if (viewPager != null && getActivity() != null) {
            List<WallpapersListFragment.Source> wallpaperSources = new ArrayList<>();
            wallpaperSources.add(WallpapersListFragment.Source.BEAUTIFUL_DEATH);
            wallpaperSources.add(WallpapersListFragment.Source.THE_TVDB);
            WallpaperCategoriesAdapter wallpaperCategoriesAdapter = new WallpaperCategoriesAdapter(
                    getChildFragmentManager(), wallpaperSources);
            viewPager.setAdapter(wallpaperCategoriesAdapter);
        } else {
            Log.wtf(TAG, "onAttach() was supposed to have been called by now, but it wasn't!");
        }

        return view;
    }



    private static class WallpaperCategoriesAdapter extends FragmentStatePagerAdapter {
        private final List<WallpapersListFragment.Source> mSources;

        WallpaperCategoriesAdapter(FragmentManager fm, List<WallpapersListFragment.Source> sources) {
            super(fm);
            mSources = sources;
        }

        @Override
        public Fragment getItem(int position) {
            WallpapersListFragment.Source source = mSources.get(position);
            return WallpapersListFragment.newInstance(source);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mSources.get(position).toString();
        }

        @Override
        public int getCount() {
            return mSources.size();
        }
    }

}
