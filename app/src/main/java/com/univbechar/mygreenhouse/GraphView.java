package com.univbechar.mygreenhouse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class GraphView extends View {

    private Paint linePaint, textPaint;
    private Path graphPath;

    private float[] temperatures = new float[0];
    private String[] timeLabels = new String[0];

    static final String AES_KEY = "0123456789abcdef0123456789abcdef"; // 32 chars AES-256
    private String userIV = "abcdef9876543210"; // default 16-char IV

    public GraphView(Context context) {
        super(context);
        init();
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#4CAF50"));
        linePaint.setStrokeWidth(6);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(28);
        textPaint.setAntiAlias(true);

        graphPath = new Path();
    }

    public void setIV(String iv) {
        this.userIV = iv;
        fetchTemperatureData();
    }

    private void fetchTemperatureData() {
        if (userIV == null || userIV.length() != 16) {
            Toast.makeText(getContext(), "Invalid IV", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("greenhouse/data");

        ref.orderByChild("timestamp").limitToLast(50)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Float> tempList = new ArrayList<>();
                        List<String> labelList = new ArrayList<>();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String encTemp = snap.child("temp").getValue(String.class);
                            Long ts = snap.child("timestamp").getValue(Long.class);

                            if (encTemp == null || ts == null) continue;

                            try {
                                String decrypted = decrypt(encTemp, AES_KEY, userIV);
                                float temp = Float.parseFloat(decrypted);
                                tempList.add(temp);

                                String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                                        .format(new Date(ts));
                                labelList.add(time);

                            } catch (Exception ignored) {
                            }
                        }

                        temperatures = toFloatArray(tempList);
                        timeLabels = labelList.toArray(new String[0]);

                        postInvalidate(); // trigger draw safely from UI thread
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String decrypt(String base64Encrypted, String key, String iv) throws Exception {
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (temperatures.length == 0) return;

        int width = getWidth();
        int height = getHeight();

        float max = getMax(temperatures);
        float stepX = (float) width / Math.max(temperatures.length - 1, 1);
        float paddingBottom = 60;

        graphPath.reset();
        graphPath.moveTo(0, height - (temperatures[0] / max) * (height - paddingBottom));

        for (int i = 1; i < temperatures.length; i++) {
            float x = i * stepX;
            float y = height - (temperatures[i] / max) * (height - paddingBottom);
            graphPath.lineTo(x, y);
        }

        canvas.drawPath(graphPath, linePaint);

        for (int i = 0; i < timeLabels.length; i++) {
            float x = i * stepX;
            canvas.drawText(timeLabels[i], x, height - 10, textPaint);
        }
    }

    private float[] toFloatArray(List<Float> list) {
        float[] result = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    private float getMax(float[] values) {
        float max = Float.MIN_VALUE;
        for (float v : values) {
            if (v > max) max = v;
        }
        return max > 0 ? max : 1f;
    }
}
