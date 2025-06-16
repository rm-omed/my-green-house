package com.univbechar.mygreenhouse;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {
    private String userEnteredIV = null;
    private DatabaseReference dbRef;
    private TextView temperatureText, humidityText, lastUpdateText;
    private ToggleButton pumpToggle, ventilatorToggle;
    private  ImageView lock1 ;
    private  ImageView lock2 ;
    private final String AES_KEY = "0123456789abcdef0123456789abcdef"; // 32 characters
   // private final String AES_IV = "abcdef9876543210";   // 16 characters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        dbRef = FirebaseDatabase.getInstance().getReference("greenhouse");

        // UI bindings
         lock1  = findViewById(R.id.lock1);
         lock2  = findViewById(R.id.lock2);
        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);
        lastUpdateText = findViewById(R.id.lastUpdateText);
        pumpToggle = findViewById(R.id.pumpToggle);
        ventilatorToggle = findViewById(R.id.ventilatorToggle);

        ImageView img_logout = findViewById(R.id.img_logout);
        ImageView img_graph = findViewById(R.id.img_graph);
        ImageView img_history = findViewById(R.id.img_history);

        img_graph.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Tempirature_Activity.class)));
        img_history.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, History_Activity.class)));
        img_logout.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Login_activity.class));
            finish();
        });

        temperatureText.setOnClickListener(v -> {
            if (userEnteredIV == null) {
                promptIvThenFetch();
            } else {
                fetchLatestSensorData(userEnteredIV);
            }
        });

        humidityText.setOnClickListener(v -> {
            if (userEnteredIV == null) {
                promptIvThenFetch();
            } else {
                fetchLatestSensorData(userEnteredIV);
            }
        });

        fetchActuatorState();

        pumpToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbRef.child("devices").child("pump").setValue(isChecked);
        });

        ventilatorToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbRef.child("devices").child("ventilator").setValue(isChecked);
        });
    }
    private void promptIvThenFetch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_iv_input, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        EditText inputIv = dialogView.findViewById(R.id.input_iv);
        Button btnDecrypt = dialogView.findViewById(R.id.btn_decrypt);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnDecrypt.setOnClickListener(v -> {
            String input = inputIv.getText().toString().trim();
            if (input.length() != 16) {
                Toast.makeText(MainActivity.this, "IV must be 16 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            userEnteredIV = input;
            dialog.dismiss();
            fetchLatestSensorData(userEnteredIV);
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    private void fetchLatestSensorData(String iv) {
        dbRef.child("data").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String encTemp = child.child("temp").getValue(String.class);
                    String encHum = child.child("hum").getValue(String.class);
                    Long timestamp = child.child("timestamp").getValue(Long.class);

                    try {
                        String temp = decrypt(encTemp, AES_KEY, iv);
                        String hum = decrypt(encHum, AES_KEY, iv);

                        temperatureText.setText(temp + "°C");
                        humidityText.setText(hum + "%");
                        lock1.setVisibility(View.GONE);
                        lock2.setVisibility(View.GONE);
                        if (timestamp != null) {
                            String time = new SimpleDateFormat("dd MMM yyyy - HH:mm:ss", Locale.getDefault())
                                    .format(new Date(timestamp));
                            lastUpdateText.setText("Last update: " + time);
                        } else {
                            lastUpdateText.setText("Last update: unknown");
                        }
                    } catch (Exception e) {
                        temperatureText.setText("Error");
                        humidityText.setText("Error");
                        lastUpdateText.setText("Last update: error");
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                temperatureText.setText("Error");
                humidityText.setText("Error");
                lastUpdateText.setText("Last update: error");
            }
        });
    }

    private void fetchActuatorState() {
        dbRef.child("devices").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean pumpState = snapshot.child("pump").getValue(Boolean.class);
                Boolean ventilatorState = snapshot.child("ventilator").getValue(Boolean.class);

                pumpToggle.setChecked(pumpState != null && pumpState);
                ventilatorToggle.setChecked(ventilatorState != null && ventilatorState);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }

    private String decrypt(String base64Encrypted, String key, String iv) throws Exception {
        if (key.length() != 32 || iv.length() != 16) {
            throw new IllegalArgumentException("Key must be 32 bytes and IV must be 16 bytes");
        }

        byte[] encryptedBytes = Base64.decode(base64Encrypted, Base64.DEFAULT);
        byte[] keyBytes = key.getBytes("UTF-8");
        byte[] ivBytes = iv.getBytes("UTF-8");

        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

        byte[] originalBytes = cipher.doFinal(encryptedBytes);
        return new String(originalBytes, "UTF-8");
    }
}
