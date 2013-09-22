package com.Square9.AndroidMapsV2Test;


import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

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
        //Draw The Arc on The map
        arc = mapCanvas.drawArc(position1, position2, position3, layer.getLayerName(), layer.getColor(), layer.getLineWidth());
        //get the positions of the markers on the map
        posOnMap01 = layer.getMeasurementPointByPosition(position1).getMarkerPositionOnMap();
        posOnMap02 = layer.getMeasurementPointByPosition(position2).getMarkerPositionOnMap();
        posOnMap03 = layer.getMeasurementPointByPosition(position3).getMarkerPositionOnMap();
        //Create a new measurementArc
        measurementArc = new MeasurementArc(position1, position2, position3, posOnMap01, posOnMap02, posOnMap03);
        //add the new measurementArc instance to the layer manager
        layer.addArc(measurementArc);
        //Create a new measurementArcOnMap instance
        MeasurementArcOnMap arcOnMap = new MeasurementArcOnMap(arc, layer.getLayerName(), posOnMap01, posOnMap02, posOnMap03);
        //Add it to the ArcsOnMap Arraylist
        mapCanvas.addMeasurementArcOnMap(arcOnMap);
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
