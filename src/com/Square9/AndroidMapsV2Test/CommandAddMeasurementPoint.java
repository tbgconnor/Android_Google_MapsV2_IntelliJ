package com.Square9.AndroidMapsV2Test;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Command Class for adding a measurement point
 * @author K. Gilissen
 * @version 1.1
 */
public class CommandAddMeasurementPoint implements Icommand
{
    private final static String DEBUGTAG = "CommandAddMeasurementPoint";
    private LayerManager layerManager;
    private LatLng position;
    private MeasurementPoint measurementPoint;
    private MapCanvasFragment mapCanvas;
    private LatLng markerPositionOnMap;
    private MeasurementLayer layer;

    public CommandAddMeasurementPoint(LayerManager lm, LatLng position, MapCanvasFragment mapCanvas)
    {
        Log.d(DEBUGTAG, "Command AddMeasurement Point Created");
        layerManager = lm;
        // maintaining a ref to the layer where the measurement points will be in create
        // the user can switch active layers before undoing or redoing....
        layer = layerManager.getCurrentLayer();
        this.position = position;
        this.mapCanvas = mapCanvas;
        // When CommandAddMeasurementPoint instance is created also EXECUTE the command
        execute();
    }

    @Override
    public void execute()
    {
        // Create A measurement point
        measurementPoint = new MeasurementPoint(position);
        // Add marker to map and get the position of the marker on the map
        markerPositionOnMap = mapCanvas.addMarker(position, layer.getLayerName(), "", layer.getColor());
        // Add marker position to measurementPoint
        measurementPoint.setMarkerPositioOnMap(markerPositionOnMap);
        // Add measurementPoint to layerManager
        layer.addMeasurementPoint(measurementPoint);
    }

    @Override
    public void unexecute()
    {
        // Try to find the measurement point in the layer
        // if the measurement point is found and removed
        if(layer.removeMeasurementPointByPosition(measurementPoint.getPosition()))
        {
            //remove local reference to the measurement point
            measurementPoint = null;
            //remove marker
            if(mapCanvas.removeMarkerFromMap(markerPositionOnMap))
            {
                 markerPositionOnMap = null;
            }
            else
            {
                Log.d(DEBUGTAG, "Error: While undoing 'add measurement point' could not remove the marker");
            }

        }
        else
        {
            Log.d(DEBUGTAG, "Error: MeasurementPoint not found in layer!");
        }
        Log.d(DEBUGTAG, "Command AddMeasurement Point Unexecuted");
    }
}
