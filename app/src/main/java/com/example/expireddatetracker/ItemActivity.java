package com.example.expireddatetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.example.expireddatetracker.Fragments.ResultFragment;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ItemActivity extends AppCompatActivity implements View.OnClickListener {
    private View cookIndicator,storageIndicator;
    private LinearLayout container;
    private Button storageBt,cookBt;
    private int height,width;
    private String foodID,mainTitle,subtitleText= "1";
    private String uid;
    private JSONArray storageJson= new JSONArray();
    private JSONArray cookJson = new JSONArray();
    final private String[] storageTypes = {"DOP_Refrigerate_Max","DOP_Pantry_Max","DOP_Freeze_Max"};
    private Calendar myCalendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener date;
    final private long dayInMilliseconds = 86400000;
    private PopupWindow popupWindow;
    private FirebaseFirestore db;
    private String startDate,endDate,where,timeSpan,navigation_title;
    private Map<String,Double> myMap = new HashMap<>();
    private TextView suggestionTx;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_details);
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth
                .getInstance().getCurrentUser().getUid();
        readIntent();
        initUI();
        InitButton();
        LoadJson load = new LoadJson();
        load.execute("cook.json");

        //Define the date of selected item
         date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

    }

    //Read the foodsource.json file
    private void readIntent()
    {
        try {
            storageJson.put(new JSONObject(getIntent().getStringExtra("jsonObject")));
            foodID = storageJson.getJSONObject(0).getString("food_id");
            mainTitle =storageJson.getJSONObject(0).getString("food_name");
            subtitleText = storageJson.getJSONObject(0).getString("food_subtitle");
            navigation_title = storageJson.getJSONObject(0).getString("Nav_category");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Update start date and best before date of selected item function
    private void updateLabel()
    {
        //myCalendar.getTimeInMillis()+dateConversion(duration)
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        startDate = sdf.format(myCalendar.getTime());
        endDate = sdf.format(new Date(myCalendar.getTime().getTime()+dateConversion(timeSpan)));
        Post upDateRecord =new Post();
        upDateRecord.execute();
        popupWindow.dismiss();
        finish();
    }

    //Initialize layout function
    private void  initUI()
    {
        TextView title = findViewById(R.id.foodname);
        TextView subTitle = findViewById(R.id.subname_label);
        suggestionTx  = findViewById(R.id.suggestTx);
        //Cooking and storing labels
        cookIndicator = findViewById(R.id.cook_indicator);
        storageIndicator =findViewById(R.id.storage_indicator);
        //Container of storage methods
        container = findViewById(R.id.edu_container);
        //Cooking and storing buttons
        storageBt = findViewById(R.id.storage_button);
        cookBt = findViewById(R.id.cooking_button);
        View close = findViewById(R.id.back2list);
        title.setText(mainTitle);
        //Make subtitle invisible if null
        if(subtitleText.equals("null"))
            subTitle.setVisibility(View.GONE);
        else subTitle.setText(subtitleText);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = (int) (displayMetrics.heightPixels );
        width = (int)(displayMetrics.widthPixels);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    //Try catch JSON file items in UTf-8
    private JSONArray getJson(String source)
    {
        String json ;
        JSONArray jarry = new JSONArray();
        try {
            InputStream is = getAssets().open(source);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
            jarry = new JSONArray(json);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return  jarry;
    }

    private  JSONArray searchResult(JSONArray jsonArray,String query)
    {
        JSONArray result= new JSONArray();
        for(int i =0;i<jsonArray.length();i++){
            try {
                JSONObject temp = (JSONObject) jsonArray.get(i);
                if((int)temp.get("food_id")==Integer.parseInt(query))
                    result.put(temp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        //Date picker from calendar
       DatePickerDialog datePickerDialog = new DatePickerDialog(ItemActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle("When did you buy it");
        //Set recommended day to choose
       datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
       datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-(long)v.getTag() + dayInMilliseconds);
       Toast.makeText(getBaseContext(),
              "If Purchase Date is not available when picking, your food might have passed its best before date",Toast.LENGTH_LONG).show();
       datePickerDialog.show();
    }

    private class LoadJson extends AsyncTask<String,Void,Void>
    {
        @Override
        protected Void doInBackground(String... strings) {
            cookJson = searchResult(getJson(strings[0]),foodID);
            readSharedPreference();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private String unitSwitcher(String src)
    {
        switch (src){
            case "DOP_Pantry_Max": return "DOP_Pantry_Metric";
            case "DOP_Freeze_Max": return "DOP_Freeze_Metric";
            case "DOP_Refrigerate_Max":return "DOP_Refrigerate_Metric";
            default: return "null";
        }
    }

    //Return string
    private String typeSwitcher(String src)
    {
        switch (src){
            case "DOP_Pantry_Max": return getResources().getString(R.string.pantry);
            case "DOP_Freeze_Max": return getResources().getString(R.string.freeze);
            case "Cooking_Temperature": return "Temperature";
            case "Preparation_size": return "Size";
            case "Cooking_time": return "Duration";
            default:return getResources().getString(R.string.refrigerate);
        }

    }

    //Images of icons for storage method
    private int imageSwitcher(String src)
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

    private View displayResult(JSONObject json,String item) throws JSONException
    {
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.edu_row, null);
        final TextView edu_info = v.findViewById(R.id.edu_info);
        final ImageView im = v.findViewById(R.id.edu_img);
        final TextView type = v.findViewById(R.id.edu_type);
        final Button addBt = v.findViewById(R.id.add_bt);
        String val = json.getString(item);
        val = val.equals("NaN")||val.equals("null")?"Not Recommended":val;
        String unit = unitSwitcher(item);
        //If null, replace with not recommended
        if (!unit.equals("null")&& !val.equals("Not Recommended"))
            val = ((int)((double)Double.valueOf(val)))+ " "+ json.getString(unit);
        else
            edu_info.setTextColor(getApplicationContext().getResources().getColor(R.color.black));
        val = item.equals("Cooking_Temperature") && !val.equals("Not Recommended")?val+" °C":val;
        type.setText(typeSwitcher(item));
        im.setImageResource(imageSwitcher(item));
        edu_info.setText(val);
        im.getLayoutParams().height = height/7;
        im.getLayoutParams().width = height/7;
        edu_info.getLayoutParams().height = height/7;
        edu_info.getLayoutParams().width = (int) (width/2.5);
        String [] tag = {typeSwitcher(item),val};
        if(ArrayUtils.contains( storageTypes, item ) )
        {
            addBt.setTag(tag);
            if (! edu_info.getText().toString().startsWith("Not")){
                    addBt.setVisibility(View.VISIBLE);
            addBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popUpChoice( (String[])v.getTag());
                }
            });}
        }
        return v;


    }

    //Switching between storage and cooking
    private void InitButton()
    {
        //Storage button function
        storageBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animSlide = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.fui_slide_out_left);
                animSlide.setDuration(500);
                animSlide.setAnimationListener(new Animation.AnimationListener() {
                    //Switch animation
                    @Override
                    public void onAnimationStart(Animation animation) {
                        storageIndicator.setVisibility(View.VISIBLE);
                        Animation animSlide = AnimationUtils.loadAnimation(getApplicationContext(),
                                R.anim.fui_slide_in_right);
                        animSlide.setDuration(500);
                        container.removeAllViews();
                        showResult("foodsource.json");
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
                if(storageIndicator.getVisibility()==View.GONE)
                    cookIndicator.startAnimation(animSlide);
            }
        });
        //Cooking button function
        cookBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animSlide = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.anim_slide_out_right);
                animSlide.setDuration(500);
                animSlide.setAnimationListener(new Animation.AnimationListener() {
                    //Switch animation
                    @Override
                    public void onAnimationStart(Animation animation) {
                        cookIndicator.setVisibility(View.VISIBLE);
                        Animation animSlide = AnimationUtils.loadAnimation(getApplicationContext(),
                                R.anim.anim_slide_in_right);
                        animSlide.setDuration(500);
                        container.removeAllViews();
                        showResult("cook.json");
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
                if(cookIndicator.getVisibility()==View.GONE)
                    storageIndicator.startAnimation(animSlide);
            }
        });
        storageBt.callOnClick();

    }

    //Storage method text
    private void showResult(String source)
    {
        JSONArray res = source.equals("foodsource.json")?storageJson:cookJson;
        if (res.length()==0) {
            suggestionTx.setVisibility(View.GONE);
            TextView error = new TextView(getApplicationContext());
            error.setText(R.string.no_recommend);
            error.setGravity(Gravity.CENTER);
            error.setTextSize(20);
            error.setTextColor(getResources().getColor(R.color.black));
            error.setTypeface(error.getTypeface(), Typeface.BOLD);
            error.setGravity(Gravity.CENTER);
            error.setPaintFlags(error.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
            container.addView(error);
        }
        else{
            try {
                suggestionTx.setVisibility(View.VISIBLE);
                if(source.equals("foodsource.json"))
                {
                    suggestionTx.setText(getResources().getString(R.string.storage_suggestion));
                    for(String item:storageTypes)
                    {
                        View v =displayResult(storageJson.getJSONObject(0),item);
                        container.addView(v);
                    }
                }
                else if (source.equals("cook.json"))
                {
                    suggestionTx.setText(getResources().getString(R.string.cook_suggestion));
                    String [] cook = {"Cooking_Temperature","Preparation_size","Cooking_time"};
                    for(int i=0;i<res.length();i++)
                    {
                        JSONObject temp = res.getJSONObject(i);
                        TextView titleView = buildTextView(width,temp);
                        container.addView(titleView);
                        for(String item:cook){
                            View v = displayResult(temp,item);
                            container.addView(v);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    //Recommended cooking method and temperature text
    private TextView buildTextView(int width, JSONObject temp) throws JSONException
    {
        String method = "preparation_text";
        TextView methodName = new TextView(getApplicationContext());
        LinearLayout.LayoutParams methodParmas =
                new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        methodParmas.gravity = Gravity.CENTER;
        methodName.setTextSize(20);
        methodName.setTextColor(getResources().getColor(R.color.black));
        methodName.setTypeface(methodName.getTypeface(), Typeface.BOLD);
        methodName.setGravity(Gravity.CENTER);
        methodName.setPaintFlags(methodName.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        methodName.setText("Cooking Method: "+ temp.getString(method));
        return  methodName;
    }

    //Popup add to storage confirmation function
    private void popUpChoice(String[] tag)
    {
        container.setVisibility(View.GONE);
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int)(displayMetrics.widthPixels*0.8);
        View popupView = layoutInflater.inflate(R.layout.tracking_type, null);
        popupWindow=new PopupWindow(popupView,
                width, LinearLayout.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.Animation_Design_BottomSheetDialog);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
        ResultFragment.applyDim(root,0.5f);
        final TextView title = popupView.findViewById(R.id.confirm_title);
        final TextView duration = popupView.findViewById(R.id.duration);
        final Button cancel = popupView.findViewById(R.id.cancel_Button);
        final Button confirm = popupView.findViewById(R.id.confirm_Button);
        confirm.setOnClickListener(this);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        //Set popup title
        title.setText("Store in "+tag[0]);
        duration.setText("Duration: " + tag[1]);
        where = tag[0];
        timeSpan = tag[1];
        confirm.setTag(dateConversion(tag[1]));
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                container.setVisibility(View.VISIBLE);
                ResultFragment.clearDim(root);
            }
        });
    }

    //Convert dayInMilliseconds to respective units
    private long dateConversion(String date)
    {
        Map<String,Long> conversion = new HashMap<>();
        conversion.put("Days",(long)dayInMilliseconds);
        conversion.put("Weeks", (long)dayInMilliseconds*7 );
        conversion.put("Months",(long)dayInMilliseconds*30) ;
        conversion.put("Years",(long)dayInMilliseconds*365);
        String[] temp = date.split( " ");
        return (long) (Integer.valueOf(temp[0].trim()) * conversion.get(temp[1].trim()))  ;
    }

    //Add records to database function
    private void postRecord()
    {
        String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        name = name==null?"":name;
        Map<String, Object>  record = new HashMap<>();
        record.put("PURCHASE_DATE", startDate);
        record.put("STORAGE_METHOD",where);
        record.put("EXPIRE_DATE",endDate);
        record.put("FOOD_ID",foodID);
        record.put("DISPLAY_NAME",mainTitle);
        record.put("SUB_NAME",subtitleText);
        record.put("NAV_TITLE",navigation_title);
        record.put("Owner",name);
        //Add to firebase
        db.collection("tracker").document(uid)
                .collection(where).document().set(record)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ItemActivity.this, "Added to storage successfully",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ItemActivity.this, "ERROR" +e.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.d("TAG", e.toString());
            }
        });
    }

    class Post extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            postRecord();
            try {
                writeSharedPreference(storageJson.getJSONObject(0).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void readSharedPreference(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(uid,Context.MODE_PRIVATE);
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
        }
    }

    private void writeSharedPreference(String item)
    {
        SharedPreferences sharedPreferences = this.getSharedPreferences(uid,Context.MODE_PRIVATE);
        if(myMap.containsKey(item))
        {
            myMap.put(item,myMap.get(item)+1);
        }
        else {
            myMap.put(item,1.0);
        }
        sharedPreferences.edit().putString("map",new Gson().toJson(myMap)).apply();
    }



}
