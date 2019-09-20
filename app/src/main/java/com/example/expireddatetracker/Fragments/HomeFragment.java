package com.example.expireddatetracker.Fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.expireddatetracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.content.Context.VIBRATOR_SERVICE;

public class HomeFragment extends Fragment {
    private EditText searchBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatePage  = inflater.inflate(R.layout.fragment_home, container, false);
        init(inflatePage);
        //Authenticate user from Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        final ImageButton searchButton = inflatePage.findViewById(R.id.searchbutton);
        //Search bar
        searchBar = inflatePage.findViewById(R.id.searchbar);
        //Welcome message
        TextView welcome = inflatePage.findViewById(R.id.welcome_mes);
        assert mUser != null;
        String displayInfo = "Hello "+  mUser.getDisplayName();
        welcome.setText(displayInfo);
        //Remove hint on click
        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    searchBar.setHint("");
                else
                    searchBar.setHint("");
            }
        });
        searchBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    search(searchBar.getText().toString());
                    return true;
                }
                return false;
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String searchQuery =  searchBar.getText().toString();
               search(searchQuery);
            }
        });
        return inflatePage;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    //View main category
    private void init(View x){
        String[] types = {"Fruits","Dairy & Eggs","Meat","Seafood","Poultry","Vegetable"};
        //int[] colors = {Color.GREEN,Color.CYAN,Color.RED,Color.YELLOW,Color.RED,Color.YELLOW};
        Map<String, Integer> map = new HashMap<String,Integer>();
        map.put("Fruits",R.drawable.fruit);
        map.put("Dairy & Eggs",R.drawable.milk_eggs);
        map.put("Meat",R.drawable.meat);
        map.put("Seafood",R.drawable.seafood);
        map.put("Poultry",R.drawable.poultry);
        map.put("Vegetable",R.drawable.vegetable);
        LinearLayout layout = x.findViewById(R.id.home_contain);
        x.findViewById(R.id.viewAll_tx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search("all");
            }
        });
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        width = (int)(width / (3.5));
        LinearLayout.LayoutParams paramsBt = new LinearLayout.LayoutParams(width, width);
        for(int temp=0;temp< types.length;temp++){
            LayoutInflater vi = (LayoutInflater) Objects.requireNonNull(getContext())
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View v = vi.inflate(R.layout.image_button, null);
            ImageButton bt1 = v.findViewById(R.id.img1);
            ImageButton bt2 = v.findViewById(R.id.img2);
            ImageButton bt3 = v.findViewById(R.id.img3);
            final TextView tx1  = v.findViewById(R.id.tx1);
            final TextView tx2  = v.findViewById(R.id.tx2);
            final TextView tx3  = v.findViewById(R.id.tx3);
            bt1.setImageResource(map.get(types[temp]));
            tx1.setText(types[temp]);
            temp++;
            bt2.setImageResource(map.get(types[temp]));
            tx2.setText(types[temp]);
            temp++;
            bt3.setImageResource(map.get(types[temp]));
            tx3.setText(types[temp]);
            bt1.setLayoutParams(paramsBt);
            bt2.setLayoutParams(paramsBt);
            bt3.setLayoutParams(paramsBt);
            tx1.getLayoutParams().width = width;
            tx2.getLayoutParams().width = width;
            tx3.getLayoutParams().width = width;
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
            bt3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String temp = tx3.getText().toString();
                    search(temp);
                }
            });
        };

    }

    //Vibrate function
    private void vibrate()
    {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(200);
        }

    }

    //Search function
    private void search(String q)
    {
        if(q.trim().length()==0){
            searchBar.setText("");
            searchBar.clearFocus();
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
                .setCustomAnimations(R.anim.anim_slide_in_right,R.anim.anim_slide_out_right,
                        R.anim.anim_slide_in_left,R.anim.anim_slide_out_left
                ).addToBackStack(null)
                .replace(R.id.fragment_container,fragment)
                .commit();
        }
    }

}
