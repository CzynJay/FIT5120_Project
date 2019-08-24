package com.example.expireddatetracker.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.expireddatetracker.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.Fragment;

import static android.content.Context.VIBRATOR_SERVICE;

public class HomeFragment extends Fragment {
    private ImageButton bt;
    private EditText searchBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View x  = inflater.inflate(R.layout.fragment_home, container, false);
        init(x);
        ImageButton bt = x.findViewById(R.id.searchbutton);
        searchBar = x.findViewById(R.id.searchbar);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String temp =  searchBar.getText().toString();
               search(temp);
            }
        });
        return x;
    }
    private void init(View x){
        String[] types = {"Fruit","Dairy & Milk","Meat","Seafood","Poultry","Vegetable"};
        //int[] colors = {Color.GREEN,Color.CYAN,Color.RED,Color.YELLOW,Color.RED,Color.YELLOW};
        Map<String, Integer> map = new HashMap<String,Integer>();
        map.put("Fruit",R.drawable.fruit);
        map.put("Dairy & Milk",R.drawable.milk);
        map.put("Meat",R.drawable.meat);
        map.put("Seafood",R.drawable.seafood);
        map.put("Poultry",R.drawable.poultry);
        map.put("Vegetable",R.drawable.vegetable);
        LinearLayout layout = x.findViewById(R.id.home_contain);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        width = (int)(width / (2.5));
        RelativeLayout.LayoutParams paramsBt = new RelativeLayout.LayoutParams(width, width);
        for(int temp=0;temp< types.length;temp++){
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View v = vi.inflate(R.layout.image_button, null);
            ImageButton bt1 = v.findViewById(R.id.img1);
            bt1.setImageResource(map.get(types[temp]));
            final TextView tx1  = v.findViewById(R.id.tx1);
            tx1.setText(types[temp]);
            temp++;
            ImageButton bt2 = v.findViewById(R.id.img2);
            bt2.setImageResource(map.get(types[temp]));
            final TextView tx2 = v.findViewById(R.id.tx2);
            tx2.setText(types[temp]);
            bt1.setLayoutParams(paramsBt);
            bt2.setLayoutParams(paramsBt);
            tx1.getLayoutParams().width = width;
            tx2.getLayoutParams().width = width;
            layout.addView(v);
            bt1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String temp = tx1.getText().toString();
                    search(temp);
                }
            });
            bt2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String temp = tx2.getText().toString();
                    search(temp);
                }
            });
        };

    }
    private void vibrate()
    {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(200);
        }

    }
    private void search(String q)
    {
        if(q.trim().length()==0){
            searchBar.setHint("Please enter food name");
            vibrate();
            searchBar.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake));}
        else{
        Bundle bundle = new Bundle();
        bundle.putString("key",q);
        Fragment fragment = new ResultFragment();
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right
                )
                .replace(R.id.fragment_container,fragment)
                .commit();
        }
    }


}
