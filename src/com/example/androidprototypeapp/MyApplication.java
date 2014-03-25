package com.example.androidprototypeapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

class MyApplication extends Application{
	BluetoothSocket sock;
	InputStream in;
	OutputStream out;

public BluetoothSocket getSocket() {
	return sock;
}

public InputStream getInStream() {
	return in;
}

public OutputStream getOutStream() {
	return out;
}

public void setSocket(BluetoothSocket sock) {
	this.sock = sock;
	try {
		this.in = sock.getInputStream();
		this.out = sock.getOutputStream();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

}