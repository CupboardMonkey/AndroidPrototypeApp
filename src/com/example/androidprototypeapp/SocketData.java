package com.example.androidprototypeapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Parcel;
import android.os.Parcelable;

public class SocketData implements Parcelable {

	BluetoothSocket sock;
	InputStream in;
	OutputStream out;
	
	public SocketData(BluetoothSocket sock) {
		
		this.sock = sock;
		try {
			in = sock.getInputStream();
			out = sock.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public BluetoothSocket getSocket() {
		return sock;
	}
	
	public InputStream getInStream() {
		return in;
	}
	
	public OutputStream getOutStream() {
		return out;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

        dest.writeParcelable(this, flags);
        
    }

	
	
}
