package com.example.expireddatetracker.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.expireddatetracker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import androidx.fragment.app.Fragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

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
        boolean multi = false;
        if (query.split("&").length>1)
            multi=true;
        JSONArray result= new JSONArray();

        for(int i =0;i<source.length();i++)
        {
            try {
                JSONObject temp = (JSONObject) source.get(i);
                String value = temp.toString();
                if (!multi) {
                    if (value.contains(query))
                        result.put(temp);
                }
                else{
                    for(String s:query.split("&"))
                    {
                        if (value.contains(s.trim()))
                            result.put(temp);
                    }

                }
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
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
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
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
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
        LayoutInflater layoutInflater = (LayoutInflater)getActivity().getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = (int) (displayMetrics.heightPixels );
        int width = (int)(displayMetrics.widthPixels);
        View popupView = layoutInflater.inflate(R.layout.item_details, null);
        final PopupWindow popupWindow=new PopupWindow(popupView,
                width, height,
                true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.Animation_Design_BottomSheetDialog);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        final TextView title = popupView.findViewById(R.id.foodname);
        title.setText(v.getTag().toString());
        final View close = popupView.findViewById(R.id.back2list);
        final View container = popupView.findViewById(R.id.edu_container);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }
}
