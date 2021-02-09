package com.gianxd.musicdev;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends  AppCompatActivity  { 
	
	private Timer _timer = new Timer();
	
	private ArrayList<HashMap<String, Object>> tempMusicData = new ArrayList<>();
	
	private LinearLayout mainLayout;
	private TextView logo;
	private ProgressBar loadanim;
	
	private Intent intent = new Intent();
	private TimerTask timer;
	private SharedPreferences savedData;
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);
		com.google.firebase.FirebaseApp.initializeApp(this);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		
		mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
		logo = (TextView) findViewById(R.id.logo);
		loadanim = (ProgressBar) findViewById(R.id.loadanim);
		savedData = getSharedPreferences("savedData", Activity.MODE_PRIVATE);
	}
	
	private void initializeLogic() {
		logo.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
		loadanim.setVisibility(View.GONE);
		loadanim.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);
		if (Build.VERSION.SDK_INT >= 23) {
			getWindow().setStatusBarColor(Color.parseColor("#03A9F4"));
			getWindow().setNavigationBarColor(Color.parseColor("#03A9F4"));
		}
		else {
			getWindow().setStatusBarColor(Color.parseColor("#000000"));
			getWindow().setNavigationBarColor(Color.parseColor("#000000"));
		}
		timer = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (savedData.contains("savedProfileData")) {
							if (savedData.contains("savedMusicData")) {
								intent.setClass(getApplicationContext(), PlayerActivity.class);
								logo.setTransitionName("fade");
								
								android.app.ActivityOptions optionsCompat = android.app.ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, logo, "fade");
								        startActivity(intent, optionsCompat.toBundle());
							}
							else {
								if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
								&& ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
									_scanMedia();
								}
								else {
									final BottomSheetDialog permRequest = new BottomSheetDialog(MainActivity.this);
									View dialogLayout = getLayoutInflater().inflate(R.layout.permission_request, null);
									permRequest.setContentView(dialogLayout);
									LinearLayout main = dialogLayout.findViewById(R.id.main);
									TextView title = dialogLayout.findViewById(R.id.title);
									Button accept = dialogLayout.findViewById(R.id.accept);
									title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
									accept.setOnClickListener(new View.OnClickListener() {
											@Override
											public void onClick(View view) {
													android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#03A9F4")), null);
											        view.setBackground(rippleButton);
													ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO }, 1);
													permRequest.dismiss();
											}
									});
									Double TopLeft = 20.0;
									Double TopRight = 20.0;
									Double BottomRight = 0.0;
									Double BottomLeft = 0.0;
									android.graphics.drawable.GradientDrawable roundedCorners = new android.graphics.drawable.GradientDrawable();
									roundedCorners.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
									roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
									roundedCorners.setColor(Color.parseColor("#FFFFFF"));
									android.graphics.drawable.GradientDrawable gradientButton = new android.graphics.drawable.GradientDrawable();
									gradientButton.setColor(Color.parseColor("#03A9F4"));
									gradientButton.setCornerRadius(20);
									accept.setBackground(gradientButton);
									((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
									permRequest.setCancelable(false);
									permRequest.show();
								}
							}
						}
						else {
							final BottomSheetDialog createProfile = new BottomSheetDialog(MainActivity.this);
							View dialogLayout = getLayoutInflater().inflate(R.layout.create_a_profile, null);
							createProfile.setContentView(dialogLayout);
							LinearLayout main = dialogLayout.findViewById(R.id.main);
							TextView title = dialogLayout.findViewById(R.id.title);
							ImageView profile_icon = dialogLayout.findViewById(R.id.profile_icon);
							final EditText profile_name = dialogLayout.findViewById(R.id.profile_name);
							Button create = dialogLayout.findViewById(R.id.create);
							title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
							profile_icon.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
											MusicDevUtil.showMessage(getApplicationContext(), "Profile picture under construction.");
									}
							});
							create.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
											if (profile_name.getText().toString().length() > 0) {
													HashMap<String, Object> tempProfileData = new HashMap<>();
													String profileName = profile_name.getText().toString();
													tempProfileData.put("profileName", profileName);
													savedData.edit().putString("savedProfileData", new Gson().toJson(tempProfileData)).commit();
													createProfile.dismiss();
													intent.setClass(getApplicationContext(), MainActivity.class);
													startActivity(intent);
													finish();
											} else {
												    profile_name.setError("Profile name is empty.");
											}
									}
							});
							Double TopLeft = 20.0;
							Double TopRight = 20.0;
							Double BottomRight = 0.0;
							Double BottomLeft = 0.0;
							android.graphics.drawable.GradientDrawable roundedCorners = new android.graphics.drawable.GradientDrawable();
							roundedCorners.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
							roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
							roundedCorners.setColor(Color.parseColor("#FFFFFF"));
							((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
							android.graphics.drawable.GradientDrawable gradientButton = new android.graphics.drawable.GradientDrawable();
							gradientButton.setColor(Color.parseColor("#03A9F4"));
							gradientButton.setCornerRadius(20);
							create.setBackground(gradientButton);
							createProfile.setCancelable(false);
							createProfile.show();
						}
					}
				});
			}
		};
		_timer.schedule(timer, (int)(1500));
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
			if (requestCode == 1) {
					if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
							if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
									_scanMedia();
							} else {
								    _scanMedia();
							}
					} else {
						   _scanMedia();
					}
			}
	}
	
	{
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		
		super.onActivityResult(_requestCode, _resultCode, _data);
		
		switch (_requestCode) {
			
			default:
			break;
		}
	}
	
	public void _scanMedia () {
		(new MediaScanTask()).execute();
	}
	
	private class MediaScanTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			tempMusicData.clear();
			loadanim.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Void doInBackground(Void... path) {
			if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
			&& ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
				String[] mediaProjection = {
					            android.provider.MediaStore.Audio.Media._ID, 
								android.provider.MediaStore.Audio.Media.ARTIST,
								android.provider.MediaStore.Audio.Media.DATA,
								android.provider.MediaStore.Audio.Media.TITLE,
					            android.provider.MediaStore.Audio.Media.ALBUM_ID
				};
				
				
				String orderBy = " " + android.provider.MediaStore.MediaColumns.DISPLAY_NAME;
				android.database.Cursor cursor = getApplicationContext().getContentResolver().query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaProjection, null, null, orderBy);
				
				try {
								if (cursor.moveToFirst()) {
										long _id;
										String name;
										String data;
										String artist;
										String encodedData;
						
						                do {
												_id = cursor.getLong(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media._ID));
							                    name = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.TITLE));
												data = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.DATA));
												artist = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.ARTIST));
												{
														
													    HashMap<String, Object> songDetails = new HashMap<>();
								                        encodedData = android.util.Base64.encodeToString(data.getBytes("UTF-8"), android.util.Base64.DEFAULT);
													    songDetails.put("songTitle", name);
													    songDetails.put("songData", encodedData);
													    songDetails.put("songArtist", artist);
														songDetails.put("id", _id);
													    tempMusicData.add(songDetails);
												}		
										} while (cursor.moveToNext());
								    }
					    } catch (Exception e) {
						   e.printStackTrace();
					}
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Void... values) {
		}
		@Override
		protected void onPostExecute(Void param){
			savedData.edit().putString("savedMusicData", new Gson().toJson(tempMusicData)).commit();
			loadanim.setVisibility(View.GONE);
			timer = new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							intent.setClass(getApplicationContext(), MainActivity.class);
							startActivity(intent);
							finish();
						}
					});
				}
			};
			_timer.schedule(timer, (int)(1500));
		}
	}
	
	
	
}
