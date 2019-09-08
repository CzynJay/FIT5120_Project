package com.example.expireddatetracker.Fragments;

import android.content.ClipData;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.expireddatetracker.MainActivity;
import com.example.expireddatetracker.Models.CircularProgressBar;
import com.example.expireddatetracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TrackFragment extends Fragment implements View.OnClickListener, TabLayout.BaseOnTabSelectedListener {
    private GridLayout container;
    private MainActivity activity;
    private String uid;
    final private String FOODID = "FOOD_ID";
    final private String METHOD = "STORAGE_METHOD";
    final private String EXPIRE = "EXPIRE_DATE";
    final private String STARTDATE = "PURCHASE_DATE";
    final private String DISPLAY = "DISPLAY_NAME";
    private TabLayout tabs;
    private View progressing,errorTx,scrollView ;
    private Button quickDiscardBt,quickConsumeBt;
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
        scrollView = x.findViewById(R.id.scroll_container);
        tabs = x.findViewById(R.id.tabLayout);
        quickDiscardBt = x.findViewById(R.id.quick_discard_bt);
        quickConsumeBt = x.findViewById(R.id.quick_consume_bt);
        quickDiscardBt.setTag(quickDiscardBt.getText());
        quickConsumeBt.setTag(quickConsumeBt.getText());
        quickConsumeBt.setOnDragListener(new MyDragListener());
        quickDiscardBt.setOnDragListener(new MyDragListener());
        container = x.findViewById(R.id.storage_container);
        errorTx = x.findViewById(R.id.no_record);
        activity = (MainActivity) getActivity();
        progressing = x.findViewById(R.id.progressing);
        progressing.setVisibility(View.VISIBLE);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tabs.addOnTabSelectedListener(this);
        fetchData("Refrigerate");
    }

    private ArrayList<Map<String,Object>> typeSwitch(String type)
    {
        switch (type)
        {
            case "Freeze": return freeze;
            case "Pantry": return pantry;
            case "Refrigerate": return refrigerate;
        }
         return new ArrayList<>();
    }
    private int imageSwitch(String type){
        switch (type)
        {
            //case "Freeze": return R.drawable.refrigerator;
            case "Pantry": return R.drawable.empty_pantry;
            case "Refrigerate": return R.drawable.refrigerator;
        }
        return R.drawable.refrigerator;

    }
    private void fetchData(final String type)
    {
        final ArrayList<Map<String,Object>> temp = typeSwitch(type);
        if (temp.size()==0){
        progressing.setVisibility(View.VISIBLE);
        activity.db.collection("tracker").document(uid)
                .collection(type).get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for(DocumentSnapshot item:task.getResult().getDocuments()){
                                placeToArrayList(item.getData(),item.getId());
                            }
                            progressing.setVisibility(View.GONE);
                            displayStatus(temp);
                        }
                    }
                });}
        else
            displayStatus(temp);
    }

    private void displayStatus( ArrayList<Map<String,Object>> lists){
        container.removeAllViews();
        errorTx.setVisibility(View.GONE);
        if(lists.size()==0){
            errorTx.setVisibility(View.VISIBLE);
                        return;}
        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width =(int) (displayMetrics.widthPixels / 6.5);
        for(Map<String,Object> item:lists)
        {
            Animation animSlide = AnimationUtils.loadAnimation(getContext(),R.anim.fade_in);
            animSlide.setDuration(1100);
            try {
            View v = vi.inflate(R.layout.storage_icon, null);
            RelativeLayout layout = v.findViewById(R.id.circle_container);
            TextView name = v.findViewById(R.id.storage_name);
            name.setText(item.get(DISPLAY).toString());
            ImageButton img = v.findViewById(R.id.storage_button);
            CircularProgressBar progressBar = v.findViewById(R.id.storage_progressBar);
            View warning = v.findViewById(R.id.warning);
            calculateProgress(progressBar,item.get(EXPIRE).toString(),item.get(STARTDATE).toString(),warning);
            layout.getLayoutParams().width = width;
            layout.getLayoutParams().height = width;
            warning.getLayoutParams().width = layout.getLayoutParams().width /3;
            warning.getLayoutParams().height = layout.getLayoutParams().height /3;
            v.startAnimation(animSlide);
            img.setTag(item);
            img.setOnClickListener(this);
            v.setOnTouchListener(new MyTouchListener());
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
        popUpWindow(v);
    }

    private void  popUpWindow(View v)
    {
        final ViewGroup root = (ViewGroup) getActivity().getWindow().getDecorView().getRootView();
        Map<String,Object> map = (Map<String, Object>) v.getTag();
        LayoutInflater layoutInflater = (LayoutInflater)getContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int)(displayMetrics.widthPixels*0.8);
        View popupView = layoutInflater.inflate(R.layout.record_info, null);
        PopupWindow popupWindow=new PopupWindow(popupView,
                width, LinearLayout.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        applyDim(root,0.5f);
        popupWindow.setAnimationStyle(R.style.Animation_Design_BottomSheetDialog);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
         TextView detail_name = popupView.findViewById(R.id.record_name);
         TextView detail_subname = popupView.findViewById(R.id.record_subname);
         TextView purchaseTx = popupView.findViewById(R.id.purchase_date);
         TextView expireTx = popupView.findViewById(R.id.expire_date);
         TextView storageTx = popupView.findViewById(R.id.storage_type);
         detail_name.setText(map.get(DISPLAY).toString());
         purchaseTx.setText("Purchase Date: " + map.get(STARTDATE).toString());
         expireTx.setText("Expire Date: " + map.get(EXPIRE).toString());
         storageTx.setText("Storage Type: "+ map.get(METHOD).toString());
         popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
             @Override
             public void onDismiss() {
                 clearDim(root);
             }
         });
    }

    public static void applyDim(@NonNull ViewGroup parent, float dimAmount){
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * dimAmount));

        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    public static void clearDim(@NonNull ViewGroup parent) {
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.clear();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        errorTx.setVisibility(View.GONE);
        //scrollView.setBackgroundResource(imageSwitch(tab.getText().toString()));
        fetchData(tab.getText().toString());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private final class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        view);
                view.startDrag(data, shadowBuilder, view, 0);
                //view.setVisibility(View.INVISIBLE);
                return true;
            } else {
                return false;
            }
        }
    }

    class MyDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            Button bt = (Button) v;
            int color = bt.getText().toString().equals("Discard")?R.color.red:R.color.green;
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundResource(R.drawable.drag_enter);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundResource(color);
                    break;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign View to ViewGroup
                    View view = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) view.getParent();
                    owner.removeView(view);
                    view.setVisibility(View.GONE);
                    Toast.makeText(getContext(),v.getTag().toString() + " Successfully",Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }

            return true;
        }
    }
}


