package com.example.androidprototypeapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BTConnection {

	public BluetoothDevice device;
	public BluetoothSocket socket;
	public String name;
	
	public BTConnection(BluetoothDevice device, BluetoothSocket socket, String name) {
		this.device = device;
		this.socket = socket;
		this.name = name;
	}
	
	public BluetoothDevice getDevice() {
		return device;
	}
	
	public BluetoothSocket getSocket() {
		return socket;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
