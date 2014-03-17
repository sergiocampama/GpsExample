package me.kaipi.gpsexample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class GpsService extends Service implements LocationListener {

	private static final String TAG = "GpsService";
	final private static int MS = 1000, MINUTES = 20;
	private int ACCEPTABLE_ACCURACY_METERS = 14;
	private int broadcast_interval = 30 * MINUTES;
	private Timer broadcast_timer = null;
	private LocationManager locationManager = null;
	private Criteria criteria = null;
	private Handler handler = null;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		this.locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		this.handler = new Handler();
		this.setupLocationCriteria();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		
		if (this.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED &&
			this.checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED) {
			Log.e(TAG, "Not permitted to perform location updates.");
			Log.e(TAG, "Add android.permission.ACCESS_FINE_LOCATION or android.permission.ACCESS_COARSE_LOCATION to the Manifest.");
			this.stopSelf();
		} else {
			this.configure(intent.getExtras());
		}
		
		return START_STICKY;
	}
	
	private void configure(Bundle extras) {
		this.broadcast_interval = extras.getInt("broadcast_interval", 30) * MINUTES * MS;
		
		if (broadcast_timer != null) {
			this.broadcast_timer.cancel();
		}
		
		this.broadcast_timer = new Timer();
		
		this.broadcast_timer.scheduleAtFixedRate(new TimerTask(){
			public void run() {
				requestBroadcastGpsPosition();
			}
		}, 0, this.broadcast_interval);
		
	}
	
	private void requestBroadcastGpsPosition() {
		Log.d(TAG, "requestBroadcastGpsPosition");
		String provider = this.locationManager.getBestProvider(this.criteria, true);
		this.locationManager.requestLocationUpdates(provider, 0, 0, this, this.handler.getLooper());
	}
	
	private void setupLocationCriteria() {
		this.criteria = new Criteria();
		criteria.setAltitudeRequired(false);
		criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		criteria.setSpeedRequired(true);
		criteria.setBearingAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setHorizontalAccuracy(Criteria.ACCURACY_FINE);
		criteria.setVerticalAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setSpeedAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(false);
		criteria.setBearingRequired(false);
	}
	
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		if (this.broadcast_timer != null)
			this.broadcast_timer.cancel();
		this.locationManager.removeUpdates(this);
	}

	public void onLocationChanged(Location location) {
		Log.d(TAG, "Location changed, accuracy is " + location.getAccuracy());
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy kk:mm:ss z");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String dateString = dateFormat.format(new Date(location.getTime()));
		Log.d(TAG, "Location changed, time is " + dateString);
		Log.d(TAG, "Provider is " + location.getProvider());
		
		if (this.isLocationGoodEnough(location)) {
			this.broadcastLocation(location);
			this.locationManager.removeUpdates(this);
		}
	}
	
	private boolean isLocationGoodEnough(Location location) {
		//Just a first approach, definitely needs more thought.
		if (location.getProvider().contentEquals(LocationManager.GPS_PROVIDER) ||
			location.getProvider().contentEquals("fused")) {
			if (location.getAccuracy() < ACCEPTABLE_ACCURACY_METERS) {
				Log.d(TAG, "Good enough, stop");
				return true;
			} else {
				Log.d(TAG, "Accuracy is " + location.getAccuracy() + ". Waiting for more accuracy.");
			}
		} else {
			Log.d(TAG, "Not from GPS, good enough");
			Log.d(TAG, "Accuracy is " + location.getAccuracy());
			return true;
		}
		return false;
	}
	
	private void broadcastLocation(Location location) {
		Intent intent = new Intent();
		intent.setAction("me.kaipi.gpsexample.LocationBroadcast");
		intent.putExtra("location", location);
		sendBroadcast(intent);
	}
	
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "Provider disabled " + provider);
	}

	public void onProviderEnabled(String provider) {
		Log.d(TAG, "Provider enabled " + provider);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, "Status changed " + status);
	}

}
