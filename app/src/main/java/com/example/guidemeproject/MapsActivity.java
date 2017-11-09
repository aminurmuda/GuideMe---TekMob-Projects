package com.example.guidemeproject;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Marker marker;
    private DatabaseReference database;
    private ChildEventListener eventChild;
    LocationListener locationListener;
    LocationManager locationManager;
    private TextInputEditText textInput;
    private ImageButton imgButton;
    private FloatingActionButton myLocation;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance().getReference("warning_road");
        database.push().setValue(marker);

        textInput = (TextInputEditText) findViewById(R.id.inputTextLocation);
        imgButton = (ImageButton) findViewById(R.id.searchIcon);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Search();
            }
        });

        myLocation = (FloatingActionButton) findViewById(R.id.circleButton);
    }

    private void search() {
        String valueSearch = textInput.getText().toString().trim();
        if (!TextUtils.isEmpty(valueSearch)) {
            List <Address> addressList = null;
            Geocoder geocoder = new Geocoder(MapsActivity.this);
            try {
                addressList = geocoder.getFromLocationName(valueSearch, 1);
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            } catch (IOException e){
                e.printStackTrace();
                Toast.makeText(this, "Location is not found.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //googleMap.setOnMarkerClickListener(this);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    LocationInformation locationInfo = s.getValue(LocationInformation.class);
                    LatLng latLng = new LatLng(locationInfo.longitude, locationInfo.latitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                    mMap.addMarker(new MarkerOptions().position(latLng).title(locationInfo.location).snippet(locationInfo.description));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, "Database has Disconnected. Check your connection", Toast.LENGTH_SHORT).show();
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                myLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        MarkerOptions mark = new MarkerOptions().position(userLocation).title("Your Current Location");
                        //mMap.addMarker(mark);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,17));
                    }
                });
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if(Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,0,0,locationListener);
            Toast toast = Toast.makeText(getApplication(),"Show Location",Toast.LENGTH_SHORT);
            toast.show();
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            } else {
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnowLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                LatLng userLocation = new LatLng(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
            }
        }
    }


    public void Search() {
        String val_search = textInput.getText().toString().trim();
        if (!TextUtils.isEmpty(val_search)) {

            List<Address> addressList = null;

            Geocoder geocoder = new Geocoder(MapsActivity.this);
            try {
                addressList = geocoder.getFromLocationName(val_search, 1);
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                Marker markerLocationCari  = mMap.addMarker(new MarkerOptions().position(latLng).title("Searching Location"));

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Location is not Found", Toast.LENGTH_SHORT).show();
            }

        }

    }
}
