package me.vickychijwani.thrones.network;

import java.util.List;

public interface WallpaperDataCallback {

    void onSuccess(List<String> wallpaperUrls);

    void onError();

}
