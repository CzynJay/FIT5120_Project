package com.example.expireddatetracker.Fragments;



import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.example.expireddatetracker.ItemActivity;
import com.example.expireddatetracker.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import androidx.fragment.app.Fragment;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ResultFragment extends Fragment{
    private TextView tx ;
    private ImageView bt;
    final private  String foodSource = "foodsource.json";
    private JSONArray foods = new JSONArray();
    Map<String,JSONArray> navi = new HashMap<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View x =  inflater.inflate(R.layout.fragment_result, container, false);
        Bundle bundle =  this.getArguments();
        tx = x.findViewById(R.id.query);
        bt = x.findViewById(R.id.back);
        String querry = bundle.getString("key");
        Log.e("check",querry);
        foods = loadJsonFile(foodSource);
        JSONArray result = searchResult(foods,querry);
        showNavigation(x);
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
        //JSONArray result= new JSONArray();

        for(int i =0;i<source.length();i++)
        {
            try {
                JSONObject temp = (JSONObject) source.get(i);
                String value = temp.toString().toLowerCase();
                query = query.toLowerCase();
                if (!multi) {
                    if (isNumeric(query))
                    {
                        if((int)temp.get("food_id")==Integer.parseInt(query)){
                            //result.put(temp);
                            putInMap(temp.getString("Nav_category"),temp);
                        }

                    }
                    else{
                    if (value.contains(query)){
                        //result.put(temp);
                        putInMap(temp.getString("Nav_category"),temp);
                    }}
                }
                else{
                    for(String s:query.split("&"))
                    {
                        if (value.contains(s.trim())){
                        //result.put(temp);
                        putInMap(temp.getString("Nav_category"),temp);
                        break;
                    }}
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void showNavigation(final View x){
        LinearLayout layout = x.findViewById(R.id.result_container);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        if(navi.isEmpty())
        {
            final View v = vi.inflate(R.layout.subtype_layout, null);
            TextView main = v.findViewById(R.id.subcateText);
            main.setText("No result, Please enter correct food name!");
            layout.addView(v);
            return;
        }
        for(Object key:navi.keySet().toArray())
        {
            if (navi.get(key).length() !=0){
            final View v = vi.inflate(R.layout.subtype_layout, null);
            v.setTag(key);
            TextView main = v.findViewById(R.id.subcateText);
            main.setText((String)key);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showResult(x,navi.get(v.getTag().toString()));
                }
            });
            layout.addView(v);
        }}
    }
    private void popUpWindow(String key,JSONArray jsonArray)
    {
        LayoutInflater layoutInflater = (LayoutInflater)getContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = (int) (displayMetrics.heightPixels );
        int width = (int)(displayMetrics.widthPixels);
        View popupView = layoutInflater.inflate(R.layout.cate_popup, null);
        final PopupWindow popupWindow=new PopupWindow(popupView,
                (int) (width*0.8), ViewGroup.LayoutParams.WRAP_CONTENT ,
                true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.Animation_Design_BottomSheetDialog);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        TextView navi_title = popupView.findViewById(R.id.navi_title);
        navi_title.setText(key);
        Button viewBt = popupView.findViewById(R.id.navi_search);

    }
    private void showResult(View x,JSONArray jsonArray){
        LinearLayout layout = x.findViewById(R.id.result_container);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthPixels  = displayMetrics.widthPixels;
        if (jsonArray.length()==0)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            final View v = vi.inflate(R.layout.search_row, null);
            TextView main = v.findViewById(R.id.mainname);
            TextView sub = v.findViewById(R.id.subname);
            main.setText("No result");
            sub.setText("Please enter correct food name");
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
                        String id = temp.getString("food_id");
                        Intent intent = new Intent(getContext(), ItemActivity.class);
                        intent.putExtra("id",id);
                        intent.putExtra("name",temp.get("food_name").toString());
                        startActivity(intent);
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

    private void putInMap(String cate,JSONObject object)
    {
        if(!navi.containsKey(cate))
            navi.put(cate,new JSONArray());
        else
            navi.get(cate).put(object);
    }
//    public void popup(View v) {
//        LayoutInflater layoutInflater = (LayoutInflater)getContext()
//                .getSystemService(LAYOUT_INFLATER_SERVICE);
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int height = (int) (displayMetrics.heightPixels );
//        int width = (int)(displayMetrics.widthPixels);
//        View popupView = layoutInflater.inflate(R.layout.item_details, null);
//        final PopupWindow popupWindow=new PopupWindow(popupView,
//                width, height,
//                true);
//        popupWindow.setTouchable(true);
//        popupWindow.setFocusable(true);
//        popupWindow.setAnimationStyle(R.style.Animation_Design_BottomSheetDialog);
//        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
//        final TextView title = popupView.findViewById(R.id.foodname);
//        final String tag = v.getTag().toString();
//        final View cookIndicator = popupView.findViewById(R.id.cook_indicator);
//        final View storageIndicator = popupView.findViewById(R.id.storage_indicator);
//        final LinearLayout container = popupView.findViewById(R.id.edu_container);
//        final Button storagebt = popupView.findViewById(R.id.storage_button);
//        final View close = popupView.findViewById(R.id.back2list);
//        title.setText(foodname);
//        storageIndicator.getLayoutParams().width =  (int)(width/2.5);
//        cookIndicator.getLayoutParams().width=(int)(width/2.5);
//        storagebt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Animation animSlide = AnimationUtils.loadAnimation(getContext(),
//                        R.anim.fui_slide_out_left);
//                animSlide.setDuration(500);
//                animSlide.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) {
//                        storageIndicator.setVisibility(View.VISIBLE);
//                        Animation animSlide = AnimationUtils.loadAnimation(getContext(),
//                                R.anim.fui_slide_in_right);
//                        animSlide.setDuration(500);
//                        container.removeAllViews();
//                        popupwindowInit(container,"foodsource.json",tag);
//                        storageIndicator.startAnimation(animSlide);
//                        container.startAnimation(animSlide);
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//                        cookIndicator.setVisibility(View.GONE);
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) {
//                    }
//                });
//                if(storageIndicator.getVisibility()==View.GONE)
//                cookIndicator.startAnimation(animSlide);
//            }
//        });
//        final Button cookingbt = popupView.findViewById(R.id.cooking_button);
//        cookingbt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Animation animSlide = AnimationUtils.loadAnimation(getContext(),
//                        R.anim.anim_slide_out_right);
//                animSlide.setDuration(500);
//                animSlide.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) {
//                        cookIndicator.setVisibility(View.VISIBLE);
//                        Animation animSlide = AnimationUtils.loadAnimation(getContext(),
//                                R.anim.anim_slide_in_right);
//                        animSlide.setDuration(500);
//                        container.removeAllViews();
//                        popupwindowInit(container,"cook.json",tag);
//                        cookIndicator.startAnimation(animSlide);
//                        container.startAnimation(animSlide);
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//                        storageIndicator.setVisibility(View.GONE);
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) {
//
//                    }
//                });
//                if(cookIndicator.getVisibility()==View.GONE)
//                    storageIndicator.startAnimation(animSlide);
//            }
//        });
//        storagebt.callOnClick();
//        close.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                popupWindow.dismiss();
//            }
//        });
//    }
//
//    @SuppressLint("ResourceAsColor")
//    private void popupwindowInit(final LinearLayout popup, String source, String id){
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int height = (int) (displayMetrics.heightPixels );
//        int width = (int)(displayMetrics.widthPixels);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(width/2.5), height/7);
//        JSONArray lists = source.equals("foodsource.json")?foods:loadJsonFile(source);
//        JSONArray res = searchResult(lists,id);
//        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
//        if (res.length()==0) {
//            View v = vi.inflate(R.layout.edu_row, null);
//            TextView edu_info = v.findViewById(R.id.edu_info);
//            ImageView im = v.findViewById(R.id.edu_img);
//            edu_info.setText("No Recommendation");
//            popup.addView(v);
//        }
//        else{
//            try {
//                JSONObject json = res.getJSONObject(0);
//                if(source.equals("foodsource.json"))
//                {
//                    for(String item:storageTypes)
//                    {
//                        View v =generateRow(json,item,params);
//                        popup.addView(v);
//                    }
//                }
//                else if (source.equals("cook.json"))
//                {
//                    String [] cook = {"Cooking_Temperature","Preparation_size","Cooking_time"};
//                    for(int i=0;i<res.length();i++)
//                    {
//                        JSONObject temp = res.getJSONObject(i);
//                        TextView titleView = buildTextView(width,temp);
//                        popup.addView(titleView);
//                        for(String item:cook){
//                            View v = generateRow(temp,item,params);
//                            popup.addView(v);
//                        }
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    private int imgSwithcher(String src)
//    {
//        switch (src)
//        {
//            case "DOP_Pantry_Max": return R.drawable.pantry;
//            case "DOP_Freeze_Max": return R.drawable.freeze;
//            case "Cooking_Temperature": return R.drawable.temperature;
//            case "Preparation_size": return R.drawable.size;
//            case "Cooking_time": return R.drawable.timer;
//            default:return R.drawable.refrigerate;
//        }
//
//    }
//
//    private String typeSwitcher(String src)
//    {
//        switch (src){
//            case "DOP_Pantry_Max": return "Pantry";
//            case "DOP_Freeze_Max": return "Freeze";
//            case "Cooking_Temperature": return "Temperature";
//            case "Preparation_size": return "Size";
//            case "Cooking_time": return "Duration";
//            default:return "Refrigerate";
//        }
//
//    }
//
//    private String unitSwitcher(String src){
//        switch (src){
//            case "DOP_Pantry_Max": return "DOP_Pantry_Metric";
//            case "DOP_Freeze_Max": return "DOP_Freeze_Metric";
//            case "DOP_Refrigerate_Max":return "DOP_Refrigerate_Metric";
//            default: return "null";
//        }
//    }
//
//    @SuppressLint("ResourceAsColor")
//    private TextView buildTextView(int width, JSONObject temp) throws JSONException {
//        String method = "preparation_text";
//        TextView methodName = new TextView(getActivity().getApplicationContext());
//        LinearLayout.LayoutParams methodParmas =
//                new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
//        methodParmas.gravity = Gravity.CENTER;
//        methodName.setTextSize(20);
//        methodName.setTextColor(R.color.fui_bgGitHub);
//        methodName.setTypeface(methodName.getTypeface(), Typeface.BOLD);
//        methodName.setGravity(Gravity.CENTER);
//        methodName.setPaintFlags(methodName.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
//        methodName.setText("Cooking Method: "+ temp.getString(method));
//        return  methodName;
//    }
//
//    private View generateRow(JSONObject json, String item, LinearLayout.LayoutParams params) throws JSONException {
//        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
//        View v = vi.inflate(R.layout.edu_row, null);
//        TextView edu_info = v.findViewById(R.id.edu_info);
//        ImageView im = v.findViewById(R.id.edu_img);
//        TextView type = v.findViewById(R.id.edu_type);
//        String val = json.getString(item);
//        val = val.equals("NaN")||val.equals("null")?"Not Recommended":val;
//        String unit = unitSwitcher(item);
//        if (!unit.equals("null")&& !val.equals("Not Recommended"))
//            val = ((int)((double)Double.valueOf(val)))+ " "+ json.getString(unit);
//        val = item.equals("Cooking_Temperature") && !val.equals("Not Recommended")?val+" °C":val;
//        type.setText(typeSwitcher(item));
//        im.setImageResource(imgSwithcher(item));
//        edu_info.setText(val);
//        im.setLayoutParams(params);
//        edu_info.setLayoutParams(params);
//        String [] tag = {typeSwitcher(item),val};
//        if(ArrayUtils.contains( storageTypes, item ) )
//        {v.setTag(tag);
//        v.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                popUpChoice( (String[])v.getTag());
//            }
//        });}
//        return v;
//    }
//
//    private void popUpChoice(String[] tag)
//    {
//        LayoutInflater layoutInflater = (LayoutInflater)getContext()
//                .getSystemService(LAYOUT_INFLATER_SERVICE);
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int width = (int)(displayMetrics.widthPixels*0.8);
//        View popupView = layoutInflater.inflate(R.layout.tracking_type, null);
//        final PopupWindow popupWindow=new PopupWindow(popupView,
//                width, LinearLayout.LayoutParams.WRAP_CONTENT,
//                true);
//        popupWindow.setTouchable(true);
//        popupWindow.setFocusable(true);
//        popupWindow.setAnimationStyle(R.style.Animation_Design_BottomSheetDialog);
//        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
//        final TextView title = popupView.findViewById(R.id.confirm_title);
//        final TextView duration = popupView.findViewById(R.id.duration);
//        final Button cancel = popupView.findViewById(R.id.cancel_Button);
//        final Button confirm = popupView.findViewById(R.id.confirm_Button);
//        confirm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//        cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                popupWindow.dismiss();
//            }
//        });
//        title.setText("I will store it in "+tag[0]);
//        duration.setText("Duration: " + tag[1]);
//    }

}
