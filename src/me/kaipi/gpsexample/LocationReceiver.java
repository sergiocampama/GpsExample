package me.kaipi.gpsexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

public class LocationReceiver extends BroadcastReceiver {
	private static final String TAG = "LocationReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Location location = intent.getParcelableExtra("location");
		Log.d(TAG, "Location received: " + location.getTime());
		Log.d(TAG, "Thread id: " + Thread.currentThread().getId());
	}

}
