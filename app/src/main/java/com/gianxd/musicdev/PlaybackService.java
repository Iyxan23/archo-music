package com.gianxd.musicdev;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class PlaybackService extends Service {
	
	public MediaPlayer mp;

	private final IBinder musicBind = new MusicBinder();

	private ArrayList<HashMap<String, Object>> musicData = new ArrayList<>();

	private static final int NOTIFY_ID = 1;

	private NotificationChannel notificationChannel;
	private NotificationManager notificationManager;

	private AudioManager audioManager;
	private AudioManager.OnAudioFocusChangeListener audioChangeListener;

	private SharedPreferences savedData;
	
	public void onCreate(){
		super.onCreate();
		initArrayList();
	}
	
	public class MusicBinder extends Binder {
		PlaybackService getService() { 
			return PlaybackService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return musicBind;
	}
	
	@Override
	public boolean onUnbind(Intent intent){
		return false;
	}
	
	@Override
	public void onTaskRemoved(Intent intent) {
		if (mp != null) {
			if (isPlaying()) {
				pause();
			}
			mp.reset();
			mp.release();
		}

		if (notificationManager != null) {
			notificationManager.cancelAll();
		}

		stopSelf();
	}
	
	public void createLocalStream(final double _position){
       if (mp != null) {
		   audioManager.abandonAudioFocus(audioChangeListener);

	       if (isPlaying()) {
		       PlayerActivity.playPause.performClick();
	       }

	       mp.reset();
	       mp.release();
       }

	   String decodedData = "";

       savedData.edit().putString("savedSongPosition", String.valueOf((long)(_position))).apply();

	    if (!musicData
				.get((int)_position)
				.get("songData")
				.toString()
				.startsWith("/")
		) {
			try {
				decodedData = new String(android.util.Base64.decode(musicData.get((int)_position).get("songData").toString(), android.util.Base64.DEFAULT), "UTF-8");

			} catch (Exception ignored) { /* do nothing if it crashes. :cheems: */ }

			mp = MediaPlayer.create(getApplicationContext(), Uri.fromFile(new java.io.File(decodedData)));

		} else {
		    mp = MediaPlayer.create(getApplicationContext(), Uri.fromFile(new java.io.File(musicData.get((int)_position).get("songData").toString())));
		}

		audioManager = ((AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE));

		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
	            try {
		            savedData.edit().putString("savedSongPosition", String.valueOf(_position + 1)).apply();

		            if (_position < musicData.size()) {
					    createLocalStream(Double.parseDouble(savedData.getString("savedSongPosition", "")));
			            PlayerActivity.playPause.performClick();
		            }

	            } catch (Exception e) {
		            savedData.edit().putString("savedSongPosition", String.valueOf(_position + 1)).apply();

		            if (_position < musicData.size()) {
			            createLocalStream(Double.parseDouble(savedData.getString("savedSongPosition", "")));
			            PlayerActivity.playPause.performClick();
		            }
	            }
			}
		});

		audioChangeListener = new AudioManager.OnAudioFocusChangeListener() {
			@Override
			public void onAudioFocusChange(int focusChange) {
				if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
					PlayerActivity.playPause.performClick();
				}
			}
		};

		audioManager.requestAudioFocus(audioChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		try {
	   		MediaMetadataRetriever artRetriever = new MediaMetadataRetriever();
	   		if (!musicData.get((int)_position).get("songData").toString().startsWith("/")) {
		   		artRetriever.setDataSource(decodedData);
	   		} else {
			   artRetriever.setDataSource(musicData.get((int)_position).get("songData").toString());
		   	}

		   	byte[] album_art = artRetriever.getEmbeddedPicture();

		   	if(album_art != null){
			   	Bitmap bitmapArt = BitmapFactory.decodeByteArray(album_art, 0, album_art.length);

			   	Glide.with(getApplicationContext()).load(bitmapArt).asBitmap().into(PlayerActivity.albumArt);
			   	Glide.with(getApplicationContext()).load(bitmapArt).asBitmap().into(PlayerActivity.miniplayerAlbumArt);
		   	} else {

			   	Glide.with(getApplicationContext()).load(R.drawable.album_art).asBitmap().into(PlayerActivity.albumArt);
		   		Glide.with(getApplicationContext()).load(R.drawable.album_art).asBitmap().into(PlayerActivity.miniplayerAlbumArt);
	   		}
		} catch (Exception ignored) { /* do nothing as it says: error loading image*/ }

       	PlayerActivity.songTitle.setText(musicData.get((int)_position).get("songTitle").toString());
       	PlayerActivity.songArtist.setText(musicData.get((int)_position).get("songArtist").toString());

       	PlayerActivity.miniplayerSongTitle.setText(musicData.get((int)_position).get("songTitle").toString());
       	PlayerActivity.miniplayerSongArtist.setText(musicData.get((int)_position).get("songArtist").toString());

       	PlayerActivity.maxDuration.setText(String.valueOf((long)((getMaxDuration() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((getMaxDuration() / 1000) % 60))));

       	PlayerActivity.currentDuration.setText(String.valueOf((long)((getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((getCurrentPosition() / 1000) % 60))));

       	PlayerActivity.seekbarDuration.setMax((int)getMaxDuration());
       	PlayerActivity.seekbarDuration.setProgress((int)getCurrentPosition());

	   	Intent notIntent = new Intent(this, PlayerActivity.class);
	   	notIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

	   	PendingIntent pendInt = PendingIntent.getActivity(
	   			this,
				0,
				notIntent,
				PendingIntent.FLAG_UPDATE_CURRENT
		);

	   	if (Build.VERSION.SDK_INT < 28 ) {
	  		Notification notification = new Notification.Builder(getApplicationContext())
				.setContentTitle(musicData.get((int)_position).get("songTitle").toString())
				.setContentText("by ".concat(musicData.get((int)_position).get("songArtist").toString()))
				.setSmallIcon(R.drawable.app_icon)
				.setContentIntent(pendInt)
				.build();

		   	startForeground(NOTIFY_ID, notification);
	   	} else {
		   	NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "ArchoMusic");

		   	notificationChannel = new NotificationChannel("ArchoMusic", "Music Notification", NotificationManager.IMPORTANCE_LOW);
		   	notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

           	notificationManager.createNotificationChannel(notificationChannel);
		   	notificationBuilder.setNumber(0);

	       	if (!musicData
					.get((int)_position)
					.get("songData")
					.toString()
					.startsWith("/")
			) {
		   		Notification notification = notificationBuilder.setOngoing(true)
					.setContentIntent(pendInt)
					.setSmallIcon(R.drawable.app_icon)
					.setLargeIcon(getAlbumArt(decodedData))
					.setContentText("by ".concat(musicData.get((int)_position).get("songArtist").toString()))
                    .setContentTitle(musicData.get((int)_position).get("songTitle").toString())
                    .setPriority(NotificationManager.IMPORTANCE_LOW)
					.setNumber(0)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();

			   startForeground(NOTIFY_ID, notification);

		   	} else {
		       	Notification notification = notificationBuilder.setOngoing(true)
					.setContentIntent(pendInt)
					.setSmallIcon(R.drawable.app_icon)
					.setLargeIcon(getAlbumArt(musicData.get((int)_position).get("songData").toString()))
					.setContentText("by ".concat(musicData.get((int)_position).get("songArtist").toString()))
                    .setContentTitle(musicData.get((int)_position).get("songTitle").toString())
                    .setPriority(NotificationManager.IMPORTANCE_LOW)
					.setNumber(0)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();

			   startForeground(NOTIFY_ID, notification);
	       }
	   }
		
	}
	
	public void initArrayList() {
		savedData = getSharedPreferences("savedData", Context.MODE_PRIVATE);
		musicData = new Gson().fromJson(savedData.getString("savedMusicData", ""), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
	}
	
	public int getCurrentPosition(){
		return mp.getCurrentPosition();
	}
	
	public int getMaxDuration(){
		return mp.getDuration();
	}
	
	public Bitmap getAlbumArt(String path) {
		Bitmap bitmapArt;
		MediaMetadataRetriever artRetriever = new MediaMetadataRetriever(); 
        artRetriever.setDataSource(path);

        byte[] album_art = artRetriever.getEmbeddedPicture();

        if (album_art != null) {
            bitmapArt = BitmapFactory.decodeByteArray(album_art, 0, album_art.length);
        } else {
			bitmapArt = ((BitmapDrawable)getResources().getDrawable(R.drawable.album_art)).getBitmap();
	    }

		return bitmapArt;
	}
	
	public boolean isPlaying(){
		return mp.isPlaying();
	}
	
	public void pause(){
		mp.pause();
	}
	
	public void seek(int position){
		mp.seekTo(position);
	}
	
	public void play(){
		mp.start();
	}
	
	@Override
	public void onDestroy() {
		stopForeground(true);
	}
}
