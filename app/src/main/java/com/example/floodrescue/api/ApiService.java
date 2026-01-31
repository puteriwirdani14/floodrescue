package com.example.floodrescue.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import com.example.floodrescue.models.Report;
import com.example.floodrescue.models.SafeLocation;

public interface ApiService {

    // === AUTH ===
    @POST("auth/login.php")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register.php")
    Call<BasicResponse> register(@Body RegisterRequest request);

    // === REPORTS (Laporan Pengguna) ===
    @POST("dashboard_reports.php")
    Call<BasicResponse> createReport(@Body ReportRequest request);

    // Ambil semua laporan pengguna untuk dipaparkan sebagai Marker Merah
    @GET("dashboard_reports.php?get_json=true")
    Call<List<Report>> getLocations();

    // === SHELTERS (Lokasi Selamat dari table locations) ===
    // Menggunakan fail api/get_shelters.php yang hanya tapis incident_type = 'shelter'
    @GET("api/get_shelters.php")
    Call<List<SafeLocation>> getShelters();

    // Endpoint tambahan jika anda mahu semua data dari folder locations/view.php
    @GET("locations/view.php")
    Call<List<SafeLocation>> getAllLocations();
}
