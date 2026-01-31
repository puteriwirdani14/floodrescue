package com.example.floodrescue.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.floodrescue.R;
import com.example.floodrescue.api.ApiClient;
import com.example.floodrescue.api.BasicResponse;
import com.example.floodrescue.api.ReportRequest;
import com.example.floodrescue.databinding.DialogReportBinding;
import com.example.floodrescue.utils.DatabaseHelper;
import com.example.floodrescue.utils.DeviceUtils;
import com.example.floodrescue.utils.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportDialog extends DialogFragment {

    private double currentLat;
    private double currentLng;
    private Runnable onReportSubmitted;

    private DialogReportBinding binding;
    private DatabaseHelper dbHelper;
    private SharedPrefManager sharedPref;

    private final List<ReportType> incident_type = new ArrayList<>();

    private int selectedTypeIndex = 0;
    private int selectedSeverity = 1;

    public ReportDialog(double currentLat, double currentLng, @NonNull Runnable onReportSubmitted) {
        this.currentLat = currentLat;
        this.currentLng = currentLng;
        this.onReportSubmitted = onReportSubmitted;

        incident_type.add(new ReportType("flood", "🌊 Flood Incident", android.R.drawable.ic_dialog_info, Color.BLUE));
        incident_type.add(new ReportType("request_help", "🆘 Request Help", android.R.drawable.ic_dialog_alert, Color.RED));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
        sharedPref = new SharedPrefManager(requireContext());

        setupUI();
        
        binding.textViewLocation.setText(String.format(Locale.getDefault(), "📍 Lat: %.6f , Lng: %.6f", currentLat, currentLng));
        String currentTime = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date());
        binding.textViewDateTime.setText("📅 " + currentTime);
        binding.textViewUserInfo.setText("👤 Reported by: " + sharedPref.getUserName());

        // Tambah fungsi klik untuk butang Back yang baru ditambah
        binding.btnBack.setOnClickListener(v -> dismiss());
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnSubmit.setOnClickListener(v -> submitReport());
    }

    private void setupUI() {
        LinearLayout container = binding.reportTypeContainer;
        container.removeAllViews();
        for (int i = 0; i < incident_type.size(); i++) {
            int index = i;
            Button b = new Button(requireContext());
            b.setText(incident_type.get(i).displayName);
            b.setAllCaps(false);
            b.setOnClickListener(v -> {
                selectedTypeIndex = index;
                highlightSelectedType();
            });
            container.addView(b);
        }
        highlightSelectedType();
        
        binding.severitySlider.addOnChangeListener((slider, value, fromUser) -> {
            selectedSeverity = (int) value;
            updateSeverityDisplay();
        });
        updateSeverityDisplay();
        
        binding.editTextDescription.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {
                binding.textCharCount.setText(s.length() + "/500");
            }
        });
    }

    private void highlightSelectedType() {
        LinearLayout container = binding.reportTypeContainer;
        for (int i = 0; i < container.getChildCount(); i++) {
            Button b = (Button) container.getChildAt(i);
            if (i == selectedTypeIndex) {
                b.setBackgroundColor(incident_type.get(i).color);
                b.setTextColor(Color.WHITE);
            } else {
                b.setBackgroundColor(Color.LTGRAY);
                b.setTextColor(Color.BLACK);
            }
        }
    }

    private void updateSeverityDisplay() {
        String txt;
        int color;
        switch (selectedSeverity) {
            case 0: txt = "LOW"; color = Color.GREEN; break;
            case 2: txt = "HIGH"; color = Color.rgb(255, 165, 0); break;
            case 3: txt = "CRITICAL"; color = Color.RED; break;
            default: txt = "MEDIUM"; color = Color.BLUE;
        }
        binding.textSeverityValue.setText(txt);
        binding.textSeverityValue.setTextColor(color);
    }

    private void submitReport() {
        String description = binding.editTextDescription.getText().toString().trim();
        if (description.length() < 10) {
            binding.editTextDescription.setError("Minimum 10 characters");
            return;
        }

        showLoading(true);
        String userName = sharedPref.getUserName();
        String type = incident_type.get(selectedTypeIndex).id;
        String severity;
        switch (selectedSeverity) {
            case 0: severity = "low"; break;
            case 2: severity = "high"; break;
            case 3: severity = "critical"; break;
            default: severity = "medium";
        }

        dbHelper.addReport(0, userName, type, currentLat, currentLng, description, severity, DeviceUtils.getUserAgent(requireContext()));

        ReportRequest request = new ReportRequest(userName, type, currentLat, currentLng, description, severity);

        try {
            ApiClient.getInstance().createReport(request).enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().isSuccess()) {
                            Toast.makeText(requireContext(), "Report submitted successfully!", Toast.LENGTH_SHORT).show();
                            onReportSubmitted.run();
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Error: " + response.body().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(requireContext(), "Network error - Saved locally.", Toast.LENGTH_SHORT).show();
                    onReportSubmitted.run();
                    dismiss();
                }
            });
        } catch (Exception e) {
            showLoading(false);
            Log.e("ReportDialog", "Error: " + e.getMessage());
        }
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setEnabled(!show);
    }

    public static class ReportType {
        public String id, displayName;
        public int iconRes, color;
        public ReportType(String id, String displayName, int iconRes, int color) {
            this.id = id; this.displayName = displayName; this.iconRes = iconRes; this.color = color;
        }
    }
}
