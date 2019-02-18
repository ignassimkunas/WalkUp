package com.example.ignas.walkup;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<LatLng> latLngList;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Button startButton;
    private boolean ifActive;
    private ArrayList<Float> distances;
    private ArrayList<Float> smallDistances;
    private SQLiteDatabase database;
    private int markerCount = 0;
    List<Trips> tripsList;
    ArrayAdapter adapter;
    Polyline line;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                centerMapOnLocation();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    Location location;
    public void centerMapOnLocation() {

        mMap.clear();

        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, locationListener);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        }

        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

        mMap.addMarker(new MarkerOptions().position(userLocation));

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        ListView listView = findViewById(R.id.listView);

        mMap = googleMap;

        ifActive = false;

        distances = new ArrayList<>();
        latLngList = new ArrayList<>();
        smallDistances = new ArrayList<>();

        tripsList = new ArrayList<>();

        adapter = new TripList(MapsActivity.this, tripsList);

        try {

            database = this.openOrCreateDatabase("Distances", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS distances (distance FLOAT(3))");

            Cursor c = database.rawQuery("SELECT * FROM distances", null);

            int distanceIndex = c.getColumnIndex("distance");
            tripsList.clear();
            c.moveToFirst();

            while (c != null){

                Trips trips = new Trips(null, null, c.getFloat(distanceIndex));
                tripsList.add(trips);
                c.moveToNext();
            }
        }
        catch (Exception e){

            e.printStackTrace();
        }

        startButton = findViewById(R.id.startButton);
        listView.setAdapter(adapter);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                if (ifActive){

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    latLngList.add(latLng);
                    markerCount++;

                    float[] results = new float[10];

                    if (markerCount > 1){

                        Location.distanceBetween(latLngList.get(markerCount - 1).latitude, latLngList.get(markerCount - 1).longitude, latLngList.get(markerCount - 2).latitude, latLngList.get(markerCount - 2).longitude, results);
                    }

                    smallDistances.add(results[0]);
                    line = mMap.addPolyline(new PolylineOptions()
                            .addAll(latLngList)
                            .width(25f)
                            .color(Color.RED));
                }
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
        };

        if (ifActive){

            startButton.setBackgroundColor(Color.RED);
            startButton.setText("Stop journey");
            centerMapOnLocation();

        }
        else {

            startButton.setBackgroundColor(Color.GREEN);
            startButton.setText("Start journey");
            centerMapOnLocation();

        }

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                database.execSQL("DELETE FROM distances WHERE distance = " + Float.toString(tripsList.get(position).distance));
                tripsList.remove(position);
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ifActive){

                    centerMapOnLocation();
                    startButton.setBackgroundColor(Color.GREEN);
                    startButton.setText("Start journey");
                    ifActive = false;
                    float distanceSum = 0;
                    if (latLngList.size() != 0){

                        markerCount = 0;
                        for (int i = 0; i < smallDistances.size(); i++){

                            distanceSum += smallDistances.get(i);
                        }
                        Trips trips = new Trips(null, null, distanceSum/1000);
                        tripsList.add(trips);
                        database.execSQL("INSERT INTO distances (distance) VALUES (" + Float.toString(distanceSum/1000) + " )");
                        smallDistances.clear();
                        adapter.notifyDataSetChanged();
                    }
                }
                else {
                    centerMapOnLocation();
                    ifActive = true;
                    startButton.setBackgroundColor(Color.RED);
                    startButton.setText("Stop journey");
                    latLngList.clear();

                }

            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng));
            centerMapOnLocation();
        }
        else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
    }
}
