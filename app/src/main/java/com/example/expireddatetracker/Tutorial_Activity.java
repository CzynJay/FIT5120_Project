package com.example.expireddatetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.example.expireddatetracker.R;
import com.hololo.tutorial.library.Step;

public class Tutorial_Activity extends com.hololo.tutorial.library.TutorialActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addFragment(new Step.Builder().setTitle("This is header")
                .setContent("This is content")
                .setBackgroundColor(Color.parseColor("#FF0957")) // int background color
                .setDrawable(R.drawable.warning) // int top drawable
                .setSummary("This is summary")
                .build());
        addFragment(new Step.Builder().setTitle("This is header")
                .setContent("This is content")
                .setBackgroundColor(Color.parseColor("#FF0957")) // int background color
                .setDrawable(R.drawable.veal) // int top drawable
                .setSummary("This is summary")
                .build());
        addFragment(new Step.Builder().setTitle("This is header")
                .setContent("This is content")
                .setBackgroundColor(Color.parseColor("#FF0957")) // int background color
                .setDrawable(R.drawable.fattyfish) // int top drawable
                .setSummary("This is summary")
                .build());
    }

    @Override
    public void currentFragmentPosition(int position) {

    }
}
