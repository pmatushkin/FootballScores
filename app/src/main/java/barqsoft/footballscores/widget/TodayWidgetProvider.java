package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;

/**
 * Created by pmatushkin on 10/4/2015.
 */
public class TodayWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//        int weatherArtResourceId = R.drawable.art_clear;
//        String description = "Clear";
//        double maxTemp = 24;
//        String formattedMaxTemperature = Utility.formatTemperature(context, maxTemp);

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_today_small;
            RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);

            // Add the data to the RemoteViews
//            views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
            views.setTextViewText(R.id.home_name, "home name");
            views.setTextViewText(R.id.away_name, "away name");

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
