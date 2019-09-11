package com.example.expireddatetracker;

import androidx.appcompat.app.AppCompatActivity;

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
    private ImageView backBt;
    private LinearLayout container;
    private JSONArray jsonArray = new JSONArray();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);
        initUI();
    }

    private void initUI()
    {
        backBt = findViewById(R.id.tips_back);
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
                tx.setTextSize(16);
                tx.setText(json.getString("tips_text"));
                container.addView(v);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
