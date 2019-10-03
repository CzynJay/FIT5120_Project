package com.example.expireddatetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.expireddatetracker.Fragments.ResultFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Group_Activity extends AppCompatActivity {
    private String uid;
    private Button createBT;
    private Button leaveBT,joinBt;
    private FirebaseFirestore db;
    private EditText codeET,invitationEt;
    private View prograssBar,invitationTx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        initUI();
        checkGroup();
    }

    private void initUI()
    {
        prograssBar = findViewById(R.id.group_progressBar);
        codeET = findViewById(R.id.invitation_et);
        invitationEt = findViewById(R.id.invitationcode_et);
        invitationTx = findViewById(R.id.invitationTx);
        Button copyBT = findViewById(R.id.copy_bt);
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

    private void leaveGroup()
    {
        prograssBar.setVisibility(View.VISIBLE);
        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Do you really want to leave this group?")
                .setIcon(R.drawable.warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        db.collection("tracker").document(uid)
                                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                prograssBar.setVisibility(View.GONE);
                                refresh();
                                Toast.makeText(getApplicationContext(), "Leave Group Successfully", Toast.LENGTH_SHORT).show();
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

                                        findViewById(R.id.code_layout).setVisibility(View.VISIBLE);
                                        groupExistHelper(true);
                                         codeET.setText(map.get("GROUP").toString());}
                                }
                        prograssBar.setVisibility(View.GONE);
                    }});
    }

    private void groupExistHelper(boolean exist)
    {
        if (exist)
        {
            findViewById(R.id.noGroupTx).setVisibility(View.GONE);
            createBT.setVisibility(View.GONE);
            leaveBT.setVisibility(View.VISIBLE);
            joinBt.setVisibility(View.GONE);
            invitationEt.setVisibility(View.GONE);
            invitationTx.setVisibility(View.VISIBLE);
        }
        else
            {
                findViewById(R.id.noGroupTx).setVisibility(View.VISIBLE);
                createBT.setVisibility( View.VISIBLE);
                leaveBT.setVisibility(View.GONE);
                joinBt.setVisibility(View.VISIBLE);
                invitationEt.setVisibility(View.VISIBLE);
                invitationTx.setVisibility(View.GONE);
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

    private void joinGroup(){
        prograssBar.setVisibility(View.VISIBLE);
        final String value = invitationEt.getText().toString().trim();
        if(value.equals("")){
            vibrate();
            prograssBar.setVisibility(View.GONE);
            invitationEt.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.shake));
            Toast.makeText(getBaseContext(),"Please enter Invitation code",Toast.LENGTH_LONG).show();}
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

                            db.collection("tracker").document(uid).set(group)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            prograssBar.setVisibility(View.GONE);
                                            Toast.makeText(getBaseContext(),"Join successfully ",Toast.LENGTH_SHORT).show();
                                            refresh();
                                        }
                                    });
                        }
                    }

                }
            });
        }

    }

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
}
