package com.example.utilisateur.anapplicationname;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends ArrayAdapter {



    public MyAdapter(Context context, ArrayList<mBluetoothDevice> blds) {
        super(context, 0, blds);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        mBluetoothDevice bltd = (mBluetoothDevice) getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.myrow, parent, false);
        }
        // Lookup view for data population
        TextView dvName = (TextView) convertView.findViewById(R.id.deviceName);
        TextView dvTitle = (TextView) convertView.findViewById(R.id.deviceTitle);
        TextView dvMac = (TextView) convertView.findViewById(R.id.deviceMac);
        // Populate the data into the template view using the data object
        dvName.setText(bltd.getName());
        dvTitle.setText(bltd.getTitle());
        dvMac.setText(bltd.getMac());

        // Return the completed view to render on screen
        return convertView;
    }

}
