package me.vickychijwani.thrones.ui;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.ThronesApplication;
import me.vickychijwani.thrones.data.entity.Episode;
import me.vickychijwani.thrones.network.HboApi;
import me.vickychijwani.thrones.util.Utility;

public class EpisodesFragment extends Fragment {

    public static final String KEY_EPISODES = "episodes";

    private EpisodesAdapter mEpisodesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_episodes, container, false);
        final RecyclerView episodesRecyclerView = (RecyclerView) view.findViewById(R.id.episodes_list);

        List<Episode> episodes = getArguments().getParcelableArrayList(KEY_EPISODES);
        if (episodes == null) {
            episodes = new ArrayList<>();
        }
        View.OnClickListener episodeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = episodesRecyclerView.getChildLayoutPosition(view);
                if (pos == RecyclerView.NO_POSITION) return;
                Episode episode = mEpisodesAdapter.getItem(pos);
                Intent intent = new Intent(EpisodesFragment.this.getActivity(), SynopsisActivity.class);
                intent.putExtra(SynopsisFragment.KEY_EPISODE, episode);
                Bundle activityOptions = ActivityOptions.makeScaleUpAnimation(view, 0, 0,
                        view.getWidth(), view.getHeight()).toBundle();
                startActivity(intent, activityOptions);
            }
        };
        mEpisodesAdapter = new EpisodesAdapter(getActivity(), episodes,
                ThronesApplication.getInstance().getPicasso(), episodeClickListener);
        episodesRecyclerView.setAdapter(mEpisodesAdapter);
        episodesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    public static EpisodesFragment newInstance(List<Episode> episodes) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_EPISODES, new ArrayList<>(episodes));
        EpisodesFragment fragment = new EpisodesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    void setEpisodes(@NonNull List<Episode> episodes) {
        Bundle args = getArguments();
        args.putParcelableArrayList(KEY_EPISODES, new ArrayList<>(episodes));

        if (getView() != null && getView().getWindowToken() != null) {
            mEpisodesAdapter.setEpisodes(episodes);
            mEpisodesAdapter.notifyDataSetChanged();
        }
    }

    private static class EpisodesAdapter extends RecyclerView.Adapter<EpisodeViewHolder> {

        private final List<Episode> mEpisodes;
        private final LayoutInflater mLayoutInflater;
        private final Picasso mPicasso;
        private final Drawable mTitleScrim;
        private final View.OnClickListener mClickListener;

        EpisodesAdapter(Context context, List<Episode> episodes, Picasso picasso,
                        View.OnClickListener clickListener) {
            mEpisodes = episodes;
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mPicasso = picasso;
            mClickListener = clickListener;
            mTitleScrim = Utility.makeCubicGradientScrimDrawable(0xaa000000, 8, Gravity.BOTTOM);
            setHasStableIds(true);
        }

        void setEpisodes(@NonNull List<Episode> episodes) {
            mEpisodes.clear();
            mEpisodes.addAll(episodes);
        }

        private Episode getItem(int position) {
            return mEpisodes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).number;
        }

        @Override
        public EpisodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(R.layout.episode_list_item, parent, false);
            return new EpisodeViewHolder(view, mClickListener);
        }

        @Override
        public void onBindViewHolder(EpisodeViewHolder holder, int position) {
            Episode episode = getItem(position);
            mPicasso.load(HboApi.getImageUrl(episode.image, HboApi.ImageSize.LARGE))
                    .fit()
                    .centerCrop()
                    .into(holder.image);
            holder.titleScrim.setBackground(mTitleScrim);
            holder.title.setText(episode.number + ". " + episode.title);
        }

        @Override
        public int getItemCount() {
            return mEpisodes.size();
        }

    }

    private static class EpisodeViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final ImageView titleScrim;
        final TextView title;

        EpisodeViewHolder(View itemView, View.OnClickListener clickListener) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.episode_image);
            titleScrim = (ImageView) itemView.findViewById(R.id.episode_title_scrim);
            title = (TextView) itemView.findViewById(R.id.episode_title);
            itemView.setOnClickListener(clickListener);
        }
    }

}
