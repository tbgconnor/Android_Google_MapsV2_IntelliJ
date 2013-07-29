package com.Square9.AndroidMapsV2Test;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.List;


public class MapCanvasFragment extends MapFragment
{
    private static final String DEBUGTAG = "MapFragment";
    private static final int MAXZOOM = 21;
    private static final LatLng defaultLocation = new LatLng(50.879668, 5.309296); // Alken Belgium
    private Marker currentPositionMarker;
    private GoogleMap map;

    private onMapFragmentEventListener onMapFragmentEventListener;

    private ArrayList<Marker> measurementPointMarkers;
    private ArrayList<Marker> selectedMarkers;

    /**
     * Creates a new instance of the mapfragment
     * @return new mapCanvasFragment instance
     */
    public static MapCanvasFragment newInstance()
    {
        Log.d(DEBUGTAG, "Created a new instance of MapCanvasFragment...");
        MapCanvasFragment mapCanvasFragment = new MapCanvasFragment();
        return mapCanvasFragment;
    }

    /*
     * called once the fragment is associated with its activity.
     */
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            onMapFragmentEventListener = (onMapFragmentEventListener) activity;
        }
        catch(Exception exp)
        {
            Log.d(DEBUGTAG, exp.toString());
        }
    }

    /*
     * Called to do initial creation of a fragment
     * Retrieves attributes from savedInstanceState bundle or arguments set
     * Initializes local instance variables
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(DEBUGTAG, "onCreate MapCanvasFragment");
    }

    /*
     * Called to have the fragment instantiate its user interface view
     * Map is instantiated here
     */
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

    /*
     * makes the fragment visible to the user (based on its containing activity being started)
     */
    @Override
    public void onStart()
    {
        super.onStart();
        if(map != null)
        {
            // Register event listeners
            map.setOnMarkerClickListener(onMarkerClick);
            map.setOnMapClickListener(onMapClickListener);
            map.setOnMapLongClickListener(onMapLongClick);
        }
        // init local variables
        measurementPointMarkers = new ArrayList<Marker>();
        selectedMarkers = new ArrayList<Marker>();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle)
    {
        super.onSaveInstanceState(bundle);
    }

    /*
     * makes the fragment interacting with the user (based on its containing activity being resumed).
     */
    @Override
    public void onResume()
    {
        super.onResume();
    }

    /*
     *  fragment is no longer interacting with the user either because its activity is being paused or a fragment operation is modifying it in the activity.
     */
    @Override
    public void onPause()
    {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /*
     * allows the fragment to clean up resources associated with its View.
     */
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /*
     *  called to do final cleanup of the fragment's state.
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     *  Internal methode for initializing the Map object, can only be used if map object exists!
     *  sets map type to normal
     *  sets default values for infowindow title and snippet
     *  moves camera to default position
     */
    private void initMap()
    {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        currentPositionMarker = map.addMarker(new MarkerOptions().position(defaultLocation));
        currentPositionMarker.setTitle("Default Location");
        currentPositionMarker.setSnippet("Lat: 50.879668° Long:  5.309296°");
        currentPositionMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_current_position_arrow_large));
        currentPositionMarker.setAnchor(0.5f, 0.5f);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, MAXZOOM));
    }

    /**
     * Method to restore the current Position marker
     * @param currentPosition the current position to center to map on
     */
    public void restoreCurrentPositionMarker(LatLng currentPosition)
    {
        currentPositionMarker = map.addMarker(new MarkerOptions().position(currentPosition));
        currentPositionMarker.setTitle("Position Loaded from file");
        currentPositionMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_current_position_arrow_large));
        currentPositionMarker.setAnchor(0.5f, 0.5f);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, MAXZOOM));
    }


    /**
     * method to move the 'current position marker' to a new location
     * @param newPosition the new position
     */
    public void moveCurrentPositionMarker(LatLng newPosition)
    {
        String mTitle = "Current Position";
        String mSnippet = newPosition.toString();
        currentPositionMarker.setPosition(newPosition);
        map.moveCamera(CameraUpdateFactory.newLatLng(newPosition));
        currentPositionMarker.setTitle(mTitle);
        currentPositionMarker.setSnippet(mSnippet);
    }

    /**
     * sets the map type
     * @param mapType integer value representing the map type: 0: none <-> 4:hybrid
     */
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

    /**
     * Add a marker object to the map
     * addition ref to marker object is held in instance markerList
     * @param position LatLng object for the position
     * @param title title of the info window
     * @param snippet snippet of the info window
     * @param color the color of the marker (The resource id)
     * @return The position of the marker (LatLng)
     */
    public LatLng addMarker(LatLng position, String title, String snippet, int color)
    {
        MarkerOptions mOptions = new MarkerOptions();
        mOptions.title(title);
        mOptions.snippet(snippet);
        mOptions.icon(BitmapDescriptorFactory.defaultMarker(resolveColorOfMarker(color)));
        mOptions.position(position);
        Marker newMarker = map.addMarker(mOptions);
        // Add to list
        measurementPointMarkers.add(newMarker);
        //Return the markerPosition as it differs from the position set (Google maps API bug)
        return newMarker.getPosition();
    }

    public boolean removeMarkerFromMap(LatLng markerPosition)
    {
        boolean successfulRemoval = false;
        for(int index = 0; index < measurementPointMarkers.size(); index++)
        {
            if(markerPosition.equals(measurementPointMarkers.get(index).getPosition()))
            {
                // Remove it from the map
                measurementPointMarkers.get(index).remove();
                // Remove it from the Arraylist
                measurementPointMarkers.remove(index);
                successfulRemoval = true;
                break;
            }
        }
        return successfulRemoval;
    }

    public boolean removeSelectedMarkerFromMap(LatLng markerPosition)
    {
        boolean successfulRemoval = false;
        for(int index = 0; index < selectedMarkers.size(); index++)
        {
            if(markerPosition.equals(selectedMarkers.get(index).getPosition()))
            {
                // Remove it from the map
                selectedMarkers.get(index).remove();
                // Remove it from the ArrayList
                selectedMarkers.remove(index);
                successfulRemoval = true;
                break;
            }
        }
        return successfulRemoval;
    }

    /**
     * Add a marker object to the map
     * @param position position of the marker
     * @param title title of the marker
     * @param snippet snippet of the marker
     * @param color the color of the marker (The resource id)
     * @return Marker object add to the map
     */
    public Marker addMarkerToMap(LatLng position, String title, String snippet, int color)
    {
        MarkerOptions mOptions = new MarkerOptions();
        mOptions.title(title);
        mOptions.snippet(snippet);
        mOptions.icon(BitmapDescriptorFactory.defaultMarker(resolveColorOfMarker(color)));
        mOptions.position(position);
        Marker newMarker = map.addMarker(mOptions);
        // Add to list
        measurementPointMarkers.add(newMarker);
        //Return the marker as ref
        return newMarker;
    }

    public List<LatLng> drawLine(PolylineOptions options)
    {
        Polyline line = map.addPolyline(options);
        return line.getPoints();
    }


    public void drawArc(LatLng start, LatLng end, double radius)
    {
        Projection pjctn = map.getProjection();
        pjctn.fromScreenLocation(new Point());

    }

    public void selectMarker(LatLng markerPosition, String layerName, String snippet)
    {
        // Remove the Measurement Point marker (temp)
        this.removeMarkerFromMap(markerPosition);
        // Create a new Selected marker for this measurement point
        MarkerOptions mOptions = new MarkerOptions();
        mOptions.title(layerName);
        mOptions.snippet(snippet);
        mOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_selected_marker));
        mOptions.position(markerPosition);
        // Add it to map
        Marker selectedMarker = map.addMarker(mOptions);
        // Add it to the ArrayList
        selectedMarkers.add(selectedMarker);
    }

    public LatLng deselectMarkerAt(LatLng positionOnMap, LatLng measurementPointPosition, String layerName, String snippet, int color)
    {
        LatLng positionOftheNewMarkerOnMap = null;
        // Remove Selected Marker
        for(int i = 0; i < selectedMarkers.size(); i++)
        {
            LatLng positionOfSelectedMarker = selectedMarkers.get(i).getPosition();
            if(positionOfSelectedMarker.equals(positionOnMap))
            {
                //Remove from arraylist and from map
                selectedMarkers.remove(i).remove();
                break;
            }
        }
        // Replace original measurement point
        positionOftheNewMarkerOnMap = this.addMarker(measurementPointPosition, layerName, snippet, color);
        return positionOftheNewMarkerOnMap;
    }

    public void clearAllSelectedMarkers()
    {
        if(selectedMarkers.size() > 0)
        {
            for(int i = 0; i < selectedMarkers.size(); i++)
            {
                selectedMarkers.get(i).remove();
            }
            selectedMarkers.clear();
        }
    }

    /*
     * Anonymous Inner Class the define marker onClick events
     */
    GoogleMap.OnMarkerClickListener onMarkerClick = new GoogleMap.OnMarkerClickListener()
    {
        @Override
        public boolean onMarkerClick(Marker marker)
        {
            // Just pass it on to the Activity
            onMapFragmentEventListener.onMarkerClicked(marker);
            return true;
        }
    };


    /**
     *  Anonymous Inner Class for map clicks
     */
    GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng)
        {
            onMapFragmentEventListener.onMapClicked(latLng);
        }
    };

    /**
     * Anonymous Inner Class for map LONG clicks
     */
    GoogleMap.OnMapLongClickListener onMapLongClick = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng)
        {
            onMapFragmentEventListener.onMapLongClicked(latLng);
        }
    };

    /**
     * Method to resolve the color value from xml to bitmapDescriptionFactory Values
     * @param color holds the integer value, color xml resource resolved, of the color value to convert
     * @return color value (float) according to: http://developer.android.com/reference/com/google/android/gms/maps/model/BitmapDescriptorFactory.html
     */
    public float resolveColorOfMarker(int color)
    {
        int azure = getActivity().getResources().getColor(R.color.azure);
        int blue = getActivity().getResources().getColor(R.color.blue);
        int cyan = getActivity().getResources().getColor(R.color.cyan);
        int green = getActivity().getResources().getColor(R.color.green);
        int magenta = getActivity().getResources().getColor(R.color.magenta);
        int orange = getActivity().getResources().getColor(R.color.orange);
        int red = getActivity().getResources().getColor(R.color.red);
        int rose = getActivity().getResources().getColor(R.color.rose);
        int violet = getActivity().getResources().getColor(R.color.violet);
        int yellow = getActivity().getResources().getColor(R.color.yellow);

        if(color == azure)
            return BitmapDescriptorFactory.HUE_AZURE;
        else if(color == blue)
            return BitmapDescriptorFactory.HUE_BLUE;
        else if(color == cyan)
            return BitmapDescriptorFactory.HUE_CYAN;
        else if(color == green)
            return BitmapDescriptorFactory.HUE_GREEN;
        else if(color == magenta)
            return BitmapDescriptorFactory.HUE_MAGENTA;
        else if(color == orange)
            return BitmapDescriptorFactory.HUE_ORANGE;
        else if(color == red)
            return BitmapDescriptorFactory.HUE_RED;
        else if(color == rose)
            return BitmapDescriptorFactory.HUE_ROSE;
        else if(color == violet)
            return BitmapDescriptorFactory.HUE_VIOLET;
        else if(color == yellow)
            return BitmapDescriptorFactory.HUE_YELLOW;
        else
            return BitmapDescriptorFactory.HUE_RED;
    }

    public void clearMap()
    {
        map.clear();
    }
}
