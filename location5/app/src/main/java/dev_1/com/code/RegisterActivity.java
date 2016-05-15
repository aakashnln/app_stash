package dev_1.com.code;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
public class RegisterActivity extends Activity{
    private static final String TAG = "RegisterActivity";

    private MainApplication gpsApp;

    private EditText etxtUsername;
    private EditText etxtPassword;
    private EditText etxtPasswordConfirmation;
    private EditText etxtEmail;
    private EditText etxtPhnum;

    private ProgressDialog pd;
    private Button submit_btn;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make this activity full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_register);

        this.gpsApp = (MainApplication) getApplication();

        etxtUsername = (EditText)findViewById(R.id.etxt_username);
        etxtPassword = (EditText)findViewById(R.id.etxt_password);
        etxtPasswordConfirmation  = (EditText)findViewById(R.id.etxt_password_confirmation);
        etxtEmail = (EditText)findViewById(R.id.etxt_email);
        etxtPhnum = (EditText)findViewById(R.id.etxt_phnum);

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
        if( gpsApp.isUsernameValid(etxtUsername, this)
                && isPasswordMatch()
                && gpsApp.isEmailValid(etxtEmail, this)
                && isPhnumValid()){
            register();
        }
    }

    private boolean isPhnumValid() {
        boolean valid = true;
        String phnum = String.valueOf(etxtPhnum.getText());
        if(phnum.isEmpty() || phnum.length() != 10){
            valid = false;
        }

        if(!valid){
            String message = getResources().getString(R.string.invalid_phnum);
            gpsApp.showDialog("Error", message, this);
        }
        return valid;
    }

    private boolean isPasswordMatch(){
        boolean valid = true;
        String password = String.valueOf(etxtPassword.getText());
        String passwordConfirm = String.valueOf(etxtPasswordConfirmation.getText());

        if(password.isEmpty() || passwordConfirm.isEmpty() || (password.equals(passwordConfirm) == false)
                || (password.length() < 8) || passwordConfirm.length() < 8){
            valid = false;
        }

        if(!valid){
            String message = getResources().getString(R.string.password_mismatch);
            gpsApp.showDialog("Error", message, this);
        }

        return valid;
    }

    private String getCleanString(EditText etxt) {
        return String.valueOf(etxt.getText()).trim();
    }

    private void register(){
        pd = ProgressDialog.show(this, "Please Wait...", "Trying to Register");

        final String url = gpsApp.REGISTER_URL;

        //curl -i -H "Content-Type applicationjson" -X POST --data
        // 'user[username]=rupert
        // &user[email]=rupert@2rmobile.com
        // &user[password]=junjunmalupet
        // &user[password_confirmation]=junjunmalupet'
        // http://127.0.0.1:3000/api/register

        final String username = getCleanString(etxtUsername);
        final String email = getCleanString(etxtEmail);
        final String password = getCleanString(etxtPassword);
        final String passwordConfirmation = getCleanString(etxtPasswordConfirmation);
        final String phnum = getCleanString(etxtPhnum);
        final String uuid = gpsApp.getUUID();

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("uuid", uuid);

//            JSONObject user = new JSONObject();
            jsonObject.put("username", username);
            jsonObject.put("email", email);
            jsonObject.put("password", password);
            jsonObject.put("password_confirmation", passwordConfirmation);
            jsonObject.put("phnum", phnum);

//            jsonObject.put("user", user);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response){

                        Log.v(TAG, "REGISTER Response: " + response.toString());
                        pd.dismiss();

                        try {

                            Log.e(TAG,"Has valid " +String.valueOf(response.has("valid")));
                            Boolean valid = response.getBoolean("valid");
//                            Log.e(TAG,"Has valid " +String.valueOf(valid));
                            if(valid) {
                                String message = "Registration successful, our team will contact you soon for verification";
                                Log.e(TAG, message);
                                final String status = response.getString("status");
                                AlertDialog.Builder dialog = new AlertDialog.Builder(RegisterActivity.this);
                                dialog.setTitle("Info");
                                dialog.setMessage(message);
                                dialog.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                        save();
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
                            Log.e(TAG,e.toString());
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

    public void save(){
        final String username = getCleanString(etxtUsername);
        final String email = getCleanString(etxtEmail);
        final String phnum = getCleanString(etxtPhnum);

        gpsApp.saveLogin(username, email, phnum);
        gpsApp.setLoggedIn(username);
    }

    private void showDialog(String message){
        Log.e(TAG, message);
        gpsApp.showDialog("Error", message, RegisterActivity.this);
    }

}
