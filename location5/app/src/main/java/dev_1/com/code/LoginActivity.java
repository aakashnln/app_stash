package dev_1.com.code;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import dev_1.com.utils.VolleySingleton;

/**
 * Created by vipul on 3/5/16.
 */
public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";

    private MainApplication gpsApp;

    private EditText etxtEmail;
    private EditText etxtPassword;

    private ProgressDialog pd;
    private Button submit_btn;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make this activity full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        this.gpsApp = (MainApplication) getApplication();

        etxtEmail = (EditText)findViewById(R.id.etxt_email);
        etxtPassword = (EditText)findViewById(R.id.etxt_password);

        submit_btn = (Button)findViewById(R.id.button_submit);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSubmitPressed(v);
            }
        });
    }

    public void buttonCancelPressed(View view){
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        super.finish();
    }

    public void buttonSubmitPressed(View view){
        if( gpsApp.isUsernameValid(etxtEmail, this)
                && gpsApp.isPasswordValid(etxtPassword, this)){
            login();
        }
    }

    private String getCleanString(EditText etxt) {
        return String.valueOf(etxt.getText()).trim();
    }

    private void login(){
        pd = ProgressDialog.show(this,"Please Wait...","Trying to Login");

        final String url = gpsApp.LOGIN_URL;

        final String email = getCleanString(etxtEmail);
        final String password = getCleanString(etxtPassword);
        final String uuid = gpsApp.getUUID();

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("uuid", uuid);
            jsonObject.put("email", email); //TODO add regex to determine phone number
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST,
                url,
                jsonObject,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response){

                        Log.v(TAG, "LOGIN Response: " + response.toString());
                        pd.dismiss();

                        try {
                            boolean valid = response.getBoolean("valid");

                            if(valid && response.has("error")){
                                String message = "Login successful!";
                                Log.e(TAG, message);

                                final String email = response.getString("email");
                                final String phnum = response.getString("phnum");
                                final String username = response.getString("username");
                                final String status = response.getString("status");

                                AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                                dialog.setTitle("Info");
                                dialog.setMessage(message);
                                dialog.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                        save(username,email, phnum);
                                        gpsApp.saveStatus(status);
                                        done();
                                    }
                                });

                                dialog.show();
                            }
                            else if(valid) {
                                String message = "Login successful!";
                                Log.e(TAG, message);

                                final String email = response.getString("email");
                                final String phnum = response.getString("phnum");
                                final String username = response.getString("username");
                                final String status = response.getString("status");

                                AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                                dialog.setTitle("Info");
                                dialog.setMessage(message);
                                dialog.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                        save(username,email, phnum);
                                        gpsApp.saveStatus(status);
                                        done();
                                    }
                                });

                                dialog.show();
                            }
                            else {
                                String message = response.getString("error");
                                showDialog(message);
                            }

                        } catch (JSONException e) {
                            String message = "Cannot parse response from " + url + "(" + response.toString() + ")";
                            showDialog(message);
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        pd.dismiss();
                        String message = "A network error has occurred on " + url + "(" + error.toString() + ")";
                        showDialog(message);
                    }
                });

        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest);
    }

    public void done(){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void save(String username,String email, String phnum){
//        final String username = getCleanString(etxtEmail);

        gpsApp.saveLogin(username, email, phnum);
        gpsApp.setLoggedIn(username);
    }

    private void showDialog(String message){
        Log.e(TAG, message);
        gpsApp.showDialog("Error", message, LoginActivity.this);
    }

}