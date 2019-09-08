package com.example.expireddatetracker.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.expireddatetracker.MainActivity;
import com.example.expireddatetracker.Models.CircularProgressBar;
import com.example.expireddatetracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class TrackFragment extends Fragment implements View.OnClickListener {
    private GridLayout container;
    private MainActivity activity;
    private String uid;
    final private String FOODID = "FOOD_ID";
    final private String METHOD = "STORAGE_METHOD";
    final private String EXPIRE = "EXPIRE_DATE";
    final private String STARTDATE = "PURCHASE_DATE";
    final private String DISPLAY = "DISPLAY_NAME";
    private ArrayList<Map<String,Object>> freeze = new ArrayList<>();
    private ArrayList<Map<String,Object>> refrigerate = new ArrayList<>();
    private ArrayList<Map<String,Object>> pantry = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout =  inflater.inflate(R.layout.fragment_track, container, false);
        init(layout);

        return layout;
}
    private void init(View x){
        container = x.findViewById(R.id.storage_container);
        activity = (MainActivity) getActivity();
        final ProgressBar progressing = x.findViewById(R.id.progressing);
        progressing.setVisibility(View.VISIBLE);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        activity.db.collection("tracker").document(uid)
                .collection("records").get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for(DocumentSnapshot item:task.getResult().getDocuments()){
                        placeToArrayList(item.getData(),item.getId());
                    }
                    progressing.setVisibility(View.GONE);
                    displayStatus(refrigerate);
                }
            }
        });

    }

    private void displayStatus( ArrayList<Map<String,Object>> lists){
        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width =(int) (displayMetrics.widthPixels / 6.5);

        for(Map<String,Object> item:lists)
        {
            Animation animSlide = AnimationUtils.loadAnimation(getContext(),
                    R.anim.fade_in);
            animSlide.setDuration(1100);
            try {
            final View v = vi.inflate(R.layout.storage_icon, null);
            RelativeLayout layout = v.findViewById(R.id.circle_container);
            TextView name = v.findViewById(R.id.storage_name);
            name.setText(item.get(DISPLAY).toString());
            CircularProgressBar progressBar = v.findViewById(R.id.storage_progressBar);
            View warning = v.findViewById(R.id.warning);
            calculateProgress(progressBar,item.get(EXPIRE).toString(),item.get(STARTDATE).toString(),warning);
            layout.getLayoutParams().width = width;
            layout.getLayoutParams().height = width;
                warning.getLayoutParams().width = layout.getLayoutParams().width /3;
                warning.getLayoutParams().height = layout.getLayoutParams().height /3;
            v.startAnimation(animSlide);
            v.setTag(item);
            v.setOnClickListener(this);
            container.addView(v);
            ((GridLayout.LayoutParams) v.getLayoutParams()).columnSpec =
                    GridLayout.spec(GridLayout.UNDEFINED, 1f);
            }catch (Exception e){e.printStackTrace();}
        }
    }

    private void calculateProgress(CircularProgressBar circle,String end,String start,View warning)
    {
        try {
            Date end_date= new SimpleDateFormat("MM/dd/yy",Locale.US).parse(end);
            Date start_date =  new SimpleDateFormat("MM/dd/yy", Locale.US).parse(start);
            float percent= ( System.currentTimeMillis() -start_date.getTime() )*100/(end_date.getTime() - start_date.getTime());
            circle.setProgress(percent);
            if (percent >70f)
                warning.setVisibility(View.VISIBLE);
            else
                warning.setVisibility(View.GONE);
        } catch (ParseException e) {
            circle.setProgress(50.0f);
            e.printStackTrace();
        }
    }

    private void placeToArrayList(Map<String,Object> item,String id){
            item.put("id",id);
            switch (item.get(METHOD).toString()){
                case "Refrigerate":
                    refrigerate.add(item);break;
                case "Freeze":
                    freeze.add(item);break;
                case "Pantry":
                    pantry.add(item);break;
            }
    }


    @Override
    public void onClick(View v) {
        
    }
}


