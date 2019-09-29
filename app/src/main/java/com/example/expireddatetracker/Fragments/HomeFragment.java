package com.example.expireddatetracker.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.expireddatetracker.ItemActivity;
import com.example.expireddatetracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

public class HomeFragment extends Fragment {
    private EditText searchBar;
    private LinearLayout fav_container;
    private LinearLayout fav_layout;
    private Map<String,Double> myMap = new HashMap<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatePage  = inflater.inflate(R.layout.fragment_home, container, false);
        init(inflatePage);
        fav_container = inflatePage.findViewById(R.id.fav_container);
        fav_layout = inflatePage.findViewById(R.id.fav_layout);
        //Authenticate user from Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        final ImageButton searchButton = inflatePage.findViewById(R.id.searchbutton);
        //Search bar
        searchBar = inflatePage.findViewById(R.id.searchbar);
        LoadPreference loadPreference = new LoadPreference();
        loadPreference.execute();
        //Welcome message
//        TextView welcome = inflatePage.findViewById(R.id.welcome_mes);
//        assert mUser != null;
//        String displayInfo = "Hello "+  mUser.getDisplayName();
//        welcome.setText(displayInfo);
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



    //View main category
    private void init(View x){
        String[] types = {"Fruits","Dairy & Eggs","Meat","Seafood","Poultry","Vegetable"};
//        int[] colors = {Color.CYAN,Color.RED,Color.YELLOW,Color.RED,Color.YELLOW,Color.GREEN};
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
//            bt1.setBackgroundColor(colors[temp]);
            temp++;
            bt2.setImageResource(map.get(types[temp]));
//            bt2.setBackgroundColor(colors[temp]);
            tx2.setText(types[temp]);
            temp++;
            bt3.setImageResource(map.get(types[temp]));
            tx3.setText(types[temp]);
//            bt3.setBackgroundColor(colors[temp]);
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

    private List<Map.Entry<String,Double>> readSharedPreference(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(FirebaseAuth
                .getInstance().getCurrentUser().getUid(),Context.MODE_PRIVATE);
        if (sharedPreferences!=null){
            String mapStr = sharedPreferences.getString("map","");
            if(!mapStr.equals(""))
            {
                try {
                    myMap = new Gson().fromJson(mapStr,myMap.getClass());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
           return sortMap(myMap);
        }
        return null;
    }


    private List<Map.Entry<String,Double>> sortMap(Map<String,Double> map)
    {
        List<Map.Entry<String,Double>> list = new LinkedList<>(map.entrySet());
        if(map.size()<=5)
            return list;
        Comparator<Map.Entry<String,Double>> cmp =  new Comparator<Map.Entry<String,Double>>() {

            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        };
        Comparator<Map.Entry<String,Double>> cmpReverse = Collections.reverseOrder(cmp);
        Collections.sort(list, cmpReverse);
        return list;
    }

    private class LoadPreference extends AsyncTask<Void,Void,List<Map.Entry<String,Double>>>{
        @Override
        protected List<Map.Entry<String,Double>> doInBackground(Void... voids) {
            return readSharedPreference();
        }

        @Override
        protected void onPostExecute(List<Map.Entry<String,Double>> entries) {
            LayoutInflater vi = (LayoutInflater) getContext()
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            super.onPostExecute(entries);
            if (entries==null || entries.size()==0) {
                fav_layout.setVisibility(View.GONE);
            } else {
                if (entries.size() > 5)
                    entries = entries.subList(0, 5);
                for (Map.Entry<String, Double> entry : entries) {
                    try {
                        View view = vi.inflate(R.layout.fav_item_layout,null);
                        final JSONObject temp = new JSONObject(entry.getKey());
                        TextView mainTx = view.findViewById(R.id.maintitle_tx);
                        TextView subTx = view.findViewById(R.id.subtitle_tx);
                        mainTx.setText(trimString(temp.getString("food_name")));
                        String subtext= temp.getString("food_subtitle");
                        if (!subtext.equals("null"))
                            subTx.setText(trimString(subtext));
                        else
                            subTx.setText("");
                        view.setBackgroundResource(R.drawable.round_button);
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), ItemActivity.class);
                                intent.putExtra("jsonObject",temp.toString());
                                startActivity(intent);
                            }
                        });
                        fav_container.addView(view);
                     } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }}
    private String trimString(String inputStr){
        String temp = inputStr;
        if (inputStr.length()>10)
            temp=inputStr.substring(0,10) + "...";
        return temp;
    }
    }


