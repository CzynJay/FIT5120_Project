package com.example.expireddatetracker.Service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.expireddatetracker.MainActivity;
import com.example.expireddatetracker.R;
import com.example.expireddatetracker.UserLoginActivity;
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
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,  0);
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        Log.e("Cancel","yes");}
        else{
        db = FirebaseFirestore.getInstance();
        myContext = context;
        map = new HashMap<>();
        map.put("Expire soon",0);
        map.put("Expire already",0);
        loadData();}
    }

    private void loadData()
    {
        String [] types = {"Freezer","Pantry","Refrigerator"};
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
        if (timeDifference<=0){
            map.put("Expire already",map.get("Expire already")+1);
        return;}
        long dayInMilliseconds = 86400000;
        if(timeDifference < dayInMilliseconds *2 ){
            map.put("Expire soon",map.get("Expire soon")+1);
       }
    }

    private Date string_to_Date(String dateStr)
    {
        Date returnDate = new Date();
        try {
            returnDate = new SimpleDateFormat("dd/MM/yy", Locale.US).parse(dateStr);
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
            String CHANNEL_ID = "Foodtyro";
            CharSequence name = "Foodtyro";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            manager.createNotificationChannel(mChannel);
        }
        Intent notificationIntent = new Intent(myContext, UserLoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(myContext, 0,
                notificationIntent, 0);
        if(map.get("Expire soon")>0){
            String subcontent = map.get("Expire soon") ==1?
                    map.get("Expire soon")+" item":map.get("Expire soon") + " items";
            Notification builder = new NotificationCompat.Builder(myContext,"Foodtyro")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(myContext.getResources().getColor(R.color.white))
                    .setContentTitle("Items expire soon")
                    .setContentIntent(pendingIntent)
                    .setContentText("You have " +subcontent+" expire soon" )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT).build();
            manager.notify(1,builder);
        }
        if(map.get("Expire already")>0){
            String subcontent = map.get("Expire already") ==1?
                    map.get("Expire already") +" item":map.get("Expire already") + " items";
            Notification builder = new NotificationCompat.Builder(myContext, "Foodtyro")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Items expire already")
                    .setContentIntent(pendingIntent)
                    .setContentText("You have " +subcontent+" expire already" )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT).build();
            manager.notify(2,builder);
        }
    }
}
