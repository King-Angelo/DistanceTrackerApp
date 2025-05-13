package com.example.distancetrackerapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private double totalDistance = 0.0;
    private TextView distanceText;
    private Button startButton, stopButton;
    private boolean tracking = false;

    private GoogleMap mMap;
    private List<LatLng> pathPoints = new ArrayList<>();
    private Polyline pathLine;

    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Database
        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance().getReference("TrackingData");

        // Test write operation to verify Firebase connection
        database.child("test").setValue("Hello, Firebase!")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.d("Firebase", "Data written successfully");
                    } else {
                        android.util.Log.e("Firebase", "Data write failed", task.getException());
                    }
                });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        distanceText = findViewById(R.id.distanceText);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        startButton.setOnClickListener(v -> startTracking());
        stopButton.setOnClickListener(v -> stopTracking());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (tracking && locationResult != null && locationResult.getLastLocation() != null) {
                    Location currentLocation = locationResult.getLastLocation();
                    if (lastLocation != null) {
                        float distance = lastLocation.distanceTo(currentLocation);
                        totalDistance += distance;
                        distanceText.setText("Distance: " + String.format("%.2f", totalDistance) + " m");
                    }

                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    pathPoints.add(latLng);
                    updatePolyline();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
                    lastLocation = currentLocation;
                }
            }
        };

        checkPermissions();
    }

    private void startTracking() {
        if (!tracking) {
            tracking = true;
            totalDistance = 0.0;
            lastLocation = null;
            pathPoints.clear();
            if (pathLine != null) pathLine.remove();
            startLocationUpdates();
            Toast.makeText(this, "Started tracking", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopTracking() {
        if (tracking) {
            tracking = false;
            fusedLocationClient.removeLocationUpdates(locationCallback);

            // Save data to Firebase
            String trackingId = database.push().getKey();
            if (trackingId != null) {
                database.child(trackingId).setValue(new TrackingData(totalDistance, pathPoints));
            }

            Toast.makeText(this, "Stopped tracking and saved data", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void updatePolyline() {
        if (mMap == null || pathPoints.size() < 2) return;

        if (pathLine != null) pathLine.remove();

        PolylineOptions options = new PolylineOptions().addAll(pathPoints).width(10).color(0xFF2196F3);
        pathLine = mMap.addPolyline(options);
    }

    public static class TrackingData {
        public double totalDistance;
        public List<LatLng> pathPoints;

        public TrackingData() {} // Default constructor for Firebase

        public TrackingData(double totalDistance, List<LatLng> pathPoints) {
            this.totalDistance = totalDistance;
            this.pathPoints = pathPoints;
        }
    }
}