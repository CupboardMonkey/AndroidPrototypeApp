package com.example.androidprototypeapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;


public class LightSwitchActivity extends Activity {

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_light_switch);

		SeekBar seekbar = (SeekBar) findViewById(R.id.select_brightness);
		
		final WindowManager.LayoutParams lp = getWindow().getAttributes();	
		seekbar.setProgress(100);
		
		seekbar.setOnSeekBarChangeListener( new OnSeekBarChangeListener()
		{
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
				// TODO Auto-generated method stub
				if(progress == 0) {
					progress = 1;
				}
			
				
				double brightness = (progress/100d);
				lp.screenBrightness = (float) brightness;
				getWindow().setAttributes(lp);
			}

			public void onStartTrackingTouch(SeekBar seekBar)
			{
				// TODO Auto-generated method stub
			}

			public void onStopTrackingTouch(SeekBar seekBar)
			{
				// TODO Auto-generated method stub
			}
		});


	}

	public void onToggleClicked(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		ToggleButton b = ((ToggleButton) view);
		
		
		if (on) {
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			float brightness=1.0f;
			lp.screenBrightness = brightness;
			getWindow().setAttributes(lp);
			b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.light_on, 0, R.drawable.blank, 0);
		} else {
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			float brightness=0.1f;
			lp.screenBrightness = brightness;
			getWindow().setAttributes(lp);
			b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.light_icon, 0, R.drawable.blank, 0);
		}
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
}
