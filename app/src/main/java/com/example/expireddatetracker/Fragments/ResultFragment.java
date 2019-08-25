package com.example.expireddatetracker.Fragments;


import android.os.Bundle;

import android.text.Layout;
import android.util.DisplayMetrics;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.expireddatetracker.R;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import androidx.fragment.app.Fragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ResultFragment extends Fragment {
    private TextView tx ;
    private ImageView bt;
    final private  String foodSource = "foodsource.json";
    private String foodname = "";
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
                    if (isNumeric(query))
                    {
                        if((int)temp.get("food_id")==Integer.parseInt(query))
                            result.put(temp);
                    }
                    else{
                    if (value.contains(query))
                        result.put(temp);}
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
        LinearLayout.LayoutParams paramsBt = new LinearLayout.LayoutParams(widthPixels, heightPixels/12);
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
            final JSONObject temp = (JSONObject) jsonArray.get(i);
            v.setTag(temp.get("food_id"));
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        foodname = temp.get("food_name").toString();
                        popup(v);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            main.setText(temp.getString("food_name"));
            v.setLayoutParams(paramsBt);
            sub.setText(temp.getString("food_subtitle").equals("null")?"":temp.getString("food_subtitle"));
            layout.addView(v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public void popup(View v) {
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
        final String tag = v.getTag().toString();
        title.setText(foodname);
        final View close = popupView.findViewById(R.id.back2list);
        final LinearLayout container = popupView.findViewById(R.id.edu_container);
        final Button storagebt = popupView.findViewById(R.id.storage_button);
        storagebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container.removeAllViews();
                popupwindowInit(container,"storage.json",tag);
            }
        });
        final Button cookingbt = popupView.findViewById(R.id.cooking_button);
        cookingbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container.removeAllViews();
                popupwindowInit(container,"cook.json",tag);
            }
        });
        popupwindowInit(container,"storage.json",tag);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void popupwindowInit(LinearLayout popup, String source, String id){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = (int) (displayMetrics.heightPixels );
        int width = (int)(displayMetrics.widthPixels);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width/2, height/6);
        JSONArray lists = loadJsonFile(source);
        JSONArray res = searchResult(lists,id);
        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View v = vi.inflate(R.layout.edu_row, null);
        final TextView tx = v.findViewById(R.id.edu_info);
        final ImageView im = v.findViewById(R.id.edu_img);
        if (res.length()==0) {
            tx.setText("no result");
            popup.addView(v);
        }
        else{
            try {
                JSONObject json = res.getJSONObject(0);
                if(source.equals("storage.json"))
                {
                    im.setImageResource(R.drawable.freeze);
                    im.setLayoutParams(params);
                    String temp = json.getString("storage_method_tips");
                    temp = temp.equals("NaN")||temp.equals("null")?"Not Available":temp;
                    tx.setText(temp);
                    tx.setLayoutParams(params);
                }
                else if (source.startsWith("c"))
                    tx.setText("succ");
                popup.addView(v);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
