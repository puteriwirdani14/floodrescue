package com.example.floodrescue.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "floodrescue_db";
    private static final int DATABASE_VERSION = 2; // ✅ bumped (new reports table)

    // =========================
    // Table Users
    // =========================
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_CREATED_AT = "created_at";

    // =========================
    // Table Reports
    // =========================
    public static final String TABLE_REPORTS = "reports";

    public static final String R_ID = "id";
    public static final String R_USER_ID = "user_id";
    public static final String R_USER_NAME = "user_name";
    public static final String R_INCIDENT_TYPE = "incident_type";
    public static final String R_LAT = "latitude";
    public static final String R_LNG = "longitude";
    public static final String R_DESC = "description";
    public static final String R_SEVERITY = "severity";
    public static final String R_USER_AGENT = "user_agent";
    public static final String R_STATUS = "status";
    public static final String R_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // -------------------------
        // Create users table
        // -------------------------
        String createUsersTable =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, " +
                        COLUMN_PASSWORD + " TEXT NOT NULL, " +
                        COLUMN_NAME + " TEXT NOT NULL, " +
                        COLUMN_PHONE + " TEXT, " +
                        COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")";

        db.execSQL(createUsersTable);

        // -------------------------
        // Create reports table
        // -------------------------
        String createReportsTable =
                "CREATE TABLE " + TABLE_REPORTS + " (" +
                        R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        R_USER_ID + " TEXT, " +
                        R_USER_NAME + " TEXT, " +
                        R_INCIDENT_TYPE + " TEXT, " +
                        R_LAT + " REAL, " +
                        R_LNG + " REAL, " +
                        R_DESC + " TEXT, " +
                        R_SEVERITY + " TEXT, " +
                        R_USER_AGENT + " TEXT, " +
                        R_STATUS + " TEXT DEFAULT 'pending', " +
                        R_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")";

        db.execSQL(createReportsTable);

        // Insert test users
        insertTestUsers(db);
    }

    private void insertTestUsers(SQLiteDatabase db) {
        String[][] testUsers = new String[][]{
                {"user1@test.com", "password123", "Ali bin Ahmad", "0123456789"},
                {"user2@test.com", "password123", "Siti binti Rahman", "0134567890"},
                {"admin@test.com", "admin123", "System Admin", "0198765432"}
        };

        for (String[] user : testUsers) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_EMAIL, user[0]);
            values.put(COLUMN_PASSWORD, user[1]);
            values.put(COLUMN_NAME, user[2]);
            values.put(COLUMN_PHONE, user[3]);
            db.insert(TABLE_USERS, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTS);
        onCreate(db);
    }

    // =========================
    // USERS
    // =========================

    // Add user (register)
    public boolean addUser(String email, String password, String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password); // In real app, hash this
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PHONE, phone);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1L;
    }

    // Check user (login)
    public HashMap<String, String> checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{email, password});

        try {
            if (cursor.moveToFirst()) {
                HashMap<String, String> userData = new HashMap<>();
                userData.put("id", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                userData.put("email", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                userData.put("name", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));

                int phoneIndex = cursor.getColumnIndex(COLUMN_PHONE);
                userData.put("phone", phoneIndex != -1 ? cursor.getString(phoneIndex) : "");

                return userData;
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    // Get all users (for testing)
    public ArrayList<HashMap<String, String>> getAllUsers() {
        ArrayList<HashMap<String, String>> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS;

        Cursor cursor = db.rawQuery(query, null);

        try {
            while (cursor.moveToNext()) {
                HashMap<String, String> user = new HashMap<>();
                user.put("id", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                user.put("email", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                user.put("name", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));

                int phoneIndex = cursor.getColumnIndex(COLUMN_PHONE);
                user.put("phone", phoneIndex != -1 ? cursor.getString(phoneIndex) : "");

                userList.add(user);
            }
        } finally {
            cursor.close();
        }

        return userList;
    }

    // =========================
    // REPORTS
    // =========================

    // ✅ Matches: addReport(int userId, String userName, String type, double lat, double lng, String desc, String severity, String userAgent)
    public long addReport(int userId,
                          String userName,
                          String incidentType,
                          double latitude,
                          double longitude,
                          String description,
                          String severity,
                          String userAgent) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(R_USER_ID, String.valueOf(userId));
        values.put(R_USER_NAME, userName);
        values.put(R_INCIDENT_TYPE, incidentType);
        values.put(R_LAT, latitude);
        values.put(R_LNG, longitude);
        values.put(R_DESC, description);
        values.put(R_SEVERITY, severity);
        values.put(R_USER_AGENT, userAgent);
        values.put(R_STATUS, "pending");

        return db.insert(TABLE_REPORTS, null, values);
    }
}
