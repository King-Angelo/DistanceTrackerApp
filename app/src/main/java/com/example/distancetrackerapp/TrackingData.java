package com.example.distancetrackerapp;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class TrackingData {
    private double totalDistance;
    private List<LatLng> pathPoints;
    private long timestamp; // Add this field

    // Required public no-argument constructor for Firebase
    public TrackingData() {
        // Default constructor required for calls to DataSnapshot.getValue(TrackingData.class)
    }

    public TrackingData(double totalDistance, List<LatLng> pathPoints) {
        this.totalDistance = totalDistance;
        this.pathPoints = pathPoints;
        this.timestamp = System.currentTimeMillis(); // Set timestamp when creating
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public List<LatLng> getPathPoints() {
        return pathPoints;
    }

    public long getTimestamp() { // Add getter for timestamp
        return timestamp;
    }
}
