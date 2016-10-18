package me.vickychijwani.thrones.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.network.BeautifulDeathApi;
import me.vickychijwani.thrones.network.TvdbApi;
import me.vickychijwani.thrones.network.WallpaperDataCallback;
import me.vickychijwani.thrones.util.AppUtils;
import me.vickychijwani.thrones.util.CrashLedger;

public class WallpapersListFragment extends Fragment {

    public static final String KEY_SOURCE = "key:source";
    public static final String KEY_URLS = "key:urls";
    private static final String TAG = "WallpapersListFragment";

    private final ArrayList<String> mUrls = new ArrayList<>();

    public enum Source {
        BEAUTIFUL_DEATH("Beautiful Death", R.layout.wallpaper_list_item_portrait, R.dimen.desired_wallpaper_width_portrait),
        THE_TVDB_PORTRAIT("The TVDB (Portrait)", R.layout.wallpaper_list_item_portrait, R.dimen.desired_wallpaper_width_portrait),
        THE_TVDB_LANDSCAPE("The TVDB (Landscape)", R.layout.wallpaper_list_item_landscape, R.dimen.desired_wallpaper_width_landscape);

        private String str;
        @LayoutRes private int layout;
        @DimenRes private int desiredColumnWidth;
        Source(String str, @LayoutRes int layout, @DimenRes int desiredColumnWidth) {
            this.str = str;
            this.layout = layout;
            this.desiredColumnWidth = desiredColumnWidth;
        }
        @LayoutRes public int getLayout() { return this.layout; }
        @DimenRes public int getDesiredColumnWidth() { return this.desiredColumnWidth; }
        public String toString() { return this.str; }
        public static Source fromString(@NonNull String text) {
            for (Source source : Source.values()) {
                if (text.equalsIgnoreCase(source.str)) {
                    return source;
                }
            }
            return null;
        }
    }

    private WallpapersAdapter mWallpapersAdapter;

    public static WallpapersListFragment newInstance(Source source) {
        WallpapersListFragment fragment = new WallpapersListFragment();
        Bundle args = new Bundle();
        args.putString(KEY_SOURCE, source.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallpapers_list, container, false);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.wallpapers_list);

        String sourceStr = getArguments().getString(KEY_SOURCE);
        if (sourceStr == null) {
            throw new IllegalStateException("No wallpaper source found!");
        }
        CrashLedger.Log.i(TAG, "Wallpaper source = " + sourceStr);
        Source source = Source.fromString(sourceStr);
        if (source == null) {
            Log.wtf(TAG, "Source enum constructed with Source.fromString() is null! This is a bug!");
            return null;
        }

        if (savedInstanceState != null) {
            List<String> savedUrls = savedInstanceState.getStringArrayList(KEY_URLS);
            if (savedUrls != null) {
                mUrls.clear();
                mUrls.addAll(savedUrls);
            }
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = recyclerView.getChildLayoutPosition(view);
                if (pos == RecyclerView.NO_POSITION) return;
                String url = mWallpapersAdapter.getItem(pos);
                CrashLedger.navigationEvent("Wallpaper list", "View wallpaper: " + url);
                Intent intent = new Intent(WallpapersListFragment.this.getActivity(),
                        WallpaperFullscreenActivity.class);
                intent.putExtra(WallpaperFullscreenFragment.KEY_URL, url);
                Bundle activityOptions;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                            view, "wallpaper").toBundle();
                } else {
                    activityOptions = ActivityOptions.makeScaleUpAnimation(view, 0, 0,
                            view.getWidth(), view.getHeight()).toBundle();
                }
                startActivity(intent, activityOptions);
            }
        };
        mWallpapersAdapter = new WallpapersAdapter(mUrls, source.getLayout(), inflater,
                Picasso.with(getActivity()), clickListener);
        recyclerView.setAdapter(mWallpapersAdapter);

        final int desiredColumnWidth = getResources().getDimensionPixelSize(source.getDesiredColumnWidth());
        int optimalColumnCount = AppUtils.getOptimalColumnCountForDesiredColumnWidth(getActivity(),
                desiredColumnWidth);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), optimalColumnCount));

        // don't request if the fragment was just destroyed and re-created
        if (mUrls.isEmpty()) {
            MyWallpaperDataCallback dataCallback = new MyWallpaperDataCallback(new WeakReference<>(this));
            switch (source) {
                case BEAUTIFUL_DEATH:
                    BeautifulDeathApi.fetchAllPosters(dataCallback);
                    break;
                case THE_TVDB_PORTRAIT:
                    TvdbApi.getInstance().fetchAllPosters(dataCallback);
                    break;
                case THE_TVDB_LANDSCAPE:
                    TvdbApi.getInstance().fetchAllFanart(dataCallback);
                    break;
                default:
                    Log.wtf(TAG, "You forgot to handle a wallpaper source: " + source.toString());
            }
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(KEY_URLS, mUrls);
    }

    private void showWallpapers(List<String> wallpaperUrls) {
        mUrls.clear();
        mUrls.addAll(wallpaperUrls);
        mWallpapersAdapter.notifyDataSetChanged();
    }

    private void showSyncError() {
        if (getView() != null) {
            Snackbar.make(getView(), R.string.sync_error, Snackbar.LENGTH_LONG).show();
        }
    }

    private static class WallpapersAdapter extends RecyclerView.Adapter<WallpaperViewHolder> {

        private final List<String> mUrls;
        private final LayoutInflater mLayoutInflater;
        private final Picasso mPicasso;
        private final View.OnClickListener mClickListener;
        private int mLayout;

        WallpapersAdapter(List<String> urls, @LayoutRes int layout, LayoutInflater layoutInflater,
                          Picasso picasso, View.OnClickListener clickListener) {
            mUrls = urls;
            mLayout = layout;
            mLayoutInflater = layoutInflater;
            mPicasso = picasso;
            mClickListener = clickListener;
            setHasStableIds(true);
        }

        private String getItem(int position) {
            return mUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public WallpaperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(mLayout, parent, false);
            return new WallpaperViewHolder(view, mClickListener);
        }

        @Override
        public void onBindViewHolder(WallpaperViewHolder holder, int position) {
            String url = getItem(position);
            mPicasso.load(url)
                    .fit()
                    .centerCrop()
                    .into(holder.image);
        }

        @Override
        public int getItemCount() {
            return mUrls.size();
        }
    }

    private static class WallpaperViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        WallpaperViewHolder(View itemView, View.OnClickListener clickListener) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.wallpaper);
            itemView.setOnClickListener(clickListener);
        }
    }

    private static class MyWallpaperDataCallback implements WallpaperDataCallback {
        private final WeakReference<WallpapersListFragment> mFragmentRef;

        MyWallpaperDataCallback(WeakReference<WallpapersListFragment> fragmentRef) {
            this.mFragmentRef = fragmentRef;
        }

        @Override
        public void onSuccess(List<String> wallpaperUrls) {
            WallpapersListFragment fragment = mFragmentRef.get();
            if (fragment != null) {
                fragment.showWallpapers(wallpaperUrls);
            }
        }

        @Override
        public void onError() {
            WallpapersListFragment fragment = mFragmentRef.get();
            if (fragment != null) {
                fragment.showSyncError();
            }
        }
    }

}
