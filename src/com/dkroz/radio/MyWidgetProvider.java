package com.dkroz.radio;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
	private static final String tag = "MyWidgetProvider";
	
	private static boolean isPlaying = false;
	
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        
		final int N = appWidgetIds.length;
		Log.d(tag, "we've got " + N + " widgets");
		
		for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

    		Log.d(tag, "update for widgetId=" + appWidgetId);
    		
    		// call service to do some work
    		Intent intent = new Intent(context, UpdateService.class);
    		//context.startService(intent);
    		
    		PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
    		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.radio_widget);
    		views.setOnClickPendingIntent(R.id.start_stop, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
	}
	
	
	public static class UpdateService extends Service {

		@Override
		public void onStart(Intent intent, int startId) {
			Log.d(tag, "service has been started");
			RemoteViews views = widgetUpdate(this);
			ComponentName thisWidget = new ComponentName(this, MyWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
			manager.updateAppWidget(thisWidget, views);
			Log.d(tag, "widget was updated");
			
			if (isPlaying)
				Log.d(tag,"stopped");
			else
				Log.d(tag,"playing");
			
			isPlaying=!isPlaying;
			
		}
		
		@Override
		public IBinder onBind(Intent arg0) {
			return null;
		}
		
		public RemoteViews widgetUpdate(Context context) {
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.radio_widget);
			views.setTextViewText(R.id.textblock, String.valueOf(System.currentTimeMillis()));
			return views;
		}
	}
	
	// put player controls here
	public static class PlayerService extends Service {

		@Override
		public void onStart(Intent intent, int startId) {
			Log.d(tag, "player service has been started");
			RemoteViews views = playerAction(this);
			ComponentName thisWidget = new ComponentName(this, MyWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
			manager.updateAppWidget(thisWidget, views);
			Log.d(tag, "player widget was updated");
		}
				
		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}
		
		public RemoteViews playerAction(Context context) {
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.radio_widget);
			
			if (isPlaying)
				views.setTextViewText(R.id.textblock, "stopped");
			else
				views.setTextViewText(R.id.textblock, "playing");
			
			isPlaying=!isPlaying;

			return views;
		}

	}

}
