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
    private ArrayList<MeasurementLineOnMap> measurementLinesOnMap;

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
            map.setOnInfoWindowClickListener(onInfoWindowClick);
        }
        // init local variables
        measurementPointMarkers = new ArrayList<Marker>();
        selectedMarkers = new ArrayList<Marker>();
        measurementLinesOnMap = new ArrayList<MeasurementLineOnMap>();
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
     *  Internal method for initializing the Map object, can only be used if map object exists!
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
                String markerSnippet = measurementPointMarkers.get(index).getSnippet();
                // If it is a selected marker also remove it from the selected list
                if(markerSnippet.equals(getActivity().getString(R.string.marker_snippet_selected)))
                {
                    selectedMarkers.remove(measurementPointMarkers.get(index));
                }
                // Remove it from the map
                measurementPointMarkers.get(index).remove();
                // Remove it from the MeasurmentPoint Arraylist
                measurementPointMarkers.remove(index);
                successfulRemoval = true;
                break;
            }
        }
        return successfulRemoval;
    }

    public void selectMarker(Marker marker)
    {
        String snippet = getActivity().getResources().getString(R.string.marker_snippet_selected);
        // Replace snippet
        marker.setSnippet(snippet);
        // Replace Icon
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_selected_marker));
        // Add it to the ArrayList
        selectedMarkers.add(marker);
    }

    public void deselectMarker(Marker marker, String userComment, int color)
    {
        // Put Snippet back
        marker.setSnippet(userComment);
        // Put Icon back
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(resolveColorOfMarker(color)));
        // Remove it from the selected Marker list
        if(!selectedMarkers.remove(marker))
        {
            Log.d(DEBUGTAG, "Error: could not find the marker in selected list to remove it from the list while deselecting the marker");
        }
    }

    public boolean updateMarkerSnippet(LatLng markerPosition, String newSnippet)
    {
        boolean markerFound = false;
        for(int mCounter = 0; mCounter < measurementPointMarkers.size(); mCounter++)
        {
            Marker m = measurementPointMarkers.get(mCounter);
            if( markerPosition.equals(m.getPosition()))
            {
                // Hide info window if shown...
                if(m.isInfoWindowShown())
                    m.hideInfoWindow();
                markerFound = true;
                m.setSnippet(newSnippet);
                m.showInfoWindow();
            }
        }
        return markerFound;
    }

    public int getNumberOfSelectedMarkers()
    {
        return selectedMarkers.size();
    }

    public LatLng getSelectedMarkerPositionAtIndex(int index)
    {
        if(index < selectedMarkers.size())
        {
            return selectedMarkers.get(index).getPosition();
        }
        else
        {
            return null;
        }
    }

    public List<LatLng> drawLine(LatLng position01, LatLng position02, String layerName, int color, int lineWidth)
    {
        //Create new polyLineOptions instance
        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.add(position01);
        lineOptions.add(position02);
        lineOptions.color(color);
        lineOptions.width((float) lineWidth);
        //add line to map
        Polyline line = map.addPolyline(lineOptions);
        //Create new MeasurementLineOnMap instance
        MeasurementLineOnMap lineOnMap = new MeasurementLineOnMap(line, layerName);
        // add it to the arryalist for future references
        measurementLinesOnMap.add(lineOnMap);
        //Return the positions of the line on the map
        return line.getPoints();
    }

    public boolean removeLine(String layerName, LatLng pos1OnMap, LatLng pos2OnMap)
    {
        boolean succesfulRemoval = false;
        for(MeasurementLineOnMap lineOnMap : measurementLinesOnMap)
        {
            if(layerName.equals(lineOnMap.getLayerName()) && pos1OnMap.equals(lineOnMap.getLine().getPoints().get(0)) && pos2OnMap.equals(lineOnMap.getLine().getPoints().get(1))
                || layerName.equals(lineOnMap.getLayerName()) && pos1OnMap.equals(lineOnMap.getLine().getPoints().get(1)) && pos2OnMap.equals(lineOnMap.getLine().getPoints().get(0)))
            {
                //remove line from map
                lineOnMap.getLine().remove();
                //remove it from the arrayList
                measurementLinesOnMap.remove(lineOnMap);
                succesfulRemoval = true;
                break;
            }
        }
        return succesfulRemoval;
    }


    public void drawArc(LatLng start, LatLng end, double radius)
    {
        Projection pjctn = map.getProjection();
        pjctn.fromScreenLocation(new Point());

    }


    /*
     * Anonymous Inner Class the define marker onClick events
     */
    GoogleMap.OnMarkerClickListener onMarkerClick = new GoogleMap.OnMarkerClickListener()
    {
        @Override
        public boolean onMarkerClick(Marker marker)
        {
            Point p = map.getProjection().toScreenLocation(marker.getPosition());
            Log.d(DEBUGTAG, "Marker at xy: " + p.toString());

            // Just pass it on to the Activity
            onMapFragmentEventListener.onMarkerClicked(marker);
            return true;
        }
    };


    /*
     *  Anonymous Inner Class for map clicks
     */
    GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng)
        {
            Point p = map.getProjection().toScreenLocation(latLng);
            Log.d(DEBUGTAG, "Map Clicked postition: " + p.toString());
            onMapFragmentEventListener.onMapClicked(latLng);
        }
    };

    /*
     * Anonymous Inner Class for map LONG clicks
     */
    GoogleMap.OnMapLongClickListener onMapLongClick = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng)
        {
            onMapFragmentEventListener.onMapLongClicked(latLng);
        }
    };

    /*
     * Anonymous Inner Class for info window clicks
     */
    GoogleMap.OnInfoWindowClickListener onInfoWindowClick = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker)
        {
            onMapFragmentEventListener.onInfoWindowClicked(marker);
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

    /**
     * Method to remove all markers from map and clear all local references to those markers
     */
    public void clearMap()
    {
        // Remove all m.p. markers from arraylist
        measurementPointMarkers.clear();
        // Remove all selected markers from ArrayList
        selectedMarkers.clear();
        // Remove all markers from map
        map.clear();
    }
}
