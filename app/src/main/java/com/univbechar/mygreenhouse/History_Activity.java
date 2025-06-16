package com.univbechar.mygreenhouse;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class History_Activity extends AppCompatActivity {

    private LinearLayout historyContainer;
    private String userEnteredIV;
    private final String AES_KEY = "0123456789abcdef0123456789abcdef";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        FirebaseApp.initializeApp(this);

        historyContainer = findViewById(R.id.historyContainer);
        promptIvThenFetch();
    }

    private void promptIvThenFetch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(History_Activity.this);
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
                Toast.makeText(this, "IV must be 16 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            userEnteredIV = input;
            dialog.dismiss();
            fetchHistoryData();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void fetchHistoryData() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("greenhouse/data");

        ref.orderByChild("timestamp").limitToLast(50)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyContainer.removeAllViews();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            try {
                                String encTemp = snap.child("temp").getValue(String.class);
                                String encHum = snap.child("hum").getValue(String.class);
                                Long ts = snap.child("timestamp").getValue(Long.class);
                                String icon = snap.child("icon").getValue(String.class);

                                if (encTemp == null || encHum == null || userEnteredIV == null) continue;

                                String temp = decrypt(encTemp, AES_KEY, userEnteredIV);
                                String hum = decrypt(encHum, AES_KEY, userEnteredIV);

                                if (temp == null || hum == null) continue;

                                String date = (ts != null)
                                        ? new SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault()).format(new Date(ts))
                                        : "Unknown Date";

                                addHistoryCard(date, temp, hum, icon);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(History_Activity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addHistoryCard(String date, String temp, String humidity, String iconName) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View cardView = inflater.inflate(R.layout.history_card_item, null);

        // Apply spacing between cards
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(20, 10, 20, 10); // left, top, right, bottom
        cardView.setLayoutParams(layoutParams);

        TextView dayText = cardView.findViewById(R.id.dayText);
        TextView dataText = cardView.findViewById(R.id.dataText);
        ImageView iconView = cardView.findViewById(R.id.iconView);

        dayText.setText(date);
        dataText.setText(temp + "°C  " + humidity + "%");

        if (iconName != null) {
            int resId = getResources().getIdentifier(iconName, "drawable", getPackageName());
            if (resId != 0) iconView.setImageResource(resId);
        }

        historyContainer.addView(cardView);
    }


    private String decrypt(String base64Encrypted, String key, String iv) throws Exception {
        if (key.length() != 32 || iv.length() != 16)
            throw new IllegalArgumentException("Key must be 32 bytes and IV must be 16 bytes");

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
