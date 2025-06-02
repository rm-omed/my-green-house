package com.univbechar.mygreenhouse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executor;

public class FingerprintAuthActivity extends AppCompatActivity {

    private TextView authMessage;
    private ImageView fingerprintIcon;
    private Button usePasswordButton;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_auth);

        authMessage = findViewById(R.id.auth_message);
        fingerprintIcon = findViewById(R.id.fingerprint_icon);
        usePasswordButton = findViewById(R.id.use_password_button);

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                authMessage.setText("Authentication error: " + errString);
                Toast.makeText(FingerprintAuthActivity.this, "Error: " + errString, Toast.LENGTH_SHORT).show();
                usePasswordButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                authMessage.setText("Authentication succeeded!");
                Toast.makeText(FingerprintAuthActivity.this, "Authentication succeeded!", Toast.LENGTH_SHORT).show();

                // Proceed to the next activity
                Intent intent = new Intent(FingerprintAuthActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                authMessage.setText("Authentication failed");
                Toast.makeText(FingerprintAuthActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Log in using your fingerprint")
                .setNegativeButtonText("Cancel")
                .setConfirmationRequired(false)
                .build();

        // Automatically show the biometric dialog when activity starts
        fingerprintIcon.setOnClickListener(view -> {
            biometricPrompt.authenticate(promptInfo);
        });

        // Show the biometric dialog when activity starts
        biometricPrompt.authenticate(promptInfo);

        // Alternative password method (optional)
        usePasswordButton.setOnClickListener(view -> {
            // Implement your password authentication here
            Toast.makeText(this, "Password authentication would go here", Toast.LENGTH_SHORT).show();
        });
    }
}