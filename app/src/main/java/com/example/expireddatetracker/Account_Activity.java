package com.example.expireddatetracker;

import android.content.Intent;
import android.os.Bundle;

import com.example.expireddatetracker.Fragments.ResultFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.InputType;
import android.transition.Explode;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.expireddatetracker.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

public class Account_Activity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_);
        initUI();

    }

    private void initUI(){
        Button logoutBt = findViewById(R.id.logout_bt);
        Button passwordBt = findViewById(R.id.changePassword_bt);
        ImageButton back = findViewById(R.id.account_back);
        EditText displayNameEt = findViewById(R.id.account_displayET);
        displayNameEt.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        passwordBt.setOnClickListener(this);
        //Sign out button, signing our from Firebase
        logoutBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getBaseContext(),UserLoginActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });
    }

    //Change password popup function
    private void  popUpWindow()
    {
        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int)(displayMetrics.widthPixels*0.8);
        View popupView = layoutInflater.inflate(R.layout.record_info, null);
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
        TextView detail_name = popupView.findViewById(R.id.record_name);
        LinearLayout layout = popupView.findViewById(R.id.popupWindowLayout);
        popupView.findViewById(R.id.record_subname).setVisibility(View.INVISIBLE);
        popupView.findViewById(R.id.purchase_date).setVisibility(View.GONE);
        popupView.findViewById(R.id.expire_date).setVisibility(View.GONE);
        popupView.findViewById(R.id.storage_type).setVisibility(View.GONE);
        popupView.findViewById(R.id.record_closeBt).setVisibility(View.GONE);
        //Instantiate buttons
        Button confirmBt = popupView.findViewById(R.id.consume_bt);
        confirmBt.setText("Confirm");
        Button discardBt = popupView.findViewById(R.id.discard_bt);
        discardBt.setText("Cancel");
        discardBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        detail_name.setText("Change Password");
        final EditText password1st = new EditText(getBaseContext());
        password1st.setHint("Please Enter new password");
        final EditText password2st = new EditText(getBaseContext());
        password2st.setHint("Enter password again");
        layout.addView(password1st,1);
        layout.addView(password2st,2);
        password1st.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        password1st.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        password2st.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        password2st.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        confirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If password too short
                if (password1st.getText().toString().trim().length()<7)
                { Toast.makeText(getApplicationContext(),
                            "Password length should be longer than 6",Toast.LENGTH_LONG).show(); return;}
                //If password is not the same
                if (!password1st.getText().toString().equals(password2st.getText().toString()))
                {
                    Toast.makeText(getApplicationContext(),
                            "Passwords are not the same",Toast.LENGTH_LONG).show();return;
                }
                //Update password on Firebase
                FirebaseAuth.getInstance().getCurrentUser().updatePassword(password1st.getText().toString().trim());
                popupWindow.dismiss();

            }
        });
        //Un-dim after popup is closed
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                ResultFragment.clearDim(root);
            }
        });
    }
    @Override
    public void onClick(View v) {
        popUpWindow();
    }
}
