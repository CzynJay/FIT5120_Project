package com.example.expireddatetracker.Fragments;



import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.example.expireddatetracker.ItemActivity;
import com.example.expireddatetracker.List_item_activity;
import com.example.expireddatetracker.MainActivity;
import com.example.expireddatetracker.R;
import com.google.android.gms.common.util.ArrayUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ResultFragment extends Fragment{
    private LinearLayout viewContainer;
    private Map<String,JSONArray> navigation = new HashMap<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View x =  inflater.inflate(R.layout.fragment_result, container, false);
        Bundle bundle =  this.getArguments();
        TextView tx = x.findViewById(R.id.query);
        viewContainer = x.findViewById(R.id.result_container);
        ImageView bt = x.findViewById(R.id.back);
        MainActivity mainActivity = (MainActivity) getActivity();
        String query = bundle.getString("key");
        searchResult(mainActivity.food_source,query);
        showNavigation();
        tx.setText(query);
        //Transition animation
        bt.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 getActivity().getSupportFragmentManager()
                         .beginTransaction()
                         .setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right,
                                 android.R.anim.slide_in_left, android.R.anim.slide_out_right
                         )
                         .replace(R.id.fragment_container,new HomeFragment())
                         .commit();
             }
         });
        return x;
    }

    //Subcategory search
    private void searchResult(JSONArray source, String query)
    {
        boolean multi = false;
        if (query.split("&").length>1)
            multi=true;
        for(int i =0;i<source.length();i++)
        {
            try {
                JSONObject temp = source.getJSONObject(i);
                //Trim and change search query to lowercase
                String value = temp.toString().trim().toLowerCase();
                query = query.trim().toLowerCase();
                if (!multi) {
                    if (isNumeric(query))
                    {

                    if((int)temp.get("food_id")==Integer.parseInt(query)){
                            putInMap(temp.getString("Nav_category"),temp);
                        }
                    }
                    else if (query.equals("all"))
                   {
                        putInMap(temp.getString("Nav_category"),temp);
                    }
                    else if (value.contains(query)){
                        putInMap(temp.getString("Nav_category"),temp);
                    }}
                else{
                    for(String s:query.split("&"))
                    {
                        if (value.contains(s.trim())){
                        putInMap(temp.getString("Nav_category"),temp);
                        break;
                    }}
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.e("check",String.valueOf(navigation.size()));
    }

    //Display subcategory
    private void showNavigation(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        LayoutInflater vi = (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(LAYOUT_INFLATER_SERVICE);
        //If subcategory does not exist
        if(Objects.requireNonNull(navigation.keySet().toArray()).length==0)
        {
            final View v = vi.inflate(R.layout.subtype_layout, null);
            View right = v.findViewById(R.id.right_arrow);
            right.setVisibility(View.GONE);
            TextView main = v.findViewById(R.id.subcateText);
            main.setText(R.string.no_result);
            viewContainer.addView(v);
            return;
        }
        Object [] cates = Objects.requireNonNull(navigation.keySet().toArray());
        Arrays.sort(cates, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().substring(0,1).compareTo(o2.toString().substring(0,1));
            }
        });
        ArrayList<Object> temp = new ArrayList<>();
        for(Object key:cates )
        {
            if (key.toString().startsWith("Other")){
                temp.add(key);
                continue;
            }
            if (navigation.get(key).length() !=0){
                viewContainer.addView(initalSingleBlock(vi,key));
        }
        }
       for(int i =0;i<temp.size();i++)
       {
           viewContainer.addView(initalSingleBlock(vi,temp.get(i)));
       }
    }

    private View initalSingleBlock(LayoutInflater vi,Object key){
        final View v = vi.inflate(R.layout.subtype_layout, null);
        v.setTag(key);
        final ImageView img = v.findViewById(R.id.image_display);
        img.setImageResource(MainActivity.String_to_img((String)key));
        final TextView main = v.findViewById(R.id.subcateText);
        main.setText((String)key);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getContext(), List_item_activity.class);
                it.putExtra("jsonArray",navigation.get(v.getTag().toString()).toString());
                it.putExtra("Title",v.getTag().toString());
                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(getActivity(), Pair.create((View)img, "type_img"),
                        Pair.create((View)main,"type_name")
                        );
                startActivity(it,options.toBundle());
            }
        });
        return v;
    }

    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private void putInMap(String cate,JSONObject object)
    {
        if(!navigation.containsKey(cate))
            navigation.put(cate,new JSONArray());
        navigation.get(cate).put(object);
    }

    public static void applyDim(@NonNull ViewGroup parent, float dimAmount){
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * dimAmount));

        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    public static void clearDim(@NonNull ViewGroup parent) {
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.clear();
    }


}
