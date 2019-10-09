package com.example.expireddatetracker;

import android.graphics.Color;
import android.os.Bundle;

import com.hololo.tutorial.library.Step;

//Tutorial page
public class Tutorial_Activity extends com.hololo.tutorial.library.TutorialActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addFragment(new Step.Builder().setTitle("Welcome to FoodTyro")
                .setContent("Your one-stop solution for managing fresh foods the right way")
                .setSummary("An app aimed to prevent food poisoning")
                .setBackgroundColor(Color.parseColor("#1A237E")) // int background color
                .setDrawable(R.drawable.app_icon)// int top drawable
                .build());
        addFragment(new Step.Builder().setTitle("Storage Guide")
                .setContent("Go through selections of fresh foods and find out where you should store them and for how long")
                .setBackgroundColor(Color.parseColor("#1A237E")) // int background color
                .setDrawable(R.drawable.tutorial_2) // int top drawable
                .build());
        addFragment(new Step.Builder().setTitle("Cooking Guide")
                .setContent("Never undercook your food again")
                .setBackgroundColor(Color.parseColor("#1A237E")) // int background color
                .setDrawable(R.drawable.tutorial_3) // int top drawable
                .build());
        addFragment(new Step.Builder().setTitle("Track Your Storage")
                .setContent("Track the spoilage progress and receive notification when the food is approaching its best before date")
                .setBackgroundColor(Color.parseColor("#1A237E")) // int background color
                .setDrawable(R.drawable.tutorial_4) // int top drawable
                .build());
        addFragment(new Step.Builder().setTitle("Collaborate with Your Housemates")
                .setContent("Invite or join a group with your housemates and manage food storage together")
                .setBackgroundColor(Color.parseColor("#1A237E")) // int background color
                .setDrawable(R.drawable.tutorial_5) // int top drawable
                .build());
    }

    @Override
    public void currentFragmentPosition(int position) {

    }
}
