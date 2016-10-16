package me.vickychijwani.thrones.data.entity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import me.vickychijwani.thrones.data.ThronesContract.EpisodeTable;

public final class Episode implements Parcelable {

    public final int number;
    public final int seasonNumber;

    public final String title;
    public final String excerpt;
    public final String image;

    public final String synopsis;
    public final String synopsisImage;

    // TODO when you update this, remember to update the Parcelable code, etc as well!
    public Episode(Cursor cursor) {
        this.number = cursor.getInt(cursor.getColumnIndexOrThrow(EpisodeTable.COL_NUMBER));
        this.seasonNumber = cursor.getInt(cursor.getColumnIndexOrThrow(EpisodeTable.COL_SEASON_NUMBER));
        this.title = cursor.getString(cursor.getColumnIndexOrThrow(EpisodeTable.COL_TITLE));
        this.excerpt = cursor.getString(cursor.getColumnIndexOrThrow(EpisodeTable.COL_EXCERPT));
        this.image = cursor.getString(cursor.getColumnIndexOrThrow(EpisodeTable.COL_IMAGE));
        this.synopsis = cursor.getString(cursor.getColumnIndexOrThrow(EpisodeTable.COL_SYNOPSIS));
        this.synopsisImage = cursor.getString(cursor.getColumnIndexOrThrow(EpisodeTable.COL_SYNOPSIS_IMAGE));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.number);
        dest.writeInt(this.seasonNumber);
        dest.writeString(this.title);
        dest.writeString(this.excerpt);
        dest.writeString(this.image);
        dest.writeString(this.synopsis);
        dest.writeString(this.synopsisImage);
    }

    @SuppressWarnings("WeakerAccess")
    public Episode(Parcel in) {
        this.number = in.readInt();
        this.seasonNumber = in.readInt();
        this.title = in.readString();
        this.excerpt = in.readString();
        this.image = in.readString();
        this.synopsis = in.readString();
        this.synopsisImage = in.readString();
    }

    public static final Parcelable.Creator<Episode> CREATOR = new Parcelable.Creator<Episode>() {
        @Override
        public Episode createFromParcel(Parcel source) {
            return new Episode(source);
        }

        @Override
        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        String paddedEpisodeNum = (number >= 10) ? "" + number : ("0" + number);
        return String.format("%1$dx%2$s - %3$s", seasonNumber, paddedEpisodeNum, title);
    }

}
