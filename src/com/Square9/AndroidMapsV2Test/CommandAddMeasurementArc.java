package com.Square9.AndroidMapsV2Test;


import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;

public class CommandAddMeasurementArc implements Icommand
{

    private final static String DEBUGTAG = "CommandAddMeasurementArc";
    private LayerManager layerManager;
    private LatLng position1;
    private LatLng position2;
    private LatLng position3;
    private MeasurementArc measurementArc;
    private MapCanvasFragment mapCanvas;
    private LatLng posOnMap01;
    private LatLng posOnMap02;
    private LatLng posOnMap03;
    private MeasurementLayer layer;
    private Polyline arc;

    public CommandAddMeasurementArc(LayerManager lm, LatLng pos1, LatLng pos2, LatLng pos3, MapCanvasFragment mapCanvas)
    {
        layerManager = lm;
        layer = lm.getCurrentLayer();
        position1 = pos1;
        position2 = pos2;
        position3 = pos3;
        this.mapCanvas = mapCanvas;
        this.execute();
    }


    @Override
    public void execute()
    {
        //Draw The Arc on The map using the actual positions of the Measurement points NOT the corresponding Marker Positions!
        ArrayList<LatLng> positionsOnMap = mapCanvas.drawArc(position1, position2, position3, layer.getLayerName(), layer.getColor(), layer.getLineWidth());
        //Get The positions A, B and C back after the arc is drawn on the map...
        posOnMap01 = positionsOnMap.get(0);
        posOnMap02 = positionsOnMap.get(1);
        posOnMap03 = positionsOnMap.get(2);
        //Create a new measurementArc
        measurementArc = new MeasurementArc(position1, position2, position3, posOnMap01, posOnMap02, posOnMap03);
        //add the new measurementArc instance to the layer manager
        layer.addArc(measurementArc);
    }

    @Override
    public void unexecute()
    {
        // Remove it from the backend layerManager
        if(layer.removeArc(measurementArc))
        {
            boolean successfulRemoval = mapCanvas.removeArc(layer.getLayerName(), posOnMap01, posOnMap02, posOnMap03);
            if(!successfulRemoval)
            {
                Log.d(DEBUGTAG, "Error: unexecute cmd AddMeasurementArc: could not remove Arc from map");
            }
        }
        else
        {
            Log.d(DEBUGTAG, "Error: unexecute cmd AddMeasurementArc: could not find Arc in layer");
        }
    }
}
