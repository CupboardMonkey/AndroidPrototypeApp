package com.example.androidprototypeapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.Gson;

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
import android.view.SoundEffectConstants;
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
		Button b = new Button(this);
		b.setMinimumWidth(300);
		b.setMinimumHeight(80);
		b.setText(name);
		b.setTextColor(Color.argb(255, 0, 162, 232));
		b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.light_off, 0, R.drawable.blank, 0);


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
		final Button b1 = new Button(this);

		b1.setHeight(60);
		b1.setWidth(60);
		b1.setBackgroundResource(R.drawable.settings);
		b1.setTextColor(Color.argb(255, 0, 162, 232));
		b1.setTag(R.string.zero, socket.getRemoteDevice().getAddress());
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
						//Toast.makeText(MainActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();  
						return true;  
					}  
				});  

				popup.show();//showing popup menu  
			}  
		};//closing the setOnClickListener method
		b1.setOnClickListener(popup);

		ViewGroup layout = (ViewGroup) findViewById(R.id.home_page);
		LinearLayout LL = new LinearLayout(this);
		LL.addView(b);
		LL.addView(b1);
		layout.addView(LL);

	}

	public void easyTVButton(String name, final BluetoothSocket socket) {
		System.out.println("Making new button");
		Button b = new Button(this);
		b.setMinimumWidth(300);
		b.setMinimumHeight(80);
		b.setText(name);
		b.setTextColor(Color.argb(255, 0, 162, 232));
		b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tv_icon, 0, R.drawable.blank, 0);

		Button.OnClickListener btnclick = new Button.OnClickListener(){

			@Override
			public void onClick(View v) {

				television(socket.getRemoteDevice().getAddress());

			}

		};


		b.setOnClickListener(btnclick);

		final Button b1 = new Button(this);

		b1.setHeight(60);
		b1.setWidth(60);
		b1.setBackgroundResource(R.drawable.settings);
		b1.setTextColor(Color.argb(255, 0, 162, 232));
		b1.setTag(R.string.zero, socket.getRemoteDevice().getAddress());
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
						//Toast.makeText(MainActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();  
						return true;  
					}  
				});  

				popup.show();//showing popup menu  
			}  
		};//closing the setOnClickListener method
		b1.setOnClickListener(popup);

		ViewGroup layout = (ViewGroup) findViewById(R.id.home_page);
		LinearLayout LL = new LinearLayout(this);
		LL.addView(b);
		LL.addView(b1);
		layout.addView(LL);
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

					//If it's an Instantaneous Actuator
					if(mmDevice.getName().equals("MagMobIA")) {
						if(!storedDevices.contains(mmDevice.getAddress())) {
							storedDevices.add(mmDevice.getAddress());
							// Get a BluetoothSocket to connect with the given BluetoothDevice
							try {
								// MY_UUID is the app's UUID string, also used by the server code
								tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
								//tempDevice = device;
							} catch (IOException e) { }
							mmSocket = tmp;


							try {
								//Connect the device through the socket. This will block
								//until it succeeds or throws an exception
								mmSocket.connect();
							} catch (IOException connectException) {
								//Unable to connect; close the socket and get out
								try {
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


							//						devices.add(new BTConnection(device, mmSocket, device.getName()));
							easynewButton(name, mmSocket);
							
							//Else it's a Transmitter Actuator
						}
					} else if(mmDevice.getName().equals("MagMobTA")) {
						if(!storedDevices.contains(mmDevice.getAddress())) {
							storedDevices.add(mmDevice.getAddress());
							// Get a BluetoothSocket to connect with the given BluetoothDevice
							try {
								// MY_UUID is the app's UUID string, also used by the server code
								tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
								//tempDevice = device;
							} catch (IOException e) { }
							mmSocket = tmp;


							try {
								//Connect the device through the socket. This will block
								//until it succeeds or throws an exception
								mmSocket.connect();
							} catch (IOException connectException) {
								//Unable to connect; close the socket and get out
								try {
									mmSocket.close();
								} catch (IOException closeException) { }
							}

							try {
								mmSocket.close();
							} catch (IOException closeException) { }
							
							if(!reset) {
								reset = true;
								devices.clear();
								storedDevices.clear();
								System.out.println("size: " + devices.size());
								LinearLayout l = (LinearLayout) findViewById(R.id.home_page);
								l.removeAllViews();
							}

							SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
							String name = settings.getString(device.getAddress(), "Unknown Device");


							//						devices.add(new BTConnection(device, mmSocket, device.getName()));
							easyTVButton(name, mmSocket);
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

				//Toast.makeText(getBaseContext(),"Did something: " + input.getText(),Toast.LENGTH_LONG).show();
				b.setText(input.getText().toString());

				renamePreference(address, input.getText().toString());

			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();
	}

}
