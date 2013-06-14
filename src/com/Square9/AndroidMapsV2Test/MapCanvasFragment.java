package com.Square9.AndroidMapsV2Test;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.List;


public class MapCanvasFragment extends MapFragment
{
    private final static String DEBUGTAG = "MapFragment";
    private static final int MAXZOOM = 21;
    private static final LatLng defaultLocation = new LatLng(50.879668, 5.309296); // Alken Belgium
    private Marker currentPositionMarker;
    private LatLng currentPosition;
    private GoogleMap map;
    private String data;
    private ArrayList<Marker> markerList;
    private LayerManager layerManager;
    private Marker markerSelected01;
    private Marker markerSelected02;


    public static MapCanvasFragment newInstance(String data, LayerManager layerManager)
    {
        Log.d(DEBUGTAG, "Created a new instance of MapCanvasFragment...");
        MapCanvasFragment mapCanvasFragment = new MapCanvasFragment();
        Bundle args = new Bundle();
        args.putString("data", data);
        args.putParcelable("layerManager", layerManager);
        mapCanvasFragment.setArguments(args);
        return mapCanvasFragment;
    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(DEBUGTAG, "onCreate MapCanvasFragment");
        if(savedInstanceState != null) // Recreating the fragment, it was destroyed ...
        {
            Log.d(DEBUGTAG, "Getting data from savedInstanceState");
            data = savedInstanceState.getString("data");
            layerManager = savedInstanceState.getParcelable("layerManager");
        }
        else // savedInstanceState == null, so this is a new activiy
             // Because it - automatically restores the state of the view hierarchy of this fragment (only views with an unique id) Default implementation
        {
            Log.d(DEBUGTAG, "Bundle was null so getting data from elsewhere");
            data = getArguments().getString("data");
            layerManager = getArguments().getParcelable("layerManager");
        }
        if(currentPosition == null)
        {
            Log.d(DEBUGTAG, "unknown current Position, setting to default location");
            currentPosition = defaultLocation;
        }
        markerList = new ArrayList<Marker>();

        Log.d(DEBUGTAG, "LAYER MANAGER:  current layer: " + layerManager.getCurrentLayer().getLayerName());

    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle)
    {
       View fragmentView = super.onCreateView(layoutInflater, viewGroup, bundle);
        if(map == null)
        {
            map = getMap();
            Log.d(DEBUGTAG, "fetching map object");
            if(map != null)
            {
                Log.d(DEBUGTAG, "Map Object acquired");
                initMap();
            }
        }
        return fragmentView;
    }


    @Override
    public void onStart()
    {
        super.onStart();
        if(map != null)
        {
            map.setOnMarkerClickListener(onMarkerClick);
            map.setOnMapLongClickListener(onMapLongClick);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onResume()
    {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onPause()
    {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     *  Internal methode for initializing the Map object, can only be used is map object exists!
     *  sets map type to normal
     *  sets default values for infowindow title and snippet
     *  moves camera to default position
     */
    private void initMap()
    {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        currentPositionMarker = map.addMarker(new MarkerOptions().position(defaultLocation).title("DEFAULT LOCATION"));
        currentPositionMarker.setTitle("Default Location");
        currentPositionMarker.setSnippet("Lat: 50.879668° Long:  5.309296°");
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, MAXZOOM));
    }

    public void moveCurrentPositionMarker(LatLng newPosition)
    {
        String mTitle = "Current Postion";
        String mSnippet = newPosition.toString();
        currentPosition = newPosition;
        currentPositionMarker.setPosition(newPosition);
        map.moveCamera(CameraUpdateFactory.newLatLng(newPosition));
        currentPositionMarker.setTitle(mTitle);
        currentPositionMarker.setSnippet(mSnippet);
    }

    public void setMapType(int mapType)
    {
        Log.d(DEBUGTAG, "Map Fragment Methode 'setMapType' called with arg: " + Integer.toString(mapType));
        if(mapType >= 0 && mapType < 5) // The mapType should be an Integer -> [0,4] (see API reference)
        {
            Log.d(DEBUGTAG, "setMapType arg valid");
            if(map != null)
            {
                Log.d(DEBUGTAG, "map object is alive, changing the map type");
                map.setMapType(mapType);
            }
        }
    }

    /**
     * getter for the current maptype
     * @return type (int) the current map type [0, 4]
     */
    public int getMapType()
    {
        int type;
        Log.d(DEBUGTAG, "current map type requested...");
        if(map != null)
        {
            type = map.getMapType();
        }
        else
        {
            // Fail in Style (map type will most probably be 1 ;-) )
            type = 1;
        }
        Log.d(DEBUGTAG, "Current Type = " +  Integer.toString(type));
        return type;
    }

    public void addMarker(LatLng position, String title, String snippet, float color)
    {
        MarkerOptions mOptions = new MarkerOptions();
        mOptions.title(title);
        mOptions.snippet(snippet);
        mOptions.icon(BitmapDescriptorFactory.defaultMarker(color));
        mOptions.position(position);
        Marker newMarker = map.addMarker(mOptions);
        markerList.add(newMarker);
    }

    GoogleMap.OnMarkerClickListener onMarkerClick = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker)
        {
            /*
             *  The selection is STRICT..
             *  No more the necessary points can get selected
             *  If there are allready 2 markers selected the user should CANCEL the action
             */
            //TODO check layer

            //behind the scene:
            if(markerSelected01 == null && markerSelected02 == null)
            {
                markerSelected01 = marker;
            }
            else if(markerSelected01 != null && markerSelected02 == null)
            {
                markerSelected02 = marker;
            }
            else if(markerSelected01 == null && markerSelected02 != null)
            {
                markerSelected01 = marker;
            }
            else // All selected position are filled -> waiting for user to cancel this action
            {
                Toast.makeText(getActivity(), "There are already 2 points selected", Toast.LENGTH_LONG).show();
            }


            return true;
        }
    };

    public void confirmedAction(int actionId)
    {
        switch(actionId)
        {
            case 0: // NO ACTION
                //DO NOTHING
                return;
            case 1: // Draw Line
                if(markerSelected01 != null && markerSelected02 != null)
                {
                    // create an Options Object the set the Line Options
                    PolylineOptions plOptions = new PolylineOptions();
                    plOptions.add(markerSelected01.getPosition());
                    plOptions.add(markerSelected02.getPosition());
                    //TODO COLOR!
                    plOptions.color(Color.RED);
                    plOptions.width((float) layerManager.getCurrentLayer().getLineWidth());
                    Polyline pl = map.addPolyline(plOptions);
                    //TODO store add polyline in layer

                }
                else
                {
                    Toast.makeText(getActivity(), "There are not enough points selected to draw the line CANCELING THE ACTION", Toast.LENGTH_LONG).show();
                    cancelAction();
                }

        }
    }


    public void cancelAction()
    {
        //dereference selected markers
        markerSelected01 = null;
        markerSelected02 = null;
    }


    GoogleMap.OnMapLongClickListener onMapLongClick = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng)
        {

        }
    };


}
