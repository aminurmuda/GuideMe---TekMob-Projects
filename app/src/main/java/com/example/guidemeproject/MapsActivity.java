package com.example.guidemeproject;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import android.content.DialogInterface;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    int prevsize;
    Marker marker;
    ArrayList markerPoints= new ArrayList();
    private DatabaseReference database;
    private ChildEventListener eventChild;
    LocationListener locationListener;
    LocationManager locationManager;
    private TextInputEditText textInputStart;
    private TextInputEditText textInputDestination;
    private ImageButton imgButtonStart;
    private ImageButton imgButtonDestination;
    private FloatingActionButton myLocation;
    private Toolbar toolbar;

    ArrayList<LocationInformation>capsul = new ArrayList<LocationInformation>();
    Button button;
    Button button_warn;

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

        textInputStart = (TextInputEditText) findViewById(R.id.inputTextLocationStart);
        textInputDestination = (TextInputEditText) findViewById(R.id.inputTextLocationDestination);
        imgButtonStart = (ImageButton) findViewById(R.id.searchIconStart);
        imgButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchStart();
            }
        });

        imgButtonDestination = (ImageButton) findViewById(R.id.searchIconDestination);
        imgButtonDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDestination();
            }
        });

        myLocation = (FloatingActionButton) findViewById(R.id.circleButton);

        button = findViewById(R.id.button_id);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getData();// Code here executes on main thread after user presses button
            }
        });

        button_warn = findViewById(R.id.warning_button);
        button_warn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                open(v);// Code here executes on main thread after user presses button
            }
        });



    }


    public void getData(){

        database.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // 1. get table  size
                        for (DataSnapshot s : dataSnapshot.getChildren()) {
                            prevsize++;
                        }
                       /*database.child("bogor").child("description").setValue("There are cars");*/
                        // 2. add new row

                        /*DatabaseReference newdb = database.child("jakarta barat");
                        LocationInformation alan = new LocationInformation(-6.227777, 106.810544, "Car free day", "Jl. Jend. Sudirman, Daerah Khusus Ibukota Jakarta");
                        newdb.setValue(alan);*/

                        /*DatabaseReference newdb = database.child("jakarta pusat");
                        LocationInformation newLocation = new LocationInformation(-6.1762, 106.823, "Festival Day", "Jalan Medan Merdeka Barat, Gambir, Kota Jakarta Pusat, DKI Jakarta 10110");
                        newdb.setValue(newLocation);*/

                        /*DatabaseReference newdb = database.child("depok");
                        LocationInformation newLocation = new LocationInformation(-6.3640102, 106.8286607, "This road is fixed", "Jl. Prof. Dr. Nugroho Notosutanto");
                        newdb.setValue(newLocation);*/

                        // Get user value
                        // LocationInformation l = dataSnapshot.getValue(LocationInformation.class);
                        for (DataSnapshot s : dataSnapshot.getChildren()) {
                            LocationInformation locationInfo = s.getValue(LocationInformation.class);
                            Log.d("DES", s.getKey()+" --- "+locationInfo.description+"----"+prevsize);
                            capsul.add(locationInfo);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MapsActivity.this, "Database has Disconnected. Check your connection", Toast.LENGTH_SHORT).show();
                    }
                });
        Log.d("CAPSUL SIZE", String.valueOf(capsul.size()));
        if(capsul.size()>0) {
           NotificationCompat.Builder builder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_notification_icon)
                            .setContentTitle("Notifications")
                            .setContentText(capsul.get(capsul.size()-1).description+" in "+capsul.get(capsul.size()-1).location); // 4. get lokasi sama descriptionnya


            Intent notificationIntent = new Intent(this, MapsActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            // Add as notification
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());

            LatLng latLng = new LatLng(capsul.get(capsul.size()-1).longitude, capsul.get(capsul.size()-1).latitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            mMap.addMarker(new MarkerOptions().position(latLng).title(capsul.get(capsul.size()-1).location).snippet(capsul.get(capsul.size()-1).description));
        }
    }

    // to detect if 200 meters away from the restricted road
    public void detectDistance(double currentLatitude, double destLatitude, double currentLongitude, double destLongitude){
        double latitude=destLatitude;
        double longitude=destLongitude;
        float distance=0;
        Location crntLocation=new Location("crntlocation");
        crntLocation.setLatitude(currentLatitude);
        crntLocation.setLongitude(currentLongitude);

        Location newLocation=new Location("newlocation");
        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);


        //float distance = crntLocation.distanceTo(newLocation);  in meters
       // distance =crntLocation.distanceTo(newLocation) / 1000;
    }


    //dialog warning
    public void open(View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure, You wanted to make decision");
        alertDialogBuilder.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Toast.makeText(MapsActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                    }
                });

        alertDialogBuilder.setNegativeButton("NO",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
                    mMap.addMarker(new MarkerOptions().position(latLng).title(locationInfo.location).snippet(locationInfo.description).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_not_allowed)));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, "Database has Disconnected. Check your connection", Toast.LENGTH_SHORT).show();
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (markerPoints.size() > 1) {
                    markerPoints.clear();
                    mMap.clear();
                }

                // Adding new item to the ArrayList
                markerPoints.add(latLng);

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(latLng);

                if (markerPoints.size() == 1) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if (markerPoints.size() == 2) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }

                // Add new marker to the Google Map Android API V2
                mMap.addMarker(options);

                // Checks, whether start and end locations are captured
                if (markerPoints.size() >= 2) {
                    LatLng origin = (LatLng) markerPoints.get(0);
                    LatLng dest = (LatLng) markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }

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
                        mMap.addMarker(mark);
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


    public void searchStart() {
        String val_search = textInputStart.getText().toString().trim();
        if (!TextUtils.isEmpty(val_search)) {

            List<Address> addressList = null;

            Geocoder geocoder = new Geocoder(MapsActivity.this);
            try {
                addressList = geocoder.getFromLocationName(val_search, 1);
                Address address = addressList.get(0);
                LatLng latLngStart = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLngStart));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngStart, 14));

                Marker markerLocationCari  = mMap.addMarker(new MarkerOptions().position(latLngStart).title("Searching Location"));

                if (markerPoints.size() > 1) {
                    markerPoints.clear();
                    mMap.clear();
                }

                // Adding new item to the ArrayList
                markerPoints.add(latLngStart);

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(latLngStart);

                if (markerPoints.size() == 1) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if (markerPoints.size() == 2) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }

                // Add new marker to the Google Map Android API V2
                mMap.addMarker(options);

                // Checks, whether start and end locations are captured
                if (markerPoints.size() >= 2) {
                    LatLng origin = (LatLng) markerPoints.get(0);
                    LatLng dest = (LatLng) markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Location is not Found", Toast.LENGTH_SHORT).show();
            }

        }

    }

    public void searchDestination() {
        String val_search = textInputDestination.getText().toString().trim();
        if (!TextUtils.isEmpty(val_search)) {

            List<Address> addressList = null;

            Geocoder geocoder = new Geocoder(MapsActivity.this);
            try {
                addressList = geocoder.getFromLocationName(val_search, 1);
                Address address = addressList.get(0);
                LatLng latLngDestination = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLngDestination));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngDestination, 14));

                Marker markerLocationCari  = mMap.addMarker(new MarkerOptions().position(latLngDestination).title("Searching Location"));

                if (markerPoints.size() > 1) {
                    markerPoints.clear();
                    mMap.clear();
                }

                // Adding new item to the ArrayList
                markerPoints.add(latLngDestination);

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(latLngDestination);

                if (markerPoints.size() == 1) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if (markerPoints.size() == 2) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }

                // Add new marker to the Google Map Android API V2
                mMap.addMarker(options);

                // Checks, whether start and end locations are captured
                if (markerPoints.size() >= 2) {
                    LatLng origin = (LatLng) markerPoints.get(0);
                    LatLng dest = (LatLng) markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Location is not Found", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

    // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
