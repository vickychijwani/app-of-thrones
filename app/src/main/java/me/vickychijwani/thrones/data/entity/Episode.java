package me.vickychijwani.thrones.data.entity;

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

    // TODO when you update this, remember to update the Parcelable code + other constructor as well!
    // use the Builder to construct this
    private Episode(int number, int seasonNumber, String title, String excerpt, String image,
                    String synopsis, String synopsisImage) {
        this.number = number;
        this.seasonNumber = seasonNumber;
        this.title = title;
        this.excerpt = excerpt;
        this.image = image;
        this.synopsis = synopsis;
        this.synopsisImage = synopsisImage;
    }

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

    public static final class Builder {
        private int number;
        private int seasonNumber;
        private String title;
        private String excerpt;
        private String image;
        private String synopsis;
        private String synopsisImage;

        public Builder setNumber(int number) {
            this.number = number;
            return this;
        }

        public Builder setSeasonNumber(int seasonNumber) {
            this.seasonNumber = seasonNumber;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setExcerpt(String excerpt) {
            this.excerpt = excerpt;
            return this;
        }

        public Builder setImage(String image) {
            this.image = image;
            return this;
        }

        public Builder setSynopsis(String synopsis) {
            this.synopsis = synopsis;
            return this;
        }

        public Builder setSynopsisImage(String synopsisImage) {
            this.synopsisImage = synopsisImage;
            return this;
        }

        public Episode build() {
            return new Episode(number, seasonNumber, title, excerpt, image, synopsis, synopsisImage);
        }
    }

}
