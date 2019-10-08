package com.example.expireddatetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.expireddatetracker.Fragments.TrackFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {
    private RadioGroup choices;
    private EditText feedbackET;
    private RatingBar ratingBar;
    private Button submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        initUI();
        buttonListener();
    }

    //Initialize layout
    private void initUI()
    {
        submit = findViewById(R.id.submit_bt);
        choices = findViewById(R.id.feedback_radio);
        feedbackET = findViewById(R.id.feedback_tx);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        feedbackET.getLayoutParams().height = (int)(displayMetrics.heightPixels*0.4);
        ratingBar = findViewById(R.id.rating);
    }
    private void buttonListener()
    {
        //Back button
        ImageButton back = findViewById(R.id.feedback_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(feedbackET.getText().toString().trim().length() ==0)
                    feedbackET.setError("Empty");
                else {
                    Map<String,String> map = new HashMap<>();
                    RadioButton rb = findViewById(choices.getCheckedRadioButtonId());
                    map.put("Feedback_content",feedbackET.getText().toString());
                    map.put("Feedback_UserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    map.put("Feedback_start",String.valueOf(ratingBar.getRating()));
                    //Add feedback to Firebase
                    FirebaseFirestore.getInstance().collection("Feedback")
                            .document(rb.getText().toString())
                            .collection(date_to_str(new Date())).add(map);
                    Toast.makeText(getApplicationContext(),"Your feedback is appreciated!",Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
            }
        });
    }

    //Convert date to string
    private  String date_to_str(Date date)
    {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        return  sdf.format(date);
    }

}
