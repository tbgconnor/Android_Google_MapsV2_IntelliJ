package com.Square9.AndroidMapsV2Test;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Command class to create a Measurment Line
 * @author K. Gilissen
 * @version 1.0
 */
public class CommandAddMeasurementLine implements Icommand
{

    private final static String DEBUGTAG = "CommandAddMeasurementLine";
    private LayerManager layerManager;
    private LatLng position1;
    private LatLng position2;
    private MeasurementLine measurementLine;
    private MapCanvasFragment mapCanvas;
    private LatLng posOnMap01;
    private LatLng posOnMap02;
    private MeasurementLayer layer;

    public CommandAddMeasurementLine(LayerManager lm, LatLng pos1, LatLng pos2, MapCanvasFragment mapCanvas)
    {
        layerManager = lm;
        layer = lm.getCurrentLayer();
        position1 = pos1;
        position2 = pos2;
        this.mapCanvas = mapCanvas;
        this.execute();
    }

    @Override
    public void execute()
    {
        // Draw The line
        List<LatLng> linePointsOnMap = mapCanvas.drawLine(position1, position2,layer.getLayerName(), layer.getColor(), layer.getLineWidth());
        // Create a new Measurenent Line
        posOnMap01 = linePointsOnMap.get(0);
        posOnMap02 = linePointsOnMap.get(1);
        measurementLine = new MeasurementLine(position1, position2, posOnMap01, posOnMap02);
        // Add it to the layer mananger;
        layer.addLine(measurementLine);
    }

    @Override
    public void unexecute()
    {
        // Remove it from the backend layerManager
        if(layer.removeLine(measurementLine))
        {
            //Remove it from the map
            if(!mapCanvas.removeLine(layer.getLayerName(), posOnMap01, posOnMap02))
            {
                Log.d(DEBUGTAG, "Error: unexecute cmd AddMeasurementLine: could not remove line from map");
            }
        }
        else
        {
            Log.d(DEBUGTAG, "Error: unexecute cmd AddMeasurementLine: could not find line in layer");
        }
    }
}
