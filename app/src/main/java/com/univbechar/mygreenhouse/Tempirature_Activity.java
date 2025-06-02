package com.univbechar.mygreenhouse;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Tempirature_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tempirature);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView Image_close = findViewById(R.id.image_close);
        Image_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Tempirature_Activity.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
        });

    }
}