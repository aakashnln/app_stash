//package dev_1.com.code;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.graphics.Color;
//import android.location.Location;
//import android.os.Bundle;
//import android.support.v4.app.FragmentActivity;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.Polyline;
//import com.google.android.gms.maps.model.PolylineOptions;
//
//import java.util.ArrayList;
//
//import dev_1.com.utils.DistanceHelper;
//
//public class MainActivity extends FragmentActivity implements LocationProvider.LocationCallback {
//
//    public static final String TAG = MapsActivity.class.getSimpleName();
//
//    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
//
//    private LocationProvider mLocationProvider;
//
//    private ArrayList<LatLng> points; //added
//    Polyline line; //added
//
//    private Button drive_btn;
//    Boolean hasStarted = false;
//
//    double distance = 0.0F;
//    LatLng startingPoint = null;
//    LatLng stopingPoint = null;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.a_1);
//        setUpMapIfNeeded();
//
//        mLocationProvider = new LocationProvider(this, this);
//
//        points = new ArrayList<LatLng>(); //added
//
//        drive_btn = (Button)findViewById(R.id.drive_btn);
//        drive_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!hasStarted) {
//                    hasStarted = true;
//                    drive_btn.setText("Stop trip");
//                    stopService(new Intent(this, GpsLoggerService.class));
//                } else {
//                    hasStarted = false;
//                    drive_btn.setText("Start trip "+Double.toString(distance)+" meters");
//                    stopingPoint = startingPoint;
//                    startingPoint = null;
//                    distance = 0.0F;
//                }
//            }
//        });
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        setUpMapIfNeeded();
//        mLocationProvider.connect();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mLocationProvider.disconnect();
//    }
//
//    /**
//     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
//     * installed) and the map has not already been instantiated.. This will ensure that we only ever
//     * call {@link #setUpMap()} once when {@link #mMap} is not null.
//     * <p/>
//     * If it isn't installed {@link SupportMapFragment} (and
//     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
//     * install/update the Google Play services APK on their device.
//     * <p/>
//     * A user can return to this FragmentActivity after following the prompt and correctly
//     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
//     * have been completely destroyed during this process (it is likely that it would only be
//     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
//     * method in {@link #onResume()} to guarantee that it will be called.
//     */
//    private void setUpMapIfNeeded() {
//        // Do a null check to confirm that we have not already instantiated the map.
//        if (mMap == null) {
//            // Try to obtain the map from the SupportMapFragment.
//            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
//                    .getMap();
//            // Check if we were successful in obtaining the map.
//            if (mMap != null) {
//                setUpMap();
//            }
//        }
//    }
//
//    /**
//     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
//     * just add a marker near Africa.
//     * <p/>
//     * This should only be called once and when we are sure that {@link #mMap} is not null.
//     */
//    private void setUpMap() {
////        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
//        mMap.setMyLocationEnabled(true);
//    }
//
//    public void handleNewLocation(Location location) {
//        Log.d(TAG, location.toString());
//
//        double currentLatitude = location.getLatitude();
//        double currentLongitude = location.getLongitude();
//        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
////        Toast.makeText(this, "yo",Toast.LENGTH_SHORT).show();
//        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
//        MarkerOptions options = new MarkerOptions()
//                .position(latLng)
//                .title("I am here!");
////        mMap.addMarker(options);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.5F));
//
//        if(hasStarted) {
//            points.add(latLng); //added
//            reDrawLine(); //added
//
//            if (startingPoint==null)
//            {
//                startingPoint = latLng;
//                distance += DistanceHelper.getDistance(startingPoint, latLng);
//            }
//            else{
//                distance += DistanceHelper.getDistance(startingPoint,latLng);
//                startingPoint = latLng;
//            }
//            Toast.makeText(this,Float.toString(location.getSpeed()),Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//    private void reDrawLine(){
//
//        mMap.clear();  //clears all Markers and Polylines
//
//        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
//        for (int i = 0; i < points.size(); i++) {
//            LatLng point = points.get(i);
//            options.add(point);
//        }
//        addMarker(); //add Marker in current position
//        line = mMap.addPolyline(options); //add Polyline
//
////        LatLngBounds.Builder builder = new LatLngBounds.Builder();
////        for (LatLng ll : mCapturedLocations) {
////            com.google.android.gms.maps.model.LatLng mapPoint =
////                    new com.google.android.gms.maps.model.LatLng(ll.lat, ll.lng);
////            builder.include(mapPoint);
////            polyline.add(mapPoint);
////        }
////        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0))
//    }
//
//    private void addMarker() {
//        MarkerOptions options = new MarkerOptions();
//
//        // following four lines requires 'Google Maps Android API Utility Library'
//        // https://developers.google.com/maps/documentation/android/utility/
//        // I have used this to display the time as title for location markers
//        // you can safely comment the following four lines but for this info
////        IconGenerator iconFactory = new IconGenerator(this);
////        iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
//        // options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(mLastUpdateTime + requiredArea + city)));
////        options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(requiredArea + ", " + city)));
////        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
////        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
////        options.position(currentLatLng);
////        Marker mapMarker = mMap.addMarker(options);
////        long atTime = mCurrentLocation.getTime();
////        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(atTime));
////        String title = mLastUpdateTime.concat(", " + requiredArea).concat(", " + city).concat(", " + country);
////        mapMarker.setTitle(title);
//
//
////        TextView mapTitle = (TextView) findViewById(R.id.textViewTitle);
////        mapTitle.setText(title);
//
//        Log.d(TAG, "Marker added.............................");
////        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,
////                13));
//        Log.d(TAG, "Zoom done.............................");
//    }
//
//    @Override
//    public void onDestroy(){
//        hasStarted = false;
//        super.onDestroy();
//    }
//}
