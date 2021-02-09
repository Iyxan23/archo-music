package com.gianxd.musicdev;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.gianxd.musicdev.PlaybackService.MusicBinder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends  AppCompatActivity  { 
	
	private Timer _timer = new Timer();
	
	private ArrayList<HashMap<String, Object>> musicData = new ArrayList<>();
	
	private LinearLayout up;
	private LinearLayout miniplayer;
	private TextView logoName;
	private TabLayout tabNavigation;
	private SwipeRefreshLayout listRefresh;
	private LinearLayout player;
	private ListView songList;
	private ImageView miniplayerSkipPrev;
	private ImageView miniplayerPlayPause;
	private ImageView miniplayerSkipNext;

	public static ImageView albumArt;
	public static TextView songTitle;
	public static TextView songArtist;
	public static TextView currentDuration;
	public static SeekBar seekbarDuration;
	public static TextView maxDuration;
	public static ImageView skipBackward;
	public static ImageView playPause;
	public static ImageView skipForward;
 	public static ImageView miniplayerAlbumArt;
 	public static TextView miniplayerSongTitle;
 	public static TextView miniplayerSongArtist;
	
	private SharedPreferences savedData;
	private TimerTask timer;
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.player);
		initialize();
		com.google.firebase.FirebaseApp.initializeApp(this);
		initializeLogic();
	}
	
	private void initialize() {
		
		up = (LinearLayout) findViewById(R.id.up);
		miniplayer = (LinearLayout) findViewById(R.id.miniplayer);
		logoName = (TextView) findViewById(R.id.logoName);
		tabNavigation = (TabLayout) findViewById(R.id.tabNavigation);
		listRefresh = (SwipeRefreshLayout) findViewById(R.id.listRefresh);
		player = (LinearLayout) findViewById(R.id.player);
		songList = (ListView) findViewById(R.id.songList);
		albumArt = (ImageView) findViewById(R.id.albumArt);
		songTitle = (TextView) findViewById(R.id.songTitle);
		songArtist = (TextView) findViewById(R.id.songArtist);
		currentDuration = (TextView) findViewById(R.id.currentDuration);
		seekbarDuration = (SeekBar) findViewById(R.id.seekbarDuration);
		maxDuration = (TextView) findViewById(R.id.maxDuration);
		skipBackward = (ImageView) findViewById(R.id.skipBackward);
		playPause = (ImageView) findViewById(R.id.playPause);
		skipForward = (ImageView) findViewById(R.id.skipForward);
		miniplayerSkipPrev = (ImageView) findViewById(R.id.miniplayerSkipPrev);
		miniplayerPlayPause = (ImageView) findViewById(R.id.miniplayerPlayPause);
		miniplayerSkipNext = (ImageView) findViewById(R.id.miniplayerSkipNext);
		miniplayerAlbumArt = (ImageView) findViewById(R.id.miniplayerAlbumArt);
		miniplayerSongTitle = (TextView) findViewById(R.id.miniplayerSongTitle);
		miniplayerSongArtist = (TextView) findViewById(R.id.miniplayerSongArtist);
		savedData = getSharedPreferences("savedData", Activity.MODE_PRIVATE);
		
		miniplayer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				miniplayer.setBackground(new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null));
				tabNavigation.getTabAt(1).select();
			}
		});
		
		tabNavigation.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				final int _position = tab.getPosition();

				if (Build.VERSION.SDK_INT >= 23) {
					tab.getIcon().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
				} else {
					tab.getIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
				}

				final ObjectAnimator fadeAnim = new ObjectAnimator();

				if (_position == 0) {
					if (fadeAnim.isRunning()) {
						fadeAnim.cancel();
					}

					savedData.edit().putString("savedNavigationID", "0").commit();

					player.setVisibility(View.VISIBLE);
					listRefresh.setVisibility(View.GONE);
					miniplayer.setVisibility(View.GONE);

					fadeAnim.setTarget(player);
					fadeAnim.setPropertyName("alpha");
					fadeAnim.setFloatValues((float)(1.0d), (float)(0.0d));
					fadeAnim.start();

					timer = new TimerTask() {
						@Override
						public void run() {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									player.setVisibility(View.GONE);
									listRefresh.setVisibility(View.VISIBLE);
									miniplayer.setVisibility(View.VISIBLE);

									fadeAnim.setTarget(listRefresh);
									fadeAnim.setPropertyName("alpha");
									fadeAnim.setFloatValues((float)(0.0d), (float)(1.0d));
									fadeAnim.start();
								}
							});
						}
					};
					_timer.schedule(timer, (int)(250));

				} else {
					if (_position == 1) {
						if (fadeAnim.isRunning()) {
							fadeAnim.cancel();
						}

						savedData.edit().putString("savedNavigationID", "1").commit();

						player.setVisibility(View.GONE);
						listRefresh.setVisibility(View.VISIBLE);
						miniplayer.setVisibility(View.VISIBLE);

						fadeAnim.setTarget(listRefresh);
						fadeAnim.setPropertyName("alpha");
						fadeAnim.setFloatValues((float)(1.0d), (float)(0.0d));
						fadeAnim.start();

						timer = new TimerTask() {
							@Override
							public void run() {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										player.setVisibility(View.VISIBLE);
										listRefresh.setVisibility(View.GONE);
										miniplayer.setVisibility(View.GONE);
										fadeAnim.setTarget(player);
										fadeAnim.setPropertyName("alpha");
										fadeAnim.setFloatValues((float)(0.0d), (float)(1.0d));
										fadeAnim.start();
									}
								});
							}
						};

						_timer.schedule(timer, (int)(250));
					}
					else {
						if (_position == 2) {
							tabNavigation.getTabAt(1).select();
							listRefresh.setVisibility(View.GONE);
							player.setVisibility(View.VISIBLE);
							miniplayer.setVisibility(View.GONE);

							BottomSheetDialog menu = new BottomSheetDialog(PlayerActivity.this);
							View dialogLayout = getLayoutInflater().inflate(R.layout.menu, null);

							menu.setContentView(dialogLayout);

							TextView title = dialogLayout.findViewById(R.id.title);
							ImageView about = dialogLayout.findViewById(R.id.about);
							LinearLayout profile = dialogLayout.findViewById(R.id.profile);
							TextView profile_name = dialogLayout.findViewById(R.id.profile_name);
							LinearLayout liveStreaming = dialogLayout.findViewById(R.id.liveStreaming);
							LinearLayout lyrics = dialogLayout.findViewById(R.id.lyrics);
							LinearLayout settings = dialogLayout.findViewById(R.id.settings);
							LinearLayout help = dialogLayout.findViewById(R.id.help);

							title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);

							if (savedData.contains("savedProfileData")) {
									HashMap<String, Object> profileData = new Gson().fromJson(
											savedData.getString("savedProfileData", ""),
											new TypeToken<HashMap<String, Object>>(){}.getType()
									);

									if (profileData.containsKey("profileName")) {
										profile_name.setText(profileData.get("profileName").toString());
									}
							}

							profile.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
											android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
									        view.setBackground(rippleButton);

											final BottomSheetDialog renameProfile = new BottomSheetDialog(PlayerActivity.this);
									        View dialogLayout = getLayoutInflater().inflate(R.layout.create_a_profile, null);
									        renameProfile.setContentView(dialogLayout);

									        LinearLayout main = dialogLayout.findViewById(R.id.main);
									        TextView title = dialogLayout.findViewById(R.id.title);
									        ImageView profile_icon = dialogLayout.findViewById(R.id.profile_icon);
									        final EditText profile_name = dialogLayout.findViewById(R.id.profile_name);
									        Button create = dialogLayout.findViewById(R.id.create);

									        title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
											title.setText("Rename profile");
											create.setText("Finish");

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

															savedData.edit().putString("savedProfileData", new Gson().toJson(tempProfileData)).apply();

															renameProfile.dismiss();
														} else {
															profile_name.setError("Profile name is empty.");
														}
													}
										        });

									        double TopLeft = 20.0;
									        double TopRight = 20.0;
									        double BottomRight = 0.0;
									        double BottomLeft = 0.0;

									        android.graphics.drawable.GradientDrawable roundedCorners = new android.graphics.drawable.GradientDrawable();
									        roundedCorners.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
											roundedCorners.setCornerRadii(new float[] {
													(float) TopLeft,
													(float) TopLeft,
													(float) TopRight,
													(float) TopRight,
													(float) BottomRight,
													(float) BottomRight,
													(float) BottomLeft,
													(float) BottomLeft
											});
									        roundedCorners.setColor(Color.parseColor("#FFFFFF"));

									        ((ViewGroup) dialogLayout.getParent()).setBackground(roundedCorners);

									        android.graphics.drawable.GradientDrawable gradientButton = new android.graphics.drawable.GradientDrawable();
									        gradientButton.setColor(Color.parseColor("#03A9F4"));
									        gradientButton.setCornerRadius(20);

									        create.setBackground(gradientButton);

									        renameProfile.show();
									}
							});

							liveStreaming.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
										view.setBackground(rippleButton);
										MusicDevUtil.showMessage(getApplicationContext(), "Feature under construction.");
									}
							});

							lyrics.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
										view.setBackground(rippleButton);
										MusicDevUtil.showMessage(getApplicationContext(), "Feature under construction.");
									}
							});

							settings.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
										view.setBackground(rippleButton);
										MusicDevUtil.showMessage(getApplicationContext(), "Feature under construction.");
									}
							});

							help.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
										view.setBackground(rippleButton);
										MusicDevUtil.showMessage(getApplicationContext(), "Feature under construction.");
									}
							});

							about.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
										view.setBackground(rippleButton);
										MusicDevUtil.showMessage(getApplicationContext(), "Feature under construction.");
									}
							});

							double TopLeft = 20.0;
							double TopRight = 20.0;
							double BottomRight = 0.0;
							double BottomLeft = 0.0;

							android.graphics.drawable.GradientDrawable roundedCorners = new android.graphics.drawable.GradientDrawable();
							roundedCorners.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
							roundedCorners.setCornerRadii(new float[] {
									(float) TopLeft,
									(float) TopLeft,
									(float) TopRight,
									(float) TopRight,
									(float) BottomRight,
									(float) BottomRight,
									(float) BottomLeft,
									(float) BottomLeft
							});
							roundedCorners.setColor(Color.parseColor("#FFFFFF"));

							((ViewGroup) dialogLayout.getParent()).setBackground(roundedCorners);

							menu.show();
						}
					}
				}
			}
			
			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
				tab.getIcon().setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.SRC_IN);
			}
			
			@Override
			public void onTabReselected(TabLayout.Tab tab) { }
		});
		
		listRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override 
			public void onRefresh() {
				if (savedData.contains("savedMusicData")) {

					musicData.clear();
					musicData = new Gson().fromJson(savedData.getString("savedMusicData", ""), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());

					if (musicData.isEmpty()) {
						HashMap<String, Object> _item = new HashMap<>();
						_item.put("isEmpty", "yes");
						musicData.add(_item);
					}

					songList.setAdapter(new SongListAdapter(musicData));
					((BaseAdapter) songList.getAdapter()).notifyDataSetChanged();

				} else {
					MusicDevUtil.showMessage(getApplicationContext(), "Library data failed to load. ");
					HashMap<String, Object> _item = new HashMap<>();
					_item.put("isEmpty", "yes");
					musicData.add(_item);
				}

				listRefresh.setRefreshing(false);
			}
		});
		
		songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> _param1, View _param2, int _param3, long _param4) {
				if (!
						(_param3 == Double.parseDouble(
										savedData.getString("savedSongPosition", "")
								)
						)
				) {

					try {
						playbackSrv.createLocalStream(_param3);
						playPause.performClick();

					} catch (Exception e) {
						MusicDevUtil.showMessage(getApplicationContext(), "Failed to play selected song. Skipping");
						try {
							savedData.edit().putString("savedSongPosition", String.valueOf((long)(Double.parseDouble(savedData.getString("savedSongPosition", "")) + 1))).apply();

							if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
								playbackSrv.createLocalStream(Double.parseDouble(savedData.getString("savedSongPosition", "")));

								playPause.performClick();
							}

						} catch (Exception e2) {
							savedData.edit().putString("savedSongPosition", String.valueOf((long)(Double.parseDouble(savedData.getString("savedSongPosition", "")) + 1))).apply();

							if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
								playbackSrv.createLocalStream(Double.parseDouble(savedData.getString("savedSongPosition", "")));

								playPause.performClick();
							}
						}
					}
				}
			}
		});
		
		seekbarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override public void onProgressChanged (SeekBar _param1, int _param2, boolean _param3) { }
			@Override public void onStartTrackingTouch(SeekBar _param1) { }
			
			@Override
			public void onStopTrackingTouch(SeekBar _param2) {
				if (playbackSrv.mp != null) {
					playbackSrv.seek(seekbarDuration.getProgress());
				}
			}
		});
		
		skipBackward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (playbackSrv != null) {
					try {
						savedData.edit().putString("savedSongPosition", String.valueOf((long) (Double.parseDouble(savedData.getString("savedSongPosition", "")) - 1))).apply();
						if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
							playbackSrv.createLocalStream(Double.parseDouble(savedData.getString("savedSongPosition", "")));

							playPause.performClick();
						}

					} catch (Exception e) {
						savedData.edit().putString("savedSongPosition", String.valueOf((long) (Double.parseDouble(savedData.getString("savedSongPosition", "")) - 1))).apply();
						if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
							playbackSrv.createLocalStream(Double.parseDouble(savedData.getString("savedSongPosition", "")));

							playPause.performClick();
						}
					}
				}
			}
		});
		
		playPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (playbackSrv.mp != null) {
					if (!playbackSrv.isPlaying()) {
						playbackSrv.play();
						playPause.setImageResource(R.drawable.pause);
						miniplayerPlayPause.setImageResource(R.drawable.pause);

						timer = new TimerTask() {
							@Override
							public void run() {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										try {
											seekbarDuration.setProgress((int)playbackSrv.getCurrentPosition());
											currentDuration.setText(String.valueOf((long)((playbackSrv.getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getCurrentPosition() / 1000) % 60))));
											savedData.edit().putString("savedSongCurrentPosition", String.valueOf((long)(playbackSrv.getCurrentPosition()))).commit();
										} catch (Exception e) { /* do nothing (bruh) */ }
									}
								});
							}
						};

						_timer.scheduleAtFixedRate(timer, (int)(0), (int)(1000));

					} else {
						playbackSrv.pause();
						playPause.setImageResource(R.drawable.play);
						miniplayerPlayPause.setImageResource(R.drawable.play);

						if (timer != null) {
							timer.cancel();
						}
					}
				}
			}
		});
		
		skipForward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (playbackSrv.mp != null) {
					try {
						savedData.edit().putString("savedSongPosition", String.valueOf((long)(Double.parseDouble(savedData.getString("savedSongPosition", "")) + 1))).apply();

						if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
							playbackSrv.createLocalStream(Double.parseDouble(savedData.getString("savedSongPosition", "")));

							playPause.performClick();
						}

					} catch (Exception e) {
						savedData.edit().putString("savedSongPosition", String.valueOf((long)(Double.parseDouble(savedData.getString("savedSongPosition", "")) + 1))).apply();

						if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
							playbackSrv.createLocalStream(Double.parseDouble(savedData.getString("savedSongPosition", "")));

							playPause.performClick();
						}
					}
				}
			}
		});
		
		miniplayerSkipPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				skipBackward.performClick();
			}
		});
		
		miniplayerPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				playPause.performClick();
			}
		});
		
		miniplayerSkipNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				skipForward.performClick();
			}
		});
	}
	
	private void initializeLogic() {
		_startupUI();
		if (savedData.contains("savedMusicData")) {
			musicData.clear();
			musicData = new Gson().fromJson(
					savedData.getString("savedMusicData", ""),
					new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType()
			);

			if (musicData.isEmpty()) {
				HashMap<String, Object> _item = new HashMap<>();
				_item.put("isEmpty", "yes");
				musicData.add(_item);
			}

		} else {
			MusicDevUtil.showMessage(getApplicationContext(), "Library data failed to load. ");

			HashMap<String, Object> _item = new HashMap<>();
			_item.put("isEmpty", "yes");
			musicData.add(_item);
		}

		songList.setAdapter(new SongListAdapter(musicData));
		((BaseAdapter) songList.getAdapter()).notifyDataSetChanged();

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		if (playIntent == null) {
			playIntent = new Intent(this, PlaybackService.class);

			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		} else {
			if (playbackSrv != null) {
				playIntent = new Intent(this, PlaybackService.class);

				unbindService(musicConnection);
				stopService(playIntent);

				// restart service
				bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
				startService(playIntent);
			}
		}
	}

	private PlaybackService playbackSrv;
	private Intent playIntent;
	private boolean musicBound = false;
	private ServiceConnection musicConnection = new ServiceConnection(){
		@Override
	  	public void onServiceConnected(ComponentName name, IBinder service) {
			MusicBinder binder = (MusicBinder)service;
			playbackSrv = binder.getService();
			musicBound = true;
			try {
				if (savedData.contains("savedSongPosition")) {

					if (!savedData.getString("savedSongPosition", "").equals("0")) {
						playbackSrv.createLocalStream(Double.parseDouble(savedData.getString("savedSongPosition", "0")));

						if (savedData.contains("savedSongCurrentPosition")) {

							playbackSrv.seek(((int)Double.parseDouble(savedData.getString("savedSongCurrentPosition", "0"))));
							maxDuration.setText(String.valueOf((long)((playbackSrv.getMaxDuration() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getMaxDuration() / 1000) % 60))));
							currentDuration.setText(String.valueOf((long)((playbackSrv.getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getCurrentPosition() / 1000) % 60))));

							seekbarDuration.setMax((int)playbackSrv.getMaxDuration());
							seekbarDuration.setProgress((int)playbackSrv.getCurrentPosition());
						}
					}

				} else {
					if (!musicData.isEmpty()) {
						savedData.edit().putString("savedSongPosition", "0").commit();
						if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
						   playbackSrv.createLocalStream(Double.parseDouble(savedData.getString("savedSongPosition", "")));

						}
					}
				}
			} catch (Exception e) {
				if (!musicData.isEmpty()) {
					savedData.edit().putString("savedSongPosition", "0").commit();
					if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
					   	playbackSrv.createLocalStream(Double.parseDouble(savedData.getString("savedSongPosition", "")));
					}
				}
			}
		}
		 
	 	@Override
		public void onServiceDisconnected(ComponentName name) {
			    musicBound = false;
			  }
	};

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	
	
	@Override
	public void onResume() {
		super.onResume();

		if (savedData.contains("savedNavigationID")) {

			if (savedData.getString("savedNavigationID", "").equals("0")) {
				tabNavigation.getTabAt(0).select();

				listRefresh.setVisibility(View.VISIBLE);
				miniplayer.setVisibility(View.VISIBLE);
				player.setVisibility(View.GONE);
			} else {
				if (savedData.getString("savedNavigationID", "").equals("1")) {
					tabNavigation.getTabAt(1).select();

					listRefresh.setVisibility(View.GONE);
					player.setVisibility(View.VISIBLE);
					miniplayer.setVisibility(View.GONE);
				}
			}
		} else {
			savedData.edit().putString("savedNavigationID", "0").commit();
			tabNavigation.getTabAt(0).select();

			listRefresh.setVisibility(View.VISIBLE);
			player.setVisibility(View.GONE);
			miniplayer.setVisibility(View.VISIBLE);
		}
	}
	@SuppressLint("ClickableViewAccessibility")
	public void _startupUI () {
		logoName.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
		songTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
		miniplayerSongTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);

		skipBackward.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		playPause.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		skipForward.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		miniplayerSkipPrev.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		miniplayerPlayPause.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		miniplayerSkipNext.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));

		tabNavigation.addTab(tabNavigation.newTab().setIcon(R.drawable.tabnav_library));
		tabNavigation.addTab(tabNavigation.newTab().setIcon(R.drawable.tabnav_nowplaying));
		tabNavigation.addTab(tabNavigation.newTab().setIcon(R.drawable.avatar_default));

		player.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View p1, MotionEvent p2){
				double f = 0;
				double t;

				switch(p2.getAction()) {
					case MotionEvent.ACTION_DOWN:
						break;
					case MotionEvent.ACTION_UP:
						t = p2.getX();

						if (((f - t) < -250)) {
							tabNavigation.getTabAt(0).select();
						}

						break;
				}

				return true;
			}
		});

		listRefresh.setColorSchemeColors(Color.parseColor("#03A9F4"), Color.parseColor("#03A9F4"), Color.parseColor("#03A9F4"));
		miniplayer.setElevation(10f);

		if (savedData.contains("savedNavigationID")) {
			if (savedData.getString("savedNavigationID", "").equals("0")) {
				tabNavigation.getTabAt(0).select();

				listRefresh.setVisibility(View.VISIBLE);
				miniplayer.setVisibility(View.VISIBLE);
				player.setVisibility(View.GONE);

			} else {
				if (savedData.getString("savedNavigationID", "").equals("1")) {
					tabNavigation.getTabAt(1).select();

					listRefresh.setVisibility(View.GONE);
					player.setVisibility(View.VISIBLE);
					miniplayer.setVisibility(View.GONE);
				}
			}
		} else {
			savedData.edit().putString("savedNavigationID", "0").apply();

			tabNavigation.getTabAt(0).select();

			listRefresh.setVisibility(View.VISIBLE);
			player.setVisibility(View.GONE);
			miniplayer.setVisibility(View.VISIBLE);
		}

		if (Build.VERSION.SDK_INT >= 23) {
			up.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

			getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
			getWindow().setNavigationBarColor(Color.parseColor("#FFFFFF"));
		} else {
			getWindow().setStatusBarColor(Color.parseColor("#000000"));
			getWindow().setNavigationBarColor(Color.parseColor("#000000"));
		}
	}
	
	public class SongListAdapter extends BaseAdapter {
		ArrayList<HashMap<String, Object>> _data;
		public SongListAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public int getCount() {
			return _data.size();
		}
		
		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}
		
		@Override
		public long getItemId(int _index) {
			return _index;
		}

		@Override
		public View getView(final int _position, View _v, ViewGroup _container) {
			LayoutInflater _inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View _view = _v;
			if (_view == null) {
				_view = _inflater.inflate(R.layout.songlist, null);
			}
			
			final LinearLayout main = (LinearLayout) _view.findViewById(R.id.main);
			final TextView emptyMsg = (TextView) _view.findViewById(R.id.emptyMsg);
			final ImageView albumArt = (ImageView) _view.findViewById(R.id.albumArt);
			final TextView songTitle = (TextView) _view.findViewById(R.id.songTitle);
			final TextView songArtist = (TextView) _view.findViewById(R.id.songArtist);
			
			if (!_data.get((int)_position).containsKey("isEmpty")) {
				songTitle.setText(_data.get((int)_position).get("songTitle").toString());
				songArtist.setText(_data.get((int)_position).get("songArtist").toString());

				main.setVisibility(View.VISIBLE);
				emptyMsg.setVisibility(View.GONE);

				try {
					MediaMetadataRetriever artRetriever = new MediaMetadataRetriever();

					if (!musicData.get((int)_position).get("songData").toString().startsWith("/")) {
						String decodedData = new String(android.util.Base64.decode(musicData.get((int)_position).get("songData").toString(), android.util.Base64.DEFAULT), "UTF-8");
						artRetriever.setDataSource(decodedData);

					} else {
						artRetriever.setDataSource(_data.get((int)_position).get("songData").toString());
					}

					byte[] album_art = artRetriever.getEmbeddedPicture();

					if (album_art != null) {
						Bitmap bitmapArt = BitmapFactory.decodeByteArray(album_art, 0, album_art.length); 
						Glide.with(getApplicationContext()).load(bitmapArt).asBitmap().into(albumArt);

					} else {
						Glide.with(getApplicationContext()).load(R.drawable.album_art).asBitmap().into(albumArt);
					}
				} catch (Exception ignored) { /* do nothing as it says: error loading image */ }

			} else {
				if (_data.get((int)_position).get("isEmpty").toString().equals("yes")) {
					main.setVisibility(View.GONE);
					emptyMsg.setVisibility(View.VISIBLE);
				}
			}

			ObjectAnimator itemAnim = new ObjectAnimator();
			itemAnim.setTarget(main);
			itemAnim.setPropertyName("alpha");
			itemAnim.setFloatValues((float)(0.0d), (float)(1.0d));
			itemAnim.start();
			
			return _view;
		}
	}
}
