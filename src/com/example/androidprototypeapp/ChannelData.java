package com.example.androidprototypeapp;

import java.util.ArrayList;

public class ChannelData {

	int id;
	ArrayList<Integer> code = new ArrayList<Integer>();
	String label;
	
	public ChannelData(int id, ArrayList<Integer> code, String label) {
		this.id = id;
		this.code = code;
		this.label = label;
	}
	
	public int getId() {
		return id;
	}
	
	public ArrayList<Integer> getCode() {
		return code;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setCode(ArrayList<Integer> code) {
		this.code = code;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
}
