//package dev_1.com.code;
//
//import android.content.Context;
//
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//
///**
// * Created by vipul on 13/4/16.
// */
//public class ActivityProvider {
//
//    private GoogleApiClient mGoogleApiClient;
//
//    public ActivityProvider(Context context) {
//        mGoogleApiClient = new GoogleApiClient.Builder(context)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//
//        mLocationCallback = callback;
//
//        // Create the LocationRequest object
//        mLocationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(INTERVAL)        // 10 seconds, in milliseconds
//                .setFastestInterval(FASTEST_INTERVAL) // 1 second, in milliseconds
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setSmallestDisplacement(SMALLEST_DISPLACEMENT);
//
//        mContext = context;
//    }
//}
