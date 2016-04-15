package dev_1.com.code;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import dev_1.com.utils.DeviceUUIDFactory;
import dev_1.com.utils.DistanceHelper;
import dev_1.com.utils.LocationDatabaseHelper;

/**
 * Created by vipul on 14/4/16.
 */
public class MainApplication extends Application implements LocationProvider.LocationCallback{
    private static final String TAG = MainApplication.class.getSimpleName();

    public static String LOCATION_NEW_URL;
    public static String REGISTER_URL;
    public static String LOGIN_URL;

    private DeviceUUIDFactory uuidFactory;
    private String deviceId;

//    private GpsManager gpsManager;
    private boolean bServiceEnabled;

    private SharedPreferences prefs;

    private boolean bLoggedIn = false;
    private String username;

    public LocationProvider mLocationProvider;



    @Override
    public void onCreate() {
        super.onCreate();
        setURLs();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        checkUser();

        Log.i(TAG, "onCreated");
        uuidFactory = new DeviceUUIDFactory(getApplicationContext());
        deviceId = uuidFactory.getDeviceUuid().toString();

//        gpsManager = GpsManager.get(getApplicationContext());
        bServiceEnabled = false;

        mLocationProvider = new LocationProvider(getApplicationContext(), this);

        //exportDB();
    }

    @Override
    public void onTerminate(){
        super.onTerminate();
        Log.i(TAG, "onTerminated");
    }

//    public GpsManager getGpsManager() {
//        return gpsManager;
//    }

    public LocationProvider getLocationProvider(){
        return mLocationProvider;
    }

    public void gApiConnect(){
        mLocationProvider.connect();
    }

    public void gApiDisconnect(){
        mLocationProvider.disconnect();
    }

    public boolean isON(){
        return bServiceEnabled;
    }

    public void setON(boolean b){
        bServiceEnabled = b;
    }

    public String getUUID(){
        return deviceId;
    }

    public void setURLs(){
        LOCATION_NEW_URL = getResources().getString(R.string.NEW_LOCATION_URL);
        REGISTER_URL = getResources().getString(R.string.REGISTER_URL);
        LOGIN_URL =  getResources().getString(R.string.LOGIN_URL);
    }

    public boolean isLoggedIn() {
        return bLoggedIn;
    }

    public String getUsername() { return username; }

    private void checkUser() {
        username = prefs.getString("username", null);
        if(username == null) {
            bLoggedIn = false;
        }
        else {
            bLoggedIn = true;
        }
    }

//    public void showDialog(String title, String message, Activity activity){
//        AlertDialog dialog = DialogBoxFactory.setDialog(title, message, activity);
//        dialog.show();
//    }

    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void setLoggedIn(String u){
        username = u;
        bLoggedIn = true;
    }

    public boolean isUsernameValid(EditText etxt, Activity activity){
        boolean valid = false;
        String value = String.valueOf(etxt.getText());
        if(value != null && value.trim().length() > 0) {
            int length = value.trim().length();
            if(length >= 6 && length <= 16) {
                valid = true;
            }
        }

        if(!valid){
            String message = getResources().getString(R.string.invalid_username);
//            showDialog("Error", message, activity);
        }

        return valid;
    }

    public boolean isEmailValid(EditText etxt, Activity activity){
        boolean valid = false;
        String value = String.valueOf(etxt.getText());
        if(value != null && value.trim().length() > 0) {
            valid = true;
        }

        if(!valid){
            String message = getResources().getString(R.string.invalid_email);
//            showDialog("Error", message, activity);
        }

        return valid;
    }

    public boolean isPasswordValid(EditText etxt, Activity activity){
        boolean valid = true;
        String password = String.valueOf(etxt.getText());

        if(password.isEmpty() || password.length() < 8){
            valid = false;
        }

        if(!valid){
            String message =  getResources().getString(R.string.invalid_password);
//            showDialog("Error", message, activity);
        }

        return valid;
    }

    public void saveLogin(String username, String email, String pin){
        final String uuid = getUUID();

        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("uuid", uuid);
        editor.putString("pin", pin);
        editor.commit();

        setLoggedIn(username);
    }

    public boolean isWiFiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        }
        return networkInfo == null ? false : networkInfo.isConnected();
    }

    private void exportDB(){
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source=null;
        FileChannel destination=null;
        String currentDBPath = "/data/"+ "dev_1.com.code/databases/" + LocationDatabaseHelper.DB_NAME;
        String backupDBPath = LocationDatabaseHelper.DB_NAME;
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Log.d(TAG, "DB Exported!");
        } catch(Exception e) {
            Log.d(TAG, "DB Export Fail " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void handleNewLocation(Location location) {
        Log.i(TAG, location.toString());

//        double currentLatitude = location.getLatitude();
//        double currentLongitude = location.getLongitude();
//        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
//        Toast.makeText(this, "yo",Toast.LENGTH_SHORT).show();
        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
//        MarkerOptions options = new MarkerOptions()
//                .position(latLng)
//                .title("I am here!");
//        mMap.addMarker(options);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.5F));
//        mLocationProvider.broadcastLocation(location);
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
    }
}