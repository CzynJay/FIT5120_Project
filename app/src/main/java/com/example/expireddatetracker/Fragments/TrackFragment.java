package com.example.expireddatetracker.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.expireddatetracker.R;

public class TrackFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View x =  inflater.inflate(R.layout.fragment_track, container, false);
        LinearLayout layout = x.findViewById(R.id.filed_container);
        final TextView temp = x.findViewById(R.id.textView);
        int y = 0;
        while(y<20) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View v = vi.inflate(R.layout.field, null);
            v.setTag(String.valueOf(y));
            Button bt = v.findViewById(R.id.check);
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View d) {
                    temp.setText(v.getTag().toString());
                }
            });
            TextView textView = (TextView) v.findViewById(R.id.text);
            textView.setText( String.valueOf(y) );

            y++;
            layout.addView(v);

        }
        return x;
    }
}
