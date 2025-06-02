package com.univbechar.mygreenhouse;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login_activity extends AppCompatActivity {

    EditText e_mail,e_passwprd;
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        e_mail=findViewById(R.id.editEMAIL1);
        e_passwprd=findViewById(R.id.editPassword1);
        btn_login=findViewById(R.id.button_login1);
        TextView txt_new_acc=findViewById(R.id.tx_creat);

        String email_text= e_mail.getText().toString();
        String pass_text= e_passwprd.getText().toString();

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Login_activity.this,FingerprintAuthActivity.class);
                startActivity(intent);
                //finish();


                if (email_text.isEmpty()||pass_text.isEmpty()){
                    Toast.makeText(Login_activity.this, "Fill your email and password !!!", Toast.LENGTH_SHORT).show();
                }else {
                  //
                }
            }
        });

        txt_new_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login_activity.this, Signup_Activity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}