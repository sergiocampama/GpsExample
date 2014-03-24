package me.kaipi.gpsexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, GpsService.class);
		serviceIntent.putExtra(GpsService.BROADCAST_INTERVAL, GpsService.BROADCAST_INTERVAL_DEFAULT);
		context.startService(serviceIntent);
	}
	
}
