package com.dkroz.radio;

import java.io.IOException;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
	private static final String tag = "MyWidgetProvider";
	
	private static final String url = "http://mp3.nashe.ru/nashe-128.mp3";
	
	private static boolean isReady = false;
	private static boolean isPlaying = false;
	private static MediaPlayer mediaPlayer = null;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        
/*		String url = "http://mp3.nashe.ru/nashe-128.mp3";
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mediaPlayer.setDataSource(url);
			mediaPlayer.prepare();
			isReady = true;
		} catch (Exception e) {
			e.printStackTrace();
			isReady = false;
		}*/
		
		final int N = appWidgetIds.length;
		Log.d(tag, "we've got " + N + " widgets");
				
		for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

    		Log.d(tag, "update for widgetId=" + appWidgetId);
    		
    		// call service to do some work
    		Intent intent = new Intent(context, UpdateService.class);
    		//context.startService(intent);    		
    		//intentPlay.putExtra("play", true);
    		//intentStop.putExtra("play", false);
    		
    		PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
    		
    		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.radio_widget);
    		views.setOnClickPendingIntent(R.id.start, pendingIntent);
    		views.setOnClickPendingIntent(R.id.stop, pendingIntent);
    		
    		if (mediaPlayer.isPlaying()) {
    			Log.d(tag, "flag - play");
				views.setViewVisibility(R.id.start, View.GONE);
				views.setViewVisibility(R.id.stop, View.VISIBLE);
			} else {
				Log.d(tag, "flag - stop");
				views.setViewVisibility(R.id.start, View.VISIBLE);
				views.setViewVisibility(R.id.stop, View.GONE);
			}
    		
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
	}
	
	@Override
	public void onEnabled(Context context) {
		try {
			if (mediaPlayer==null) {
				mediaPlayer = new MediaPlayer();
			}
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource(url);
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer player) {
					player.start();
				}
			});
				
			mediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
				@Override
				public void onBufferingUpdate(MediaPlayer player, int percent) {
					Log.d(tag, "buffering: " + percent);
					//setStatus(percent+"%");
					//pushUpdate();
				}
			});
				
			mediaPlayer.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer player, int error, int extra) {
					switch (error) {
					case MediaPlayer.MEDIA_ERROR_IO:
						//setStatus("Error: IO error!");
						break;
					case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
						//setStatus("Error: Server is out!");
						// TODO must release mediaPlayer here!
						player.stop();
						break;
					case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
						//setStatus("Error: Timed out!");
						break;
					case MediaPlayer.MEDIA_ERROR_UNKNOWN:
						//setStatus("Error: Unknown error!");
						break;
					case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
						//setStatus("Error: Stream format is not supported by your device!");
						break;	
					}
					//pushUpdate();
					return false;
				}
			});
				
			mediaPlayer.setOnInfoListener(new OnInfoListener() {
				@Override
				public boolean onInfo(MediaPlayer player, int info, int extra) {
					switch (info) {
					case MediaPlayer.MEDIA_INFO_BUFFERING_START:
						//setStatus("Info: Buffering...");
						break;
					case MediaPlayer.MEDIA_INFO_BUFFERING_END:
						//setStatus("Info: Ready!");
						break;
					case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
						//setStatus("Info: Stream has meta data!");
						break;
					}
					return false;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			mediaPlayer = null;
		}
	}
	
	@Override
	public void onDisabled(Context context) {
		if (mediaPlayer!=null) {
			if (mediaPlayer.isPlaying())
				mediaPlayer.stop();
			mediaPlayer.release();
		}
	}
	
	public static class UpdateService extends Service {
		
		private RemoteViews views;// = new RemoteViews(this.getPackageName(), R.layout.radio_widget);
		
		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			Log.d(tag, "service has been started");
			try {
				// get widget view
				views = new RemoteViews(this.getPackageName(), R.layout.radio_widget);
				
				if (!mediaPlayer.isPlaying()) {
					Log.d(tag, "start playing");
					mediaPlayer.prepareAsync();
					views.setViewVisibility(R.id.start, View.GONE);
					views.setViewVisibility(R.id.stop, View.VISIBLE);
				} else {
					Log.d(tag, "stop playing");
					if (mediaPlayer.isPlaying()) {
						mediaPlayer.stop();
						//mediaPlayer.release();
					}
					views.setViewVisibility(R.id.start, View.VISIBLE);
					views.setViewVisibility(R.id.stop, View.GONE);
				}
				
				pushUpdate();
				
				/*Bundle bundle = intent.getExtras();
				if (bundle!=null) {
					Log.d(tag, "bundle is not empty");
				}
				
				if (bundle.getBoolean("play")) {
					Log.d(tag, "flag - play");
					// start to play
					mediaPlayer.prepareAsync();
					
					views.setViewVisibility(R.id.start, View.GONE);
					views.setViewVisibility(R.id.stop, View.VISIBLE);
					
				} else {
					Log.d(tag, "flag - stop");
					
					if (mediaPlayer.isPlaying()) {
						mediaPlayer.stop();
						mediaPlayer.release();
					}
					
					views.setViewVisibility(R.id.start, View.VISIBLE);
					views.setViewVisibility(R.id.stop, View.GONE);
				}
			*/
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			// get widget view
			/*views = new RemoteViews(this.getPackageName(), R.layout.radio_widget);
				
			if (mediaPlayer==null) {
				mediaPlayer = new MediaPlayer();
			} else {
				mediaPlayer.stop();
				mediaPlayer.reset();
			}

			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try {
				mediaPlayer.setDataSource(url);
					
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer player) {
						player.start();
					}
				});
					
				mediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
					@Override
					public void onBufferingUpdate(MediaPlayer player, int percent) {
						Log.d(tag, "buffering: " + percent);
						//setStatus(percent+"%");
						//pushUpdate();
					}
				});
					
				mediaPlayer.setOnErrorListener(new OnErrorListener() {
					@Override
					public boolean onError(MediaPlayer player, int error, int extra) {
						switch (error) {
						case MediaPlayer.MEDIA_ERROR_IO:
							setStatus("Error: IO error!");
							break;
						case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
							setStatus("Error: Server is out!");
							// TODO must release mediaPlayer here!
							player.stop();
							break;
						case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
							setStatus("Error: Timed out!");
							break;
						case MediaPlayer.MEDIA_ERROR_UNKNOWN:
							setStatus("Error: Unknown error!");
							break;
						case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
							setStatus("Error: Stream format is not supported by your device!");
							break;	
						}
						pushUpdate();
						return false;
					}
				});
					
				mediaPlayer.setOnInfoListener(new OnInfoListener() {
					@Override
					public boolean onInfo(MediaPlayer player, int info, int extra) {
						switch (info) {
						case MediaPlayer.MEDIA_INFO_BUFFERING_START:
							setStatus("Info: Buffering...");
							break;
						case MediaPlayer.MEDIA_INFO_BUFFERING_END:
							setStatus("Info: Ready!");
							break;
						case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
							setStatus("Info: Stream has meta data!");
							break;
						}
						return false;
					}
				});

					
				isReady = true;
			} catch (Exception e) {
				e.printStackTrace();
				isReady = false;
			}

			
			
			if (mediaPlayer!=null && isReady) {
				if (isPlaying) {
					Log.d(tag,"stopped");
					mediaPlayer.stop();
					mediaPlayer.reset();
				} else {
					Log.d(tag,"trying to start");
					try {
						mediaPlayer.prepareAsync();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			isPlaying=!isPlaying;
			*/
		
			return 0;
			
		}
		
		private void setStatus(String value) {
			views.setTextViewText(R.id.textblock, value);
			pushUpdate();
		}
		
		private void pushUpdate() {
			// push updates to the widget
			ComponentName thisWidget = new ComponentName(getApplicationContext(), MyWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this.getApplicationContext());
			manager.updateAppWidget(thisWidget, views);
		}
		
		@Override
		public IBinder onBind(Intent arg0) {
			return null;
		}
		
		/*public RemoteViews widgetUpdate(Context context) {
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.radio_widget);
			views.setTextViewText(R.id.textblock, String.valueOf(System.currentTimeMillis()));
			return views;
		}*/
	}

}
