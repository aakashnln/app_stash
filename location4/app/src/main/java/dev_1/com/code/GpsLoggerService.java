package dev_1.com.code;

/**
 * Created by vipul on 14/4/16.
 */
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import dev_1.com.utils.PostLocationToServer;

public class GpsLoggerService extends Service {
    private static final String TAG = GpsLoggerService.class.getSimpleName();

    private MainApplication gpsApp;
//    private GpsManager gpsManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.gpsApp = (MainApplication) getApplication();
//        this.gpsManager = gpsApp.getGpsManager();
        this.registerReceiver(singleLocationUpdateReceiver, new IntentFilter(IntentCodes.ACTION_LOCATION));
        Log.d(TAG, "onCreated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStarted");

//        start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroyed");
        this.unregisterReceiver(singleLocationUpdateReceiver);
//        stop();
    }

//    private void start(){
//        if(!gpsApp.isON() && !gpsManager.isGPSActive()){
//            gpsApp.setON(true);
//            gpsManager.startLocationUpdates();
//        }
//    }
//
//    private void stop() {
//        if(gpsApp.isON() && gpsManager.isGPSActive()){
//            gpsApp.setON(false);
//            gpsManager.stopLocationProviders();
//        }
//    }

    protected BroadcastReceiver singleLocationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Receives a single location update from singleLocationPI
            Location location = (Location)intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);;

            PostLocationToServer.postLocation(gpsApp,location);
            // Do routine stuff for location
//            if (gpsLocationListener != null && location != null)
//                gpsLocationListener.onLocationChanged(location);

            // Remove updates for location manager to conserve batery
//            mLocationManager.removeUpdates(singleLocationPI);

            // Unregister single update receiver since we register it when the alarm kicks off
//            mAppContext.unregisterReceiver(singleLocationUpdateReceiver);
        }
    };

}
