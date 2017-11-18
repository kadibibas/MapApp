package com.example.kadib.mapapp;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.icu.text.DateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.internal.ForegroundLinearLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity implements android.location.LocationListener {


    private static final String TAG = MainActivity.class.getName();

    final Handler h = new Handler();

    //menu
    FloatingActionsMenu fabMenu;

    //mapbox
    MapView mapView;
    MapboxMap mapbox;

    //Buttons
    ImageButton mLocationBTN;
    ImageView mLocationDisableBTN;
    ImageButton myLocation;
    ImageButton myLocationDisable;


    private ProgressDialog mProgress;

    //Http request
    private RequestQueue mRequestQueue;
    private StringRequest stringRequest;
    private String url = "http://194.90.203.74/looking/api/user";



    //location
    double Lat;
    double Lng;

    double LatPerv;
    double LngPrev;
    //LocationRequest locationrequest;
    LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private FusedLocationProviderClient mFusedLocationClient1;
    //private FusedLocationProviderClient mFusedLocationClient2;
    private boolean visible = false;


    //markers hashmap by id,marker value
    HashMap<String,Marker> Markers = new HashMap<>();

    //user object
    private User user;
    HashMap<String,User> users = new HashMap<>();
    HashMap<String,User> usersPrev = new HashMap<>();

    //shared preferences
    private static final String MY_PREFS_NAME = "MyPrefs";
    SharedPreferences sharedpreferens;

    //current user id
    private String currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //locationrequest = new LocationRequest();
        //locationrequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient1 = LocationServices.getFusedLocationProviderClient(this);
        //mFusedLocationClient2 = LocationServices.getFusedLocationProviderClient(this);

        //shared preferences
        sharedpreferens = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        currentUser = sharedpreferens.getString("Id",null);



        //map box
        Mapbox.getInstance(this, "pk.eyJ1Ijoia2FkaWJpYmFzIiwiYSI6ImNqNnJ2bXN0aTBkZDYyeG56bnA5OGoxN3EifQ.gtbNnIgSv-jG9ssLf9I7SA");
        setContentView(R.layout.activity_main);
        //map view
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        mapView.onCreate(savedInstanceState);

        //buttons
        mLocationBTN = (ImageButton) findViewById(R.id.myLocationButton);
        mLocationDisableBTN = (ImageView)findViewById(R.id.myLocationDisableButton);
        myLocation = (ImageButton) findViewById(R.id.myEyeButton);
        myLocationDisable = (ImageButton) findViewById(R.id.myEyeDisableButton);

        onStartUserLocation();


        //menu
        fabMenu = (FloatingActionsMenu) findViewById(R.id.fab_menu);


        mLocationBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                centerUser();
                mLocationBTN.setVisibility(View.GONE);
                mLocationDisableBTN.setVisibility(View.VISIBLE);

                mapbox.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {
                        mapbox.getUiSettings().setRotateGesturesEnabled(true);
                        mapbox.getUiSettings().setZoomGesturesEnabled(true);
                        mapbox.getUiSettings().setScrollGesturesEnabled(true);

                        mLocationDisableBTN.setVisibility(View.GONE);
                        mLocationBTN.setVisibility(View.VISIBLE);

                    }
                });



            }
        });


        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visible = true;
                mapbox.setMyLocationEnabled(false);
                visible();
                h.postDelayed(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        mapbox.clear();
                        Markers.clear();
                        visible();
                        h.postDelayed(this, 2000);
                    }
                }, 2000); // 1 second delay (takes millis)

                myLocation.setVisibility(View.GONE);
                myLocationDisable.setVisibility(View.VISIBLE);



            }
        });

        //
        myLocationDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visible = false;
                mapbox.setMyLocationEnabled(true);
                h.removeCallbacksAndMessages(null);
                sendPutRequest();
                myLocationDisable.setVisibility(View.GONE);
                myLocation.setVisibility(View.VISIBLE);
                mapbox.clear();

            }
        });
    }


    // fAB Menu change map styles
    public void selectStyle(View view) {
        fabMenu.collapse();
        mapView.setStyleUrl(Style.DARK);
    }

    public void selectStyle2(View view) {
        fabMenu.collapse();
        mapView.setStyleUrl(Style.LIGHT);
    }

    public void selectStyle3(View view) {
        fabMenu.collapse();
        mapView.setStyleUrl("mapbox://styles/kadibibas/cj6ou588l1zsn2rszxeic9w37");
    }

    public void selectStyle4(View view) {
        fabMenu.collapse();
        mapView.setStyleUrl(Style.SATELLITE_STREETS);
    }

    public void selectStyle5(View view) {
        fabMenu.collapse();
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
    }



    //lifecycle methods
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();

        //Toast.makeText(getApplicationContext(), "pause", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        sendPutRequest();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    //check location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                centerUser();
                break;
            default:
                break;
        }
    }


    //method - centered the user location
    public void centerUser() {
        final double[] lat = new double[1];
        final double[] lon = new double[1];
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }

        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            lat[0] = location.getLatitude();
                            lon[0] = location.getLongitude();
                            LatLng latLng = new LatLng(lat[0], lon[0]);


                            mapbox.getUiSettings().setRotateGesturesEnabled(false);
                            mapbox.getUiSettings().setZoomGesturesEnabled(false);
                            mapbox.getUiSettings().setScrollGesturesEnabled(false);


                            mapbox.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                            .target(latLng)
                                            .zoom(13)
                                            .tilt(45.0)
                                            .build()),
                                    200);



                        }
                    }
                });
    }


    //method - put the current user location on the map, and animate the camera
    public void onStartUserLocation() {
        final double[] lat = new double[1];
        final double[] lon = new double[1];
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }

        }
        mFusedLocationClient1.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            lat[0] = location.getLatitude();
                            lon[0] = location.getLongitude();
                            final LatLng latLng = new LatLng(lat[0], lon[0]);


                            mapView.getMapAsync(new OnMapReadyCallback() {
                                @Override
                                public void onMapReady(MapboxMap map) {
                                    mapbox = map;
                                    map.setMyLocationEnabled(true);
                                    map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                                    .target(latLng)
                                                    .zoom(13)
                                                    .tilt(45.0)
                                                    .build()),
                                            0);

                                }
                            });


                        }
                    }
                });
    }


    //method - location history
    /*public void LocationsHistory() throws ParseException {
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);

        String lat1 = String.valueOf(Lat);
        String lon1 = String.valueOf(Lng);
    }*/


    //send request to sever to update location and get all users back as response
    public void visible() {
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);

        final String lat1 = String.valueOf(Lat);
        final String lon1 = String.valueOf(Lng);

        String URL = "http://194.90.203.74/looking/api/user"+"/"+currentUser;

        mRequestQueue = Volley.newRequestQueue(this);
        stringRequest = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.i(TAG,"Response: "+response.toString());
                JSONArray array = null;
                try {
                    array = new JSONArray(response);
                    for (int i = 0; i<array.length();i++)
                    {
                        JSONObject jsonobject = array.getJSONObject(i);
                        user = new User();
                        user.Device_token = jsonobject.getString("Device_token");
                        user.Visibility = jsonobject.getString("Visibility");
                        user.Lat = jsonobject.getString("Lat");
                        user.Lng = jsonobject.getString("Lng");
                        usersPrev.put(jsonobject.getString("Id"),users.get(jsonobject.getString("Id")));
                        users.put(jsonobject.getString("Id"),user);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
                params.put("Lat",lat1);
                params.put("Lng",lon1);
                params.put("Visibility","true");
                return params;
            }
        };
        mRequestQueue.add(stringRequest);
        HashMapToMap();
    }


    //insert the users location to map
    public void HashMapToMap()
    {
        for(Map.Entry<String,User>entry : users.entrySet())
        {
            User user = entry.getValue();
            String Slat = user.Lat;
            String Slng = user.Lng;

            double lat = Double.parseDouble(Slat);
            double lng = Double.parseDouble(Slng);
            LatLng latlng = new LatLng(lat,lng);

            Marker marker = mapbox.addMarker(new MarkerOptions()
                    .position(latlng)
            );



            Markers.put(entry.getKey(), marker);



        }
    }

    public void sendPutRequest()
    {
        String URL = "http://194.90.203.74/looking/api/user"+"/"+currentUser;

        mRequestQueue = Volley.newRequestQueue(this);
        stringRequest = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.i(TAG,"Response: "+response.toString());

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
                params.put("Visibility","false");
                return params;
            }
        };
        mRequestQueue.add(stringRequest);


    }





    //location services methods
    @Override
    public void onLocationChanged(Location location) {

        double lat =  location.getLatitude();
        double lon =  location.getLongitude();

        Lat =  lat;
        Lng = lon;

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /*public double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }


    public double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }


    public double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
*/

}

