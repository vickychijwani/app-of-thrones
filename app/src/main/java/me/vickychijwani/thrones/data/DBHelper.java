package me.vickychijwani.thrones.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import me.vickychijwani.thrones.data.ThronesContract.EpisodeTable;


final class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "thrones.db";

    DBHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(EpisodeTable.createTable());
    }

    // WARNING!!!
    private void dropDatabaseDANGEROUS(SQLiteDatabase db) {
        db.execSQL(EpisodeTable.dropTable());
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        dropDatabaseDANGEROUS(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this database is only a cache for online data, so its downgrade policy is
        // to simply to discard the data and start over
        dropDatabaseDANGEROUS(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }


    Cursor getAllEpisodes() {
        String sql = "SELECT * FROM " + EpisodeTable.TABLE_NAME
                + " ORDER BY " + EpisodeTable.COL_SEASON_NUMBER + ", " + EpisodeTable.COL_NUMBER + " ASC";
        return getReadableDatabase().rawQuery(sql, null);
    }

    Cursor getEpisode(long episodeId) {
        String sql = "SELECT * FROM " + EpisodeTable.TABLE_NAME
                + " WHERE " + EpisodeTable._ID + " = " + episodeId;
        return getReadableDatabase().rawQuery(sql, null);
    }

    long insertOrUpdateEpisode(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(EpisodeTable.TABLE_NAME, new String[] {EpisodeTable._ID},
                EpisodeTable.COL_SEASON_NUMBER + " = ? AND " + EpisodeTable.COL_NUMBER + " = ?",
                new String[]{String.valueOf(values.getAsInteger(EpisodeTable.COL_SEASON_NUMBER)),
                        String.valueOf(values.getAsInteger(EpisodeTable.COL_NUMBER))},
                null, null, null);
        try {
            if (cursor.getCount() > 0) {
                long episodeId = cursor.getLong(cursor.getColumnIndexOrThrow(EpisodeTable._ID));
                updateEpisode(episodeId, values);
                return episodeId;
            }
        } finally {
            cursor.close();
        }
        return db.insertWithOnConflict(EpisodeTable.TABLE_NAME, null, values,
                SQLiteDatabase.CONFLICT_ABORT);
    }

    int updateEpisode(long episodeId, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        return db.updateWithOnConflict(EpisodeTable.TABLE_NAME, values,
                EpisodeTable._ID + " = ?", new String[]{String.valueOf(episodeId)},
                SQLiteDatabase.CONFLICT_ABORT);
    }

}
