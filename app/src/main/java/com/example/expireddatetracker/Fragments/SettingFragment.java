package com.example.expireddatetracker.Fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.expireddatetracker.Account_Activity;
import com.example.expireddatetracker.FeedbackActivity;
import com.example.expireddatetracker.ItemActivity;
import com.example.expireddatetracker.R;
import com.example.expireddatetracker.TipsActivity;

import androidx.fragment.app.Fragment;

public class SettingFragment extends Fragment {
    private View accountLayout,tipsLayout,feedbackLayout,websiteLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View settingView = inflater.inflate(R.layout.fragment_setting, container, false);
        initUI(settingView);
        return settingView;
    }

    //Initialise layouts items in settings page
    private void initUI(View parent){
        accountLayout = parent.findViewById(R.id.account_layout);
        tipsLayout = parent.findViewById(R.id.tips_layout);
        feedbackLayout = parent.findViewById(R.id.feedback_layout);
        websiteLayout = parent.findViewById(R.id.ourwebsite_layout);
        clickButtonListener();
    }

    //Buttons in settings page
    private void clickButtonListener()
    {
        //Account button
        accountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Account_Activity.class);
                startActivity(intent);
            }
        });
        //Tips button
        tipsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), TipsActivity.class);
                intent.putExtra("tips",getActivity().getIntent().getStringExtra("tips"));
                startActivity(intent);
            }
        });
        //Feedback button
        feedbackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FeedbackActivity.class);
                startActivity(intent);
            }
        });
        //Link to website
        websiteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.foodtyro.tk";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

    }


}
