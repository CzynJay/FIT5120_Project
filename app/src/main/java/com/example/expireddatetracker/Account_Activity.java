package com.example.expireddatetracker;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.expireddatetracker.R;
import com.google.firebase.auth.FirebaseAuth;

public class Account_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        // set an exit transition
        getWindow().setExitTransition(new Explode());
        setContentView(R.layout.activity_account_);
        Button logoutBt = findViewById(R.id.logout_bt);
        logoutBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getBaseContext(),UserLoginActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });

    }

}
