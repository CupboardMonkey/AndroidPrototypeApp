package com.example.androidprototypeapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;


public class TelevisionActivity extends Activity {

	private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	BluetoothDevice device;
	BluetoothSocket sock;

	InputStream in;
	OutputStream out;

	ArrayList<Integer> channelCode;
	ArrayList<ChannelData> signals = new ArrayList<ChannelData>();

	int idCounter = 0;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		System.out.println("In tv activity");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_television);

		//
		//		System.out.println("Got data");

		//		in = socketDetails.getInStream();
		//		out = socketDetails.getOutStream();

		System.out.println("Getting intent");
		Intent intenty = getIntent();
		System.out.println("Got intent");

		String address = intenty.getStringExtra("Address");
		System.out.println(address);

		getDetails(address);

		System.out.println("Got all details, making buttons");

		System.out.println("Created buttons");
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
		System.out.println(size);
		for(int i = 0; i < size; i++) {
			intList.add(getInt(in));
			System.out.println(i + ": " + intList.get(i));
		}

		System.out.println("Items received: " + intList.size());
		//for(int i = 0; i < intList.size(); i++) {
		//System.out.println(i + ": " + intList.get(i));
		//}

		return intList;

	}

	public int getInt(InputStream in) {

		String temp = "";

		boolean cond = true;

		while(cond) {

			//int received = inStream.read();
			char c;
			try {
				int intIn = in.read();
				if(intIn < 128 && intIn > 47) {
					c = ((char) intIn);
					System.out.println(intIn + " = " + c);
					//System.out.println(c);
					if(c == 'x') {
						System.out.println("Finished reading. Result is: " + temp);
						cond = false;
					} else {
						temp += c;
						//System.out.println(temp);
					}
				}
				else {
					c = ((char) intIn);
					System.out.println("Bad: " + intIn + " = " + c);
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
			System.out.println("Sent out: " + i);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendIntArray(ArrayList<Integer> intList, OutputStream out, InputStream in) {

		System.out.println("Sending size: " + intList.size());
		sendInt(intList.size(), out);

		for(int i = 0; i < intList.size(); i++) {
			System.out.println("Sending  " + i + ": " + intList.get(i));
			sendInt(intList.get(i), out);
		}

	}



	public void getDetails(String address) {
		BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
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
		
		if(signals.size() > 0) {
			System.out.println("Refreshing");
			ViewGroup layout = (ViewGroup) findViewById(R.id.remote);
			layout.removeAllViews();
			for (int i = 0; i < signals.size(); i++) {
				create(signals.get(i));
			}
		}

	}

	public void newCreate(View view) {
		ChannelData temp = new ChannelData(idCounter,new ArrayList<Integer>(),"Unnamed");
		signals.add(temp);
		create(temp);
		idCounter++;
	}
	
	public void create(final ChannelData cd) {

		int id = cd.getId();
		final ArrayList<Integer> code = cd.getCode();
		String label = cd.getLabel();
		
		Button b = new Button(this);

		b.setMinimumWidth(300);
		b.setMinimumHeight(80);
		b.setText(label);
		b.setTextColor(Color.argb(255, 0, 162, 232));

		Button b1 = new Button(this);

		b1.setMinimumWidth(80);
		b1.setMinimumHeight(80);
		b1.setText("Record");
		b1.setTextColor(Color.argb(255, 0, 162, 232));


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

		Button.OnClickListener btnset = new Button.OnClickListener(){

			@Override
			public void onClick(View v) {

				try {

					String message = "0";
					byte[] toSend = message.getBytes();
					out.write(toSend);

					System.out.println("Sent, waiting");
					
					cd.setCode(getArray(in));
					System.out.println(cd.getCode().toString());
					loadButtons();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}

		};

		b1.setOnClickListener(btnset);
		ViewGroup layout = (ViewGroup) findViewById(R.id.remote);
		LinearLayout LL = new LinearLayout(this);
		LL.addView(b);
		LL.addView(b1);
		layout.addView(LL);

	}


}


