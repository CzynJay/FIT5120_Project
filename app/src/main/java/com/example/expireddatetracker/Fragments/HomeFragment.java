package com.example.expireddatetracker.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.expireddatetracker.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View x  = inflater.inflate(R.layout.fragment_home, container, false);
        String[] types = {"fruit","milk","meat","seafood","poultry","vegetable"};
        Map<String, Integer> map = new HashMap<String,Integer>();
        map.put("fruit",R.drawable.fruit);
        map.put("milk",R.drawable.milk);
        map.put("meat",R.drawable.meat);
        map.put("seafood",R.drawable.seafood);
        map.put("poultry",R.drawable.poultry);
        map.put("vegetable",R.drawable.vegetable);
        LinearLayout layout = x.findViewById(R.id.home_contain);
        for(int temp=0;temp< types.length;temp++){
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View v = vi.inflate(R.layout.image_button, null);
            ImageButton bt1 = v.findViewById(R.id.img1);
            bt1.setImageResource(map.get(types[temp]));
            TextView tx1  = v.findViewById(R.id.tx1);
            tx1.setText(types[temp]);
            temp++;
            ImageButton bt2 = v.findViewById(R.id.img2);
            bt2.setImageResource(map.get(types[temp]));
            TextView tx2 = v.findViewById(R.id.tx2);
            tx2.setText(types[temp]);
            layout.addView(v);
        }
        return x;
    }



}
