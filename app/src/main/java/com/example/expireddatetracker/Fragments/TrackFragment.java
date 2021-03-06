package com.example.expireddatetracker.Fragments;

import android.content.ClipData;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.expireddatetracker.MainActivity;
import com.example.expireddatetracker.Models.CircularProgressBar;
import com.example.expireddatetracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
import static android.content.Context.VIBRATOR_SERVICE;

public class TrackFragment extends Fragment implements View.OnClickListener, TabLayout.BaseOnTabSelectedListener {
    private GridLayout container;
    private MainActivity activity;
    private String uid;
    final private String METHOD = "STORAGE_METHOD";
    final private String EXPIRE = "EXPIRE_DATE";
    final private String STARTDATE = "PURCHASE_DATE";
    final private String DISPLAY = "DISPLAY_NAME";
    private ScrollView sv ;
    private TabLayout tabs;
    private View progressing,errorTx,bt_layout;

    private  long dayInMilliseconds = 86400000;

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
        sv = x.findViewById(R.id.scroll_container);
        bt_layout = x.findViewById(R.id.buttonLayout);
        //Discard and consume button
        Button quickDiscardBt = x.findViewById(R.id.quick_discard_bt);
        Button quickConsumeBt = x.findViewById(R.id.quick_consume_bt);
        quickDiscardBt.setTag(quickDiscardBt.getText());
        quickConsumeBt.setTag(quickConsumeBt.getText());
        //Drag listener for buttons
        sv.setOnDragListener(new Drop_fail_listener());
        quickConsumeBt.setOnDragListener(new MyDragListener());
        quickDiscardBt.setOnDragListener(new MyDragListener());
        container = x.findViewById(R.id.storage_container);
        errorTx = x.findViewById(R.id.no_record);
        activity = (MainActivity) getActivity();
        //Progress circle of food items
        progressing = x.findViewById(R.id.progressing);
        progressing.setVisibility(View.VISIBLE);
        //Authenticate user from Firebase
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tabs.addOnTabSelectedListener(this);
        fetchGroupData("Refrigerator");
    }

    private void fetchGroupData(final String type)
    {
        progressing.setVisibility(View.VISIBLE);
        activity.db.collection("tracker").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                     if(task.isSuccessful()){
                         String value = task.getResult().getString("GROUP");
                         if(value!=null)
                         {
                             activity.db.collection("tracker")
                                     .whereEqualTo("GROUP",value).get()
                                     .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                 @Override
                                 public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                     if(task.isSuccessful())
                                     {
                                         for(DocumentSnapshot item:task.getResult().getDocuments()){
                                             String id = item.getId();
                                             String ownerName = (String)item.get("Name");
                                             String color = (String) item.get("Color");
                                             if (!id.equals(uid))
                                                fetchData(id,type,ownerName,true,color);
                                         }
                                     }
                                 }
                             });
                         }
                         fetchData(uid,type,FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),false,null);
                     }
                progressing.setVisibility(View.VISIBLE);
            }
        });
    }
    //Get data from Firebase
    private void fetchData(final String id, final String type, final String ownerName, final boolean group, final String color)
    {
        final ArrayList<Map<String,Object>> temp = new ArrayList<>();
        activity.db.collection("tracker").document(id)
                .collection(type).get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            //Get data from Firebase
                            for(DocumentSnapshot item:task.getResult().getDocuments()){
                                Map<String,Object> newMap = item.getData();
                                newMap.put("id",item.getId());
                                temp.add(newMap);
                            }
                            progressing.setVisibility(View.GONE);
                            displayStatus(temp,id,ownerName,group,color);
                            if(container.getChildCount()==0)
                                errorTx.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void vibrate()
    {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }

    }
    //Display list of all the food in storage
    private void displayStatus( ArrayList<Map<String,Object>> lists,String id,String ownerName,boolean group,String color){

        errorTx.setVisibility(View.GONE);


        for(Map<String,Object> item:lists)
     {
            try {
            LayoutInflater vi = (LayoutInflater) getContext()
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width =(int) (displayMetrics.widthPixels / 5.5);
            View v = vi.inflate(R.layout.storage_icon, null);
            RelativeLayout layout = v.findViewById(R.id.circle_container);
            TextView name = v.findViewById(R.id.storage_name);
            TextView oneLetter = v.findViewById(R.id.owner_One_letter_tx);
            name.setText(item.get(DISPLAY).toString());
            if (group){
                oneLetter.setVisibility(View.VISIBLE);
                GradientDrawable drawable = (GradientDrawable)oneLetter.getBackground();
                drawable.setColor(Color.parseColor(color==null?"#FFAB13":color));
                oneLetter.setText(ownerName==null?"U":ownerName.substring(0,1).toUpperCase());
            }
            int imgResource = R.drawable.app_icon;
              if(item.get("NAV_TITLE")!=null)
                  //Get subcategory image
                  imgResource = MainActivity.String_to_img(item.get("NAV_TITLE").toString());
            ImageButton img = v.findViewById(R.id.storage_button);
            img.setImageResource(imgResource);
            CircularProgressBar progressBar = v.findViewById(R.id.storage_progressBar);
            View warning = v.findViewById(R.id.warning);
            long dayDifference = calculateProgress(progressBar,item.get(EXPIRE).toString()
                                    ,item.get(STARTDATE).toString(),warning);
            layout.getLayoutParams().width = width;
            layout.getLayoutParams().height = width;
            //Set dimensions of popup
            warning.getLayoutParams().width = layout.getLayoutParams().width /3;
            warning.getLayoutParams().height = layout.getLayoutParams().height /3;
            item.put("DayDifference",dayDifference);
            item.put("UserID",id);
            item.put("DisplayName",ownerName);
            v.setTag(item);
            img.setTag(item);
            img.setOnClickListener(this);
            img.setOnLongClickListener(new MyLongClickListener());
            container.addView(v);
            ((GridLayout.LayoutParams) v.getLayoutParams()).columnSpec =
                    GridLayout.spec(GridLayout.UNDEFINED, 1f);
            }catch (Exception e){e.printStackTrace();}
        }
    }

    //Progress circle logic
    private long calculateProgress(CircularProgressBar circle,String end,String start,View warning)
    {
        long dayDifference =  0 ;
        try {
            Date end_date= new SimpleDateFormat("dd/MM/yy",Locale.US).parse(end);
            Date start_date =  new SimpleDateFormat("dd/MM/yy", Locale.US).parse(start);

            //Change date to percentage
            float percent= ( new Date().getTime() -start_date.getTime() )*100/(end_date.getTime() - start_date.getTime());
            circle.setProgress(percent);
            //Calculate remaining days
            dayDifference = end_date.getTime() - new Date().getTime();
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

    @Override
    public void onClick(View v) {
        popUpWindow(v);
    }

    //Popup after food item is selected
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
        //Allow popup to be touchable & focusable
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        applyDim(root,0.5f);
        //Set popup animation
        popupWindow.setAnimationStyle(R.style.Animation_Design_BottomSheetDialog);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        //Display name and subtitle of selected food item
         TextView detail_name = popupView.findViewById(R.id.record_name);
         TextView detail_subname = popupView.findViewById(R.id.record_subname);
         //Display purchase and best before date
         TextView dayLeftTx = popupView.findViewById(R.id.dayleftTx);
         TextView purchaseTx = popupView.findViewById(R.id.purchase_date);
         TextView expireTx = popupView.findViewById(R.id.expire_date);
         TextView storageTx = popupView.findViewById(R.id.storage_type);
         TextView ownerTx = popupView.findViewById(R.id.ownerTx);
         ImageView imgView = popupView.findViewById(R.id.record_closeBt);
         Button discardBt = popupView.findViewById(R.id.discard_bt);
         Button consumeBt = popupView.findViewById(R.id.consume_bt);
         discardBt.setTag(discardBt.getText().toString());
         consumeBt.setTag(consumeBt.getText().toString());
         detail_name.setText(map.get(DISPLAY).toString());
         String subname = map.get("SUB_NAME").toString();
         subname = subname.equals("null")?"":subname;
         detail_subname.setText(subname);
         String dayLeft = (long) map.get("DayDifference") > 0 ?
                 (int)((long)map.get("DayDifference")/dayInMilliseconds) + " days left": "Spoiled Already";
         dayLeftTx.setText(dayLeft);
         dayLeftTx.setVisibility(View.VISIBLE);
         //Define texts for purchase date, best before date, and storage method
         purchaseTx.setText("Purchase Date: " + map.get(STARTDATE).toString());
         expireTx.setText("Best Before Date: " + map.get(EXPIRE).toString());
         storageTx.setText("Storage Type: "+ map.get(METHOD).toString());
         ownerTx.setText("Owner: " + map.get("Owner").toString());
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

    //Dimming effect when pop up appears
    public static void applyDim(@NonNull ViewGroup parent, float dimAmount){
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * dimAmount));

        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    //Un-dim after popup is closed
    public static void clearDim(@NonNull ViewGroup parent) {
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.clear();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        container.removeAllViews();
        errorTx.setVisibility(View.GONE);
        fetchGroupData(tab.getText().toString());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    //Drag and drop function
    private final class MyLongClickListener implements View.OnLongClickListener {


        @Override
        public boolean onLongClick(View view) {
            vibrate();
//            bt_layout.setVisibility(View.VISIBLE);
            slide_anim(bt_layout,true);
            View parent =(View)view.getParent().getParent();
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                    parent);
            view.startDrag(data, shadowBuilder, parent, 0);
            return false;
        }
    }

    //Drag and drop class
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
//                    bt_layout.setVisibility(View.GONE);
                    slide_anim(bt_layout,false);
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    class Drop_fail_listener implements  View.OnDragListener{
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                case DragEvent.ACTION_DRAG_EXITED:
                case DragEvent.ACTION_DRAG_ENDED:
                    break;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign View to ViewGroup
//                    bt_layout.setVisibility(View.GONE);
                    slide_anim(bt_layout,false);
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

    //Remove food storage function
    private void removeView(final View view,String finishType){
        final ViewGroup owner = (ViewGroup) view.getParent();
        String type_temp =tabs.getTabAt(tabs.getSelectedTabPosition()).getText().toString();
        Map<String,Object> temp = (Map<String, Object>) view.getTag();
        temp.put("OPERATION_DATE",date_to_str(new Date()));
        String id = (String) temp.get("id");
        String userId = (String)temp.get("UserID");
        activity.db.collection("tracker").document(uid)
                .collection(finishType).add(temp);
        //Remove data from Firebase
        activity.db.collection("tracker").document(userId)
                .collection(type_temp).document(id).delete();
        view.setVisibility(View.GONE);
        owner.removeView(view);
        Snackbar.make(container,finishType + " Successfully",Snackbar.LENGTH_LONG).show();
    }

    private void slide_anim(final View view, boolean up){
        if (up)
        {
            view.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getContext(),R.anim.slide_up);
            animation.setDuration(500);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(animation);

        }
        else {
            Animation animation = AnimationUtils.loadAnimation(getContext(),R.anim.slide_down);
            animation.setDuration(500);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                        view.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(animation);
        }


    }
}


