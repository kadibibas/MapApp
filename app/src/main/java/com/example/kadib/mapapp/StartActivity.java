package com.example.kadib.mapapp;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import android.Manifest;
import android.telephony.TelephonyManager;
import android.content.Context;


public class StartActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    private Button mSignin;
    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    //Http request
    private RequestQueue mRequestQueue;
    private StringRequest stringRequest;
    private String url = "http://194.90.203.74/looking/api/user";

    //ProgressDialog
    private ProgressDialog mRegProgress;
    private ProgressBar mProgress;


    private String id;
    private String currentUser;
    private String TAG = StartActivity.class.getName();
    private static final String MY_PREFS_NAME = "MyPrefs";

    SharedPreferences sharedpreferens;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        //check if user is connected
        sharedpreferens = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        if(sharedpreferens.contains("Id"))
        {
            Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }

        //check location permission
        checkLocationPermission();

        //progress bar
        mRegProgress = new ProgressDialog(this);
        mProgress = (ProgressBar)findViewById(R.id.progressbar);

        id = id(this);

        //sign in button
        mSignin = (Button)findViewById(R.id.mSigninBTN);
        mSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String url = "http://192.168.0.92/looking/api/user"+currentUser;
                //Toast.makeText(getApplicationContext(), currentUser, Toast.LENGTH_SHORT).show();
                sendPostRequest();

            }
        });

    }

    //method - check the location permission
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("")
                        .setMessage("")
                        .setPositiveButton("", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(StartActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    //method - create a unique id
    public synchronized static String id(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }
        return uniqueID;
    }

    //server request - post request
    //response - user id
    public void sendPostRequest()
    {
        mRequestQueue = Volley.newRequestQueue(this);
        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.i(TAG,"Response: "+response.toString());
                SharedPreferences.Editor editor = sharedpreferens.edit();
                editor.putString("Id", response);
                editor.commit();

                Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.i(TAG,"Error: "+error.toString());

            }
        }){
            @Override
            protected Map<String,String> getParams()
            {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Device_token",id);
                params.put("Visibility","false");
                params.put("Lat","0.0");
                params.put("Lng","0.0");
                return params;
            }
        };
        mRequestQueue.add(stringRequest);

    }


}

