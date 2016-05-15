package dev_1.com.code;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import dev_1.com.code.MainApplication;
import dev_1.com.code.MapsActivity;
import dev_1.com.code.R;

public class StartupActivity extends Activity implements View.OnClickListener {

    private MainApplication gpsApp;

    private WebView wvAds;

    private Button login_btn,signup_btn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make this activity full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.gpsApp = (MainApplication) getApplication();

        if(gpsApp.isLoggedIn()){
            setContentView(R.layout.activity_splash);

//            wvAds = (WebView) findViewById(R.id.wv_ads);

            String ADS_URL = getResources().getString(R.string.ADS_URL);
            wvAds.loadUrl(ADS_URL);
        }
        else {
            // Register or Login
            setContentView(R.layout.activity_splash);
        }

        login_btn = (Button)findViewById(R.id.button_login);
        login_btn.setOnClickListener(this);

        signup_btn = (Button)findViewById(R.id.button_register);
        signup_btn.setOnClickListener(this);

    }


    public void buttonRegisterPressed(View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void buttonLoginPressed(View view){
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
    }

    public void buttonEnterPressed(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //Ensures that we don't go back to previous activity
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button_register:
                // do your code
                buttonRegisterPressed(v);
                break;

            case R.id.button_login:
                // do your code
                buttonLoginPressed(v);
                break;

            default:
                break;
        }
    }
}