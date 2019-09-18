package com.example.expireddatetracker.Fragments;

import android.content.ClipData;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TrackFragment extends Fragment implements View.OnClickListener, TabLayout.BaseOnTabSelectedListener {
    private GridLayout container;
    private MainActivity activity;
    private String uid;
    final private String METHOD = "STORAGE_METHOD";
    final private String EXPIRE = "EXPIRE_DATE";
    final private String STARTDATE = "PURCHASE_DATE";
    final private String DISPLAY = "DISPLAY_NAME";
    private TabLayout tabs;
    private View progressing,errorTx;
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
        tabs = x.findViewById(R.id.tabLayout);
        Button quickDiscardBt = x.findViewById(R.id.quick_discard_bt);
        Button quickConsumeBt = x.findViewById(R.id.quick_consume_bt);
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
        fetchData("Pantry");
    }

    private ArrayList<Map<String,Object>> typeSwitch(String type)
    {
        switch (type)
        {
            case "Freezer": return freeze;
            case "Pantry": return pantry;
            case "Refrigerator": return refrigerate;
        }
         return new ArrayList<>();
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

        errorTx.setVisibility(View.GONE);
        if(lists.size()==0){
            errorTx.setVisibility(View.VISIBLE);
                        return;}

        for(Map<String,Object> item:lists)
     {
            try {
            LayoutInflater vi = (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(LAYOUT_INFLATER_SERVICE);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width =(int) (displayMetrics.widthPixels / 5.5);
            View v = vi.inflate(R.layout.storage_icon, null);
            RelativeLayout layout = v.findViewById(R.id.circle_container);
            TextView name = v.findViewById(R.id.storage_name);
            name.setText(item.get(DISPLAY).toString());
            int imgResource = R.drawable.app_icon;
              if(item.get("NAV_TITLE")!=null)
                  imgResource = MainActivity.String_to_img(item.get("NAV_TITLE").toString());
            ImageButton img = v.findViewById(R.id.storage_button);
            img.setImageResource(imgResource);
            CircularProgressBar progressBar = v.findViewById(R.id.storage_progressBar);
            View warning = v.findViewById(R.id.warning);
            int dayDifference = calculateProgress(progressBar,item.get(EXPIRE).toString()
                                    ,item.get(STARTDATE).toString(),warning);
            layout.getLayoutParams().width = width;
            layout.getLayoutParams().height = width;
            warning.getLayoutParams().width = layout.getLayoutParams().width /3;
            warning.getLayoutParams().height = layout.getLayoutParams().height /3;
            item.put("DayDifference",dayDifference);
            v.setTag(item);
            img.setTag(item);
            img.setOnClickListener(this);
            v.setOnTouchListener(new MyTouchListener());
            container.addView(v);
            ((GridLayout.LayoutParams) v.getLayoutParams()).columnSpec =
                    GridLayout.spec(GridLayout.UNDEFINED, 1f);
            }catch (Exception e){e.printStackTrace();}
        }
    }

    private int calculateProgress(CircularProgressBar circle,String end,String start,View warning)
    {
        int dayDifference =  0 ;
        try {
            Date end_date= new SimpleDateFormat("dd/MM/yy",Locale.US).parse(end);
            Date start_date =  new SimpleDateFormat("dd/MM/yy", Locale.US).parse(start);
            long dayInMilliseconds = 86400000;

            float percent= ( System.currentTimeMillis() -start_date.getTime() )*100/(end_date.getTime() - start_date.getTime());
            circle.setProgress(percent);
            dayDifference = (int)((end_date.getTime() - System.currentTimeMillis())/dayInMilliseconds);
            if (percent >=100f)
            {
                warning.setVisibility(View.VISIBLE);
                ((ImageView)warning).setImageResource(R.drawable.dead_expire);
            }
            else if (end_date.getTime()-System.currentTimeMillis()<dayInMilliseconds*2)
                warning.setVisibility(View.VISIBLE);
            else
                warning.setVisibility(View.GONE);
        } catch (ParseException e) {
            circle.setProgress(50.0f);
            e.printStackTrace();
        }
        return dayDifference;
    }

    private void placeToArrayList(Map<String,Object> item,String id){
            item.put("id",id);
            switch (item.get(METHOD).toString()){
                case "Refrigerator":
                    refrigerate.add(item);break;
                case "Freezer":
                    freeze.add(item);break;
                case "Pantry":
                    pantry.add(item);break;
            }
    }

    @Override
    public void onClick(View v) {
        popUpWindow(v);
    }

    private void  popUpWindow(final View itemView)
    {
        final ViewGroup root = (ViewGroup) getActivity().getWindow().getDecorView().getRootView();
        Map<String,Object> map = (Map<String, Object>) itemView.getTag();
        LayoutInflater layoutInflater = (LayoutInflater)getContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int)(displayMetrics.widthPixels*0.8);
        View popupView = layoutInflater.inflate(R.layout.record_info, null);
        final PopupWindow popupWindow=new PopupWindow(popupView,
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
         ImageView imgView = popupView.findViewById(R.id.record_closeBt);
         Button discardBt = popupView.findViewById(R.id.discard_bt);
         Button consumeBt = popupView.findViewById(R.id.consume_bt);
        discardBt.setTag(discardBt.getText().toString());
        consumeBt.setTag(consumeBt.getText().toString());
         detail_name.setText(map.get(DISPLAY).toString());
         String subname = map.get("SUB_NAME").toString();
         subname = subname.equals("null")?"":subname;
         detail_subname.setText(subname);
         String dayLeft = (int) map.get("DayDifference") > 0 ?map.get("DayDifference") + " days left": "Spoiled Already";
         dayLeft = " \n ("+dayLeft +")";
         purchaseTx.setText("Purchase Date: " + map.get(STARTDATE).toString());
         expireTx.setText("Best Before Date: " + map.get(EXPIRE).toString() + dayLeft);
         storageTx.setText("Storage Type: "+ map.get(METHOD).toString());
         popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
             @Override
             public void onDismiss() {
                 clearDim(root);
             }
         });
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
         discardBt.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 removeView((View) itemView.getParent().getParent(),v.getTag().toString());
                 popupWindow.dismiss();
             }
         });
         consumeBt.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 removeView((View) itemView.getParent().getParent(),v.getTag().toString());
                 popupWindow.dismiss();
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
        container.removeAllViews();
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
                return true;
            }
            else {
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
                    final View view = (View) event.getLocalState();
                    removeView(view, v.getTag().toString());
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    public static String date_to_str(Date date)
    {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        return  sdf.format(date);
    }

    private void removeView(final View view,String finishType){
        final ViewGroup owner = (ViewGroup) view.getParent();
        String type_temp =tabs.getTabAt(tabs.getSelectedTabPosition()).getText().toString();
        Map<String,Object> temp = (Map<String, Object>) view.getTag();
        temp.put("OPERATION_DATE",date_to_str(new Date()));
        String id = (String) temp.get("id");
        activity.db.collection("tracker").document(uid)
                .collection(finishType).add(temp);
        activity.db.collection("tracker").document(uid)
                .collection(type_temp).document(id).delete();
        freeze = new ArrayList<>();
        refrigerate = new ArrayList<>();
        pantry = new ArrayList<>();
        view.setVisibility(View.GONE);
        owner.removeView(view);
        Toast.makeText(getContext(),finishType + " Successfully",Toast.LENGTH_LONG).show();
    }
}


