package com.example.androidprototypeapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
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
import android.widget.Toast;


public class HomeActivity extends Activity implements AnimationListener  {
	public final static String EXTRA_MESSAGE = "com.example.androidprototypeapp.MESSAGE";
	public Boolean loaded = false;
	public static final String PREFS_NAME = "MyPrefsFile";

	BluetoothDevice mmDevice;
	BluetoothSocket mmSocket;
	ArrayList<BTConnection> devices = new ArrayList<BTConnection>();
	ArrayList<String> storedDevices = new ArrayList<String>();

	private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setContentView(R.layout.activity_main);
			refresh();
		}
	};

	private Handler discoverHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Button ref = (Button) findViewById(R.id.refresh);
			ref.setEnabled(true);
		}
	};

	Animation animFadeIn;
	Animation moveRight;
	int count = 0;
	ImageView c;
	ImageView blank;
	Boolean reset = false;

	private BluetoothAdapter BA;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BA = BluetoothAdapter.getDefaultAdapter();

		/*
		//Introduction Version
		setContentView(R.layout.logo_screen);
		handler.sendMessageDelayed(new Message(), 2000);
		 */

		//Simple Version
		setContentView(R.layout.activity_main);
		refresh();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void television(String address) {
		Intent intent = new Intent(this, TelevisionActivity.class);
		intent.putExtra("Address", address);
		startActivity(intent);
	}

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

	public void createButton(String name, final BluetoothDevice device, final BluetoothSocket socket, String mode) {
		Button b = new Button(this);
		b.setAlpha(0f);
		b.setMinimumWidth(300);
		b.setMinimumHeight(80);
		b.setText(name);
		b.setTextColor(Color.argb(255, 0, 162, 232));

		Button.OnClickListener btnclick = null;
		if(mode.equals("IA")) {

			b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.light_off, 0, 0, 0);
			btnclick = new Button.OnClickListener(){
				@Override
				public void onClick(View v) {
					InputStream inStream;
					OutputStream outStream;
					try {
						outStream = socket.getOutputStream();
						String message = "r";
						
						byte[] toSend = message.getBytes();
						outStream.write(toSend);
						inStream = socket.getInputStream();
						
						byte byt[] = new byte[1];
						int received = inStream.read(byt, 0, 1);
						inStream.read();
						inStream.read();
						
						if(received == 1) {
							if(((int)byt[0] & 0xff) == '0') {
								message = "1";
								toSend = message.getBytes();
								outStream.write(toSend);
							} else if(((int)byt[0] & 0xff) == '1') {
								message = "0";
								toSend = message.getBytes();
								outStream.write(toSend);
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
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

		final Button b1 = new Button(this);

		b1.setHeight(60);
		b1.setWidth(60);
		b1.setBackgroundResource(R.drawable.settings);
		b1.setTextColor(Color.argb(255, 0, 162, 232));
		b1.setTag(R.string.zero, device.getAddress());
		b1.setTag(R.string.one, b);
		Button.OnClickListener popup = new Button.OnClickListener() {  

			@Override  
			public void onClick(final View v) {  
				//Creating the instance of PopupMenu  
				PopupMenu popup = new PopupMenu(getApplicationContext(), b1);  
				//Inflating the Popup using xml file  
				popup.getMenuInflater().inflate(R.menu.device_options, popup.getMenu());  

				//registering popup with OnMenuItemClickListener  
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {  
					public boolean onMenuItemClick(MenuItem item) {  
						if(item.getTitle().equals("Rename")) {
							rename(v.getTag(R.string.zero), v.getTag(R.string.one));
						}  
						return true;  
					}  
				});  

				popup.show();//showing popup menu  
			}  
		};//closing the setOnClickListener method

		b1.setOnClickListener(popup);

		ViewGroup layout = (ViewGroup) findViewById(R.id.home_page);
		RelativeLayout LL = new RelativeLayout(this);
		LL.setMinimumHeight(90);

		b1.setX(310);
		b1.setY(10);

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

		delayedAppear(b1);

		LL.addView(b);
		LL.addView(b1);
		LL.addView(block);
		LL.addView(cover);		
		layout.addView(LL);
	}

	@Override
	public void onAnimationEnd(Animation animation) {

	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub

	}

	public void refresh(View view) {
		refresh();
	}

	public void refresh() {

		Button ref = (Button) findViewById(R.id.refresh);
		ref.setEnabled(false);
		reset = false;
		if(BA.isDiscovering()) {
			BA.cancelDiscovery();
		}
		Toast.makeText(getApplicationContext(),"Refresh" 
				,Toast.LENGTH_SHORT).show();

		BA.startDiscovery();

		discoverHandler.sendMessageDelayed(new Message(), 12000);

		BroadcastReceiver BR = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {

				String action = intent.getAction();

				if (BluetoothDevice.ACTION_FOUND.equals(action)) 
				{
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

					BluetoothSocket tmp = null;
					mmDevice = device;

					if(mmDevice.getName().equals("MagMobIA")) {
						if(!storedDevices.contains(mmDevice.getAddress())) {
							storedDevices.add(mmDevice.getAddress());
							// Get a BluetoothSocket to connect with the given BluetoothDevice
							try {
								// MY_UUID is the app's UUID string, also used by the server code
								tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
							} catch (IOException e) { }
							mmSocket = tmp;
							System.out.println(mmSocket);
							try {
								//Connect the device through the socket. This will block
								//until it succeeds or throws an exception
								mmSocket.connect();
							} catch (IOException connectException) {
								//Unable to connect; close the socket and get out
								try {
									connectException.printStackTrace();
									mmSocket.close();
								} catch (IOException closeException) { }
							}

							if(!reset) {
								reset = true;
								devices.clear();
								storedDevices.clear();
								LinearLayout l = (LinearLayout) findViewById(R.id.home_page);
								l.removeAllViews();
							}

							SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
							String name = settings.getString(device.getAddress(), "Unknown Device");
							createButton(name, device, mmSocket, "IA");
						}						
					} else if(mmDevice.getName().equals("MagMobTA")) {
						if(!storedDevices.contains(mmDevice.getAddress())) {
							storedDevices.add(mmDevice.getAddress());

							if(!reset) {
								reset = true;
								devices.clear();
								storedDevices.clear();
								LinearLayout l = (LinearLayout) findViewById(R.id.home_page);
								l.removeAllViews();
							}

							SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
							String name = settings.getString(device.getAddress(), "Unknown Device");

							createButton(name, mmDevice, null, "TA");
						}						
					} 
				}
			}

		};

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND); 
		registerReceiver(BR, filter);

	}

	public void renamePreference(String key, String text) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.putString(key, text);
		editor.commit();
	}

	public void rename(Object addressObj, Object buttonObj) {

		final String address = addressObj.toString();
		final Button b = (Button) buttonObj;

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Title");
		alert.setMessage("Message");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setText(b.getText());

		alert.setTitle("Rename");
		alert.setMessage("Rename button to:");
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				b.setText(input.getText().toString());
				renamePreference(address, input.getText().toString());
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Cancelled.
			}
		});

		alert.show();
	}

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
