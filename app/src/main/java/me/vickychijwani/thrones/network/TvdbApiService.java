package me.vickychijwani.thrones.network;

import me.vickychijwani.thrones.network.entity.TvdbApiKey;
import me.vickychijwani.thrones.network.entity.TvdbImageList;
import me.vickychijwani.thrones.network.entity.TvdbToken;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface TvdbApiService {

    @POST("login")
    Call<TvdbToken> login(@Body TvdbApiKey request);

    @GET("refresh_token")
    Call<TvdbToken> refreshToken();

    @GET("series/{id}/images/query")
    Call<TvdbImageList> getImages(@Path("id") int seriesId, @Query("keyType") String keyType);

}
