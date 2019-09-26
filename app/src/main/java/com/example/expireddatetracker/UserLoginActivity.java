package com.example.expireddatetracker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class UserLoginActivity extends AppCompatActivity {

    private EditText emailTV, passwordTV;
    private Button loginBtn;
    private ProgressBar progressBar;
    private TextView  tipsView;
    private View loginForm;
    private JSONArray tips;
    private FirebaseAuth mAuth;
    private View progressForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        mAuth = FirebaseAuth.getInstance();
        getJson();
        initializeUI();
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserAccount();
            }
        });
    }

    //At launch, check if user is authenticated
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            showProgress(true);
            UserLoginTask u= new UserLoginTask(true);
            u.execute();
        }
    }

    //After login or authenticated launch, initizalize tips and progress circle function
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        Random rand= new Random();
        int tipnum = rand.nextInt(tips.length());
        try {
            String temp =  tips.getJSONObject(tipnum).getString("tips_text");
            tipsView.setText(temp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            loginForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            progressForm.setVisibility(show?View.VISIBLE:View.GONE);

            progressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressForm.setVisibility(show ? View.VISIBLE : View.GONE);
            loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    //Login function
    private void loginUserAccount() {
        String email, password;
        email = emailTV.getText().toString();
        password = passwordTV.getText().toString();
        //If email field is empty
        if (TextUtils.isEmpty(email)) {
            emailTV.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake));
            Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            return;
        }
        //If invalid email was entered
        if (!isEmailValid(email))
        {   emailTV.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake));
            Toast.makeText(getApplicationContext(), "Email is invalid", Toast.LENGTH_LONG).show();
            return;
        }
        //If password field is empty
        if (TextUtils.isEmpty(password)) {
            passwordTV.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake));
            Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
            return;
        }
        showProgress(true);
        UserLoginTask u = new UserLoginTask(false);
        u.execute(email,password);

    }

    //Login logic
    private class UserLoginTask extends AsyncTask<String, Void, Boolean> {
        boolean res =false;
        UserLoginTask(boolean val)
        {
            res =val;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO: attempt authentication against a network service.
            if (!res){
                //Authenticate with Firebase
                mAuth.signInWithEmailAndPassword(params[0],params[1])
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    res = true;
                                } else {
                                    res=false;
                                }}
                        });}
            try {
                // Simulate network access.
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                return false;
            }
            return res;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                Intent toMain = new Intent(UserLoginActivity.this,MainActivity.class);
                toMain.putExtra("tips",tips.toString());
                startActivity(toMain);
                finish();
            }
            //If login failed
            else {
                showProgress(false);
                passwordTV.setError(getString(R.string.error_incorrect_password));
                passwordTV.requestFocus();
                Toast.makeText(getApplicationContext(), "Login failed! Please try again", Toast.LENGTH_LONG).show();
            }
        }
        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }

    //Initialize login layout
    private void initializeUI() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels );
        emailTV = findViewById(R.id.email);
        passwordTV = findViewById(R.id.password);
        tipsView = findViewById(R.id.tipsView);
        loginBtn = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar);
        loginForm = findViewById(R.id.email_login_form);
        TextView signUp = findViewById(R.id.sign_up);
        View appIcon = findViewById(R.id.app_icon);
        appIcon.getLayoutParams().width = (int)(width*0.5);
        appIcon.getLayoutParams().height = (int)(width*0.5);
        progressForm = findViewById(R.id.progress_layout);
        signUp.setPaintFlags(signUp.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(UserLoginActivity.this,RegistrationActivity.class);
                startActivity(it);
            }
        });
    }

    //Check if email is valid
    public static boolean isEmailValid(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    //Get data from tips.json
    private void getJson(){
        String json ;
        tips = new JSONArray();
        try {
            InputStream is = getAssets().open("tips.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
            tips = new JSONArray(json);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }
}
