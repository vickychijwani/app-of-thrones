package me.vickychijwani.thrones.data;

import android.net.Uri;
import android.text.TextUtils;

public final class ThronesContract {

    private static final String CONTENT_TYPE_BASE = "vnd.android.cursor.dir/vnd.me.vickychijwani.thrones.";
    private static final String CONTENT_ITEM_TYPE_BASE = "vnd.android.cursor.item/vnd.me.vickychijwani.thrones.";

    /**
     * NOTE: if you change the content authority, remember to find-and-replace it elsewhere!
     * Can't use @string resource as explained here: http://stackoverflow.com/a/6670656/504611
     */
    public static final String CONTENT_AUTHORITY = "me.vickychijwani.thrones.provider";

    private static final Uri CONTENT_URI_BASE = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static abstract class EpisodeTable {
        public static final String TABLE_NAME = "episodes";

        /** Use if multiple items get returned */
        static final String CONTENT_TYPE = CONTENT_TYPE_BASE + TABLE_NAME;

        /** Use if a single item is returned */
        static final String CONTENT_ITEM_TYPE = CONTENT_ITEM_TYPE_BASE + TABLE_NAME;

        /** Content URI for listing all episodes */
        public static final Uri CONTENT_URI_LIST = CONTENT_URI_BASE.buildUpon()
                .appendPath(EpisodeTable.TABLE_NAME)
                .build();

        // unique episode id as per HBO Viewer's Guide API
        public static final String COL_HBO_ID = "hboId";

        public static final String COL_NUMBER = "number";
        public static final String COL_SEASON_NUMBER = "seasonNumber";

        public static final String COL_TITLE = "title";
        public static final String COL_EXCERPT = "excerpt";
        public static final String COL_SYNOPSIS = "synopsis";
        public static final String COL_IMAGE = "image";
        public static final String COL_SYNOPSIS_IMAGE = "synopsisImage";

        static String createTable() {
            String[] colDefs = new String[] {
                    COL_HBO_ID + " INTEGER PRIMARY KEY",
                    COL_NUMBER + " INTEGER NOT NULL",
                    COL_SEASON_NUMBER + " INTEGER NOT NULL",
                    COL_TITLE + " TEXT NOT NULL",
                    COL_IMAGE + " TEXT NOT NULL",
                    COL_EXCERPT + " TEXT NOT NULL",
                    COL_SYNOPSIS + " TEXT",
                    COL_SYNOPSIS_IMAGE + " TEXT"
            };
            return "CREATE TABLE " + TABLE_NAME + " (" + TextUtils.join(", ", colDefs) + ")";
        }

        static String dropTable() {
            return "DROP TABLE IF EXISTS " + TABLE_NAME;
        }
    }

}
