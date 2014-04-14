package com.example.androidprototypeapp;

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
import android.os.SystemClock;
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

public class TelevisionActivity extends Activity {

	private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final String PREFS_NAME = "MyPrefsFile";
	BluetoothDevice device;
	BluetoothSocket sock;

	InputStream in;
	OutputStream out;

	ArrayList<Integer> channelCode;
	ArrayList<ChannelData> signals = new ArrayList<ChannelData>();
	BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
	int idCounter;
	int gotData = 0;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_television);
		Intent intenty = getIntent();
		String address = intenty.getStringExtra("Address");
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



	public ArrayList<Integer> getArray(InputStream in) {
		int size = getInt(in);
		ArrayList<Integer> intList = new ArrayList<Integer>();
		for(int i = 0; i < size; i++) {
			intList.add(getInt(in));
		}

		return intList;
	}

	public int getInt(InputStream in) {

		String temp = "";
		boolean cond = true;

		while(cond) {
			char c;
			try {
				int intIn = in.read();
				if(intIn < 128 && intIn > 47) {
					c = ((char) intIn);
					if(c == 'x') {
						cond = false;
					} else {
						temp += c;
					}
				}
				else {
					c = ((char) intIn);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return (Integer.parseInt(temp));
	}

	public void sendInt(Integer i, OutputStream out) {
		try {
			String s = i.toString();

			byte[] toSend = s.getBytes();
			out.write(toSend);
			toSend = ("x").getBytes();
			out.write(toSend);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendIntArray(ArrayList<Integer> intList, OutputStream out, InputStream in) {

		sendInt(intList.size(), out);
		for(int i = 0; i < intList.size(); i++) {
			sendInt(intList.get(i), out);
		}

	}



	public void getDetails(String address) {
		device = BA.getRemoteDevice(address);

		try {
			sock = device.createRfcommSocketToServiceRecord(MY_UUID);
			sock.connect();
			in = sock.getInputStream();
			out = sock.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getChannelData(int id) {
		for(int i = 0; i < signals.size(); i++) {
			if(signals.get(i).getId() == id) {
				return i;
			}
		}
		return -1;
	}

	public void loadButtons() {

		ViewGroup layout = (ViewGroup) findViewById(R.id.remote);
		layout.removeAllViews();
		if(signals.size() > 0) {
			for (int i = 0; i < signals.size(); i++) {
				System.out.println(signals.get(i).getId() + " " + signals.get(i).getLabel());
				create(signals.get(i));
			}
		} else {
			TextView t = new TextView(this);
			t.setTextSize(20);
			t.setTextColor(Color.argb(255, 0, 162, 232));
			t.setText("There are no stored commands. Try adding one by pressing the button in the top-right corner");
			layout.addView(t);
		}
		saveButtons();
	}

	public void newCreate(View view) {
		ChannelData temp = new ChannelData(idCounter,new ArrayList<Integer>(),"Unnamed");

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

	public void create(final ChannelData cd) {
		final ArrayList<Integer> code = cd.getCode();
		String label = cd.getLabel();

		Button b = new Button(this);
		b.setMinimumWidth(300);
		b.setMinimumHeight(80);
		b.setText(label);
		b.setTextColor(Color.argb(255, 0, 162, 232));

		final Button b1 = new Button(this);
		b1.setHeight(60);
		b1.setWidth(60);
		b1.setBackgroundResource(R.drawable.settings);
		b1.setTextColor(Color.argb(255, 0, 162, 232));
		b1.setTag(cd.getId());

		Button.OnClickListener btnclick = new Button.OnClickListener(){

			@Override
			public void onClick(View v) {

				try {
					String message = "1";
					byte[] toSend = message.getBytes();
					out.write(toSend);
					sendIntArray(code, out, in);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
		};

		b.setOnClickListener(btnclick);
		Button.OnClickListener popup = new Button.OnClickListener() {  

			@Override  
			public void onClick(final View v) {  
				//Creating the instance of PopupMenu  
				PopupMenu popup = new PopupMenu(getApplicationContext(), b1);  
				//Inflating the Popup using xml file  
				popup.getMenuInflater().inflate(R.menu.transmitter_options, popup.getMenu());  

				//registering popup with OnMenuItemClickListener  
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

		b1.setOnClickListener(popup);
		ViewGroup layout = (ViewGroup) findViewById(R.id.remote);
		LinearLayout LL = new LinearLayout(this);
		LL.addView(b);
		LL.addView(b1);
		layout.addView(LL);
	}

	public ChannelData getCDbyID(int id) {
		for(ChannelData cd : signals) {
			if(cd.getId() == id) {
				return cd;
			}
		}
		return null;
	}

	public void rename(int id) {
		final ChannelData cd = getCDbyID(id);
		if(cd != null) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Title");
			alert.setMessage("Message");

			// Set an EditText view to get user input 
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
				public void onClick(DialogInterface dialog, int whichButton) {
					// Cancelled.
				}
			});

			alert.show();


		} else {
			Toast.makeText(getBaseContext(),"Cannot find data",Toast.LENGTH_LONG).show();
		}

	}

	public void record(int id) {
		try {
			final ChannelData cd = getCDbyID(id);
			if(cd != null) {
				String message = "0";
				byte[] toSend = message.getBytes();
				out.write(toSend);


				//if(in.available() > 0) {
					cd.setCode(getArray(in));
					loadButtons();

				//} else {
				//	System.out.println("Unavailable");
				//}


			} else {
				Toast.makeText(getBaseContext(),"Cannot find data",Toast.LENGTH_LONG).show();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void delete(int id) {
		ChannelData cd = getCDbyID(id);
		if(cd != null) {
			signals.remove(cd);
			loadButtons();
		} else {
			Toast.makeText(getBaseContext(),"Cannot find data",Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		try {
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveButtons() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.putInt(device.getAddress()+"size", signals.size());
		editor.commit();

		for (int i = 0; i < signals.size(); i++) {
			saveObject(device.getAddress()+i, signals.get(i));
		}
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

	public void returnHome(View view) {
		this.finish();
	}
}