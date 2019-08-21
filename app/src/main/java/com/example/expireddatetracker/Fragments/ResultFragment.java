package com.example.expireddatetracker.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.expireddatetracker.R;

public class ResultFragment extends Fragment {
    private TextView tx ;
    private ImageView bt;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View x =  inflater.inflate(R.layout.fragment_result, container, false);
        Bundle bundle =  this.getArguments();
        tx = x.findViewById(R.id.query);
        bt = x.findViewById(R.id.back);
        String v = bundle.getString("key");
        tx.setText(v);


         bt.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 getFragmentManager().popBackStack();
             }
         });
        return x;
    }


}
