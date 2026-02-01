package com.example.floodrescue.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floodrescue.R;
import com.example.floodrescue.models.Report;

import java.util.List;

public class IncidentListAdapter extends RecyclerView.Adapter<IncidentListAdapter.ViewHolder> {

    private List<Report> reports;

    public IncidentListAdapter(List<Report> reports) {
        this.reports = reports;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incident, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Report report = reports.get(position);
        
        String typeLabel = report.getIncidentType().equals("flood") ? "🌊 Flood" : "🆘 Help Request";
        holder.tvTitle.setText(typeLabel + ": " + report.getDescription());
        
        // Mocking date format similar to lecture diagram
        holder.tvDetails.setText("Reported by " + report.getUserName());
        
        holder.tvSeverity.setText(report.getSeverity().toUpperCase());
        
        // Set severity color
        int severityColor;
        switch (report.getSeverity().toLowerCase()) {
            case "critical": severityColor = 0xFFEF4444; break; // Red
            case "high": severityColor = 0xFFF97316; break; // Orange
            default: severityColor = 0xFF2563EB; // Blue
        }
        holder.tvSeverity.setTextColor(severityColor);
    }

    @Override
    public int getItemCount() {
        return reports != null ? reports.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails, tvSeverity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvIncidentTitle);
            tvDetails = itemView.findViewById(R.id.tvIncidentDetails);
            tvSeverity = itemView.findViewById(R.id.tvSeverity);
        }
    }
}
