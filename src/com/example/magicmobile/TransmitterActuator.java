package com.example.magicmobile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
import com.google.gson.Gson;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class TransmitterActuator extends Activity {

	private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final String PREFS_NAME = "MyPrefsFile";
	BluetoothDevice device;
	BluetoothSocket sock;

	InputStream in;
	OutputStream out;

	ArrayList<ChannelData> signals = new ArrayList<ChannelData>();
	BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
	int idCounter;
	int gotData = 0;
	String temp = "";

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_television);
		String address = getIntent().getStringExtra("Address");
		getDetails(address);		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		idCounter = settings.getInt(device.getAddress()+"id", 0);
		loadSavedButtons();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//Method to get integer array from TA
	public ArrayList<Integer> getArray(InputStream in) {
		int size = getInt(in);
		ArrayList<Integer> intList = new ArrayList<Integer>();
		for(int i = 0; i < size; i++) {
			intList.add(getInt(in));
		}

		return intList;
	}

	//Method to get single int from TA
	public int getInt(final InputStream in) {

		temp = "";

		boolean cond = true;
		while(cond) {
			char c;
			try {
				int intIn = in.read();
				//If intIn is an valid char
				if(intIn < 128 && intIn > 47) {
					c = ((char) intIn);
					//'x' signifies end of string
					if(c == 'x') {
						cond = false;
						gotData = 2;
					} else {
						temp += c;
					}
				}
				else {
					c = ((char) intIn);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return (Integer.parseInt(temp));

	}

	//Method to sent single int to TA
	public void sendInt(Integer i, OutputStream out) {
		try {
			String s = i.toString();

			byte[] toSend = s.getBytes();
			out.write(toSend);
			toSend = ("x").getBytes();
			out.write(toSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Method to send int array to TA
	public void sendIntArray(ArrayList<Integer> intList, OutputStream out) {

		sendInt(intList.size(), out);
		for(int i = 0; i < intList.size(); i++) {
			sendInt(intList.get(i), out);
		}

	}

	//Method to establish connection to TA
	public void getDetails(String address) {
		device = BA.getRemoteDevice(address);

		try {
			sock = device.createRfcommSocketToServiceRecord(MY_UUID);
			sock.connect();
			in = sock.getInputStream();
			out = sock.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Returns channelData location
	public int getChannelData(int id) {
		for(int i = 0; i < signals.size(); i++) {
			if(signals.get(i).getId() == id) {
				return i;
			}
		}
		return -1;
	}

	//Method to refresh list of buttons
	public void loadButtons() {

		ViewGroup layout = (ViewGroup) findViewById(R.id.remote);
		layout.removeAllViews();
		
		//If buttons are stored
		if(signals.size() > 0) {
			//Create all buttons
			for (int i = 0; i < signals.size(); i++) {
				create(signals.get(i));
			}
		//Else display message
		} else {
			TextView t = new TextView(this);
			t.setTextSize(20);
			t.setTextColor(Color.argb(255, 0, 162, 232));
			t.setText("There are no stored commands. Try adding one by pressing the button in the top-right corner");
			layout.addView(t);
		}
		//Save list of buttons
		saveButtons();
	}

	//Method to create a new button
	public void newCreate(View view) {
		ChannelData temp = new ChannelData(idCounter,new ArrayList<Integer>(),"Unnamed");

		//Increase ID reference
		idCounter++;

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(device.getAddress()+"id", idCounter);
		editor.commit();
		
		signals.add(temp);
		create(temp);
		saveButtons();
		loadButtons();
	}

	//Create a button given channel data
	public void create(final ChannelData cd) {
		final ArrayList<Integer> code = cd.getCode();
		String label = cd.getLabel();

		Button b = new Button(this);
		b.setMinimumWidth(300);
		b.setMinimumHeight(80);
		b.setText(label);
		b.setTextColor(Color.argb(255, 0, 162, 232));

		final Button settingsButton = new Button(this);
		settingsButton.setHeight(60);
		settingsButton.setWidth(60);
		settingsButton.setBackgroundResource(R.drawable.settings);
		settingsButton.setTextColor(Color.argb(255, 0, 162, 232));
		settingsButton.setTag(cd.getId());

		Button.OnClickListener btnclick = new Button.OnClickListener(){

			@Override
			public void onClick(View v) {

				try {
					//Send '1' message to TA to transmit
					String message = "1";
					byte[] toSend = message.getBytes();
					out.write(toSend);
					sendIntArray(code, out);
				} catch (IOException e) {
					e.printStackTrace();
				}			
			}
		};

		b.setOnClickListener(btnclick);
		Button.OnClickListener popup = new Button.OnClickListener() {  

			@Override  
			public void onClick(final View v) {    
				PopupMenu popup = new PopupMenu(getApplicationContext(), settingsButton);  
				//Inflating the Popup using XML file  
				popup.getMenuInflater().inflate(R.menu.transmitter_options, popup.getMenu());  
				
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {  
					public boolean onMenuItemClick(MenuItem item) {  
						if(item.getTitle().equals("Rename")) {
							rename((Integer) v.getTag());
						} else if(item.getTitle().equals("Record")) {
							record((Integer) v.getTag());
						} else if(item.getTitle().equals("Delete")) {
							delete((Integer) v.getTag());
						}   
						return true;  
					}  
				});  
				popup.show();  
			}  
		};

		settingsButton.setOnClickListener(popup);
		ViewGroup layout = (ViewGroup) findViewById(R.id.remote);
		LinearLayout LL = new LinearLayout(this);
		LL.addView(b);
		LL.addView(settingsButton);
		layout.addView(LL);
	}

	//Convenience method to get channelData
	public ChannelData getCDbyID(int id) {
		for(ChannelData cd : signals) {
			if(cd.getId() == id) {
				return cd;
			}
		}
		return null;
	}

	//Rename button method
	public void rename(int id) {
		final ChannelData cd = getCDbyID(id);
		if(cd != null) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Title");
			alert.setMessage("Message");
 
			final EditText input = new EditText(this);
			input.setText(cd.getLabel());

			alert.setTitle("Rename");
			alert.setMessage("Rename button to:");
			alert.setView(input);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					cd.setLabel(input.getText().toString());
					loadButtons();
				}
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});

			alert.show();


		} else {
			Toast.makeText(getBaseContext(),"Cannot find data",Toast.LENGTH_LONG).show();
		}

	}

	//Method to store new IR signal
	public void record(int id) {
		gotData = 1;
		try {
			final ChannelData cd = getCDbyID(id);
			
			if(cd != null) {
				//Send '0' message to TA to start recording
				String message = "0";
				byte[] toSend = message.getBytes();
				out.write(toSend);

				//10 second timeout in new thread
				Thread getInput = new Thread(new Runnable() {
					@SuppressLint("NewApi")
					@Override
					public void run() {
						try {
							while(in.available() > 0) {
								in.read();
							}
							int count = 0;
							while(count < 100) {
								Thread.sleep(100);
								if(in.available() > 0) {
									count = 200;
									gotData = 2;
								} else {
									if(count == 99) {
										gotData = 0;
									} else {
										count++;
									}
								}
							}
						} catch (Exception e) {
							e.getLocalizedMessage();
						}
					}
				});


				getInput.start();

				if(waitForInput()) {
					getInput.interrupt();				
					cd.setCode(getArray(in));
					loadButtons();
					Toast.makeText(getBaseContext(),"Command Saved",Toast.LENGTH_SHORT).show();
				} else {
					getInput.interrupt();
					Toast.makeText(getBaseContext(),"Timeout/Connection Lost",Toast.LENGTH_LONG).show();
				}

			} else {
				Toast.makeText(getBaseContext(),"Cannot find data",Toast.LENGTH_LONG).show();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Delete button method
	public void delete(int id) {
		ChannelData cd = getCDbyID(id);
		if(cd != null) {
			signals.remove(cd);
			loadButtons();
		} else {
			Toast.makeText(getBaseContext(),"Cannot find data",Toast.LENGTH_LONG).show();
		}
	}

	//Close connection if activity ends
	@Override
	protected void onStop() {
		super.onStop();
		try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Save current buttons
	public void saveButtons() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.putInt(device.getAddress()+"size", signals.size());
		editor.commit();

		for (int i = 0; i < signals.size(); i++) {
			saveObject(device.getAddress()+i, signals.get(i));
		}
	}

	//Save object as Gson string
	public void saveObject(String key, Object o) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		Gson gson = new Gson();
		String json = gson.toJson(o);
		editor.putString(key, json);
		editor.commit();
	}

	//Method to decode saved Gson string
	public ChannelData getChannelData(String key) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		Gson gson = new Gson();
		String json = settings.getString(key, "");
		ChannelData obj = gson.fromJson(json, ChannelData.class);
		return obj;
	}

	//Load buttons saved in shared preferences
	public void loadSavedButtons() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String add = device.getAddress();
		int size = settings.getInt(add+"size", 0);
		if(size > 0) {
			for(int i = 0; i < size; i++) {
				ChannelData cd = getChannelData(add+i);
				signals.add(cd);
			}
		}
		loadButtons();
	}

	//Home method
	public void returnHome(View view) {
		this.finish();
	}

	//Wait while data is received
	public boolean waitForInput() {
		while(gotData == 1) {}
		if(gotData == 0) {
			return false;
		} else {
			return true;
		}
	}

}