package com.Square9.AndroidMapsV2Test;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Command Class for adding a measurement point
 * @author K. Gilissen
 * @version 1.0
 */
public class CommandAddMeasurementPoint implements Icommand
{
    private final static String DEBUGTAG = "CommandAddMeasurementPoint";
    private LayerManager layerManager;
    private LatLng position;
    private MeasurementPoint measurementPoint;
    private MapCanvasFragment mapCanvas;
    private Marker markerOnMap;
    private MeasurementLayer layer;

    public CommandAddMeasurementPoint(LayerManager lm, LatLng position, MapCanvasFragment mapCanvas)
    {
        Log.d(DEBUGTAG, "Command AddMeasurement Point Created");
        layerManager = lm;
        layer = layerManager.getCurrentLayer();
        Log.d(DEBUGTAG, "Current Layer: " + layer.getLayerName() + " code: " + layer.hashCode());
        this.position = position;
        this.mapCanvas = mapCanvas;
        execute();
    }

    @Override
    public void execute()
    {
        // Create A measurement point
        measurementPoint = new MeasurementPoint(position);
        // Add marker to map.
        markerOnMap = mapCanvas.addMarkerToMap(position, layer.getLayerName(), "", layer.getColor());
        // Add marker position to measurementPoint
        measurementPoint.setMarkerPositioOnMap(markerOnMap.getPosition());
        // Add measurementPoint to layerManager
        layer.addMeasurementPoint(measurementPoint);
        Log.d(DEBUGTAG, "Adding MP to Layer: " + layer.getLayerName() + " code: " + layer.hashCode());
        Log.d(DEBUGTAG, "Command AddMeasurement Point Executed");
        Log.d(DEBUGTAG, "MeasurementPoint position (real): " + measurementPoint.getPosition());
        Log.d(DEBUGTAG, "MeasurementPoint position (On Map): " + measurementPoint.getMarkerPositioOnMap());
    }

    @Override
    public void unexecute()
    {
        // Try to find the measurment point in the layer
        // if the measurement point is found and removed
        if(layer.removeMeasurementPointByPosition(measurementPoint.getPosition()))
        {
            //remove measurementPoint from layer
            measurementPoint = null;
            //remove marker
            markerOnMap.remove();
            markerOnMap = null;
        }
        else
        {
            Log.d(DEBUGTAG, "Error: MeasurementPoint not found in layer!");
        }
        Log.d(DEBUGTAG, "Command AddMeasurement Point Unexecuted");
    }
}
