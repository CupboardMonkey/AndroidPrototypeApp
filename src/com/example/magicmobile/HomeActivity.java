package com.example.magicmobile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class HomeActivity extends Activity implements AnimationListener  {

	//Location of shared preferences
	public static final String PREFS_NAME = "MyPrefsFile";
	//UUID needed to establish a connection
	private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private BluetoothAdapter BA;
	ArrayList<String> storedDevices = new ArrayList<String>();
	ArrayList<BluetoothSocket> deviceSockets = new ArrayList<BluetoothSocket>();
	int gotData = 0;	

	//Handler for refreshing
	private Handler discoverHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Button ref = (Button) findViewById(R.id.refresh);
			ref.setBackgroundResource(R.drawable.refresh);
			ref.setEnabled(true);
			if(storedDevices.size() == 0) {
				LinearLayout l = (LinearLayout) findViewById(R.id.home_page);
				l.removeAllViews();

				TextView t = new TextView(getBaseContext());
				t.setTextSize(20);
				t.setTextColor(Color.argb(255, 0, 162, 232));
				t.setText(R.string.no_devices_found);
				l.addView(t);				
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BA = BluetoothAdapter.getDefaultAdapter();
		setContentView(R.layout.activity_main);
		refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	//Go to TA mode
	public void television(String address) {
		Intent intent = new Intent(this, TransmitterActuator.class);
		intent.putExtra("Address", address);
		startActivity(intent);
	}

	//Various methods used to animate a button creation
	public void appear(Button b) {
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
		anim.setAnimationListener(this);
		b.startAnimation(anim);		
	}
	public void delayedAppear(Button b) {
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.delayed_anim_fade_in);
		anim.setAnimationListener(this);
		b.startAnimation(anim);		
	}
	public void appearAndSlide(Button b) {
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in_translate_right);
		anim.setAnimationListener(this);
		b.startAnimation(anim);		
	}	
	public void appearAndHide(Button b) {
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in_hide);
		anim.setAnimationListener(this);
		b.startAnimation(anim);		
	}

	//Method to create a button, giving different functionality dependent on mode
	public void createButton(String name, final BluetoothDevice device, final BluetoothSocket socket, String mode, Boolean prox) {
		Button b = new Button(this);
		b.setAlpha(0f);
		b.setMinimumWidth(300);
		b.setMinimumHeight(80);
		b.setText(name);
		b.setTextColor(Color.argb(255, 0, 162, 232));

		Button.OnClickListener btnclick = null;

		//If device is an IA
		if(mode.equals("IA")) {
			//Check if device should be activated on proximity
			if(prox) {
				OutputStream outStream;
				try {
					outStream = socket.getOutputStream();
					String message = "1";
					byte[] toSend = message.getBytes();
					outStream.write(toSend);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.light_off, 0, 0, 0);
			btnclick = new Button.OnClickListener(){
				@Override
				public void onClick(View v) {
					final InputStream inStream;
					final OutputStream outStream;	
					try {
						//Request current output level
						outStream = socket.getOutputStream();
						String message = "r";

						byte[] toSend = message.getBytes();
						outStream.write(toSend);
						inStream = socket.getInputStream();

						//Data collection is pending
						gotData = 1;

						//Run timer in thread to prevent stalling
						Thread getInput = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									//Give small delay
									Thread.sleep(100);

									byte byt[] = new byte[1];

									//Check if reply from IA
									if(inStream.available() > 0) {
										int received = inStream.read(byt, 0, 1);
										inStream.read();
										inStream.read();

										if(received == 1) {
											//Toggle output power level
											if(((int)byt[0] & 0xff) == '0') {
												String message = "1";
												byte[] toSend = message.getBytes();
												outStream.write(toSend);
											} else if(((int)byt[0] & 0xff) == '1') {
												String message = "0";
												byte[] toSend = message.getBytes();
												outStream.write(toSend);
											}

										} 
										//Data received
										gotData = 2;

										//Else no reply, IA has lost connection
									} else {
										//No data received
										gotData = 0;
									}

								} catch (Exception e) {
									e.getLocalizedMessage();
								}
							}
						});
						getInput.start();

						//Wait until gotData is 0 or 2
						while(gotData == 1) {}

						//If not data then notify and refresh
						if(gotData == 0) {
							Toast.makeText(getBaseContext(),R.string.no_data,Toast.LENGTH_LONG).show();
							refresh();
						}


					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};

			//Or if device is TA
		} else if(mode.equals("TA")) {
			b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tv_icon, 0, 0, 0);
			btnclick = new Button.OnClickListener(){
				@Override
				public void onClick(View v) {
					television(device.getAddress());
				}
			};
		}

		b.setOnClickListener(btnclick);

		final Button settingsButton = new Button(this);

		settingsButton.setHeight(60);
		settingsButton.setWidth(60);
		settingsButton.setBackgroundResource(R.drawable.settings);
		settingsButton.setTextColor(Color.argb(255, 0, 162, 232));
		//Set associated variables
		settingsButton.setTag(R.string.zero, device.getAddress());
		settingsButton.setTag(R.string.one, b);


		Button.OnClickListener popup = null;

		//TA settings
		if(mode.equals("TA")) {

			popup = new Button.OnClickListener() {  

				@Override  
				public void onClick(final View v) {  
					PopupMenu popup = new PopupMenu(getApplicationContext(), settingsButton);

					//Use layout for TA settings
					popup.getMenuInflater().inflate(R.menu.device_options, popup.getMenu());  

					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {  
						public boolean onMenuItemClick(MenuItem item) {

							//If rename button was pressed
							if(item.getTitle().equals("Rename")) {
								rename(v.getTag(R.string.zero), v.getTag(R.string.one));
							}  
							return true;  
						}  
					});  

					popup.show();  
				}  
			};

			//IA settings
		} else if (mode.equals("IA")) {

			popup = new Button.OnClickListener() {  

				@Override  
				public void onClick(final View v) {  
					PopupMenu popup = new PopupMenu(getApplicationContext(), settingsButton);
					popup.getMenuInflater().inflate(R.menu.ia_device_options, popup.getMenu());  

					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {  
						public boolean onMenuItemClick(MenuItem item) {  
							if(item.getTitle().equals("Rename")) {
								rename(v.getTag(R.string.zero), v.getTag(R.string.one));
							}

							SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
							SharedPreferences.Editor editor = settings.edit();

							if(item.getTitle().equals("Activate on proximity")) {
								editor.putBoolean(device.getAddress()+"prox", true);
								editor.commit();
							}  
							if(item.getTitle().equals("Do not activate on proximity")) {
								editor.putBoolean(device.getAddress()+"prox", false);
								editor.commit();
							}  

							return true;  
						}  
					});  

					popup.show();  
				}  
			};
		}
		settingsButton.setOnClickListener(popup);

		//Add button to list and animate
		ViewGroup layout = (ViewGroup) findViewById(R.id.home_page);
		RelativeLayout LL = new RelativeLayout(this);
		LL.setMinimumHeight(90);

		settingsButton.setX(310);
		settingsButton.setY(10);

		Button cover = new Button(this);
		cover.setBackgroundResource(R.drawable.scroll_cover);
		cover.setX(10);
		cover.setHeight(80);

		Button block = new Button(this);
		block.setBackgroundColor(Color.BLACK);
		block.setX(10);
		block.setHeight(80);

		b.setAlpha(1f);
		appear(b);

		appearAndSlide(cover);
		appearAndHide(block);
		deleteObject(cover, 2000);
		deleteObject(block, 1000);

		delayedAppear(settingsButton);

		LL.addView(b);
		LL.addView(settingsButton);
		LL.addView(block);
		LL.addView(cover);		
		layout.addView(LL);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	public void refresh(View view) {
		refresh();
	}

	@SuppressLint("NewApi")
	public void refresh() {

		//Disable refresh button to prevent errors
		Button ref = (Button) findViewById(R.id.refresh);
		ref.setEnabled(false);
		ref.setBackgroundResource(R.drawable.refreshing);

		//Clear list
		final LinearLayout l = (LinearLayout) findViewById(R.id.home_page);
		l.removeAllViews();

		//For any IA, kill connection
		for(int i = 0; i < storedDevices.size(); i++) {
			BluetoothDevice temp = BA.getRemoteDevice(storedDevices.get(i));
			if(temp.getName().equals("MagMobIA")) {
				try {
					deviceSockets.get(i).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		storedDevices.clear();
		deviceSockets.clear();

		if(BA.isDiscovering()) {
			BA.cancelDiscovery();
		}

		BA.startDiscovery();

		//Reactivate refresh in 12 seconds
		discoverHandler.sendMessageDelayed(new Message(), 12000);

		BroadcastReceiver BR = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {

				String action = intent.getAction();

				if (BluetoothDevice.ACTION_FOUND.equals(action)) 
				{
					//Get the Bluetooth device
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					System.out.println(device.getAddress());

					BluetoothSocket socket = null;

					//If connecting to an IA
					if(device.getName().equals("MagMobIA")) {

						//And device has not been added then add
						if(!storedDevices.contains(device.getAddress())) {
							if(storedDevices.size() == 0) {
								l.removeAllViews();
							}
							storedDevices.add(device.getAddress());

							//Connect to IA
							try {
								socket = device.createRfcommSocketToServiceRecord(MY_UUID);
								socket.connect();
							} catch (IOException e) { 
								e.printStackTrace();
								try {
									socket.close();
								} catch (IOException e2) { 
									e2.printStackTrace();
								}
							}

							SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

							//Create the button and add the socket connection to saved list
							Boolean prox = settings.getBoolean(device.getAddress()+"prox", false);
							String name = settings.getString(device.getAddress(), "Unknown Device");
							deviceSockets.add(socket);
							createButton(name, device, socket, "IA", prox);

						}
					} else if(device.getName().equals("MagMobTA")) {
						if(!storedDevices.contains(device.getAddress())) {
							if(storedDevices.size() == 0) {
								l.removeAllViews();
							}
							storedDevices.add(device.getAddress());
							SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
							String name = settings.getString(device.getAddress(), "Unknown Device");
							deviceSockets.add(null);
							createButton(name, device, null, "TA", false);
						}
					} 
				}
			}
		};

		//Display refreshing if no devices currently found
		if(storedDevices.size() == 0) {
			TextView t = new TextView(getBaseContext());
			t.setTextSize(20);
			t.setTextColor(Color.argb(255, 0, 162, 232));
			t.setText(R.string.refreshing);
			l.addView(t);
		}

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND); 
		registerReceiver(BR, filter);

	}

	//Convenience method for saving string shared preference
	public void renamePreference(String key, String text) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.putString(key, text);
		editor.commit();
	}

	//Rename device method
	public void rename(Object addressObj, Object buttonObj) {

		final String address = addressObj.toString();
		final Button b = (Button) buttonObj;

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Title");
		alert.setMessage("Message");

		//Get user input
		final EditText input = new EditText(this);
		input.setText(b.getText());

		alert.setTitle("Rename");
		alert.setMessage("Rename button to:");
		alert.setView(input);
		
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//Save text
				b.setText(input.getText().toString());
				renamePreference(address, input.getText().toString());
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//Cancel window
			}
		});

		alert.show();
	}

	//Methods to remove object after x time
	private Handler deleteObj = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			View o = (View) msg.obj;
			o.setVisibility(View.GONE);
		}
	};

	public void deleteObject(View obj, int time) {
		Message m = new Message();
		m.obj = obj;
		deleteObj.sendMessageDelayed(m, time);
	}

}
