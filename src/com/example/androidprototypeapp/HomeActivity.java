package com.example.androidprototypeapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.Gson;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


public class HomeActivity extends Activity implements AnimationListener {
	public final static String EXTRA_MESSAGE = "com.example.androidprototypeapp.MESSAGE";
	public Boolean loaded = false;
	public static final String PREFS_NAME = "MyPrefsFile";

	BluetoothDevice mmDevice;
	BluetoothSocket mmSocket;
	ArrayList<BTConnection> devices = new ArrayList<BTConnection>();
	private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setContentView(R.layout.activity_main);
			System.out.println("Should be refreshing");
			refresh();
			System.out.println("Should be refreshed");
		}
	};
	Animation animFadeIn;
	Animation moveRight;
	int count = 0;
	ImageView c;
	ImageView blank;
	Boolean reset = false;
	//MyApplication socketDetails;

	private BluetoothAdapter BA;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BA = BluetoothAdapter.getDefaultAdapter();


		//if(!loaded) {
			//loaded = true;
			setContentView(R.layout.logo_screen);
			handler.sendMessageDelayed(new Message(), 2000);
		//} else {
			
			
			//setContentView(R.layout.activity_main);
			
		//}

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

		animFadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
		animFadeIn.setAnimationListener(this);

		moveRight = AnimationUtils.loadAnimation(this, R.anim.translate_right);
		moveRight.setAnimationListener(this);

		c = new ImageView(this);
		c.setMinimumWidth(300);
		c.setMinimumHeight(80);
		c.setImageResource(R.drawable.scroll_cover);
		c.setVisibility(View.VISIBLE);

		blank = new ImageView(this);
		blank.setMinimumWidth(300);
		blank.setMinimumHeight(80);
		blank.setImageResource(R.drawable.cover);
		blank.setVisibility(View.VISIBLE);

		//c = (ImageView)findViewById(R.id.cover);
		//blank = (ImageView)findViewById(R.id.blank);

		LinearLayout l = (LinearLayout)findViewById(R.id.home_page);

		l.addView(b);
		l.addView(c);
		l.addView(blank);

		System.out.println("l y = " + l.getY());

		blank.setAlpha(1f);
		b.setAlpha(1f);
		c.setAlpha(1f);

		blank.setX(10);
		blank.setY(-80);

		c.setX(10);
		c.setY(-160);

		System.out.println(blank.getX() + ", " + blank.getY());
		System.out.println("l size:" + l.getChildCount());

		blank.bringToFront();
		c.bringToFront();


		c.startAnimation(animFadeIn);
		b.startAnimation(animFadeIn);

	}

	public void newButton(String name, final BluetoothSocket socket) {
		ViewGroup linearLayout = (ViewGroup) findViewById(R.id.home_page);

		//Button button = (Button)getLayoutInflater().inflate(R.layout.button_light, null);
		Button b = new Button(this);
		b.setMinimumWidth(300);
		b.setMinimumHeight(80);
		b.setText(name);
		b.setTextColor(Color.argb(255, 0, 162, 232));
		//b.setBackgroundColor(Color.WHITE);
		//b.setBackgroundResource(R.layout.button_light);
		b.setAlpha(0f);
		//linearLayout.addView(b, 300, 80);
		//linearLayout.addView(b);

		b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unknown, 0, R.drawable.blank, 0);
		appear(b);

		//linearLayout.addView(b);
		System.out.println("New button");
		Button.OnClickListener btnclick = new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				v.playSoundEffect(SoundEffectConstants.CLICK);
				InputStream inStream;
				OutputStream outStream;
				try {

					System.out.println("here");

					outStream = socket.getOutputStream();
					String message = "r";
					byte[] toSend = message.getBytes();
					outStream.write(toSend);

					inStream = socket.getInputStream();
					int received = inStream.read();
					System.out.println(received);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}

		};


		b.setOnClickListener(btnclick);
	}

	public void easynewButton(String name, final BluetoothSocket socket) {
		ViewGroup linearLayout = (ViewGroup) findViewById(R.id.home_page);

		Button b = new Button(this);
		b.setMinimumWidth(300);
		b.setMinimumHeight(80);
		b.setText(name);
		b.setTextColor(Color.argb(255, 0, 162, 232));
		b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unknown, 0, R.drawable.blank, 0);

		linearLayout.addView(b);

		Button.OnClickListener btnclick = new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//v.playSoundEffect(SoundEffectConstants.CLICK);
				InputStream inStream;
				OutputStream outStream;
				try {

					System.out.println("here");

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
						System.out.println("Changed from " + ((int)byt[0] & 0xff));
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}

		};


		b.setOnClickListener(btnclick);

	}

	public void easyTVButton(String name, final BluetoothSocket socket) {
		ViewGroup linearLayout = (ViewGroup) findViewById(R.id.home_page);

		Button b = new Button(this);
		b.setMinimumWidth(300);
		b.setMinimumHeight(80);
		b.setText(name);
		b.setTextColor(Color.argb(255, 0, 162, 232));
		b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tv_icon, 0, R.drawable.blank, 0);

		linearLayout.addView(b);

		Button.OnClickListener btnclick = new Button.OnClickListener(){

			@Override
			public void onClick(View v) {

				television(socket.getRemoteDevice().getAddress());

			}

		};


		b.setOnClickListener(btnclick);

	}


	//public void phase (View view) {
	//		animFadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
	//		animFadeIn.setAnimationListener(this);
	//
	//		moveRight = AnimationUtils.loadAnimation(this, R.anim.translate_right);
	//		moveRight.setAnimationListener(this);
	//
	//		Button b = (Button)findViewById(R.id.new_light);
	//		c = (ImageView)findViewById(R.id.cover);
	//		blank = (ImageView)findViewById(R.id.blank);
	//
	//		blank.setAlpha(1f);
	//		b.setAlpha(1f);
	//		c.setAlpha(1f);
	//
	//		blank.bringToFront();
	//		c.bringToFront();
	//
	//		blank.setX(b.getX()+10);
	//		blank.setY(b.getY());
	//		blank.setAlpha(1f);
	//
	//
	//		c.setX(b.getX()+10);
	//		c.setY(b.getY());
	//
	//		c.startAnimation(animFadeIn);
	//		b.startAnimation(animFadeIn);
	//	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (animation == animFadeIn) {
			blank.setAlpha(0f);
			c.startAnimation(moveRight);
		}

		if (animation == moveRight) {
			c.setAlpha(0f);
		}
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

		if(BA.isDiscovering()) {
			BA.cancelDiscovery();
		}
		Toast.makeText(getApplicationContext(),"Refresh" 
				,Toast.LENGTH_SHORT).show();

		BA.startDiscovery();

		BroadcastReceiver BR = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {

				String action = intent.getAction();

				if (BluetoothDevice.ACTION_FOUND.equals(action)) 
				{
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					// Add the name and address to an array adapter to show in a ListView
					//mArrayAdapter.add(device.getName() + "\n" + device.getAddress());



					BluetoothSocket tmp = null;
					mmDevice = device;

					System.out.println(device.getName());

					if(mmDevice.getName().equals("HC-05")) {
						// Get a BluetoothSocket to connect with the given BluetoothDevice
						try {
							// MY_UUID is the app's UUID string, also used by the server code
							tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
							//tempDevice = device;
						} catch (IOException e) { }
						mmSocket = tmp;


						//						try {
						// Connect the device through the socket. This will block
						// until it succeeds or throws an exception
						//							mmSocket.connect();

						//						} catch (IOException connectException) {
						// Unable to connect; close the socket and get out
						//							try {
						//								mmSocket.close();
						//							} catch (IOException closeException) { }
						//						}

						Toast.makeText(getApplicationContext(), "Connected:\n" + device.getName() + "\n" + device.getAddress() 
								,Toast.LENGTH_SHORT).show();
						System.out.println("Connected");
						if(!reset) {
							reset = true;
							devices.clear();
							LinearLayout l = (LinearLayout) findViewById(R.id.home_page);
							l.removeAllViews();
						}

						//String name = "Unknown Device";
						SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

						//if(settings.contains(device.getAddress()))
						String name = settings.getString(device.getAddress(), "Unknown Device");


						//						devices.add(new BTConnection(device, mmSocket, device.getName()));
						easyTVButton(name, mmSocket);

					} else {
						//						Toast.makeText(getApplicationContext(), "Not Connected:\n" + device.getName() + "\n" + device.getAddress() 
						//								,Toast.LENGTH_SHORT).show();
						//						System.out.println("Not Connected");
						//						easynewButton(device.getName());
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

	public void saveObject(String key, Object o) {

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		Gson gson = new Gson();
		String json = gson.toJson(o);
		editor.putString(key, json);
		editor.commit();
	}

	public ChannelData getChannelData(String key) {

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

		Gson gson = new Gson();

		String json = settings.getString(key, "");
		ChannelData obj = gson.fromJson(json, ChannelData.class);
		return obj;
	}
}
