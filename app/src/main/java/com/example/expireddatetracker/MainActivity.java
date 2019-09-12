package com.example.expireddatetracker;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;
import com.example.expireddatetracker.Fragments.HomeFragment;
import com.example.expireddatetracker.Fragments.ResultFragment;
import com.example.expireddatetracker.Fragments.SettingFragment;
import com.example.expireddatetracker.Fragments.TrackFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


public class MainActivity extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce = false;
    public FirebaseFirestore db;
    public JSONArray food_source;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    FragmentManager fm = getSupportFragmentManager();
                    for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                        fm.popBackStack();
                    }
                    fragment = new HomeFragment();
                    break;
                    case R.id.navigation_dashboard:
                    fragment = new TrackFragment();
                    break;
                case R.id.navigation_setting:
                    fragment = new SettingFragment();
                    break;
            }
            return loadFragment(fragment);
        }
    };

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        LoadJson asynTask = new LoadJson();
        asynTask.execute();
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
            case "Edible Plant Stem": return R.drawable.edible_plant_stem;
            case "Eggs":return R.drawable.eggs;
            case "Fish": return R.drawable.fish;
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
            default: return R.drawable.app_icon;
        }

    }
}
