package com.Square9.AndroidMapsV2Test;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.*;
import java.util.ArrayList;


public class MapCanvasFragment extends MapFragment
{
    private static final String DEBUGTAG = "MapFragment";
    private static final int MAXZOOM = 21;
    private static final double EARTH_RADIUS = 6378100.0;
    private int offset;
    private static final LatLng defaultLocation = new LatLng(50.879668, 5.309296); // Alken Belgium
    private Marker currentPositionMarker;
    private LatLng currentPosition;
    private GoogleMap map;
    private String data;
    private ArrayList<Marker> markerList;
    private ArrayList<Polyline> polyLineList;
    private LayerManager layerManager;

    private Marker markerSelected01;
    private Marker markerSelected02;

    //Actions
    /*
     *  actionId list:
     *  0 = Reserved, "no action"
     *  1 = add measurement point
     *  2 = draw line
     *  3 = draw arc
     *  4 = add picture
     */
    private int actionId;

    /**
     * Creates a new instance of the mapfragment
     * initializing the instance with data and layermanager
     * @param data dummy data object
     * @param layerManager model object which holds the structure of and to the measurement points
     * @return new mapCanvasFragment instance
     */
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

    /*
     * called once the fragment is associated with its activity.
     */
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
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
        polyLineList =  new ArrayList<Polyline>();
        actionId = 0;
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
            map.setOnMarkerClickListener(onMarkerClick);
            map.setOnMapLongClickListener(onMapLongClick);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle)
    {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable("layerManager", layerManager); //Saving the Layer Manager
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
     * methode to move the 'current position marker' to a new location
     * @param newPosition the new position
     */
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
     * @param color the color of the marker
     */
    public void addMarker(LatLng position, String title, String snippet, int color, MeasurementPoint point)
    {
        MarkerOptions mOptions = new MarkerOptions();
        mOptions.title(title);
        mOptions.snippet(snippet);
        mOptions.icon(BitmapDescriptorFactory.defaultMarker(resolveColorOfMarker(color)));
        mOptions.position(position);
        Marker newMarker = map.addMarker(mOptions);
        //TODO add markerPositionOnMap to model for future ref
        point.setMarkerPositioOnMap(newMarker.getPosition());
        markerList.add(newMarker);
    }

    /*
     * Anonymous Inner Class the define marker onClick events
     */
    GoogleMap.OnMarkerClickListener onMarkerClick = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker)
        {
            /*
             *  Actions that only need 1 selected marker are handled here
             *  Actions that need more selected markers are handled after they have been confirmed (see below...)
             */
            //TODO check layer
            //TODO is Action in progress ?

            switch(actionId)
            {
                case 0:
                    return true;
                case 1:
                    return true;
                case 2:   //draw line ...
                    if(markerSelected01 == null && markerSelected02 == null)
                    {
                        markerSelected01 = marker;
                        marker.setSnippet("First Point Selected, please select a second point");
                        marker.showInfoWindow();
                    }
                    else if(markerSelected01 != null && markerSelected02 == null)
                    {
                        markerSelected02 = marker;
                        marker.setSnippet("Second point selected");
                        marker.showInfoWindow();
                    }
                    else if(markerSelected01 == null && markerSelected02 != null)
                    {
                        markerSelected01 = marker;
                        markerSelected02 = marker;
                        marker.setSnippet("Second point selected");
                        marker.showInfoWindow();
                    }
                    else // All selected position are filled -> waiting for user to cancel this action
                    {
                        Toast.makeText(getActivity(), "There are already 2 points selected", Toast.LENGTH_LONG).show();
                    }
                    return true;
                case 3:  //draw arc
                    return true;
                case 4: // add photo to measurement point
                    markerSelected01 = marker;
                    LatLng markerPosition = markerSelected01.getPosition();
                    String markerLayerName = markerSelected01.getTitle();
                    //Todo Marker title to match the layer by name ... could be possible source of bug
                    MeasurementLayer layer = layerManager.getLayerByName(markerLayerName);
                    markerSelected01.setSnippet("Error while attaching photo");
                    if(layer != null) // Correct Layer Found
                    {
                        Log.d(DEBUGTAG, "Layer found");
                        MeasurementPoint point = layer.getMeasurementPointByMarkerPosition(markerPosition);

                        if(point != null)
                        {
                            Log.d(DEBUGTAG, "Measurement Point Found");
                            Activity act =  getActivity();
                            String ref = ((MapTest) act).getLastPhotoReference();
                            point.setPhotoFilePath(ref);
                            markerSelected01.setSnippet("photo added");
                        }
                        else
                        {
                            Log.d(DEBUGTAG, "ERROR: Point not found in layer while adding photo ref");
                            Log.d(DEBUGTAG, "Marker Position: " + markerPosition.toString());
                            for(int loopIndex = 0; loopIndex < layer.getNumberOfMeasurementPoints(); loopIndex++)
                            {
                                Log.d(DEBUGTAG, "Measurement Point " + Integer.toString(loopIndex) + ":" + layer.getMeasurementPointByIndex(loopIndex).getPosition().toString());
                            }
                        }
                    }
                    else
                    {
                        Log.d(DEBUGTAG, "ERROR: layer not found while adding photo ref");
                    }

                    markerSelected01.showInfoWindow();
                    actionId = 0; // Reset action id
                default:
                    return true;

            }
        }
    };

    /**
     * User can perform 'actions' on the map such as draw lines ....
     * These actions are confirmed by by an actionbar but of the activity.
     * This method performs the desired action
     */
    public void confirmedAction()
    {
        switch(actionId)
        {
            case 0: // NO ACTION
                //DO NOTHING
                return;
            case 1://add marker to map
                MeasurementLayer currentLayer = layerManager.getCurrentLayer();
                // get the last measurement point added to the current layer
                MeasurementPoint last = currentLayer.getMeasurementPointByIndex(currentLayer.getNumberOfMeasurementPoints()-1);
                addMarker(last.getPosition(), currentLayer.getLayerName(), Integer.toString(currentLayer.hashCode()), currentLayer.getColor(), last);
                actionId = 0;   //reset action id
                return;
            case 2: // Draw Line
                if(markerSelected01 != null && markerSelected02 != null)
                {
                    // create an Options Object the set the Line Options
                    PolylineOptions plOptions = new PolylineOptions();
                    plOptions.add(markerSelected01.getPosition());
                    plOptions.add(markerSelected02.getPosition());
                    plOptions.color(layerManager.getCurrentLayer().getColor());
                    plOptions.width((float) layerManager.getCurrentLayer().getLineWidth());
                    Polyline pl = map.addPolyline(plOptions);
                    polyLineList.add(pl);
                    MapLine line = new MapLine(markerSelected01.getPosition(), markerSelected02.getPosition());
                    layerManager.getCurrentLayer().addLine(line);
                    // reset variables
                    // dereference selected markers
                    markerSelected01 = null;
                    markerSelected02 = null;
                    //clear user selected action
                    actionId = 0; // Reset action id
                }
                else
                {
                    Toast.makeText(getActivity(), "There are not enough points selected to draw the line CANCELING THE ACTION", Toast.LENGTH_LONG).show();
                    cancelAction();
                }
                return;
            case 3: // Draw Arc
                Toast.makeText(getActivity(), "Drawing Arc", Toast.LENGTH_LONG).show();
                actionId = 0; // Reset action id
                return;
            case 4: // add photo
                //Nothing to do, this can be handled completely in markeronclicklistener
                return;
            default:
                return;
        }
    }

    /**
     * Method for cancelling the Action
     */
    public void cancelAction()
    {
        //dereference selected markers
        markerSelected01 = null;
        markerSelected02 = null;
        //clear user selected action
        actionId = 0;

        Toast.makeText(getActivity(), "Action Canceled", Toast.LENGTH_SHORT).show();
    }

    /**
     * Getter for actionId attribute
     * the int resembles an action chosen by the user
     * @return the actionId value
     */
    public int getActionId()
    {
        return actionId;
    }

    /**
     * Setter for actionId attribute
     * @param actionId
     */
    public void setActionId(int actionId)
    {
        this.actionId = actionId;
    }

    /**
     * Anonymous Inner Class for map clicks
     */
    GoogleMap.OnMapLongClickListener onMapLongClick = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng)
        {

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

    /*
     * Canvas Bitmap overlay on Map does not scale ...
     * Only here to illustrate problem
     *  free draw help method
     */
    private int convertMetersToPixels(double lat, double lng, double radiusInMeters)
    {
        double lat1 = radiusInMeters / EARTH_RADIUS;
        double lng1 = radiusInMeters / (EARTH_RADIUS * Math.cos((Math.PI * lat / 180)));

        double lat2 = lat + lat1 * 180 / Math.PI;
        double lng2 = lng + lng1 * 180 / Math.PI;

        Point p1 = map.getProjection().toScreenLocation(new LatLng(lat, lng));
        Point p2 = map.getProjection().toScreenLocation(new LatLng(lat2, lng2));

        return Math.abs(p1.x - p2.x);
    }

    /*
    *  http://stackoverflow.com/questions/13991301/android-maps-api-v2-draw-circle
    */
    private Bitmap getBitmap()
    {
        // stroke color
        Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setColor(0xFF0000FF);
        paint2.setStyle(Paint.Style.STROKE);


        // circle radius - 200 meters
        int radius = offset = convertMetersToPixels(currentPosition.latitude, currentPosition.longitude, 5);

        // create empty bitmap
        Bitmap b = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        c.drawCircle(radius, radius, radius, paint2);

        return b;
    }

    private LatLng getCoords(double lat, double lng) {

        LatLng latLng = new LatLng(lat, lng);

        Projection proj = map.getProjection();
        Point p = proj.toScreenLocation(latLng);
        p.set(p.x, p.y + offset);

        return proj.fromScreenLocation(p);
    }

    public void drawArc()
    {
        MarkerOptions options = new MarkerOptions();
        options.position(getCoords(currentPosition.latitude, currentPosition.longitude));
        options.icon(BitmapDescriptorFactory.fromBitmap(getBitmap()));

        Marker marker = map.addMarker(options);
    }
}
