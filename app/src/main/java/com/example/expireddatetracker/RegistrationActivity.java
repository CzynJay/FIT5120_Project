package com.example.expireddatetracker;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailTV, passwordTV, nameTV;
    private ProgressBar progress;
    private ImageView backBt;
    private Button regBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

        initializeUI();
        //Register button clicked
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });
    }

    //Register new user function
    private void registerNewUser() {
        //Launch circle progress bar
        progress.setVisibility(View.VISIBLE);
        String email, password;
        email = emailTV.getText().toString();
        password = passwordTV.getText().toString();
        final String name = nameTV.getText().toString();
        //If name field is empty
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "Please enter Name...", Toast.LENGTH_LONG).show();
            progress.setVisibility(View.GONE);
            return;
        }
        //If email field is empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            progress.setVisibility(View.GONE);
            return;
        }
        //If invalid email was entered
        if (!UserLoginActivity.isEmailValid(email)){
            Toast.makeText(getApplicationContext(), "Please enter valid email...", Toast.LENGTH_LONG).show();
            progress.setVisibility(View.GONE);
            return;
        }
        //If password fiels is empty
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
            progress.setVisibility(View.GONE);
            return;
        }
        //If invalid password was entered
        if(!isPasswordValid(password))
        {
            Toast.makeText(getApplicationContext(), "password length should be at least 7", Toast.LENGTH_LONG).show();
            progress.setVisibility(View.GONE);
            return;
        }

        //Register user on firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                            user.updateProfile(profileUpdates);
                            //Message to user that registration is successful
                            Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                            progress.setVisibility(View.GONE);
                            Intent intent = new Intent(RegistrationActivity.this, UserLoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), task.getException().toString().split(":")[1], Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void initializeUI() {
        emailTV = findViewById(R.id.email);
        passwordTV = findViewById(R.id.password);
        regBtn = findViewById(R.id.register);
        nameTV = findViewById(R.id.nameView);
        progress = findViewById(R.id.register_pro);
        backBt = findViewById(R.id.register_back);
        backBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public static boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 6;
    }

}