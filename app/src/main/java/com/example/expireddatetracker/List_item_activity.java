package com.example.expireddatetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class List_item_activity extends AppCompatActivity {
    private JSONArray jsonArray = new JSONArray();
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itemlist_layout);
        initUI();
        displayRow();
    }

    //Initialize layout for search result item list
    private void initUI()
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height =  (displayMetrics.heightPixels );
        findViewById(R.id.itemlist_img).getLayoutParams().height = (int)(height/3);
        container = findViewById(R.id.itemlist_container);
        try {
            jsonArray = new JSONArray(getIntent().getStringExtra("jsonArray"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String keyTitle = getIntent().getStringExtra("Title");
        findViewById(R.id.listitem_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {onBackPressed();
            }
        });
        TextView titleTx = findViewById(R.id.itemlist_title);
        titleTx.setText(keyTitle);
        ImageView titleImg = findViewById(R.id.itemlist_titleimg);
        titleImg.setImageResource(MainActivity.String_to_img(keyTitle));
    }

    //Display row function of search results
    private void displayRow(){
            //If there is no search result
            if (jsonArray.length()==0)
            {
                LayoutInflater vi = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                final View v = vi.inflate(R.layout.search_row, null);
                TextView main = v.findViewById(R.id.mainname);
                TextView sub = v.findViewById(R.id.subname);
                main.setText("No result");
                sub.setText("Please enter correct food name");
                container.addView(v);
            }
            //If search result exists
            for(int i=0;i<jsonArray.length();i++)
            {
                try {
                    LayoutInflater vi = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
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
                                Intent intent = new Intent(getBaseContext(), ItemActivity.class);
                                intent.putExtra("id",id);
                                intent.putExtra("name",temp.get("food_name").toString());
                                intent.putExtra("sub",temp.get("food_subtitle").toString());
                                intent.putExtra("jsonObject",temp.toString());
                                finish();
                                startActivity(intent)
                                ;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    //Add food name and subtitle to row
                    main.setText(temp.getString("food_name"));
                    sub.setText(temp.getString("food_subtitle").equals("null")?"":temp.getString("food_subtitle"));
                    container.addView(v);

                } catch (Exception e) {
                    e.printStackTrace();
                } } }

}
