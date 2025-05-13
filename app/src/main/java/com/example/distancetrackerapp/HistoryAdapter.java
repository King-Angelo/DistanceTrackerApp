package com.example.distancetrackerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.model.LatLng;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<TrackingData> trackingHistoryList;

    public HistoryAdapter(List<TrackingData> trackingHistoryList) {
        this.trackingHistoryList = trackingHistoryList;
    }

    public void setTrackingHistoryList(List<TrackingData> trackingHistoryList) {
        this.trackingHistoryList = trackingHistoryList;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tracking_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        TrackingData trackingData = trackingHistoryList.get(position);

        // Format the distance (you might want to add units like km or miles)
        holder.distanceTextView.setText(String.format(Locale.getDefault(), "Distance: %.2f km", trackingData.getTotalDistance()));

        // Format the timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String formattedTimestamp = sdf.format(new Date(trackingData.getTimestamp()));
        holder.timestampTextView.setText("Timestamp: " + formattedTimestamp);
    }

    @Override
    public int getItemCount() {
        return trackingHistoryList != null ? trackingHistoryList.size() : 0;
    }

    // Step 6: Create a RecyclerView ViewHolder
    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView distanceTextView;
        TextView timestampTextView;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}