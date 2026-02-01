package com.example.floodrescue;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floodrescue.api.ApiClient;
import com.example.floodrescue.models.Report;
import com.example.floodrescue.ui.IncidentListAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncidentListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IncidentListAdapter adapter;
    private List<Report> reportList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recent Incidents");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewIncidents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new IncidentListAdapter(reportList);
        recyclerView.setAdapter(adapter);

        fetchIncidents();
    }

    private void fetchIncidents() {
        ApiClient.getInstance().getLocations().enqueue(new Callback<List<Report>>() {
            @Override
            public void onResponse(Call<List<Report>> call, Response<List<Report>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reportList.clear();
                    reportList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(IncidentListActivity.this, "Failed to load incidents", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Report>> call, Throwable t) {
                Log.e("IncidentList", "Error: " + t.getMessage());
                Toast.makeText(IncidentListActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
