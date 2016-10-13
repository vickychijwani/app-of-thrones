package me.vickychijwani.thrones.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.picasso.Picasso;

import me.vickychijwani.thrones.ThronesApplication;
import me.vickychijwani.thrones.data.ThronesContract.EpisodeTable;
import me.vickychijwani.thrones.network.SyncUtils;
import me.vickychijwani.thrones.util.CrashLedger;


public final class ThronesProvider extends ContentProvider {

    private static final String TAG = "ThronesProvider";

    private static final int EPISODES = 100;
    private static final int EPISODE_WITH_ID = 101;

    private static UriMatcher sUriMatcher;
    private DBHelper mDbHelper;

    static {
        sUriMatcher = buildUriMatcher();
    }

    // getContext() cannot be null in onCreate(), see getContext() documentation
    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onCreate() {
        CrashLedger.lifecycleEvent(ThronesProvider.class, "onCreate");

        INIT_APP(getContext());

        mDbHelper = new DBHelper(getContext());
        return true;
    }

    private static void INIT_APP(@NonNull Context context) {
        /**
         * If you want to initialize something ONLY in the main UI process, do it here. If you want
         * it in ALL processes (sync process, Firebase crash reporter process, etc), do it in
         * {@link ThronesApplication#onCreate()}.
         */
        SyncUtils.setupSyncAdapter(context);
        Picasso.setSingletonInstance(new Picasso.Builder(context)
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        CrashLedger.reportNonFatal(new ImageLoadFailedException(uri.toString(), exception));
                    }
                })
                .build());
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        CrashLedger.Log.i(TAG, "[query ] uri: " + uri);
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case EPISODES:
                cursor = mDbHelper.getAllEpisodes();
                break;
            case EPISODE_WITH_ID:
                cursor = mDbHelper.getEpisode(ContentUris.parseId(uri));
                break;
            default:
                throw new IllegalArgumentException("[query ] this operation doesn't support uri: " + uri);
        }

        // enable notifications on the cursor when underlying data changes
        //noinspection ConstantConditions
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        CrashLedger.Log.i(TAG, "[query ] returning " + cursor.getCount() + " result(s)");
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        CrashLedger.Log.i(TAG, "[insert] uri: " + uri);
        Uri insertedUri = null;
        long insertedId;

        switch (sUriMatcher.match(uri)) {
            case EPISODES:
                insertedId = mDbHelper.insertOrUpdateEpisode(values);
                if (insertedId >= 0) {
                    insertedUri = ContentUris.withAppendedId(EpisodeTable.CONTENT_URI_LIST, insertedId);
                }
                break;
            default:
                throw new IllegalArgumentException("[insert] this operation doesn't support uri: " + uri);
        }

        if (insertedId >= 0) {
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return insertedUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        CrashLedger.Log.i(TAG, "[update] uri: " + uri);
        long updatedId;
        int numRowsUpdated;
        switch (sUriMatcher.match(uri)) {
            case EPISODE_WITH_ID:
                updatedId = ContentUris.parseId(uri);
                numRowsUpdated = mDbHelper.updateEpisode(updatedId, values);
                break;
            default:
                throw new IllegalArgumentException("[update] this operation doesn't support uri: " + uri);
        }

        if (updatedId >= 0) {
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsUpdated;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case EPISODES:
                return EpisodeTable.CONTENT_TYPE;
            case EPISODE_WITH_ID:
                return EpisodeTable.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = ThronesContract.CONTENT_AUTHORITY;

        // episodes
        matcher.addURI(authority, EpisodeTable.TABLE_NAME, EPISODES);
        matcher.addURI(authority, EpisodeTable.TABLE_NAME + "/#", EPISODE_WITH_ID);

        return matcher;
    }

}
