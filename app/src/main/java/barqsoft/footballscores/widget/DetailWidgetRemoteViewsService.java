package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.scoresAdapter;

/**
 * Created by pmatushkin on 10/9/2015.
 */
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            private final String[] SCORES_COLUMNS = {
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

            /**
             * Called when your factory is first constructed. The same factory may be shared across
             * multiple RemoteViewAdapters depending on the intent passed.
             */
            @Override
            public void onCreate() {
                // do nothing
            }

            /**
             * See {@link Adapter#getCount()}
             *
             * @return Count of items.
             */
            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            /**
             * Called when notifyDataSetChanged() is triggered on the remote adapter. This allows a
             * RemoteViewsFactory to respond to data changes by updating any internal references.
             * <p/>
             * Note: expensive tasks can be safely performed synchronously within this method. In the
             * interim, the old data will be displayed within the widget.
             *
             * @see AppWidgetManager#notifyAppWidgetViewDataChanged(int[], int)
             */
            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                // build a date parameter
                Date date = new Date(System.currentTimeMillis());
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(
                        DatabaseContract.scores_table.buildScoreWithDate(),
                        SCORES_COLUMNS,
                        null,
                        new String[] { dateFormat.format(date) },
                        DatabaseContract.scores_table.HOME_GOALS_COL + " DESC");

                Binder.restoreCallingIdentity(identityToken);
            }

            /**
             * See {@link Adapter#getViewTypeCount()}.
             *
             * @return The number of types of Views that will be returned by this factory.
             */
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            /**
             * See {@link Adapter#getView(int, View, ViewGroup)}.
             * <p/>
             * Note: expensive tasks can be safely performed synchronously within this method, and a
             * loading view will be displayed in the interim. See {@link #getLoadingView()}.
             *
             * @param position The position of the item within the Factory's data set of the item whose
             *                 view we want.
             * @return A RemoteViews object corresponding to the data at the specified position.
             */
            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_today_small);

                // read the values from a cursor
                String homeTeam = data.getString(scoresAdapter.COL_HOME);
                String awayTeam = data.getString(scoresAdapter.COL_AWAY);
                String matchTime = data.getString(scoresAdapter.COL_MATCHTIME);
                int homeGoals = data.getInt(scoresAdapter.COL_HOME_GOALS);
                int awayGoals = data.getInt(scoresAdapter.COL_AWAY_GOALS);

                // Add the data to the RemoteViews
                views.setTextViewText(R.id.home_name, homeTeam);
                views.setTextViewText(R.id.away_name, awayTeam);
                views.setTextViewText(R.id.data_textview, matchTime);
                views.setTextViewText(R.id.score_textview, Utilies.getScores(homeGoals, awayGoals));
                views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(homeTeam));
                views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(awayTeam));

                final Intent fillInIntent = new Intent();
                views.setOnClickFillInIntent(R.id.widget, fillInIntent);

                return views;
            }

            /**
             * See {@link Adapter#getItemId(int)}.
             *
             * @param position The position of the item within the data set whose row id we want.
             * @return The id of the item at the specified position.
             */
            @Override
            public long getItemId(int position) {
                return position;
            }

            /**
             * Called when the last RemoteViewsAdapter that is associated with this factory is
             * unbound.
             */
            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            /**
             * This allows for the use of a custom loading view which appears between the time that
             * {@link #getViewAt(int)} is called and returns. If null is returned, a default loading
             * view will be used.
             *
             * @return The RemoteViews representing the desired loading view.
             */
            @Override
            public RemoteViews getLoadingView() {
                return null;
//                return new RemoteViews(getPackageName(), R.layout.widget_today_small);
            }

            /**
             * See {@link Adapter#hasStableIds()}.
             *
             * @return True if the same id always refers to the same object.
             */
            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}