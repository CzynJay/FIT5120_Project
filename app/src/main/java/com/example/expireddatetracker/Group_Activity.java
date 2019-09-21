package com.example.expireddatetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.expireddatetracker.Fragments.ResultFragment;
import com.example.expireddatetracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class Group_Activity extends AppCompatActivity {
    private String uid;
    private Button createBT,leaveBT,copyBT;
    private FirebaseFirestore db;
    private EditText codeET;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        initUI();
        checkGroup();
    }

    private void initUI()
    {   codeET = findViewById(R.id.invitation_et);
        copyBT = findViewById(R.id.copy_bt);
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        findViewById(R.id.group_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        createBT = findViewById(R.id.create_groupBt);
        leaveBT = findViewById(R.id.leave_groupBt);
        copyBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", codeET.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getBaseContext(),"Code is copied",Toast.LENGTH_LONG).show();
            }
        });
        createBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUpWindow();
            }
        });
    }

    private void checkGroup()
    {
        db.collection("tracker").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful())
                                {
                                   Map<String,Object> map =  task.getResult().getData();
                                   if (map==null)
                                        groupExistHelper(false);
                                   else{
                                        findViewById(R.id.code_layout).setVisibility(View.VISIBLE);
                                        groupExistHelper(true);
                                         codeET.setText(map.get("Group").toString());

                                   }
                                }
                    }
                });

    }
    private void groupExistHelper(boolean exist)
    {
        if (exist)
        {
            createBT.setVisibility(View.GONE);
            leaveBT.setVisibility(View.VISIBLE);
        }
        else
            {
                createBT.setVisibility( View.VISIBLE);
                leaveBT.setVisibility(View.GONE);
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

    }
}
