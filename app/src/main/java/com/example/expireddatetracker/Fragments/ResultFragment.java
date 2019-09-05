package com.example.expireddatetracker.Fragments;



import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.example.expireddatetracker.MainActivity;
import com.example.expireddatetracker.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ResultFragment extends Fragment{
    private LinearLayout viewContainer;
    private Map<String,JSONArray> navigation = new HashMap<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View x =  inflater.inflate(R.layout.fragment_result, container, false);
        Bundle bundle =  this.getArguments();
        TextView tx = x.findViewById(R.id.query);
        viewContainer = x.findViewById(R.id.result_container);
        ImageView bt = x.findViewById(R.id.back);
        String query = bundle.getString("key");
        searchResult(MainActivity.food_source,query);
        showNavigation();
        tx.setText(query);
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

    private void searchResult(JSONArray source, String query)
    {
        Log.e("check",query);
        boolean multi = false;
        if (query.split("&").length>1)
            multi=true;
        //JSONArray result= new JSONArray();

        for(int i =0;i<source.length();i++)
        {
            try {
                JSONObject temp = source.getJSONObject(i);
                String value = temp.toString().toLowerCase();
                query = query.trim().toLowerCase();
                if (!multi) {
                    if (isNumeric(query))
                    {
                        if((int)temp.get("food_id")==Integer.parseInt(query)){
                            putInMap(temp.getString("Nav_category"),temp);
                        }
                    }
                    else{
                    if (value.contains(query)){
                        putInMap(temp.getString("Nav_category"),temp);
                    }}
                }
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
    }

    private void showNavigation(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        if(navigation.keySet().toArray().length==0)
        {
            final View v = vi.inflate(R.layout.subtype_layout, null);
            View right = v.findViewById(R.id.right_arrow);
            right.setVisibility(View.GONE);
            TextView main = v.findViewById(R.id.subcateText);
            main.setText(R.string.no_result);

            viewContainer.addView(v);
            return;
        }
        for(Object key:navigation.keySet().toArray())
        {
            Log.e("abc",navigation.get(key).toString());
            if (navigation.get(key).length() !=0){
            final View v = vi.inflate(R.layout.subtype_layout, null);
            v.setTag(key);
            TextView main = v.findViewById(R.id.subcateText);
            main.setText((String)key);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popUpWindow(v.getTag().toString(),navigation.get(v.getTag().toString()));
                }
            });
                viewContainer.addView(v);
        }}
    }

    private void popUpWindow(String key,JSONArray jsonArray)
    {
        viewContainer.setVisibility(View.GONE);
        LayoutInflater layoutInflater = (LayoutInflater)getContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = (int) (displayMetrics.heightPixels );
        int width = (int)(displayMetrics.widthPixels);
        View popupView = layoutInflater.inflate(R.layout.cate_popup, null);
        final PopupWindow popupWindow=new PopupWindow(popupView,
                (int) (width*0.9), ViewGroup.LayoutParams.WRAP_CONTENT ,
                true);
        View scrollView = popupView.findViewById(R.id.subcate_scroll);
        scrollView.getLayoutParams().height = (int)(height*0.5);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        View close = popupView.findViewById(R.id.cate_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        final ViewGroup root = (ViewGroup) getActivity().getWindow().getDecorView().getRootView();
        applyDim(root,0.5f);
        popupWindow.setAnimationStyle(R.style.Animation_Design_BottomSheetDialog);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        TextView navi_title = popupView.findViewById(R.id.navi_title);
        navi_title.setText(key);
        showResult(popupView,jsonArray);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                viewContainer.setVisibility(View.VISIBLE);
                clearDim(root);
            }
        });

    }

    private void showResult(View x,JSONArray jsonArray){
        LinearLayout layout = x.findViewById(R.id.cate_container);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthPixels  = displayMetrics.widthPixels;
        if (jsonArray.length()==0)
        {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            final View v = vi.inflate(R.layout.search_row, null);
            TextView main = v.findViewById(R.id.mainname);
            TextView sub = v.findViewById(R.id.subname);
            main.setText("No result");
            sub.setText("Please enter correct food name");
            layout.addView(v);
        }
        for(int i=0;i<jsonArray.length();i++)
        {
            try {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            final View v = vi.inflate(R.layout.search_row, null);
            TextView main = v.findViewById(R.id.mainname);
            TextView sub = v.findViewById(R.id.subname);
            final JSONObject temp = (JSONObject) jsonArray.get(i);
            v.setTag(temp.get("food_id"));
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String id = temp.getString("food_id");
                        Intent intent = new Intent(getContext(), ItemActivity.class);
                        intent.putExtra("id",id);
                        intent.putExtra("name",temp.get("food_name").toString());
                        intent.putExtra("sub",temp.get("food_subtitle").toString());
                        intent.putExtra("jsonObject",temp.toString());
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            main.setText(temp.getString("food_name"));
            sub.setText(temp.getString("food_subtitle").equals("null")?"":temp.getString("food_subtitle"));
            int main_height = main.getMaxHeight();
            int sub_height = sub.getMaxHeight();
            int total_height = main_height + sub_height;
                LinearLayout.LayoutParams paramsBt = new LinearLayout.LayoutParams(widthPixels, total_height);
            v.setLayoutParams(paramsBt);
            layout.addView(v);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

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
