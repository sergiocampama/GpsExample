package me.kaipi.gpsexample;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	int interval = 1;
	private BroadcastReceiver locationReceiver = new LocationReceiver();
	private Timer serviceTextTimer = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setServiceText();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(GpsService.LOCATION_BROADCAST_ACTION);
		this.registerReceiver(this.locationReceiver, filter);
		this.serviceTextTimer = new Timer();
		this.serviceTextTimer.scheduleAtFixedRate(new TimerTask(){
			@Override public void run() {
				runOnUiThread(new Runnable(){ @Override public void run() { setServiceText(); } });
			}			
		}, 0, 1000);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(this.locationReceiver);
		this.serviceTextTimer.cancel();
	}
	
	public void startService(View v) {
		Intent serviceIntent = new Intent(this, GpsService.class);
		serviceIntent.putExtra(GpsService.BROADCAST_INTERVAL, this.interval);
		this.interval = this.interval % 5 + 1;
		this.startService(serviceIntent);
		Log.d(TAG, "Thread id: " + Thread.currentThread().getId());
	}
	
	public void stopService(View v) {
		Intent serviceIntent = new Intent(this, GpsService.class);
		this.stopService(serviceIntent);
	}
	
	private void setServiceText() {
		TextView serviceText = (TextView)findViewById(R.id.serviceText);
		if (isServiceRunning())
			serviceText.setText(getString(R.string.serviceRunning));
		else
			serviceText.setText(getString(R.string.serviceNotRunning));
	}
	
	private boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
	        if (GpsService.class.getName().equals(service.service.getClassName()))
	            return true;
	    return false;
	}
	
	private class LocationReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			Location location = intent.getParcelableExtra(GpsService.LOCATION_EXTRA);
			Log.d(TAG, "Location received: " + location.getTime());
			Log.d(TAG, "Thread id: " + Thread.currentThread().getId());
		}
	}

}
