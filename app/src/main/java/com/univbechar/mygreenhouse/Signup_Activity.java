package com.univbechar.mygreenhouse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Signup_Activity extends AppCompatActivity {

    EditText txt_fullname,txt_email,txt_phone,txt_pass,txt_cpass;
    Button btn_signup;
    TextView txt_myacc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Change the status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.p4));
        }

        txt_fullname=findViewById(R.id.edit_fullname);
        txt_email=findViewById(R.id.edit_email);
        txt_phone=findViewById(R.id.edt_phone);
        txt_pass=findViewById(R.id.editPassword);
        txt_cpass=findViewById(R.id.editTPasswordconf);

        btn_signup=findViewById(R.id.button_sign);

        txt_myacc=findViewById(R.id.tx_login);

        String fullname =txt_fullname.getText().toString();
        String email=txt_email.getText().toString();
        String phone=txt_phone.getText().toString();
        String password=txt_pass.getText().toString();
        String confirm_password=txt_cpass.getText().toString();




        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Signup_Activity.this, MainActivity.class);
                startActivity(intent);
                finish();


             if (fullname.isEmpty()||email.isEmpty()||phone.isEmpty()||password.isEmpty()||confirm_password.isEmpty()) {
                 Toast.makeText(Signup_Activity.this, "Fill your infos !", Toast.LENGTH_SHORT).show();
             }else {

                 /*Intent intent = new Intent(Signup_Activity.this, MainActivity.class);
                 startActivity(intent);
                 finish();*/

             }
            }
        });

        txt_myacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Signup_Activity.this, Login_activity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}