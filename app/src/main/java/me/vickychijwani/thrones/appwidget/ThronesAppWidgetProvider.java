package me.vickychijwani.thrones.appwidget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.data.ThronesContract.EpisodeTable;
import me.vickychijwani.thrones.data.entity.Episode;
import me.vickychijwani.thrones.network.HboApi;
import me.vickychijwani.thrones.ui.SynopsisActivity;
import me.vickychijwani.thrones.ui.SynopsisFragment;
import me.vickychijwani.thrones.util.Analytics;

import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;
import static me.vickychijwani.thrones.appwidget.ThronesAppWidgetProvider.UpdateService.EXTRA_APPWIDGET_ID;

public final class ThronesAppWidgetProvider extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Analytics.enableAppWidget();
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Analytics.disableAppWidget();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Analytics.updateAppWidget();
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, UpdateService.class);
            intent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
            context.startService(intent);
        }
    }

    public static class UpdateService extends IntentService {
        final static String EXTRA_APPWIDGET_ID = "key:appwidget_id";
        final Handler mMainHandler = new Handler(Looper.getMainLooper());

        public UpdateService() {
            super("appwidget update service");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(this);
            int incomingAppWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID,
                    INVALID_APPWIDGET_ID);
            if (incomingAppWidgetId != INVALID_APPWIDGET_ID) {
                updateOneAppWidget(appWidgetManager, incomingAppWidgetId);
            }
        }

        private void updateOneAppWidget(AppWidgetManager appWidgetManager, int appWidgetId) {
            Cursor cursor = getContentResolver().query(EpisodeTable.CONTENT_URI_LIST, null, null,
                    null, null);
            if (cursor != null) {
                if (cursor.moveToLast()) {
                    Episode episode = new Episode(cursor);
                    displayEpisodeInAppWidget(episode, appWidgetManager, appWidgetId);
                }
                cursor.close();
            }
        }

        private void displayEpisodeInAppWidget(final Episode episode, AppWidgetManager appWidgetManager,
                                               final int appWidgetId) {
            Intent intent = new Intent(this, SynopsisActivity.class);
            intent.putExtra(SynopsisFragment.KEY_EPISODE, episode);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            final RemoteViews views = new RemoteViews(getPackageName(), R.layout.appwidget);
            views.setOnClickPendingIntent(R.id.root_view, pendingIntent);
            views.setTextViewText(R.id.episode_title, episode.toString());
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Picasso.with(UpdateService.this)
                            .load(HboApi.getImageUrl(episode.image, HboApi.ImageSize.LARGE))
                            .into(views, R.id.episode_image, new int[] {appWidgetId});
                }
            });
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
