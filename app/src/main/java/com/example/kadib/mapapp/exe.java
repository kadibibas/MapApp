package com.example.kadib.mapapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class exe extends AppCompatActivity implements android.location.LocationListener {


    FloatingActionsMenu fabMenu;
    MapView mapView;
    MapboxMap mapbox;
    ImageButton mLocationBTN;
    ImageView mLocationDisableBTN;
    ImageButton myLocation;
    ImageButton myLocationDisable;


    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgress;

    //location
    double Lat;
    double Lng;

    double prevLat;
    double prevLng;
    LocationRequest locationrequest;
    LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private FusedLocationProviderClient mFusedLocationClient1;
    private FusedLocationProviderClient mFusedLocationClient2;
    private  boolean move = false;
    private  boolean visible = false;

    HashMap<String,LatLng> Locations = new HashMap<>();
    HashMap<String,Marker> Markers = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        move = true;


        locationrequest = new LocationRequest();
        locationrequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        Mapbox.getInstance(this, "pk.eyJ1Ijoia2FkaWJpYmFzIiwiYSI6ImNqNnJ2bXN0aTBkZDYyeG56bnA5OGoxN3EifQ.gtbNnIgSv-jG9ssLf9I7SA");

        //firebase
        mAuth = FirebaseAuth.getInstance();

        //setContentView(R.layout.activity_main);
        mLocationBTN = (ImageButton) findViewById(R.id.myLocationButton);
        mLocationDisableBTN = (ImageView)findViewById(R.id.myLocationDisableButton);
        myLocation = (ImageButton) findViewById(R.id.myEyeButton);
        myLocationDisable = (ImageButton) findViewById(R.id.myEyeDisableButton);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient1 = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient2 = LocationServices.getFusedLocationProviderClient(this);


        //map view
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        mapView.onCreate(savedInstanceState);

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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 3, this);

        getLatLon2();


        //menu
        fabMenu = (FloatingActionsMenu) findViewById(R.id.fab_menu);


        mLocationBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getLatLon1();
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
                FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = current_user.getUid();
                mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                mDatabase.child("visibility").setValue("visible").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            mapbox.setMyLocationEnabled(false);
                            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Visibles");
                            mRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot ds : dataSnapshot.getChildren())
                                    {
                                        String key = dataSnapshot.getKey();
                                        String lat = (String) ds.child("lat").getValue();
                                        String lng = (String) ds.child("lon").getValue();

                                        double lat1 = Double.valueOf(lat);
                                        double lng1 = Double.valueOf(lng);
                                        LatLng latlng = new LatLng(lat1,lng1);

                                        Marker marker = mapbox.addMarker(new MarkerViewOptions()
                                                .position(latlng)
                                        );
                                        Markers.put(key,marker);



                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            myLocation.setVisibility(View.GONE);
                            myLocationDisable.setVisibility(View.VISIBLE);
                            visible = true;
                            FirebaseChanges();
                            /*
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Visibles");
                            mDatabase.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                    String key = dataSnapshot.getKey();
                                    String lat = (String) dataSnapshot.child("lat").getValue();
                                    String lng = (String) dataSnapshot.child("lon").getValue();

                                    double lat1 = Double.valueOf(lat);
                                    double lng1 = Double.valueOf(lng);

                                    LatLng latlng = new LatLng(lat1,lng1);
                                    Locations.put(key,latlng);

                                    mapbox.clear();
                                    for(Map.Entry<String,LatLng> entery : Locations.entrySet())
                                    {
                                        mapbox.addMarker(new MarkerViewOptions()
                                                .position(entery.getValue())
                                        );


                                    }

                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                    String key = dataSnapshot.getKey();
                                    String lat = (String) dataSnapshot.child("lat").getValue();
                                    String lng = (String) dataSnapshot.child("lon").getValue();

                                    double lat1 = Double.valueOf(lat);
                                    double lng1 = Double.valueOf(lng);

                                    LatLng latlng = new LatLng(lat1,lng1);
                                    Locations.put(key,latlng);

                                    mapbox.clear();
                                    for(Map.Entry<String,LatLng> entery : Locations.entrySet())
                                    {
                                        mapbox.addMarker(new MarkerViewOptions()
                                                .position(entery.getValue())
                                        );


                                    }
                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                    String key = dataSnapshot.getKey();
                                    String lat = (String) dataSnapshot.child("lat").getValue();
                                    String lng = (String) dataSnapshot.child("lon").getValue();

                                    double lat1 = Double.valueOf(lat);
                                    double lng1 = Double.valueOf(lng);

                                    LatLng latlng = new LatLng(lat1,lng1);
                                    Locations.put(key,latlng);

                                    mapbox.clear();
                                    for(Map.Entry<String,LatLng> entery : Locations.entrySet())
                                    {
                                        if(!entery.getKey().equals(key))
                                        {
                                            mapbox.addMarker(new MarkerViewOptions()
                                                    .position(entery.getValue())
                                            );
                                        }
                                        else
                                        {
                                            Locations.remove(key);
                                        }
                                    }


                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            */


                        } else {

                            Toast.makeText(getApplicationContext(), "There was some error to set visibility.", Toast.LENGTH_LONG).show();

                        }

                    }
                });


            }
        });

        myLocationDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Progress
                /*
                mProgress = new ProgressDialog(MainActivity.this);
                mProgress.setTitle("hied you'r visibility");
                mProgress.setMessage("Please wait while we hied you'r visibility");
                mProgress.show();
                */
                FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = current_user.getUid();
                mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                mDatabase.child("visibility").setValue("Invisible").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {


                            myLocationDisable.setVisibility(View.GONE);
                            myLocation.setVisibility(View.VISIBLE);
                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Visibles").child(uid);
                            mDatabase.setValue(null);
                            mDatabase.removeValue(null);
                            visible = false;
                            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Visibles");
                            mRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    mapbox.clear();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            mapbox.clear();
                            mapbox.setMyLocationEnabled(true);



                        } else {

                            Toast.makeText(getApplicationContext(), "There was some error to set visibility.", Toast.LENGTH_LONG).show();

                        }

                    }
                });


            }
        });


    }


    private void sendToStart() {

        Intent startIntent = new Intent(exe.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }


    // fAB Menu
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


    //mapbox



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {

            sendToStart();
        }
        mapView.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        move=true;
        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = current_user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("visibility");
        if(mDatabase.equals("visible"))
        {
            visible = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        move=true;
    }


    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        move=false;



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                getLatLon1();
                break;
            default:
                break;
        }
    }


    public void getLatLon1() {
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
                                            .zoom(14)
                                            .tilt(45.0)
                                            .build()),
                                    200);



                        }
                    }
                });
    }

    public void getLatLon2() {
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
                                                    .zoom(14)
                                                    .tilt(45.0)
                                                    .build()),
                                            0);

                                }
                            });


                        }
                    }
                });
    }


    public void LocationsHistory() throws ParseException {
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


        SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM d, ''yy");
        SimpleDateFormat formatTime = new SimpleDateFormat("h:mm a");
        Date date = Calendar.getInstance().getTime();
        String todayWithZeroTime = formatter.format(date);
        String time = formatTime.format(date);
        long current_Time =System.currentTimeMillis()/1000;
        String currentTime = String.valueOf(current_Time);
        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = current_user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("locations").child(uid).child(todayWithZeroTime).child(time);
        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("lat", lat1);
        userMap.put("lon", lon1);
        mDatabase.setValue(userMap);
    }



    public void SendLocation() {
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

        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = current_user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Visibles").child(uid);
        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("lat",lat1);
        userMap.put("lon", lon1);
        mDatabase.setValue(userMap);
    }



    @Override
    public void onLocationChanged(Location location) {




        double lat =  Math.round(location.getLatitude()* 1000000.0) / 1000000.0;
        double lon =  Math.round(location.getLongitude()* 1000000.0) / 1000000.0;

        Lat =  lat;
        Lng = lon;


        if(move == true)
        {

            if((prevLng!=Lng && prevLat!=Lat))
            {
                try {
                    LocationsHistory();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if(visible == true)
            {
                SendLocation();
            }
            /*
            else
            {
                mapbox.clear();
            }
            */


        }
        else
        {
            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = current_user.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
            mDatabase.child("visibility").setValue("Invisible");
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Visibles").child(uid);
            mDatabase.setValue(null);
            mDatabase.removeValue(null);
        }
        prevLat= Lat;
        prevLng = Lng;




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

    public void FirebaseChanges()
    {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Visibles");
        mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String key = dataSnapshot.getKey();
                String lat = (String) dataSnapshot.child("lat").getValue();
                String lng = (String) dataSnapshot.child("lon").getValue();

                double lat1 = Double.valueOf(lat);
                double lng1 = Double.valueOf(lng);

                LatLng latlng = new LatLng(lat1,lng1);
                Locations.put(key,latlng);

                mapbox.clear();
                Marker marker = mapbox.addMarker(new MarkerViewOptions()
                        .position(latlng)
                );
                Markers.put(key,marker);
                /*
                for(Map.Entry<String,LatLng> entery : Locations.entrySet())
                {
                    mapbox.addMarker(new MarkerViewOptions()
                            .position(entery.getValue())
                    );


                }
                */

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String key = dataSnapshot.getKey();
                String lat = (String) dataSnapshot.child("lat").getValue();
                String lng = (String) dataSnapshot.child("lon").getValue();

                double lat1 = Double.valueOf(lat);
                double lng1 = Double.valueOf(lng);

                LatLng latlng = new LatLng(lat1,lng1);
                Locations.put(key,latlng);
                //mapbox.clear();
                Marker marker = mapbox.addMarker(new MarkerViewOptions()
                        .position(latlng)
                );

                Markers.put(key,marker);


                /*
                mapbox.clear();
                mapbox.addMarker(new MarkerViewOptions()
                        .position(latlng)
                );
                */
                /*
                for(Map.Entry<String,LatLng> entery : Locations.entrySet())
                {
                    mapbox.addMarker(new MarkerViewOptions()
                            .position(entery.getValue())
                    );


                }
                */
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {


                mapbox.clear();



            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }






}

