package com.example.floodrescue.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.floodrescue.WelcomeActivity;
import com.example.floodrescue.databinding.ActivityLoginBinding;
import com.example.floodrescue.utils.BaseActivity;
import com.example.floodrescue.utils.DatabaseHelper;
import com.example.floodrescue.utils.SharedPrefManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;

import java.util.Map;

public class LoginActivity extends BaseActivity {

    private static final int RC_SIGN_IN = 100;

    private ActivityLoginBinding binding;
    private DatabaseHelper dbHelper;
    private SharedPrefManager sharedPref;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
        sharedPref = new SharedPrefManager(this);

        if (sharedPref.isLoggedIn()) {
            goToWelcome(sharedPref.getUserName());
            return;
        }

        // ---------------- EMAIL LOGIN ----------------
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                performLogin(email, password);
            }
        });

        binding.btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        // ---------------- GOOGLE SIGN IN ----------------
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton googleBtn = binding.btnGoogleSignIn;
        googleBtn.setSize(SignInButton.SIZE_WIDE);

        googleBtn.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    // EMAIL LOGIN
    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            binding.editTextEmail.setError("Email required");
            return false;
        }
        if (password.isEmpty()) {
            binding.editTextPassword.setError("Password required");
            return false;
        }
        return true;
    }

    private void performLogin(String email, String password) {
        Map<String, String> user = dbHelper.checkUser(email, password);

        if (user != null) {
            String name = user.get("name");
            sharedPref.saveUserSession(
                    user.get("id"),
                    name,
                    user.get("email")
            );
            goToWelcome(name);
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    // GOOGLE SIGN IN RESULT
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignIn(task);
        }
    }

    private void handleGoogleSignIn(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            String name = account.getDisplayName();
            String email = account.getEmail();

            sharedPref.saveUserSession("google_user", name, email);

            Toast.makeText(this, "Signed in as " + name, Toast.LENGTH_SHORT).show();
            goToWelcome(name);

        } catch (ApiException e) {
            Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToWelcome(String userName) {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.putExtra("user_name", userName);
        startActivity(intent);
        finish();
    }
}
