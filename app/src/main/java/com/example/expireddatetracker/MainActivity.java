package com.example.expireddatetracker;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.expireddatetracker.Fragments.DashboardFragment;
import com.example.expireddatetracker.Fragments.HomeFragment;
import com.example.expireddatetracker.Fragments.ResultFragment;
import com.example.expireddatetracker.Fragments.SettingFragment;
import com.example.expireddatetracker.Fragments.TrackFragment;
import com.example.expireddatetracker.Service.NotificationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce = false;
    public FirebaseFirestore db;
    public JSONArray food_source;
    public Map<String,JSONArray> dayLeftRefrige = new HashMap<>();
    public Map<String,JSONArray> dayLeftPantry = new HashMap<>();
    public Map<String,JSONArray> dayLeftFreeze = new HashMap<>();
    private final String SPOILED = "Spoiled";
    private final String TWODAYS = "Less than 2 days left";
    private final String TWO_SEVEN = "2-7 days left";
    private final String MORETHANAWEEK = "More than a week";

    //Bottom navigation menu
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            //Fragments for bottom navigation menu
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    FragmentManager fm = getSupportFragmentManager();
                    for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                        fm.popBackStack();
                    }
                    fragment = new HomeFragment();
                    break;
                case R.id.navigation_storage:
                    fragment = new TrackFragment();
                    break;
                case R.id.navigation_setting:
                    fragment = new SettingFragment();
                    break;
                case R.id.navigation_dashboard:
                    fragment = new DashboardFragment();
                    break;
            }
            return loadFragment(fragment);
        }
    };

    //Load fragment
    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void initMap(){
        dayLeftRefrige.put(SPOILED,new JSONArray());
        dayLeftRefrige.put(TWODAYS,new JSONArray());
        dayLeftRefrige.put(TWO_SEVEN,new JSONArray());
        dayLeftRefrige.put(MORETHANAWEEK,new JSONArray());
        dayLeftPantry.put(SPOILED,new JSONArray());
        dayLeftPantry.put(TWODAYS,new JSONArray());
        dayLeftPantry.put(TWO_SEVEN,new JSONArray());
        dayLeftPantry.put(MORETHANAWEEK,new JSONArray());
        dayLeftFreeze.put(SPOILED,new JSONArray());
        dayLeftFreeze.put(TWODAYS,new JSONArray());
        dayLeftFreeze.put(TWO_SEVEN,new JSONArray());
        dayLeftFreeze.put(MORETHANAWEEK,new JSONArray());

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startAlertAtParticularTime();
        db = FirebaseFirestore.getInstance();
        LoadJson asynTask = new LoadJson();
        asynTask.execute();
        loadChart();
        //Home fragment on launch
        loadFragment(new HomeFragment());
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    //double click to exit
    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (doubleBackToExitPressedOnce || f instanceof ResultFragment) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private JSONArray loadJsonFile()
    {
        String json;
        JSONArray res = new JSONArray();
        try {
            InputStream is = getAssets().open("foodsource.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
            res = new JSONArray(json);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return  res;
    }

    class LoadJson extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            food_source = loadJsonFile();
            return null;
        }
    }

    //Sub-category image
    public static int String_to_img(String category)
    {
        switch (category)
        {
            case "Allium": return R.drawable.allium;
            case "Apples and Pears": return R.drawable.apples_pears;
            case "Beef": return R.drawable.beef;
            case "Berries":return R.drawable.berries;
            case "Cheese":return R.drawable.cheese;
            case "Chicken":return R.drawable.chicken;
            case "Citrus":return R.drawable.citrus;
            case "Cruciferous": return R.drawable.cruciferous;
            case "Edible Plant Stem": return R.drawable.edibleplantstem;
            case "Eggs":return R.drawable.eggs;
            case "Fatty fish": return R.drawable.fattyfish;
            case "Lean fish": return R.drawable.leanfish;
            case "Goat": return R.drawable.goat;
            case "Lamb": return R.drawable.lamb;
            case "Leafy Green":return R.drawable.leafy_green;
            case "Marrow": return R.drawable.marrow;
            case "Melons": return R.drawable.melons;
            case "Milk": return R.drawable.milk;
            case "Pork": return R.drawable.pork;
            case "Root": return R.drawable.root;
            case "Shellfish": return R.drawable.shellfish;
            case "Tropical and Exotic": return R.drawable.tropical;
            case "Turkey": return R.drawable.turkey;
            case "Veal": return R.drawable.veal;
            case "Other Poultries": return R.drawable.poultry_others;
            case "Other Fruits": return R.drawable.fruit_others;
            case "Other Meats": return R.drawable.meat_others;
            case "Other Vegetables": return R.drawable.vegetables_others;
            default: return R.drawable.app_icon;
        }

    }

    //Push notification function
    public void startAlertAtParticularTime() {
        Intent intent = new Intent(this, NotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), 0, intent,  0);
        int interval = 1000 * 60 * 60 * 24;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 05);
        calendar.set(Calendar.SECOND, 0 );
        calendar.set(Calendar.MILLISECOND,0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                100, pendingIntent);
    }

    public void loadChart()
    {
        initMap();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("tracker")
                .document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    String value = task.getResult().getString("GROUP");
                    if (value!=null)
                    {
                        db.collection("tracker")
                                .whereEqualTo("GROUP",value).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful())
                                        {
                                            for(DocumentSnapshot item:task.getResult().getDocuments()){
                                                String id = item.getId();
                                                fetchData(id);
                                            }
                                        }
                                    }
                                });
                    }
                    else
                        fetchData(uid);
                }
            }
        });
    }

    public void fetchData(String uid){
        String [] types = {"Refrigerator","Pantry","Freezer"};
        for (final String type:types)
            db.collection("tracker").document(uid).collection(type)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful())
                    {
                        for(DocumentSnapshot item:task.getResult().getDocuments()){
                            Map<String,Object> newMap = item.getData();
                            switch (type){
                                case "Refrigerator": placeToMap(dayLeftRefrige,newMap);break;
                                case "Pantry": placeToMap(dayLeftPantry,newMap);break;
                                case "Freezer": placeToMap(dayLeftFreeze,newMap);break;
                            }
                        }
                    }
                }
            });

    }

    private void placeToMap(Map<String,JSONArray> storage,Map<String,Object> objectMap)
    {
        String date = objectMap.get("EXPIRE_DATE").toString();
        long dayleft = calculateDayDifference(date);
        long dayInMilliseconds = 86400000;
        if(dayleft<=0)
            storage.get(SPOILED).put(objectMap);
        else if(dayleft <= dayInMilliseconds *2)
            storage.get(TWODAYS).put(objectMap);
        else if(dayleft <= dayInMilliseconds *7)
            storage.get(TWO_SEVEN).put(objectMap);
        else
            storage.get(MORETHANAWEEK).put(objectMap);
    }
    private long calculateDayDifference(String date)
    {
        Date myDate = new Date();
        try {
            myDate= new SimpleDateFormat("dd/MM/yy", Locale.US).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  (myDate.getTime() - new Date().getTime());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==1 && resultCode==100)
                    loadChart();
    }
}
