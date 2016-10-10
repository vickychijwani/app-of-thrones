package me.vickychijwani.thrones.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

import me.vickychijwani.thrones.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class NetworkUtils {

    public static OkHttpClient makeHttpClient() {
        return makeHttpClient(null);
    }

    public static OkHttpClient makeHttpClientForBearerAuth(@NonNull final String bearerToken) {
        return makeHttpClient(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                return chain.proceed(chain.request().newBuilder()
                        .header("Authorization", "Bearer " + bearerToken)
                        .build());
            }
        });
    }

    private static OkHttpClient makeHttpClient(@Nullable Interceptor interceptor) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (interceptor != null) {
            builder.addInterceptor(interceptor);
        }
        // add logging as LAST interceptor
        builder.addInterceptor(loggingInterceptor);
        return builder.build();
    }

}
