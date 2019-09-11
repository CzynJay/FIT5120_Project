package com.example.expireddatetracker.Service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;

public class GettingDeviceToken extends FirebaseMessagingService {

//    private static final String TAG = "MainActivity";
//
//    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//        if (!task.isSuccessful()) {
//            Log.w(TAG, "getInstanceId failed", task.getException());
//            return;
//        }
//    }


        public void onTokenRefresh (){
            String DeviceToken = FirebaseInstanceId.getInstance().getToken();
            Log.w("DEVICE TOKEN: ",DeviceToken);

        }

}
