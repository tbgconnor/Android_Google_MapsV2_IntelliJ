package com.Square9.AndroidMapsV2Test;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapCanvasFragment extends MapFragment
{
    private final static String TAG_MAP_FRAGMENT = "MapFragment";
    private static final int MAXZOOM = 21;
    private GoogleMap map;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle)
    {
        View v = super.onCreateView(layoutInflater, viewGroup, bundle);
        if(map == null)
        {
            map = getMap();
            Log.d(TAG_MAP_FRAGMENT, "fetching map object");
            if(map != null)
            {
                Log.d(TAG_MAP_FRAGMENT, "Map Object acquired");
                initMap();
            }
        }

        return v;
    }

    private void initMap()
    {
        LatLng currentLocation = new LatLng(50.879668, 5.309296);
        Marker currentPositionMarker;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        currentPositionMarker = map.addMarker(new MarkerOptions().position(currentLocation).title("DEFAULT LOCATION"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, MAXZOOM));
    }
}
