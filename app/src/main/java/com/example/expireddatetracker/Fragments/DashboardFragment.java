package com.example.expireddatetracker.Fragments;


import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import com.example.expireddatetracker.MainActivity;
import com.example.expireddatetracker.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class DashboardFragment extends Fragment implements TabLayout.BaseOnTabSelectedListener {
    PieChart pieChart;
    List<PieEntry> entries =  new ArrayList<>();
    MainActivity activity ;
    Map<String, JSONArray> dayLeftDict = new HashMap<>();
    private final String SPOILED = "Spoiled";
    private final String TWODAYS = "Less than 2 days left";
    private final String TWO_SEVEN = "2-7 days left";
    private final String MORETHANAWEEK = "More than a week";
    long dayInMilliseconds = 86400000;
    private View progress;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatePage  = inflater.inflate(R.layout.fragment_dashboard, container, false);
        TabLayout tabs = inflatePage.findViewById(R.id.tabLayout);
        progress = inflatePage.findViewById(R.id.progressing);
        tabs.addOnTabSelectedListener(this);
        activity = (MainActivity) getActivity();
        pieChart = (PieChart) inflatePage.findViewById(R.id.piechart);
        pieChart.setDrawEntryLabels(true);
        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.animateY(2000);
        pieChart.setCenterTextSize(20f);
        fetchData(getResources().getString(R.string.pantry));
        return inflatePage;
    }

    private void fetchData(final String place)
    {
        entries.clear();
        pieChart.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        dayLeftDict.put(SPOILED,new JSONArray());
        dayLeftDict.put(TWODAYS,new JSONArray());
        dayLeftDict.put(TWO_SEVEN,new JSONArray());
        dayLeftDict.put(MORETHANAWEEK,new JSONArray());
        activity.db.collection("tracker")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(place).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        for(DocumentSnapshot item:task.getResult().getDocuments()){
                            placeToMap(item.getData());
                        }
                        pieChart.setVisibility(View.VISIBLE);
                        drawPieChart(place);
                    }
                    progress.setVisibility(View.GONE);
                }
            });
        }


    private void placeToMap(Map<String,Object> objectMap)
    {
        String date = objectMap.get("EXPIRE_DATE").toString();
        long dayleft = calculateDayDifference(date);
        if(dayleft<=0)
            dayLeftDict.get(SPOILED).put(objectMap);
        else if(dayleft <= dayInMilliseconds*2)
            dayLeftDict.get(TWODAYS).put(objectMap);
        else if(dayleft <=dayInMilliseconds*7)
            dayLeftDict.get(TWO_SEVEN).put(objectMap);
        else
            dayLeftDict.get(MORETHANAWEEK).put(objectMap);
    }

    private long calculateDayDifference(String date)
    {

        Date myDate = new Date();
        try {
            myDate= new SimpleDateFormat("dd/MM/yy", Locale.US).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  (myDate.getTime() - new Date().getTime());
    }

    private void drawPieChart(String title)
    {
        pieChart.setCenterText(title);
        for(String name: dayLeftDict.keySet())
        {
            int value = dayLeftDict.get(name).length();
            if (value!=0)
                entries.add(new PieEntry((float) value, name));
        }
        if (entries.size()==0)
            pieChart.setCenterText("No record yet");
        PieDataSet pieDataSet = new PieDataSet(entries, "");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setValueFormatter(new DefaultValueFormatter(0));
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(20f);
        pieChart.getLegend().setTextSize(16f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setData(pieData);
        pieChart.animateY(2000);

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        fetchData(tab.getText().toString());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
