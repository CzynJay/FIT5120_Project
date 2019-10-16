package com.example.expireddatetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TipsActivity extends AppCompatActivity {
    private LinearLayout container;
    private JSONArray jsonArray = new JSONArray();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);
        initUI();
    }

    //Initialize layout
    private void initUI()
    {
        ImageView backBt = findViewById(R.id.tips_back);
        container = findViewById(R.id.tips_container);
        try {
            jsonArray = new JSONArray(getIntent().getStringExtra("tips"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        displayTips(jsonArray);
        backBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    //Display tips function
    private void displayTips(JSONArray jsonArray)
    {
        for(int i = 0; i<jsonArray.length();i++)
        {
            try {
                JSONObject json = jsonArray.getJSONObject(i);
                LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                final View v = vi.inflate(R.layout.subtype_layout, null);
                v.findViewById(R.id.right_arrow).setVisibility(View.GONE);
                v.findViewById(R.id.image_display).setVisibility(View.GONE);
                TextView tx = v.findViewById(R.id.subcateText);
                tx.setPadding(0,35,0,35);
                tx.setTypeface(Typeface.DEFAULT);
                tx.setTextSize(16);
                tx.setText(json.getString("tips_text"));
                container.addView(v);
                LinearLayout.LayoutParams lp =  (LinearLayout.LayoutParams)v.getLayoutParams();
                lp.setMarginStart(20);
                lp.setMarginEnd(20);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
