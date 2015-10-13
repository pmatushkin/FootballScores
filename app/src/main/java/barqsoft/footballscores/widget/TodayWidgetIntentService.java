package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.scoresAdapter;

/**
 * Created by pmatushkin on 10/6/2015.
 */
public class TodayWidgetIntentService extends IntentService {

    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_DAY
    };

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));

        // build a date parameter
        Date date = new Date(System.currentTimeMillis());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // we want to display a single match from today that has a goal data, which probably means the match has begun
        Cursor cursor = getContentResolver().query(
                DatabaseContract.scores_table.buildScoreWithDate(),
                SCORES_COLUMNS,
                null,
                new String[] { dateFormat.format(date) },
                DatabaseContract.scores_table.HOME_GOALS_COL + " DESC LIMIT 1");

        if (cursor == null) {
            showEmptyWidget(appWidgetManager, appWidgetIds);

            return;
        }
        if (!cursor.moveToFirst()) {
            cursor.close();
            showEmptyWidget(appWidgetManager, appWidgetIds);

            return;
        }

        // read the values from a cursor
        String homeTeam = cursor.getString(scoresAdapter.COL_HOME);
        String awayTeam = cursor.getString(scoresAdapter.COL_AWAY);
        String matchTime = cursor.getString(scoresAdapter.COL_MATCHTIME);
        int homeGoals = cursor.getInt(scoresAdapter.COL_HOME_GOALS);
        int awayGoals = cursor.getInt(scoresAdapter.COL_AWAY_GOALS);

        cursor.close();

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_today_small;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            views.setTextViewText(R.id.home_name, homeTeam);
            views.setTextViewText(R.id.away_name, awayTeam);
            views.setTextViewText(R.id.data_textview, matchTime);
            views.setTextViewText(R.id.score_textview, Utilities.getScores(homeGoals, awayGoals));
            views.setImageViewResource(R.id.home_crest, Utilities.getTeamCrestByTeamName(homeTeam));
            views.setImageViewResource(R.id.away_crest, Utilities.getTeamCrestByTeamName(awayTeam));

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private void showEmptyWidget(AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_detail_empty;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
