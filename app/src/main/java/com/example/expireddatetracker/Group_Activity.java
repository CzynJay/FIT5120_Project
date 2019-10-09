package com.example.expireddatetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.expireddatetracker.Fragments.ResultFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class Group_Activity extends AppCompatActivity {
    private String uid;
    private Button createBT;
    private Button leaveBT,joinBt,changeColor_BT;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private EditText codeET,invitationEt;
    private View prograssBar,invitationTx;
    private TextView teamnameTx, groupIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        initUI();
        checkGroup();
    }

    private void initUI()
    {
        groupIcon = findViewById(R.id.group_icon_tx);
        teamnameTx = findViewById(R.id.teamnameTx);
        changeColor_BT = findViewById(R.id.change_colorBT);
        prograssBar = findViewById(R.id.group_progressBar);
        codeET = findViewById(R.id.invitation_et);
        invitationEt = findViewById(R.id.invitationcode_et);
        invitationTx = findViewById(R.id.invitationTx);
        Button copyBT = findViewById(R.id.copy_bt);
        db = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = firebaseUser.getUid();
        groupIcon.setText(firebaseUser.getDisplayName().toUpperCase());
        findViewById(R.id.group_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        joinBt = findViewById(R.id.joinBt);
        createBT = findViewById(R.id.create_groupBt);
        leaveBT = findViewById(R.id.leave_groupBt);
        leaveBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveGroup();
            }
        });
        changeColor_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup_colorChange();
            }
        });
        createBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUpWindow();
            }
        });
        joinBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinGroup();
            }
        });
        copyBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", codeET.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getBaseContext(),"Invitation Code is copied",Toast.LENGTH_LONG).show();
            }
        });
    }

    //Leave group function
    private void leaveGroup()
    {
        prograssBar.setVisibility(View.VISIBLE);
        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Do you really want to leave this group?")
                .setIcon(R.drawable.warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Map<String,Object> updates = new HashMap<>();
                        updates.put("GROUP", FieldValue.delete());
                        db.collection("tracker").document(uid)
                                .update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                prograssBar.setVisibility(View.GONE);
                                refresh();
                                Toast.makeText(getApplicationContext(), "Left group successfully", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prograssBar.setVisibility(View.GONE);
                    }
                }).show();
    }

    //Get group name and ID function
    private void checkGroup()
    {

        prograssBar.setVisibility(View.VISIBLE);
        db.collection("tracker").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful())
                                {
                                   Map<String,Object> map =  task.getResult().getData();
                                   if (map==null||!map.containsKey("GROUP"))
                                        groupExistHelper(false);
                                   else{
                                       String color = (String) map.get("Color");
                                       color = color==null||color.equals("null")?"#FFAB13":color;
                                       GradientDrawable gradientDrawable = (GradientDrawable) groupIcon.getBackground();
                                       gradientDrawable.setColor(Color.parseColor(color));
                                       findViewById(R.id.code_layout).setVisibility(View.VISIBLE);
                                       groupExistHelper(true);
                                       String groupId = map.get("GROUP").toString();
                                       db.collection("group").document(groupId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()){
                                                    teamnameTx.setText("Welcome to Group "+ task.getResult().get("GROUP NAME").toString());
                                                    teamnameTx.setVisibility(View.VISIBLE);
                                                }}
                                       });
                                         codeET.setText(groupId);}
                                }
                        prograssBar.setVisibility(View.GONE);
                    }});
    }

    //Switch layout function depending id group exists or not
    private void groupExistHelper(boolean exist)
    {
        if (exist)
        {
            findViewById(R.id.share_code_tx).setVisibility(View.VISIBLE);
            findViewById(R.id.joinGroup_tx).setVisibility(View.GONE);
            findViewById(R.id.create_groupTx).setVisibility(View.GONE);
            findViewById(R.id.change_color_tx).setVisibility(View.VISIBLE);
            findViewById(R.id.customize_color_tx).setVisibility(View.VISIBLE);
            findViewById(R.id.noGroupTx).setVisibility(View.GONE);
            groupIcon.setVisibility(View.VISIBLE);
            createBT.setVisibility(View.GONE);
            leaveBT.setVisibility(View.VISIBLE);
            joinBt.setVisibility(View.GONE);
            invitationEt.setVisibility(View.GONE);
            invitationTx.setVisibility(View.VISIBLE);
            changeColor_BT.setVisibility(View.VISIBLE);
        }
        else
            {

                findViewById(R.id.joinGroup_tx).setVisibility(View.VISIBLE);
                findViewById(R.id.create_groupTx).setVisibility(View.VISIBLE);
                findViewById(R.id.change_color_tx).setVisibility(View.GONE);
                findViewById(R.id.customize_color_tx).setVisibility(View.GONE);
                findViewById(R.id.share_code_tx).setVisibility(View.GONE);
                findViewById(R.id.noGroupTx).setVisibility(View.VISIBLE);
                teamnameTx.setVisibility(View.GONE);
                groupIcon.setVisibility(View.GONE);
                createBT.setVisibility( View.VISIBLE);
                leaveBT.setVisibility(View.GONE);
                joinBt.setVisibility(View.VISIBLE);
                invitationEt.setVisibility(View.VISIBLE);
                invitationTx.setVisibility(View.GONE);
                changeColor_BT.setVisibility(View.GONE);
            }
    }

    private void popUpWindow()
    {
        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int)(displayMetrics.widthPixels*0.8);
        View popupView = layoutInflater.inflate(R.layout.group_create_popup, null);
        final PopupWindow popupWindow=new PopupWindow(popupView,
                width, LinearLayout.LayoutParams.WRAP_CONTENT,
                true);
        //Allow popup to be touchable & focusable
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        ResultFragment.applyDim(root,0.5f);
        //Popup window animation
        popupWindow.setAnimationStyle(R.style.Animation_Design_BottomSheetDialog);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                ResultFragment.clearDim(root);
            }
        });
        final EditText editText = popupView.findViewById(R.id.group_name_et);
        popupView.findViewById(R.id.group_create_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.group_confirm_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().toString().trim().equals(""))
                    Toast.makeText(getBaseContext(),"Please enter a group name",Toast.LENGTH_LONG).show();
                else
                    {
                        createNewGroup(editText.getText().toString());
                        popupWindow.dismiss();
                    }
            }
        });

    }

    //Join group function
    private void joinGroup(){
        prograssBar.setVisibility(View.VISIBLE);
        final String value = invitationEt.getText().toString().trim();
        if(value.equals("")){
            vibrate();
            prograssBar.setVisibility(View.GONE);
            invitationEt.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.shake));
            Toast.makeText(getBaseContext(),"Please enter Invitation Code",Toast.LENGTH_LONG).show();}
        else
        {
            db.collection("group").document(value)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful())
                    {
                        Map<String,Object> map =  task.getResult().getData() ;
                        if (map==null)
                        {
                            Toast.makeText(getBaseContext(),"Invalid Code",Toast.LENGTH_LONG).show();
                            invitationEt.setError("Invalid Code");
                            vibrate();
                            invitationEt.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.shake));
                            prograssBar.setVisibility(View.GONE);
                        }
                        else {
                            Map<String,Object> group = new HashMap<>();
                            group.put("GROUP",value);
                            db.collection("tracker").document(uid).set(group,SetOptions.merge())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            prograssBar.setVisibility(View.GONE);
                                            Toast.makeText(getBaseContext(),"Joined group successfully ",Toast.LENGTH_SHORT).show();
                                            refresh();
                                        }
                                    });
                        }
                    }

                }
            });
        }

    }

    //Create new group name in Firebase function
    private void createNewGroup(String name)
    {
        DocumentReference addedDocRef = db.collection("group").document();
        final String groupID = addedDocRef.getId();
        Map<String,String> data = new HashMap<>();
        data.put("GROUP NAME",name);
        addedDocRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                HashMap<String,String> map = new HashMap<>();
                map.put("GROUP",groupID);
                db.collection("tracker").document(uid).set(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        refresh();
                    }
                });
            }
        });
    }

    private void refresh(){
        finish();
        startActivity(getIntent());
    }

    private void vibrate()
    {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(200);
        }
    }

    //Choose colour popup function
    private void popup_colorChange()
    {
        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int)(displayMetrics.widthPixels);
        final View popupView = layoutInflater.inflate(R.layout.color_change_popup, null);
        final PopupWindow popupWindow=new PopupWindow(popupView,
                width, LinearLayout.LayoutParams.WRAP_CONTENT,
                true);
        //Allow popup to be touchable & focusable
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        ResultFragment.applyDim(root,0.5f);
        //Popup window animation
        popupWindow.setAnimationStyle(R.style.Animation_Design_BottomSheetDialog);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                ResultFragment.clearDim(root);
            }
        });
        final RadioGroup rg = popupView.findViewById(R.id.group_radio_color);
        //Confirm and cancel button
        Button cancel = popupView.findViewById(R.id.color_cancel);
        Button confirm = popupView.findViewById(R.id.color_confirm);
        TextView sampleIcon = popupView.findViewById(R.id.icon_sample);
        String initName = firebaseUser.getDisplayName();
        sampleIcon.setText(initName==null||initName.isEmpty()?"U":initName.toUpperCase());
        final GradientDrawable drawable = (GradientDrawable) sampleIcon.getBackground();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton checkedBt = popupView.findViewById(rg.getCheckedRadioButtonId());
               Map<String,String> tempMap = new HashMap<>();
               String color = checkedBt.getTag().toString();
               GradientDrawable gradientDrawable = (GradientDrawable) groupIcon.getBackground();
               gradientDrawable.setColor(Color.parseColor(color));
               tempMap.put("Color",color);
               db.collection("tracker").document(uid).update("Color",checkedBt.getTag().toString());
                popupWindow.dismiss();
                Snackbar.make(findViewById(R.id.group_main_layout),checkedBt.getText().toString()+ " Selected",Snackbar.LENGTH_LONG).show();
//                Toast toast = Toast.makeText(getBaseContext(),checkedBt.getText().toString()+ " Selected",Toast.LENGTH_LONG);
//                View view = toast.getView();
//                view.setBackgroundResource(R.drawable.round_button);
////                GradientDrawable gradientDrawable = (GradientDrawable) view.getBackground();
//////                gradientDrawable.setColor(Color.parseColor(checkedBt.getTag().toString()));
//                TextView text = (TextView) view.findViewById(android.R.id.message);
//                text.setTextColor(getResources().getColor(R.color.white));
//                toast.show();
            }});
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = popupView.findViewById(checkedId);
                 drawable.setColor(Color.parseColor(rb.getTag().toString()));
            }
        });
    }

}
