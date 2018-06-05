package com.example.omer.haritaolcum.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.example.omer.haritaolcum.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mWindow;
    private Context mContext;

    public CustomInfoWindowAdapter(Context context){
        mContext = context;
        mWindow= LayoutInflater.from(context).inflate(R.layout.custom_info_window,null);

    }

    private void randerWindowText(Marker marker,View view){

    }



    @Override
    public View getInfoWindow(Marker marker) {

        randerWindowText(marker,mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        randerWindowText(marker,mWindow);
        return mWindow;
    }
}
