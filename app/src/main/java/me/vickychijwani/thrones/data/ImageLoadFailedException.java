package me.vickychijwani.thrones.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

final class ImageLoadFailedException extends RuntimeException {

    ImageLoadFailedException(@NonNull String image, @Nullable Throwable cause) {
        super("Failed to load image: " + image, cause);
    }

}
