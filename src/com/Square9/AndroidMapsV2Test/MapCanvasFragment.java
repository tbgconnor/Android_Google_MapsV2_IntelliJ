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
import java.util.Iterator;
import java.util.List;


public class MapCanvasFragment extends MapFragment
{
    private static final String DEBUGTAG = "MapFragment";
    private static final int MAXZOOM = 21;
    private static final LatLng defaultLocation = new LatLng(50.879668, 5.309296); // Alken Belgium
    private Marker currentPositionMarker;
    private GoogleMap map;

    private IonMapFragmentEventListener onMapFragmentEventListener;

    private ArrayList<Marker> measurementPointMarkers;
    private ArrayList<Marker> selectedMarkers;
    private ArrayList<MeasurementLineOnMap> measurementLinesOnMap;
    private ArrayList<MeasurementLineOnMap> selectedLines;
    private ArrayList<MeasurementArcOnMap> measurementArcsOnMap;
    private ArrayList<MeasurementArcOnMap> selectedArcs;

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
            onMapFragmentEventListener = (IonMapFragmentEventListener) activity;
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
        selectedLines = new ArrayList<MeasurementLineOnMap>();
        measurementArcsOnMap = new ArrayList<MeasurementArcOnMap>();
        selectedArcs = new ArrayList<MeasurementArcOnMap>();
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
        currentPositionMarker.setTitle(getResources().getString(R.string.currentPositionMarkerTitle));
        currentPositionMarker.setSnippet(defaultLocation.toString());
        currentPositionMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_current_position_arrow_small));
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
        currentPositionMarker.setTitle(getResources().getString(R.string.currentPositionMarkerTitle));
        currentPositionMarker.setSnippet(currentPosition.toString());
        currentPositionMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_current_position_arrow_small));
        currentPositionMarker.setAnchor(0.5f, 0.5f);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, MAXZOOM));
    }


    /**
     * method to move the 'current position marker' to a new location
     * @param newPosition the new position
     */
    public void moveCurrentPositionMarker(LatLng newPosition)
    {
        String mTitle = getResources().getString(R.string.currentPositionMarkerTitle);
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
        if(mapType >= 0 && mapType < 5) // The mapType should be an Integer -> [0,4] (see API reference)
        {
            if(map != null)
            {
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
        if(map != null)
        {
            type = map.getMapType();
        }
        else
        {
            // Fail in Style (map type will most probably be 1 ;-) )
            type = 1;
        }
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

    /**
     * Method to remove a marker from both arraylist and from map and use its position for identification
     * @param markerPosition the position of the marker to remove
     * @return true if successful removal or false if there is no marker found with the method parameter 'markerPosition'
     */
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

    public boolean deleteMarker(LatLng markerPostion)
    {
        boolean successful = false;
        for(Marker m : measurementPointMarkers)
        {
            if(m.getPosition().equals(markerPostion))
            {
                m.remove();
                Log.d(DEBUGTAG, "DELETED marker at position: " + m.getPosition().toString());
                successful = true;
                break;
            }
        }
        return successful;
    }

    /**
     * Method to select a marker on map
     * @param marker to be selected
     */
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

    /**
     * Method to deselect a Marker and reset the user comment and color
     * @param marker the marker to deselect
     * @param userComment the user comment to replace the selected snippet with
     * @param color the color to replace the selection color
     */
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

    /**
     * Method to update a marker snippet, the user can change the snippet text so it needs to be updated
     * @param markerPosition position of the marker to update the snippet
     * @param newSnippet the new snippet
     * @return boolean true if marker is found at markerPosition false if marker is NOT Found!
     */
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

    /**
     * Method to get the number of selected Markers
     * @return integer the number of selected markers
     */
    public int getNumberOfSelectedMarkers()
    {
        return selectedMarkers.size();
    }

    /**
     * Method to get the marker position at index of the selectedMarkers Arryaylist
     * @param index the array index
     * @return the position (LatLng) of the marker at the specified index
     */
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

    /**
     * Method to get the selectedMarker at the array index
     * @param index int the array index
     * @return null if index is out of range, the marker reference will be returned otherwise
     */
    public Marker getSelectedMarkerAtIndex(int index)
    {
        if(index < selectedMarkers.size())
        {
            return selectedMarkers.get(index);
        }
        else
        {
            return null;
        }
    }

    public Iterator<Marker> getSelectedMeasurementMarkerIterator()
    {
        return selectedMarkers.iterator();
    }

    public void clearSelectedMarkerList()
    {
        selectedMarkers.clear();
    }


    /**
     * Method to draw a line on the map from position01 to position02 in the specified color and with the specified stroke width
     * @param position01 the first position
     * @param position02 the second position
     * @param layerName the layername of the layer in which the line is drawn
     * @param color the color of that layer
     * @param lineWidth the linewidth property of that layer
     * @return the list of points where the line was drawn (Google API Bug)
     */
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

    /**
     * Method to remove a specific line (pos1 to pos2) in layer with layerName
     * @param layerName the name of the layer to which to line belongs to
     * @param pos1OnMap the position of the first point of the line
     * @param pos2OnMap the position of the last point of the line
     * @return true if line was removed ok false if the line was not found
     */
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

    public Iterator<MeasurementLineOnMap> getSelectedMeasurementLineOnMapIterator()
    {
        return selectedLines.iterator();
    }

    public void clearSelectedLinesList()
    {
        selectedLines.clear();
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


    /*
     *  Anonymous Inner Class for map clicks
     */
    GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng)
        {
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
     * Method to remove all measurementPoint/Lines/Arcs from map and clear all local references to them
     */
    public void clearMap()
    {
        // Remove all m.p. markers from arraylist
        measurementPointMarkers.clear();
        // Remove all selected markers from ArrayList
        selectedMarkers.clear();
        // Remove all lines
        measurementLinesOnMap.clear();
        // Remove all selected lines:
        selectedLines.clear();
        // Remove all Arcs
        measurementArcsOnMap.clear();
        // Remove all Selected arcs
        selectedArcs.clear();
        // Clear Map
        map.clear();
    }


    /**
     * Method to check if the clickedPosition is on a line in Layer with layerName
     * @param clickedPosition the position of the user click on the screen
     * @param layerName the name of the layer a line can be found
     * @param layerColor the color of that layer
     */
    public void onLineClicked(LatLng clickedPosition, String layerName, int layerColor)
    {
        //Create a projection instance to change coordinate system
        Projection projection = map.getProjection();
        // Get the XY coordinate for the user click
        Point pointClicked = projection.toScreenLocation(clickedPosition);
        // Get all the line's coordinates  in the layer
        ArrayList<Point> linePointsList = new ArrayList<Point>();

        for(int index = 0; index < measurementLinesOnMap.size(); index ++)
        {
            if(measurementLinesOnMap.get(index).getLayerName().equals(layerName))
            {
                LatLng ll1 = measurementLinesOnMap.get(index).getLine().getPoints().get(0);
                LatLng ll2 = measurementLinesOnMap.get(index).getLine().getPoints().get(1);
                Point p1 = projection.toScreenLocation(ll1);
                Point p2 = projection.toScreenLocation(ll2);
                linePointsList.add(p1);
                linePointsList.add(p2);
            }
        }
        // If the current layer contains LINES:
        if(!linePointsList.isEmpty())
        {
            //Check if we got an even number of points (every lines has to points so the size must be even...)
            int numberOfLinePoints = linePointsList.size();
            int remainder = numberOfLinePoints % 2;
            if(remainder == 0) // Even #LinePoints is required
            {
                // lines are at paired indexes: [0,1] [2,3] ... [n, n+1] --> 0 is point1 of line0 and 1 is point2 of line0 --> 2 is point1 of line1 and 3 is point2 of line1 --> ....
                // Line Index :                  0     1    .... n/2     --> ever pair n and n+1 is a line so the position of the line in array would be n/2
                int lineIndex = 0;
                for(int index = 0; index < linePointsList.size()-1; index=index+2)
                {
                    Point pOne = linePointsList.get(index);
                    Point pTwo = linePointsList.get(index+1);
                    // y = ax + c
                    double a = getLineSlope(pOne, pTwo);
                    double c = getLineOffset(pOne, a);
                    int errorMargin = 10; // Only for Y
                    // Determine X bounds
                    int xUpperBound = 0;
                    int xLowerBound = 0;
                    if(pOne.x > pTwo.x)
                    {
                        xUpperBound = pOne.x;
                        xLowerBound = pTwo.x;
                    }
                    else
                    {
                        xUpperBound = pTwo.x;
                        xLowerBound = pOne.x;
                    }
                    // Check if the click is within the bounds (x and y)
                    if( (pointClicked.y + errorMargin ) >= (a*pointClicked.x+c) && (pointClicked.y - errorMargin ) <= (a*pointClicked.x+c)
                            && pointClicked.x < xUpperBound && pointClicked.x > xLowerBound )
                    {
                        // Calc lineIndex
                        lineIndex = index / 2;
                        // Select the line by this index
                        this.selectLineByIndex(layerName, layerColor, lineIndex);
                        break;
                    }
                }
            }
            else
            {
                Log.d(DEBUGTAG, "Error: (Map Clicked) uneven number of line points received!");
            }
        }
        else
        {
            Log.d(DEBUGTAG, "(Map Clicked) No lines in current layer");
        }

    }

    /**
     * Method to get the number of Lines in a specific layer with layerName
     * @param layerName the name of the specified layer
     * @return the number of lines in the layer
     */
    public int getNumberOfLinesInLayer(String layerName)
    {
        int result =0;
        for(MeasurementLineOnMap lineOnMap : measurementLinesOnMap)
        {
            if(layerName.equals(lineOnMap.getLayerName()))
            {
                result = result + 1;
            }
        }
        return result;
    }

    /**
     * Method to calculate a lines slope based on 2 points ( 2D orthogonal space XY)
     * in terms of y = a*x +c --> a = slope
     * @param one a point of the line
     * @param two another point of the line
     * @return the slope
     */
    public double getLineSlope(Point one, Point two)
    {
        double deltaX = (double) (two.x - one.x);
        double deltaY = (double) (two.y - one.y);
        double result = deltaY/deltaX;
        return result;
    }

    /**
     * Method to calculate a line's offset based on 1 points and the slope of the line ( 2D orthogonal space XY)
     * @param one a point of the line
     * @param slope the slope of the line
     * @return the offset of the line
     */
    public double getLineOffset(Point one, double slope)
    {
        // y = ax+c => c = y - ax
        double result = one.y - (slope*one.x);
        return result;
    }

     public void selectLineByIndex(String layerName, int color, int lineNumber)
    {
        int lineNumberCounter = 0;
        for(int index = 0; index < measurementLinesOnMap.size(); index++)
        {
            if(measurementLinesOnMap.get(index).getLayerName().equals(layerName) && lineNumberCounter == lineNumber)
            {
                //LINE FOUND
                MeasurementLineOnMap selectedLine = measurementLinesOnMap.get(index);
                Polyline selectedPolyLine = selectedLine.getLine();
                if(selectedPolyLine.getColor() == getResources().getColor(R.color.selectionColor)) //It is selected and should be deselected
                {
                    selectedLines.remove(selectedLine);
                    selectedPolyLine.setColor(color);
                }
                else //Not Selected and should be selected
                {
                    selectedLines.add(selectedLine);
                    selectedPolyLine.setColor(getResources().getColor(R.color.selectionColor));
                }

                break;
            }
            else if( measurementLinesOnMap.get(index).getLayerName().equals(layerName) && lineNumberCounter != lineNumber )
            {
                lineNumberCounter++;
            }

        }
    }

    public void deselectAllMeasurementLinesOnMap(int color)
    {
        for(int index = selectedLines.size()-1; index >= 0; index--)
        {
            // Replace Color
            selectedLines.get(index).getLine().setColor(color);
            // Remove the line from the "selected (array) list"
            selectedLines.remove(index);
        }
    }

    public int getNumberOfSelectedLinesOnaMap()
    {
        return selectedLines.size();
    }

    /**
     * Method to calculate the slope of a line in a orthogonal axis system
     * @param a point 1 on the line
     * @param b point 2 on the line
     * @return the slope (double)
     */
    public double calcSlope(Point a, Point b)
    {
        double deltaY = (double) a.y - (double) b.y;
        double deltaX = (double) a.x - (double) b.x;
        return deltaY/deltaX;
    }

    /**
     * Method to calculate the angle of a radius line (from circle center to a point on the circle circumference) in an orthogonal axis space
     * @param a  point on the circle circumference
     * @param center circle center
     * @return angle in radiance (RAD)
     */
    public double calcRadAngleOfRadiusLine(Point a, Point center)
    {
        double deltaY = (double) a.y - (double) center.y;
        double deltaX = (double) a.x - (double) center.x;
        double angle = Math.atan(deltaY/deltaX);
        if(deltaY > 0 && deltaX > 0) // First Quadrant
        {
            return angle;
        }
        else if(deltaY > 0 && deltaX < 0) // Second Quadrant
        {
            return angle + Math.PI;
        }
        else if(deltaY < 0 && deltaX < 0) // Third Quadrant
        {
            return angle + Math.PI;
        }
        else                              // Fourth Quadrant
        {
            return angle + (2*Math.PI);
        }

    }

    /**
     * Method to calculate the determinant |A| 3x3
     *       _            _
     *      |  a11 a12 a13 |
     *  A = |  a21 a22 a23 |
     *      |_ a31 a32 a33_|
     *
     * From Linear Algebra:
     *
     *        |  x1 y1 1 |
     *  |A| = |  x2 y2 1 |
     *        |  x3 y3 1 |
     *
     * if   |A| > 0 -> the points were selected in a CW manner
     *      |A| = 0 -> the points are collinear
     *      |A| < 0 -> the points were selected in a CCW manner
     *
     */
    public double calcDeterminant3x3(double a11, double a12, double a13, double a21, double a22, double a23, double a31, double a32, double a33)
    {
        double determinant = a11*a22*a33 + a12*a23*a31 + a13*a21*a32 - a13*a22*a31 - a12*a21*a33 - a11*a23*a32;
        return determinant;
    }


    /**
     * Method to draw an arc on the map base on 3 points
     * @param posA LatLng position 1
     * @param posB LatLng position 2
     * @param posC LatLng position 3
     * @param layerName the name of layer to which the arc belongs to
     * @param color color that the arc should be drawn in
     * @param lineWidth line width of the arc
     */
    // from: http://www.regentsprep.org/Regents/math/geometry/GCG6/RCir.htm
    public ArrayList<LatLng> drawArc(LatLng posA, LatLng posB, LatLng posC, String layerName, int color, int lineWidth)
    {
        // Keep track of the 3 points used to create the Arc
        ArrayList<LatLng> threePointArcPointsOnMap = new ArrayList<LatLng>();
        int posAIndex = 0;
        int posBIndex = 0;
        int posCIndex = 0;
        LatLng posAOnMap;
        LatLng posBOnMap;
        LatLng posCOnMap;
        // Get the cartesian coordinates for the 3 positions
        Point pointA = map.getProjection().toScreenLocation(posA);
        Point pointB = map.getProjection().toScreenLocation(posB);
        Point pointC = map.getProjection().toScreenLocation(posC);
        //Calculate Slopes of Circle Cords [AB] sT & [BC] sR
        double sR = calcSlope(pointA, pointB);
        double sT = calcSlope(pointB, pointC);
        // Get coordinates of the 3 points
        double x1 = (double) pointA.x;
        double y1 = (double) pointA.y;
        double x2 = (double) pointB.x;
        double y2 = (double) pointB.y;
        double x3 = (double) pointC.x;
        double y3 = (double) pointC.y;
        // Calculate Center X
        double cXnum = ((sR*sT*(y3-y1)) + (sR*(x2+x3)) - (sT*(x1+x2)));
        double cXden = (2*(sR-sT));
        double cX = cXnum/cXden;
        // Calculate Center Y
        double cY = (-(1/sR) * (cX - ((x1+x2)/2))) + ((y1+y2)/2);
        Point center = new Point((int) cX, (int) cY);
        // Calculate Circle Radius
        double dx =  x1-cX;
        double dy =  y1-cY;
        double r = Math.sqrt(Math.pow(dx, 2)+Math.pow(dy ,2));
        // Calculate Radius line Angles
        double alpha = calcRadAngleOfRadiusLine(pointA, center);
        double beta = calcRadAngleOfRadiusLine(pointB, center);
        double gamma = calcRadAngleOfRadiusLine(pointC, center);
        // Calculate the determinant of coordinate matrix
        double determinant = calcDeterminant3x3(x1,y1,1, x2, y2, 1, x3, y3, 1);
        double dTheta = Math.PI/180; // 1 RAD increments
        // Create a polyline that approximates the circle segment between A and C via B
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(color);
        polylineOptions.width((float) lineWidth);
        double rotatingPhase = 0.0;
        Point newPoint = new Point(0, 0);
        polylineOptions.add(posA); // Start point of line
        //Keep track of the index of PosA in the list to retrieve it later after the polyline is place on the map (Google API bug)
        int polylinePointsListSize = polylineOptions.getPoints().size();
        posAIndex = polylinePointsListSize -1;

        if(determinant > 0) // CW
        {
            if(alpha > beta)
                alpha = alpha - 2*Math.PI ; // Calc conjugated angle
            rotatingPhase = alpha; // set start position
            while( (rotatingPhase+dTheta) < beta) // rotate by dTheta until beta is reached
            {
                rotatingPhase = rotatingPhase + dTheta;
                // Calculate new points on circumference of circle segment
                newPoint.x = center.x + (int) (r * Math.cos(rotatingPhase));
                newPoint.y = center.y + (int) (r * Math.sin(rotatingPhase));
                polylineOptions.add(map.getProjection().fromScreenLocation(newPoint));
            }
            polylineOptions.add(posB);
            polylinePointsListSize = polylineOptions.getPoints().size();
            posBIndex = polylinePointsListSize -1;
            if(beta > gamma)
                beta = beta - 2*Math.PI;
            rotatingPhase = beta;
            while( (rotatingPhase+dTheta) < gamma)
            {
                rotatingPhase = rotatingPhase + dTheta;
                // Calculate new points on circumference of circle segment
                newPoint.x = center.x + (int) (r * Math.cos(rotatingPhase));
                newPoint.y = center.y + (int) (r * Math.sin(rotatingPhase));
                polylineOptions.add(map.getProjection().fromScreenLocation(newPoint));
            }
            polylineOptions.add(posC);
            polylinePointsListSize = polylineOptions.getPoints().size();
            posCIndex = polylinePointsListSize -1;
            Polyline arc = map.addPolyline(polylineOptions);
            //PosA PosB and PosC are now part of the arc points and placed on the map
            //To bypass Android API bug: ask the positions again (they differ from the original --> posA != posAOnMap)
            posAOnMap = arc.getPoints().get(posAIndex);
            threePointArcPointsOnMap.add(posAOnMap);
            posBOnMap = arc.getPoints().get(posBIndex);
            threePointArcPointsOnMap.add(posBOnMap);
            posCOnMap = arc.getPoints().get(posCIndex);
            threePointArcPointsOnMap.add(posCOnMap);
            //Create a new measurementArcOnMap instance
            MeasurementArcOnMap arcOnMap = new MeasurementArcOnMap(arc, layerName, posAOnMap, posBOnMap, posCOnMap);
            //Add it to the ArcsOnMap Arraylist
            measurementArcsOnMap.add(arcOnMap);
            return threePointArcPointsOnMap;
        }
        else if( determinant < 0) // CCW
        {
            if(alpha < beta)
                beta = beta  - 2*Math.PI;
            rotatingPhase = alpha; // set start position
            while( (rotatingPhase-dTheta) > beta)
            {
                rotatingPhase = rotatingPhase - dTheta;
                // Calculate new points on circumference of circle segment
                newPoint.x = center.x + (int) (r * Math.cos(rotatingPhase));
                newPoint.y = center.y + (int) (r * Math.sin(rotatingPhase));
                polylineOptions.add(map.getProjection().fromScreenLocation(newPoint));
            }
            polylineOptions.add(posB);
            polylinePointsListSize = polylineOptions.getPoints().size();
            posBIndex = polylinePointsListSize -1;
            if(beta < gamma)
                gamma = gamma - 2*Math.PI;
            rotatingPhase = beta;
            while( (rotatingPhase-dTheta) > gamma)
            {
                rotatingPhase = rotatingPhase - dTheta;
                // Calculate new points on circumference of circle segment
                newPoint.x = center.x + (int) (r * Math.cos(rotatingPhase));
                newPoint.y = center.y + (int) (r * Math.sin(rotatingPhase));
                polylineOptions.add(map.getProjection().fromScreenLocation(newPoint));
            }
            polylineOptions.add(posC);
            polylinePointsListSize = polylineOptions.getPoints().size();
            posCIndex = polylinePointsListSize -1;
            Polyline arc = map.addPolyline(polylineOptions);
            //PosA PosB and PosC are now part of the arc points and placed on the map
            //To bypass Android API bug: ask the positions again (they differ from the original --> posA != posAOnMap)
            posAOnMap = arc.getPoints().get(posAIndex);
            threePointArcPointsOnMap.add(posAOnMap);
            posBOnMap = arc.getPoints().get(posBIndex);
            threePointArcPointsOnMap.add(posBOnMap);
            posCOnMap = arc.getPoints().get(posCIndex);
            threePointArcPointsOnMap.add(posCOnMap);
            //Create a new measurementArcOnMap instance
            MeasurementArcOnMap arcOnMap = new MeasurementArcOnMap(arc, layerName, posAOnMap, posBOnMap, posCOnMap);
            //Add it to the ArcsOnMap Arraylist
            measurementArcsOnMap.add(arcOnMap);
            return threePointArcPointsOnMap;
        }
        else // Collinear Point -> no Arc possible
        {
            Log.d(DEBUGTAG, "Points passed to draw arc method are collinear...");
            return null;
        }
    }

    /**
     * Method to remove an Arc from the map specified by layerName, position1, position2, position3
     * @param layerName the name of the layer the arc belongs to
     * @param p1 position 1 of the arc
     * @param p2 position 2 of the arc
     * @param p3 position 3 of the arc
     * @return true if successful removed, false if not found and thus not removed
     */
    public boolean removeArc(String layerName, LatLng p1, LatLng p2, LatLng p3)
    {
        boolean successfulRemoval = false;
        for(MeasurementArcOnMap mArcOnMap : measurementArcsOnMap)
        {
            if(layerName.equals(mArcOnMap.getLayerName()) && p1.equals(mArcOnMap.getPosition01()) && p2.equals(mArcOnMap.getPosition02()) && p3.equals(mArcOnMap.getPosition03()))
            {
                //remove line from map
                mArcOnMap.getArc().remove();
                //remove it from the arrayList
                measurementArcsOnMap.remove(mArcOnMap);
                successfulRemoval = true;
                break;
            }
        }
        return successfulRemoval;
    }

    /**
     * Method to test if there is clicked on an Arc in layer with layerName if so the arc is selected
     * @param clickedPosition the position clicked by user
     * @param layerName the name of the current layer
     * @param layerColor the color of the current layer
     */
    public void onArcClicked(LatLng clickedPosition, String layerName, int layerColor)
    {
        final int delta = 10; // Delta in screen points to determine if the user clicked near or on an arc
        Projection projection = map.getProjection();
        Point clickedPoint = projection.toScreenLocation(clickedPosition);
        int deltaX1 = clickedPoint.x + delta;
        int deltaX2 = clickedPoint.x - delta;
        int deltaY1 = clickedPoint.y + delta;
        int deltaY2 = clickedPoint.y - delta;
        int selectedColor = getResources().getColor(R.color.selectionColor);

        for(MeasurementArcOnMap arcOnMap : measurementArcsOnMap)      // Get all Arcs in the Map
        {
            if(arcOnMap.getLayerName().equals(layerName))         // Filter for correct layer
            {
                List<LatLng> arcGeoPoints = arcOnMap.getArc().getPoints();   // Get the arc's Geopoints
                for(LatLng geoPoint : arcGeoPoints)
                {
                    Point arcPoint =  projection.toScreenLocation(geoPoint);
                    if(arcPoint.x < deltaX1 && arcPoint.x > deltaX2 && arcPoint.y < deltaY1 && arcPoint.y > deltaY2) //If the point of the arc is within the error range - > the point was clicked
                    {
                        if(arcOnMap.getArc().getColor() == selectedColor) //Arc is Selected -> deselect it
                        {
                            // Reset color
                            arcOnMap.getArc().setColor(layerColor);
                            // Remove the arc from the selected list
                            selectedArcs.remove(arcOnMap);
                        }
                        else //it is not selected -> select it
                        {
                            // set selected color
                            arcOnMap.getArc().setColor(selectedColor);
                            //Add it to the selected arcs arraylist
                            selectedArcs.add(arcOnMap);
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Method to get the number of selected Arcs in Layer
     * @param layerName the name of the layer
     * @return the amount of selected Arcs
     */
    public int getNumberOfArcsOnMapInLayer(String layerName)
    {
        int result = 0;
        for(MeasurementArcOnMap arcOnMap : measurementArcsOnMap)
        {
            if(arcOnMap.getLayerName().equals(layerName))
            {
                result = result + 1;
            }
        }
        return result;
    }

    /**
     * Method to deselect all Arcs in Layer
     * @param layerName  the name of the layer
     * @param colorOfLayer  the color of the layer
     * @return true OK | False if there are Selected Arcs in an other layer still
     */
    public boolean deselectAllMeasurentArcsOnMap(String layerName, int colorOfLayer)
    {
        boolean inconsistentEntryFound = false;
        for(MeasurementArcOnMap arcOnMap : selectedArcs)
        {
            if(arcOnMap.getLayerName().equals(layerName))
            {
                arcOnMap.getArc().setColor(colorOfLayer);
            }
            else
            {
                    inconsistentEntryFound = true;
            }
        }
        selectedArcs.clear();
        return inconsistentEntryFound;
    }

    /**
     * Method to get the number of selected Arcs on the map
     * @return the (total) amount of selected Arcs
     */
    public int getNumberOfSelectedArcsOnMap()
    {
        return selectedArcs.size();
    }

    public Iterator<MeasurementArcOnMap> getSelectedMeasurementArcsOnMapIterator()
    {
        return selectedArcs.iterator();
    }

    public void clearSelectedMeasurementArcsOnMap()
    {
        selectedArcs.clear();
    }
}
