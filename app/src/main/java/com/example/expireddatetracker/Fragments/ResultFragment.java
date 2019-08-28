package com.example.expireddatetracker.Fragments;


import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;

import android.text.Layout;
import android.util.DisplayMetrics;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.expireddatetracker.R;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.Fragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class ResultFragment extends Fragment {
    private TextView tx ;

    private ImageView bt;
    final private  String foodSource = "foodsource.json";
    private String foodname = "";
    private JSONArray foods = new JSONArray();
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View x =  inflater.inflate(R.layout.fragment_result, container, false);
        Bundle bundle =  this.getArguments();
        tx = x.findViewById(R.id.query);
        bt = x.findViewById(R.id.back);
        String querry = bundle.getString("key");
        foods = loadJsonFile(foodSource);
        JSONArray result = searchResult(foods,querry);
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
                String value = temp.toString().toLowerCase();
                query = query.toLowerCase();
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
                        result.put(temp);break;
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
        //This line was causing subtitles missing
        //int heightPixels = displayMetrics.heightPixels;
        int widthPixels  = displayMetrics.widthPixels;
        //LinearLayout.LayoutParams paramsBt = new LinearLayout.LayoutParams(widthPixels, heightPixels/20);
        if (jsonArray.length()==0)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            final View v = vi.inflate(R.layout.search_row, null);
            TextView main = v.findViewById(R.id.mainname);
            TextView sub = v.findViewById(R.id.subname);
            main.setText("No result");
            sub.setText("Please enter correct food name");
            int main_height = main.getMaxHeight();
            int sub_height = sub.getMaxHeight();
            int total_height = main_height + sub_height;


            LinearLayout.LayoutParams paramsBt = new LinearLayout.LayoutParams(widthPixels, total_height);
            //v.setLayoutParams(paramsBt);
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

            sub.setText(temp.getString("food_subtitle").equals("null")?"":temp.getString("food_subtitle"));

            int main_height = main.getMaxHeight();
            int sub_height = sub.getMaxHeight();
            int total_height = main_height + sub_height;


                LinearLayout.LayoutParams paramsBt = new LinearLayout.LayoutParams(widthPixels, total_height);

            v.setLayoutParams(paramsBt);
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
        final View cookIndicator = popupView.findViewById(R.id.cook_indicator);
        final View storageIndicator = popupView.findViewById(R.id.storage_indicator);
        title.setText(foodname);
        final View close = popupView.findViewById(R.id.back2list);
        storageIndicator.getLayoutParams().width =  (int)(width/2.5);
        cookIndicator.getLayoutParams().width=(int)(width/2.5);
        final LinearLayout container = popupView.findViewById(R.id.edu_container);
        final Button storagebt = popupView.findViewById(R.id.storage_button);
        storagebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animSlide = AnimationUtils.loadAnimation(getContext(),
                        R.anim.fui_slide_out_left);
                animSlide.setDuration(500);
                animSlide.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        storageIndicator.setVisibility(View.VISIBLE);
                        Animation animSlide = AnimationUtils.loadAnimation(getContext(),
                                R.anim.fui_slide_in_right);
                        animSlide.setDuration(500);
                        container.removeAllViews();
                        popupwindowInit(container,"foodsource.json",tag);
                        storageIndicator.startAnimation(animSlide);
                        container.startAnimation(animSlide);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        cookIndicator.setVisibility(View.GONE);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                cookIndicator.startAnimation(animSlide);



            }
        });
        final Button cookingbt = popupView.findViewById(R.id.cooking_button);
        cookingbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animSlide = AnimationUtils.loadAnimation(getContext(),
                        R.anim.anim_slide_out_right);
                animSlide.setDuration(500);
                animSlide.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        cookIndicator.setVisibility(View.VISIBLE);
                        Animation animSlide = AnimationUtils.loadAnimation(getContext(),
                                R.anim.anim_slide_in_right);
                        animSlide.setDuration(500);
                        container.removeAllViews();
                        popupwindowInit(container,"cook.json",tag);
                        cookIndicator.startAnimation(animSlide);
                        container.startAnimation(animSlide);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        storageIndicator.setVisibility(View.GONE);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                storageIndicator.startAnimation(animSlide);


            }
        });
        storagebt.callOnClick();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void popupwindowInit(final LinearLayout popup, String source, String id){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = (int) (displayMetrics.heightPixels );
        int width = (int)(displayMetrics.widthPixels);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(width/2.5), height/7);
        JSONArray lists = source.equals("foodsource.json")?foods:loadJsonFile(source);
        JSONArray res = searchResult(lists,id);
        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        if (res.length()==0) {
            View v = vi.inflate(R.layout.edu_row, null);
            TextView edu_info = v.findViewById(R.id.edu_info);
            ImageView im = v.findViewById(R.id.edu_img);
            edu_info.setText("No Recommendation");
            popup.addView(v);
        }
        else{
            try {
                JSONObject json = res.getJSONObject(0);
                if(source.equals("foodsource.json"))
                {
                    String[] storageTypes = {"DOP_Pantry_Max","DOP_Freeze_Max","DOP_Refrigerate_Max"};
                    for(String item:storageTypes)
                    {
                        View v = vi.inflate(R.layout.edu_row, null);
                        TextView edu_info = v.findViewById(R.id.edu_info);
                        ImageView im = v.findViewById(R.id.edu_img);
                        TextView type = v.findViewById(R.id.edu_type);
                        String temp = json.getString(item);
                        String unit = unitSwitcher(item);
                        temp = temp.equals("NaN")||temp.equals("null")?"Not Recommended":(String.valueOf((int)((double)Double.valueOf(temp)))+ " "+ json.getString(unit));
                        im.setImageResource(imgSwithcher(item));
                        type.setText(typeSwitcher(item));
                        edu_info.setText(temp);
                        im.setLayoutParams(params);
                        edu_info.setLayoutParams(params);
                        popup.addView(v);
                    }
                }
                else if (source.equals("cook.json"))
                {
                    String method = "preparation_text";
                    String [] cook = {"Cooking_Temperature","Preparation_size","Cooking_time"};
                    for(int i=0;i<res.length();i++)
                    {
                        JSONObject temp = res.getJSONObject(i);
                        TextView methodName = new TextView(getActivity().getApplicationContext());
                        LinearLayout.LayoutParams methodParmas =
                        new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
                        methodParmas.gravity = Gravity.CENTER;
                        methodName.setTextSize(20);
                        methodName.setTextColor(R.color.fui_bgGitHub);
                        methodName.setTypeface(methodName.getTypeface(), Typeface.BOLD);
                        methodName.setGravity(Gravity.CENTER);
                        methodName.setPaintFlags(methodName.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
                        methodName.setText("Cooking Method: "+ temp.getString(method));
                        popup.addView(methodName);
                        for(String item:cook){
                            View v = vi.inflate(R.layout.edu_row, null);
                            TextView edu_info = v.findViewById(R.id.edu_info);
                            ImageView im = v.findViewById(R.id.edu_img);
                            TextView type = v.findViewById(R.id.edu_type);
                            String val = json.getString(item);
                            val = val.equals("NaN")||val.equals("null")?"Not Recommended":val;
                            val = item.equals("Cooking_Temperature") && !val.equals("Not Recommended")?val+" °C":val;
                            type.setText(typeSwitcher(item));
                            im.setImageResource(imgSwithcher(item));
                            edu_info.setText(val);
                            im.setLayoutParams(params);
                            edu_info.setLayoutParams(params);
                            popup.addView(v);
                        }

                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private int imgSwithcher(String src)
    {
        switch (src)
        {
            case "DOP_Pantry_Max": return R.drawable.pantry;
            case "DOP_Freeze_Max": return R.drawable.freeze;
            case "Cooking_Temperature": return R.drawable.temperature;
            case "Preparation_size": return R.drawable.size;
            case "Cooking_time": return R.drawable.timer;
            default:return R.drawable.refrigerate;
        }

    }

    private String typeSwitcher(String src)
    {
        switch (src){
            case "DOP_Pantry_Max": return "Pantry";
            case "DOP_Freeze_Max": return "Freeze";
            case "Cooking_Temperature": return "Temperature";
            case "Preparation_size": return "Size";
            case "Cooking_time": return "Duration";
            default:return "Refrigerate";
        }

    }

    private String unitSwitcher(String src){
        switch (src){
            case "DOP_Pantry_Max": return "DOP_Pantry_Metric";
            case "DOP_Freeze_Max": return "DOP_Freeze_Metric";
            default:return "DOP_Refrigerate_Metric";

        }
    }

}
