package com.example.expireddatetracker.Fragments;


import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.JsonReader;
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
    private PieChart pieChart;
    private List<PieEntry> entries =  new ArrayList<>();
    private MainActivity activity ;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatePage  = inflater.inflate(R.layout.fragment_dashboard, container, false);
        TabLayout tabs = inflatePage.findViewById(R.id.tabLayout);
        View progress = inflatePage.findViewById(R.id.progressing);
        tabs.addOnTabSelectedListener(this);
        activity = (MainActivity) getActivity();
        pieChart = (PieChart) inflatePage.findViewById(R.id.piechart);
        pieChart.setDrawEntryLabels(true);
        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.animateXY(300,300);
        pieChart.setCenterTextSize(20f);
        Log.e("c",activity.dayLeftFreeze.toString());
        Log.e("c",activity.dayLeftPantry.toString());
        Log.e("c",activity.dayLeftRefrige.toString());
        drawPieChart(getResources().getString(R.string.refrigerate),switchMap(getResources().getString(R.string.refrigerate)));
//        fetchData();
        return inflatePage;
    }

//    private void fetchData(final String place)
//    {
//        entries.clear();
//        pieChart.setVisibility(View.GONE);
//        progress.setVisibility(View.VISIBLE);
//        dayLeftDict.put(SPOILED,new JSONArray());
//        dayLeftDict.put(TWODAYS,new JSONArray());
//        dayLeftDict.put(TWO_SEVEN,new JSONArray());
//        dayLeftDict.put(MORETHANAWEEK,new JSONArray());
//        activity.db.collection("tracker")
//                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .collection(place).get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                    if (task.isSuccessful()){
//                        for(DocumentSnapshot item:task.getResult().getDocuments()){
//                            placeToMap(item.getData());
//                        }
//                        pieChart.setVisibility(View.VISIBLE);
//                        drawPieChart(place,switchMap(place));
//                    }
//                    progress.setVisibility(View.GONE);
//                }
//            });
//        }

        private Map<String, JSONArray> switchMap(String place)
        {
            switch (place)
            {
                case "Pantry": return activity.dayLeftPantry;
                case "Freezer": return activity.dayLeftFreeze;
                default: return activity.dayLeftRefrige;
            }

        }

//    private void placeToMap(Map<String,Object> objectMap)
//    {
//        String date = objectMap.get("EXPIRE_DATE").toString();
//        long dayleft = calculateDayDifference(date);
//        long dayInMilliseconds = 86400000;
//        if(dayleft<=0)
//            dayLeftDict.get(SPOILED).put(objectMap);
//        else if(dayleft <= dayInMilliseconds *2)
//            dayLeftDict.get(TWODAYS).put(objectMap);
//        else if(dayleft <= dayInMilliseconds *7)
//            dayLeftDict.get(TWO_SEVEN).put(objectMap);
//        else
//            dayLeftDict.get(MORETHANAWEEK).put(objectMap);
//    }
//
//    private long calculateDayDifference(String date)
//    {
//        Date myDate = new Date();
//        try {
//            myDate= new SimpleDateFormat("dd/MM/yy", Locale.US).parse(date);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return  (myDate.getTime() - new Date().getTime());
//    }

    private void drawPieChart(String title,Map<String,JSONArray> entryMap)
    {
        entries.clear();
        pieChart.setCenterText(title);
        for(String name: entryMap.keySet())
        {
            int value = entryMap.get(name).length();
            if (value!=0)
                entries.add(new PieEntry((float) value, name));
        }
        if (entries.size()==0)
            pieChart.setCenterText("No record yet");

        PieDataSet pieDataSet = new PieDataSet(entries, "");
        final int[] chart_color = {Color.rgb(244,67,54), Color.rgb(255,235,59), Color.rgb(76,175,80)};
        pieDataSet.setColors(chart_color);
        pieDataSet.setValueFormatter(new DefaultValueFormatter(0));
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(20f);
        pieChart.getLegend().setTextSize(14f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setData(pieData);
        pieChart.animateY(1300);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        drawPieChart(tab.getText().toString(),switchMap(tab.getText().toString()));
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }
}
