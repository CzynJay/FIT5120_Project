package com.example.expireddatetracker;
import android.content.Intent;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.expireddatetracker.Fragments.TrackFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailTV, passwordTV, nameTV;
    private ProgressBar progress;
    private Button regBtn;
    private FirebaseAuth mAuth;
    private CheckBox readBt;

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
        if(!readBt.isChecked())
        {
            Toast.makeText(getApplicationContext(), "Terms of use are not accepted", Toast.LENGTH_LONG).show();
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
                            String uid = user.getUid();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                            user.updateProfile(profileUpdates);

                            Map<String,Object> map = new HashMap<>();
                            map.put("Name",name);
                            map.put("Color","#FFAB13");
                            FirebaseFirestore.getInstance().collection("tracker")
                                    .document(uid).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Message to user that registration is successful
                                    Toast.makeText(getApplicationContext()
                                            , "Registration successful!", Toast.LENGTH_LONG).show();
                                    progress.setVisibility(View.GONE);
                                    Intent intent = new Intent(RegistrationActivity.this, Tutorial_Activity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                        else {
                            progress.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), task.getException().toString().split(":")[1], Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void initializeUI() {
        readBt = findViewById(R.id.checkbox_accept);
        TextView termTx = findViewById(R.id.termTx);
        termTx.setPaintFlags(termTx.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        emailTV = findViewById(R.id.email);
        passwordTV = findViewById(R.id.password);
        regBtn = findViewById(R.id.register);
        nameTV = findViewById(R.id.nameView);
        progress = findViewById(R.id.register_pro);
        ImageView backBt = findViewById(R.id.register_back);
        backBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        termTx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUpTermOfUse();
            }
        });

    }

    public static boolean isPasswordValid(String password) {
        return password.length() > 6;
    }

    private void popUpTermOfUse()
    {
        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int)(displayMetrics.widthPixels*0.8);
        int height = (int)(displayMetrics.heightPixels*0.7);
        View popupView = layoutInflater.inflate(R.layout.termofuse_popup, null);
        final PopupWindow popupWindow=new PopupWindow(popupView,
                width, height,
                true);
        //Allow popup to be touchable & focusable
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        TrackFragment.applyDim(root,0.5f);
        //Set popup animation
        popupWindow.setAnimationStyle(R.style.Animation_Design_BottomSheetDialog);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        popupView.findViewById(R.id.cancel_bt_term).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.accept_bt_term).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readBt.setChecked(true);
                popupWindow.dismiss();
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                TrackFragment.clearDim(root);
            }
        });
    }

}