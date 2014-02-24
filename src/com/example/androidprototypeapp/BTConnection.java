package com.example.androidprototypeapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BTConnection {

	public BluetoothDevice device;
	public BluetoothSocket socket;
	
	public BTConnection(BluetoothDevice device, BluetoothSocket socket) {
		this.device = device;
		this.socket = socket;
	}
	
	public BluetoothDevice getDevice() {
		return device;
	}
	
	public BluetoothSocket getSocket() {
		return socket;
	}
}
