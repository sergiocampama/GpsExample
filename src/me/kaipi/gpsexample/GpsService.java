package me.kaipi.gpsexample;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class GpsService extends Service implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	public static final String LOCATION_BROADCAST_ACTION = "me.kaipi.gpsexample.LocationBroadcast";
	public static final String LOCATION_EXTRA = "location";
	public static final String BROADCAST_INTERVAL = "broadcast_interval";
	public static final Integer BROADCAST_INTERVAL_DEFAULT = 5;
	
	private static final String TAG = "GpsService";
	final private static int SECONDS = 1000, MINUTES = 60 * SECONDS;
	private static final int FASTEST_INTERVAL = 30 * SECONDS;
	private LocationRequest locationRequest = null;
	private LocationClient locationClient = null;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		this.locationRequest = new LocationRequest();
		if (hasFineLocationPermission())
			this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		else if (hasCoarseLocationPermission())
			this.locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
		else
			this.locationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
		
		this.locationRequest.setFastestInterval(FASTEST_INTERVAL);
		this.locationRequest.setInterval(FASTEST_INTERVAL);
		this.locationClient = new LocationClient(this, this, this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		
		if (!hasFineLocationPermission() && !hasCoarseLocationPermission() ||
				GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
			Log.e(TAG, getString(R.string.gpsServiceStartError));
			this.stopSelf();
			return START_NOT_STICKY;
		}
		
		if (intent != null) {
			if (this.locationClient.isConnected() || this.locationClient.isConnecting())
				this.locationClient.disconnect();
			
			Integer broadcast_interval = intent.getIntExtra(BROADCAST_INTERVAL, BROADCAST_INTERVAL_DEFAULT); 
			this.locationRequest.setInterval(broadcast_interval * MINUTES);
			
			this.locationClient.connect();
		}
		
		return START_STICKY;
	}
	
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		if (this.locationClient.isConnected()) {
			this.locationClient.removeLocationUpdates(this);
		}
		this.locationClient.disconnect();
		super.onDestroy();
	}

	//Google Play Location Services
	
	@Override
	public void onConnected(Bundle dataBundle) {
		Log.d(TAG, "Play: onConnected");
		
		this.locationClient.requestLocationUpdates(this.locationRequest, this);
	}
	
	@Override
	public void onDisconnected() {
		Log.d(TAG, "Play: onDisconnected");
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d(TAG, "Play: onConnectionFailed");
	}
	
	@Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged " + location.toString());
        broadcastLocation(location);
    }
	
	private boolean hasFineLocationPermission() {
		return this.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED;
	}
	
	private boolean hasCoarseLocationPermission() {
		return this.checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED;
	}
	
	private void broadcastLocation(Location location) {
		Intent intent = new Intent();
		intent.setAction("me.kaipi.gpsexample.LocationBroadcast");
		intent.putExtra("location", location);
		sendBroadcast(intent);
	}

}
