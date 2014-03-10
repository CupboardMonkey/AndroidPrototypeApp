package com.example.androidprototypeapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class TelevisionActivity extends Activity {

	InputStream in;
	OutputStream out;

	ArrayList<ArrayList<Integer>> signals = new ArrayList<ArrayList<Integer>>();

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		System.out.println("In tv activity");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_television);
		System.out.println("Getting intent");
		Intent intenty = getIntent();
		System.out.println("Got intent");
		
		SocketData data = intenty.getParcelableExtra("SocketData");

		System.out.println("Got data");
		
		in = data.getInStream();
		out = data.getOutStream();
		
		create(1);
		create(2);
		create(3);
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

	public void create(final int i) {

		Button b = new Button(this);

		b.setMinimumWidth(300);
		b.setMinimumHeight(80);
		b.setText(i);
		b.setTextColor(Color.argb(255, 0, 162, 232));

		Button b1 = new Button(this);

		b1.setMinimumWidth(300);
		b1.setMinimumHeight(80);
		b1.setText("Record " + i);
		b1.setTextColor(Color.argb(255, 0, 162, 232));

		Button.OnClickListener btnclick = new Button.OnClickListener(){

			@Override
			public void onClick(View v) {

				try {

					String message = "1";
					byte[] toSend = message.getBytes();
					out.write(toSend);

					for(int j = 0; j < signals.get(i).size(); j++) {
						System.out.println(signals.get(i).get(j));
					}

					sendIntArray(signals.get(i), out);


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
					signals.remove(i);
					signals.add(i, getArray(in));
					System.out.println("Got code");


				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}

		};
		
		b1.setOnClickListener(btnset);
	}

	public ArrayList<Integer> getArray(InputStream in) {
		int size = getInt(in);
		ArrayList<Integer> intList = new ArrayList<Integer>();

		for(int i = 0; i < size; i++) {
			intList.add(getInt(in));
		}

		System.out.println("Items received: " + intList.size());
		for(int i = 0; i < intList.size(); i++) {
			System.out.println(intList.get(i));
		}

		return intList;

	}

	public int getInt(InputStream in) {

		String temp = "";

		boolean cond = true;

		while(cond) {

			//int received = inStream.read();
			char c;
			try {
				c = ((char) in.read());
				System.out.println(c);
				if(c == 'x') {
					System.out.println("Finished reading. Result is: " + temp);
					cond = false;
				} else {
					temp += c;
					System.out.println(temp);
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

	public void sendIntArray(ArrayList<Integer> intList, OutputStream out) {
		sendInt(intList.size(), out);
		for(int i = 0; i < intList.size(); i++) {
			sendInt(intList.get(i), out);
		}
	}

}

