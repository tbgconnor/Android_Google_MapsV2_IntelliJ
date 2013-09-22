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
        Log.d(DEBUGTAG, "Selecting Marker: " + marker.getId());
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
        Log.d(DEBUGTAG, "Deselecting Marker: " + marker.getId());
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

    /*
     * Anonymous Inner Class the define marker onClick events
     */
    GoogleMap.OnMarkerClickListener onMarkerClick = new GoogleMap.OnMarkerClickListener()
    {
        @Override
        public boolean onMarkerClick(Marker marker)
        {
            // Just pass it on to the Activity
            Log.d(DEBUGTAG, "Marker Clicked Event received in MapCanvas " + marker.getId());
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
     * Method to remove all markers from map and clear all local references to those markers
     */
    public void clearMap()
    {
        // Remove all m.p. markers from arraylist
        measurementPointMarkers.clear();
        // Remove all selected markers from ArrayList
        selectedMarkers.clear();
        // Remove all lines
        measurementLinesOnMap.clear();
        //todo remove all selected lines and arcs selected arcs
        // Remove all markers from map
        map.clear();
    }

    /**
     * Method to convert geographic coordinates on the surface of the Earth in screen location XY coordinates
     * @param position (LatLng) the postition
     * @return (point) x,y coordinates
     */
    public Point getMapProjection(LatLng position)
    {
        Projection proj = map.getProjection();
        Point points = proj.toScreenLocation(position);
        return points;
    }

    public ArrayList<Point> getLineProjectionPointsOfLayer(String layerName)
    {
        ArrayList<Point> result = new ArrayList<Point>();

        for(int index = 0; index < measurementLinesOnMap.size(); index ++)
        {
            if(measurementLinesOnMap.get(index).getLayerName().equals(layerName))
            {
                LatLng ll1 = measurementLinesOnMap.get(index).getLine().getPoints().get(0);
                LatLng ll2 = measurementLinesOnMap.get(index).getLine().getPoints().get(1);
                Point p1 = getMapProjection(ll1);
                Point p2 = getMapProjection(ll2);
                result.add(p1);
                result.add(p2);
            }
        }
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
        Log.d(DEBUGTAG, "Angle not corrected: " + angle*(180/Math.PI));
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
    public Polyline drawArc(LatLng posA, LatLng posB, LatLng posC, String layerName, int color, int lineWidth)
    {
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

            Polyline arc = map.addPolyline(polylineOptions);
            return arc;
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
            Polyline arc = map.addPolyline(polylineOptions);
            return arc;
        }
        else // Collinear Point -> no Arc possible
        {
            Log.d(DEBUGTAG, "Points passed to draw arc method are collinear...");
            return null;
        }
    }

    public void addMeasurementArcOnMap(MeasurementArcOnMap mArcOnMap)
    {
        measurementArcsOnMap.add(mArcOnMap);
    }

    public boolean removeArc(String layerName, LatLng p1, LatLng p2, LatLng p3)
    {
        boolean successfulRemoval = false;
        for(MeasurementArcOnMap mArcOnMap : measurementArcsOnMap)
        {
            Log.d(DEBUGTAG, "Layer Name from unexecute command: " + layerName + " layer name expected: " + mArcOnMap.getLayerName());
            Log.d(DEBUGTAG, "Position 1 from unexecute command: " + p1.toString() + " Position 1 expected: " + mArcOnMap.getPosition01());
            Log.d(DEBUGTAG, "Position 2 from unexecute command: " + p2.toString() + " Position 2 expected: " + mArcOnMap.getPosition02());
            Log.d(DEBUGTAG, "Position 3 from unexecute command: " + p3.toString() + " Position 3 expected: " + mArcOnMap.getPosition03());
            if(layerName.equals(mArcOnMap.getLayerName()) && p1.equals(mArcOnMap.getPosition01()) && p2.equals(mArcOnMap.getPosition02()) && p3.equals(mArcOnMap.getPosition03()))
            {
                //remove line from map
                mArcOnMap.getArc().remove();
                //remove it from the arrayList
                measurementArcsOnMap.remove(mArcOnMap);
                Log.d(DEBUGTAG, "Arc Removed!");
                successfulRemoval = true;
                break;
            }
        }
        Log.d(DEBUGTAG, "SuccesfulRemoval? " + successfulRemoval);
        return successfulRemoval;
    }

}
