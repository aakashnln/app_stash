package dev_1.com.code;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;

import dev_1.com.utils.PostLocationToServer;
import dev_1.com.utils.VolleySingleton;

/**
 * Created by vipul on 04/11/16.
 */
public class LocationProvider implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public abstract interface LocationCallback {
        public void handleNewLocation(Location location);
    }

    public static final String TAG = LocationProvider.class.getSimpleName();

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static final float SMALLEST_DISPLACEMENT = 0.0F; //five of a meter
    private static final long INTERVAL = 2000 * 1; //1 minute
    private static final long FASTEST_INTERVAL = 500 * 1; // 1 minute

    private int counter = 0;

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private MainApplication gpsApp;

    public LocationProvider(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
//                .addApi(ActivityRecognition.API) // added to detect activity
                .build();


        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(INTERVAL)        // 10 seconds, in milliseconds
                .setFastestInterval(FASTEST_INTERVAL) // 1 second, in milliseconds
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(SMALLEST_DISPLACEMENT);

        mContext = context;

        gpsApp = (MainApplication) mContext.getApplicationContext();
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");

//        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//        if (location == null) {
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//        }
//        else {
////            mLocationCallback.handleNewLocation(location);
//        }
    }

    public void getLastLocation(){
        Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        broadcastLocation(loc);
        PostLocationToServer.postLocation(gpsApp,loc);
        Log.i(TAG, "Broadcast location connected."+loc.toString());
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution() && mContext instanceof Activity) {
            try {
                Activity activity = (Activity)mContext;
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            /*
             * Thrown if Google Play services canceled the original
             * PendingIntent
             */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "New location update");
//        mLocationCallback.handleNewLocation(location);
//        gpsApp.sendBroadcast(broadcast);
//        broadcastLocation
        broadcastLocation(location);
    }

//    public void getActivity(){ // Added to verify if the driver is driving
//        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
//                mGoogleApiClient,
//                FASTEST_INTERVAL,
//                getActivityDetectionPendingIntent(),
//        ).setResultCallback(this);
//    }

    // new code added
    /**
     * Broadcast the location to receivers. Afterwards, insert the location to the database and post the location via HTTP.
     *
     * @param location The accepted location from onLocationChanged.
     */
    private void broadcastLocation(Location location) {
        if(location != null){
            counter++;
            Intent broadcast = new Intent(IntentCodes.ACTION_LOCATION);
            broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
            broadcast.putExtra("counter", counter);
            mContext.sendBroadcast(broadcast);

            insertLocation(location);
//            PostLocationToServer.postLocation(gpsApp,location);
        }
    }

    /**
     * Returns the number of location updates received from location listeners.
     */
    public int getCounter() {
        return counter;
    }

    public void insertLocation(Location location){
//        mDatabaseHelper.insertLocation(location);
    }

    public void postLocation(final Location location){
        if(location == null)
            return;

        final String url = gpsApp.LOCATION_NEW_URL;

        final String timestamp = Long.toString(location.getTime());
        final String latitude = Double.toString(location.getLatitude());
        final String longitude = Double.toString(location.getLongitude());
        final String speedInKPH = Float.toString(location.getSpeed());
        final String heading = Float.toString(location.getBearing());
        final String provider = location.getProvider();
//        final String timeInterval = Integer.toString((int) minTimeInMilliseconds);
//        final String distanceInterval = Integer.toString((int) minDistanceInMeters);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response){
                        VolleyLog.v("Response:%n %s", response);
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.d(TAG, "Error on " + url);
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("uuid", gpsApp.getUUID());
                params.put("gps_timestamp", timestamp);
                params.put("gps_latitude", latitude);
                params.put("gps_longitude", longitude);
                params.put("gps_speed", speedInKPH);
                params.put("gps_heading", heading);
                params.put("provider", provider);
//                params.put("time_interval", timeInterval);
//                params.put("distance_interval", distanceInterval);

                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 1, 1.0f));
        VolleySingleton.getInstance(mContext).addToRequestQueue(postRequest);
        Log.i(TAG, "New POST location update");
    }
}
