package me.vickychijwani.thrones.network;

import me.vickychijwani.thrones.network.entity.HboSynopsisId;
import me.vickychijwani.thrones.network.entity.HboSeasonList;
import me.vickychijwani.thrones.network.entity.HboSynopsis;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface HboViewerGuideService {

    @GET("seasons?lang=1")
    Call<HboSeasonList> getSeasons();

    @GET("episodeHome?lang=1")
    Call<HboSynopsisId> getEpisodeSynopsisId(@Query("episode_id") int episodeId);

    @GET("appendixDetails?lang=1")
    Call<HboSynopsis> getEpisodeSynopsis(@Query("episode_id") int episodeId,
                                         @Query("id") int synopsisId);

}
