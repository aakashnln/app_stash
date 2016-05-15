package dev_1.com.code;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev_1.com.utils.DistanceHelper;
import dev_1.com.utils.PostLocationToServer;
import dev_1.com.utils.VolleySingleton;

public class MapsActivity2 extends FragmentActivity implements LocationProvider.LocationCallback {

    public static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

//    private LocationProvider mLocationProvider;

    private ArrayList<LatLng> points; //added
    Polyline line; //added

    private Button drive_btn;
    Boolean hasStarted = false;

    double distance = 0.0F;
    LatLng startingPoint = null;
    LatLng stopingPoint = null;

    private ProgressDialog pd;

    private MainApplication gpsApp;

    private String status;//TODO move this variable to MainApplication class

    private PageIndicator mPageIndicator;

    private String city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_1);
//        mLocationProvider = new LocationProvider(this, this);
        status = gpsApp.getStatus();

        points = new ArrayList<LatLng>(); //added

        drive_btn = (Button) findViewById(R.id.drive_btn);
        drive_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasStarted) {
                    hasStarted = true;
                    drive_btn.setText("Stop trip");
                    startService(new Intent(gpsApp.getApplicationContext(), GpsLoggerService.class));
                    gpsApp.mLocationProvider.getLastLocation();
                } else {
                    hasStarted = false;
                    drive_btn.setText("Start trip " + Double.toString(distance) + " meters");
                    stopService(new Intent(gpsApp.getApplicationContext(), GpsLoggerService.class));
                    stopingPoint = startingPoint;
                    startingPoint = null;
                    distance = 0.0F;
                }
            }
        });

        this.gpsApp = (MainApplication) getApplication();

        getUpdates();

        if(!gpsApp.DEBUG){
            switch (status){
                case "4":
                    drive_btn.setClickable(true);
                    break;
                case "3":
                    drive_btn.setClickable(false);
                    showDialog("Info","Hi it appears you have not part of any campaign,\n Join and start earning.");
                    break;
                default:
                    drive_btn.setClickable(false);
                    showDialog("Info","Your account has not been activated yet, once activated you can join campaigns");
                    break;
            }
        }

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        /** set the adapter for ViewPager */
        mViewPager.setAdapter(new SamplePagerAdapter(
                getSupportFragmentManager()));

//        pager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //TODO, hit the server to get the earnings if position is 2
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        mPageIndicator = (PageIndicator) findViewById(R.id.indicator);
        mPageIndicator.setViewPager(mViewPager);

        city = gpsApp.getCity();
        if(city==null){

        }
    }

    private void getUpdates() { // get updates when the user gets logged in , like user status, earnings etc, may be earning not here

        pd = ProgressDialog.show(this, "Please Wait...", "Loading Data");

        final String url = gpsApp.STATUS_URL;
        final String username = gpsApp.getUsername();
        final String uuid = gpsApp.getUUID();

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("username", username);
            jsonObject.put("uuid", uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST,
                url,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.v(TAG, "Status Response: " + response.toString());
                        pd.dismiss();

                        try {
                            boolean valid = response.getBoolean("valid");
                            String message = "Data loaded successfully!";
                            if (valid) {
                                status = response.getString("status");
                                switch (status){
                                    case "1":
                                        message = message+"/n You account need to be verified, kindly give us a missed call on $$$";
                                        break;
                                }
                                Log.e(TAG, message);

                                final String status = response.getString("status");

                                AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity2.this);
                                dialog.setTitle("Info");
                                dialog.setMessage(message);
                                dialog.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                                        gpsApp.saveStatus(username,email, phnum);
                                        gpsApp.saveStatus(status);
//                                        done();
                                    }
                                });

                                dialog.show();
                            } else {
                                message = response.getString("errors");
                                showDialog(message);
                            }

                        } catch (JSONException e) {
                            String message = "Cannot parse response from " + url + "(" + response.toString() + ")";
                            showDialog(message);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        String message = "A network error has occurred on " + url + "(" + error.toString() + ")";
                        showDialog(message);
                    }
                });

        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        gpsApp.gApiConnect();
        this.registerReceiver(locationUpdateReceiver, new IntentFilter(IntentCodes.ACTION_LOCATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mLocationProvider.disconnect();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    public void handleNewLocation(Location location) {
        Log.i(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
//        Toast.makeText(this, "yo",Toast.LENGTH_SHORT).show();
        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("My location");
//        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.5F));
//        gpsApp.mLocationProvider.broadcastLocation(location);
        if(hasStarted) {
            points.add(latLng); //added
            reDrawLine(); //added

            if (startingPoint==null)
            {
                startingPoint = latLng;
                distance += DistanceHelper.getDistance(startingPoint, latLng);
            }
            else{
                distance += DistanceHelper.getDistance(startingPoint,latLng);
                startingPoint = latLng;
            }
            gpsApp.showToast(Float.toString(location.getSpeed()));
//            Toast.makeText(this,Float.toString(location.getSpeed()),Toast.LENGTH_SHORT).show();
        }

    }

    private void reDrawLine(){

        mMap.clear();  //clears all Markers and Polylines

        PolylineOptions options = new PolylineOptions().width(15).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }
        addMarker(); //add Marker in current position
        line = mMap.addPolyline(options); //add Polyline

//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//        for (LatLng ll : mCapturedLocations) {
//            com.google.android.gms.maps.model.LatLng mapPoint =
//                    new com.google.android.gms.maps.model.LatLng(ll.lat, ll.lng);
//            builder.include(mapPoint);
//            polyline.add(mapPoint);
//        }
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0))
    }

    private void addMarker() {
        MarkerOptions options = new MarkerOptions();

        // following four lines requires 'Google Maps Android API Utility Library'
        // https://developers.google.com/maps/documentation/android/utility/
        // I have used this to display the time as title for location markers
        // you can safely comment the following four lines but for this info
//        IconGenerator iconFactory = new IconGenerator(this);
//        iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
        // options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(mLastUpdateTime + requiredArea + city)));
//        options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(requiredArea + ", " + city)));
//        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
//        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//        options.position(currentLatLng);
//        Marker mapMarker = mMap.addMarker(options);
//        long atTime = mCurrentLocation.getTime();
//        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(atTime));
//        String title = mLastUpdateTime.concat(", " + requiredArea).concat(", " + city).concat(", " + country);
//        mapMarker.setTitle(title);


//        TextView mapTitle = (TextView) findViewById(R.id.textViewTitle);
//        mapTitle.setText(title);

        Log.d(TAG, "Marker added.............................");
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,
//                13));
        Log.d(TAG, "Zoom done.............................");
    }

    @Override
    public void onDestroy(){
        hasStarted = false;
        super.onDestroy();
        gpsApp.gApiDisconnect();
        this.unregisterReceiver(locationUpdateReceiver);
    }

    protected BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Receives a single location update from singleLocationPI
            Location location = (Location)intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);;
            try{
                handleNewLocation(location);
                if (city==null){
                    Geocoder gcd = new Geocoder(gpsApp.getBaseContext(), Locale.getDefault());
                    List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses.size() > 0)
                    {
                        city = addresses.get(0).getLocality();
                        gpsApp.setCity(city);
                        gpsApp.showToast(city);
//                        System.out.println(addresses.get(0).getLocality());
                    }

                }
//                abortBroadcast();
            }catch (Exception e){

            }
//            PostLocationToServer.postLocation(gpsApp,location);
            // Do routine stuff for location
//            if (gpsLocationListener != null && location != null)
//                gpsLocationListener.onLocationChanged(location);

            // Remove updates for location manager to conserve batery
//            mLocationManager.removeUpdates(singleLocationPI);

            // Unregister single update receiver since we register it when the alarm kicks off
//            mAppContext.unregisterReceiver(singleLocationUpdateReceiver);
        }
    };


    private void showDialog(String message){
        Log.e(TAG, message);
        gpsApp.showDialog("Error", message, MapsActivity2.this);
    }
    private void showDialog(String title,String message){
        Log.e(TAG, message);
        gpsApp.showDialog(title, message, MapsActivity2.this);
    }

    /** Defining a FragmentPagerAdapter class for controlling the fragments to be shown when user swipes on the screen. */
    public class SamplePagerAdapter extends FragmentPagerAdapter {

        public SamplePagerAdapter(FragmentManager fm) {
            super(fm);
        }
    //fragmentRegister.textViewLanguage.setText("hello mister how do you do");
        @Override
        public Fragment getItem(int position) {
            /** Show a Fragment based on the position of the current screen */
            if (position == 0) {
                return new SampleFragment();// blank fragment
            } else
                return new SampleFragmentTwo();// fragment with the earnings
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }
}
