package me.kaipi.gpsexample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	int interval = 1;
	private BroadcastReceiver locationReceiver = new LocationReceiver();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter("me.kaipi.gpsexample.LocationBroadcast");
		this.registerReceiver(this.locationReceiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(this.locationReceiver);
	}
	
	public void startService(View v) {
		Intent serviceIntent = new Intent(this, GpsService.class);
		serviceIntent.putExtra("broadcast_interval", this.interval);
		this.interval = this.interval % 5 + 1;
		this.startService(serviceIntent);
	}
	
	public void stopService(View v) {
		Intent serviceIntent = new Intent(this, GpsService.class);
		this.stopService(serviceIntent);
	}
	
	private class LocationReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			Location location = intent.getParcelableExtra("location");
			Log.d(TAG, "Location received: " + location.getTime());
		}
	}

}
