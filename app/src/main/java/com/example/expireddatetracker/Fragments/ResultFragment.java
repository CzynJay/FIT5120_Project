package com.example.expireddatetracker.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.expireddatetracker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import androidx.fragment.app.Fragment;

public class ResultFragment extends Fragment implements View.OnClickListener {
    private TextView tx ;
    private ImageView bt;
    final private  String foodSource = "foodsource.json";
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View x =  inflater.inflate(R.layout.fragment_result, container, false);
        Bundle bundle =  this.getArguments();
        tx = x.findViewById(R.id.query);
        bt = x.findViewById(R.id.back);
        String querry = bundle.getString("key");
        JSONArray jarry = loadJsonFile(foodSource);
        JSONArray result = searchResult(jarry,querry);
        showResult(x,result);
        tx.setText(querry);
        bt.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 getActivity().getSupportFragmentManager()
                         .beginTransaction()
                         .setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right,
                                 android.R.anim.slide_in_left, android.R.anim.slide_out_right
                         )
                         .replace(R.id.fragment_container,new HomeFragment())
                         .commit();
             }
         });
        return x;
    }

    private JSONArray loadJsonFile(String source)
    {
        String json ;
        JSONArray jarry = new JSONArray();
        try {
            InputStream is = getActivity().getAssets().open(source);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer,"UTF-8");
            jarry = new JSONArray(json);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
    }
        return  jarry;
    }

    private JSONArray searchResult(JSONArray source,String query)
    {
        JSONArray result= new JSONArray();

        for(int i =0;i<source.length();i++)
        {
            try {
                JSONObject temp = (JSONObject) source.get(i);
                //todo
                if(temp.toString().contains(query))
                    result.put(temp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private void showResult(View x,JSONArray jsonArray){
        LinearLayout layout = x.findViewById(R.id.result_container);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels  = displayMetrics.widthPixels;
        LinearLayout.LayoutParams paramsBt = new LinearLayout.LayoutParams(widthPixels, heightPixels/8);
        if (jsonArray.length()==0)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View v = vi.inflate(R.layout.search_row, null);
            TextView main = v.findViewById(R.id.mainname);
            TextView sub = v.findViewById(R.id.subname);
            main.setText("No result");
            sub.setText("Please enter correct food name");
            v.setLayoutParams(paramsBt);
            layout.addView(v);
        }
        for(int i=0;i<jsonArray.length();i++)
        {
            try {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View v = vi.inflate(R.layout.search_row, null);
            TextView main = v.findViewById(R.id.mainname);
            TextView sub = v.findViewById(R.id.subname);
            JSONObject temp = (JSONObject) jsonArray.get(i);
            v.setTag(temp.get("food_id"));
            v.setOnClickListener(this);
            main.setText(temp.getString("food_name"));
            v.setLayoutParams(paramsBt);
            sub.setText(temp.getString("food_subtitle").equals("null")?"":temp.getString("food_subtitle"));
            layout.addView(v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void onClick(View v) {

    }
}
