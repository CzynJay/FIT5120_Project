package com.example.expireddatetracker.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.example.expireddatetracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class NotificationService extends BroadcastReceiver {
    private FirebaseFirestore db;
    private Map<String,Integer> map;
    private Context myContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        db = FirebaseFirestore.getInstance();
        myContext = context;
        map = new HashMap<>();
        map.put("Expire soon",0);
        map.put("Expire already",0);
        loadData();

    }

    private void loadData()
    {
        Log.e("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
        String [] types = {"Freeze","Pantry","Refrigerate"};
        for(String type: types)
        {
        db.collection("tracker").document(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).collection(type).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for(DocumentSnapshot item:task.getResult().getDocuments()){
                                countingDate(item.getData());
                            }
                            popNotification();
                        }
                    }
                });}
    }

    private void countingDate(Map<String,Object> data)
    {
        Date expireDate = string_to_Date(Objects.requireNonNull(data.get("EXPIRE_DATE")).toString());
        long timeDifference =  expireDate.getTime() - new Date().getTime();
        Log.e("time",String.valueOf(timeDifference));
        if (timeDifference<=0){
            map.put("Expire already",map.get("Expire already")+1);
        return;}
        long dayInMilliseconds = 86400000;
        if(timeDifference < dayInMilliseconds *2 ){
            map.put("Expire soon",map.get("Expire soon")+1);
        Log.e("check",map.toString());}
    }

    private Date string_to_Date(String dateStr)
    {
        Date returnDate = new Date();
        try {
            returnDate = new SimpleDateFormat("MM/dd/yy", Locale.US).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  returnDate;
    }


    private void popNotification()
    {
        NotificationManager manager = (NotificationManager)myContext.
                getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = "my_channel_01";
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            manager.createNotificationChannel(mChannel);
        }
        if(map.get("Expire soon")>0){
            Notification builder = new NotificationCompat.Builder(myContext,"my_channel_01")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(myContext.getResources().getColor(R.color.white))
                    .setContentTitle("Items expire soon")
                    .setContentText("You have " +map.get("Expire soon")+" items Expire soon" )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT).build();

            manager.notify(1,builder);
        }
        if(map.get("Expire already")>0){
            Notification builder = new NotificationCompat.Builder(myContext, "my_channel_01")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Items expire already")
                    .setContentText("You have " +map.get("Expire already")+" items Expire already" )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT).build();
            manager.notify(2,builder);
        }
    }
}
