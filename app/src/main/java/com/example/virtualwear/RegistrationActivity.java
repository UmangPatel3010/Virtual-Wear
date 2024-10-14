package com.example.virtualwear;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    EditText name,email,password;
    Button btn_signup;
    TextView txt_sigin;
    private FirebaseAuth auth;
    CheckBox chbk_show;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        auth=FirebaseAuth.getInstance();

        email=findViewById(R.id.edt_email);
        password=findViewById(R.id.edt_password);
        btn_signup = findViewById(R.id.btn_signup);
        txt_sigin = findViewById(R.id.txt_signin);
        chbk_show = findViewById(R.id.chbk_show);

        chbk_show.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked)
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                else
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        txt_sigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistrationActivity.this,LoginActivity.class));
            }
        });

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail=email.getText().toString();
                String userPassword=password.getText().toString();

                if(TextUtils.isEmpty(userEmail)) {

                    Toast.makeText(RegistrationActivity.this,"Enter Email Address!",Toast.LENGTH_LONG).show();
                    return;

                }

                if(TextUtils.isEmpty(userPassword)) {

                    Toast.makeText(RegistrationActivity.this,"Enter Password!",Toast.LENGTH_LONG).show();
                    return;

                }

                if(userPassword.length() < 8){

                    Toast.makeText(RegistrationActivity.this,"Password must be of minimum 8 characters",Toast.LENGTH_LONG).show();
                    return;

                }

                auth.createUserWithEmailAndPassword(userEmail,userPassword)
                        .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(RegistrationActivity.this,"Successfully Registered",Toast.LENGTH_LONG).show();
                                    auth.signOut();
                                    startActivity(new Intent(RegistrationActivity.this,LoginActivity.class));
                                }
                                else{
                                    Toast.makeText(RegistrationActivity.this,"Email Already Registered",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

    }
}