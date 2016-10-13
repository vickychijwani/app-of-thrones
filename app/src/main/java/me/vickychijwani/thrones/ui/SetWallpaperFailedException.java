package me.vickychijwani.thrones.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class SetWallpaperFailedException extends RuntimeException {

    SetWallpaperFailedException(@NonNull String wallpaper, @Nullable Throwable cause) {
        super("Failed to set wallpaper: " + wallpaper, cause);
    }

    SetWallpaperFailedException(@NonNull String wallpaper, @NonNull String message) {
        super("Failed to set wallpaper [" + wallpaper + "], message = " + message);
    }

}
