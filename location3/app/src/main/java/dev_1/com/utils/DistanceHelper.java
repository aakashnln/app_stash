package dev_1.com.utils;


import com.google.android.gms.maps.model.LatLng;

/**
 * Created by vipul on 13/4/16.
 */
public class DistanceHelper {

    public static double deg2rad(double x){
        return x * Math.PI / 180;
    }

    // Haversine formula:
    public static double getDistance(LatLng p1, LatLng p2){
        float R = 6378137F;
        double dLat = deg2rad(p2.latitude - p1.latitude);
        double dLong = deg2rad(p2.longitude - p1.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(deg2rad(p1.latitude)) * Math.cos(deg2rad(p2.longitude)) *
                        Math.sin(dLong / 2) * Math.sin(dLong / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double d = R * c;
        return d; // returns the distance in meter
    }
}
