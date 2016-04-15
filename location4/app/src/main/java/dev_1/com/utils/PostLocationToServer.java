package dev_1.com.utils;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import dev_1.com.code.MainApplication;

/**
 * Created by vipul on 15/4/16.
 */
public class PostLocationToServer {

    public static final String TAG = PostLocationToServer.class.getSimpleName();

    public static void postLocation(final MainApplication gpsApp, final Location location){
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
        VolleySingleton.getInstance(gpsApp).addToRequestQueue(postRequest);
        Log.i(TAG, "New POST location update");
    }
}
