package com.Square9.AndroidMapsV2Test;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Command class to delete measurement items: measurement points, measurement lines and measurement lines
 * @author K. Gilissen
 * @version 1.0
 */
public class CommandDeleteMeasurementItems implements Icommand
{
    private final static String DEBUGTAG = "CommandDelete";
    private LayerManager layerManager;
    private MapCanvasFragment mapCanvasFragment;
    private MeasurementLayer activeLayer;

    private ArrayList<MeasurementPoint> deletedMeasurementPoints;
    private ArrayList<MeasurementLine> deletedMeasurementLines;
    private ArrayList<MeasurementArc> deletedMeasurementArcs;


    public CommandDeleteMeasurementItems(LayerManager lm, MapCanvasFragment mapCanvas)
    {
        layerManager = lm;
        activeLayer = layerManager.getCurrentLayer();
        mapCanvasFragment = mapCanvas;

        deletedMeasurementPoints = new ArrayList<MeasurementPoint>();
        deletedMeasurementLines = new ArrayList<MeasurementLine>();
        deletedMeasurementArcs = new ArrayList<MeasurementArc>();
        this.execute();
    }


    @Override
    public void execute()
    {
        //DELETE MEASUREMENTPOINTS - MARKERS
        Log.d(DEBUGTAG, "DELETING Measurement Points");
        Iterator<Marker> mpIterator = mapCanvasFragment.getSelectedMeasurementMarkerIterator();
        while(mpIterator.hasNext())
        {
            Marker marker = mpIterator.next();
            LatLng markerPosition = marker.getPosition();
            //Remove marker from map
            if(!mapCanvasFragment.deleteMarker(markerPosition))
            {
                Log.d(DEBUGTAG, "Error trying to delete a marker from the map");
            }
            else
            {
                Log.d(DEBUGTAG, "Marker: " + marker.hashCode() + "  Deleted at position on map: " + markerPosition.toString());
            }
            //Remove the measurementPoint From the layerManager
            MeasurementPoint mp = activeLayer.getMeasurementPointByMarkerPosition(markerPosition);
            if(mp != null)
            {
                LatLng measurementPointPosition = mp.getPosition();
                if(!activeLayer.removeMeasurementPointByPosition(measurementPointPosition))
                {
                    Log.d(DEBUGTAG, "Error trying to delete a measurement point");
                }
                else
                {
                    //The Measurement point is deleted: Add it to the delete list
                    Log.d(DEBUGTAG, "Measurement Point deleted at position on map: " + mp.getMarkerPositionOnMap().toString());
                    deletedMeasurementPoints.add(mp);
                }
            }
        }
        // Clear Selected marker list as they are all deleted ;-)
        mapCanvasFragment.clearSelectedMarkerList();

        //DELETE All Selected Measurement Lines
        Log.d(DEBUGTAG, "DELETING Measurement Lines");
        Iterator<MeasurementLineOnMap> mlIterator = mapCanvasFragment.getSelectedMeasurementLineOnMapIterator();
        while(mlIterator.hasNext())
        {
            MeasurementLineOnMap mlineOnMap = mlIterator.next();
            LatLng linePosOnMap01 = mlineOnMap.getLine().getPoints().get(0);
            LatLng linePosOnMap02 = mlineOnMap.getLine().getPoints().get(1);
            //Remove it from the map
            if(!mapCanvasFragment.removeLine(mlineOnMap.getLayerName(), linePosOnMap01,  linePosOnMap02))
            {
                Log.d(DEBUGTAG, "ERROR: could not delete the line... trying again swapping the line points");
                if(!mapCanvasFragment.removeLine(mlineOnMap.getLayerName(), linePosOnMap01,  linePosOnMap02))
                {
                    Log.d(DEBUGTAG, "ERROR: could not delete the line even when swapping the line points....");
                }

            }
            // Remove the measurementline from the layer
            MeasurementLine mLine = activeLayer.getMeasurementLine(linePosOnMap01, linePosOnMap02);
            if(mLine != null)
            {
                //Remove the line
                activeLayer.removeLine(mLine);
                //Add it to the deleted list
                deletedMeasurementLines.add(mLine);
            }
            else
            {
                Log.d(DEBUGTAG, "ERROR: selected MeasurementLine not found in layer");
            }
        }
        //If all selected lines on map are deleted we can clear the selectedLines buffer
        mapCanvasFragment.clearSelectedLinesList();
        //DELETE MEASUREMENT ARCS
        Log.d(DEBUGTAG, "DELETING Measurement Arcs");
        Iterator<MeasurementArcOnMap> mArcsIterator = mapCanvasFragment.getSelectedMeasurementArcsOnMapIterator();
        while(mArcsIterator.hasNext())
        {
            MeasurementArcOnMap mArcOnMap = mArcsIterator.next();
            LatLng arcPosOnMap01 = mArcOnMap.getPosition01();
            LatLng arcPosOnMap02 = mArcOnMap.getPosition02();
            LatLng arcPosOnMap03 = mArcOnMap.getPosition03();
            //Remove Arc From the Map
            if(!mapCanvasFragment.removeArc(activeLayer.getLayerName(), arcPosOnMap01, arcPosOnMap02, arcPosOnMap03))
            {
                Log.d(DEBUGTAG, "ERROR: could not delete the Arc from the map");
            }
            else
            {
                Log.d(DEBUGTAG, "DELETED arc: " + mArcOnMap.hashCode() + " at position: " + arcPosOnMap01.toString() + arcPosOnMap02.toString() + arcPosOnMap03.toString());
            }
            //remove measurement Arc from layer
            MeasurementArc arc = activeLayer.getMeasurementArc(arcPosOnMap01, arcPosOnMap02, arcPosOnMap03);
            if(arc != null)
            {
                Log.d(DEBUGTAG, "DELETED measurement Arc");
                if(!activeLayer.removeArc(arc))
                {
                    Log.d(DEBUGTAG, "ERROR: Could not DELETE Measurement Arc from this layer");
                }
                else
                {
                    //Keep reference to the deleted measurement Arc
                    deletedMeasurementArcs.add(arc);
                }
            }
        }
        //Clear the list of selected measurement Arcs
        mapCanvasFragment.clearSelectedMeasurementArcsOnMap();
    }

    @Override
    public void unexecute()
    {
        //Restore all deleted measurementpoints
        for(MeasurementPoint mp : deletedMeasurementPoints)
        {
            //Select the correct layer
            layerManager.setCurrentLayer(activeLayer.getLayerName());
            //Add the measurement point
            layerManager.getCurrentLayer().addMeasurementPoint(mp);
            //put it back on the map
            LatLng markerPos = mapCanvasFragment.addMarker(mp.getPosition(), activeLayer.getLayerName(), mp.getComment(), activeLayer.getColor());
            //update the measurementPoint
            mp.setMarkerPositionOnMap(markerPos);
        }
        //Remove all the restored mp from the deleted list
        deletedMeasurementPoints.clear();

        //Restore all deleted measurementLines
        for(MeasurementLine ml : deletedMeasurementLines)
        {
            //Select the correct layer
            layerManager.setCurrentLayer(activeLayer.getLayerName());
            //Add the measurement point
            layerManager.getCurrentLayer().addLine(ml);
            //put it back on the map
            List<LatLng> linePositions = mapCanvasFragment.drawLine(ml.getPointOne(), ml.getPointTwo(), activeLayer.getLayerName(), activeLayer.getColor(), activeLayer.getLineWidth());
            //update the measurementLine
            ml.setLinePositionOnMap01(linePositions.get(0));
            ml.setLinePositionOnMap02(linePositions.get(1));
        }
        deletedMeasurementLines.clear();

        for(MeasurementArc mArc : deletedMeasurementArcs)
        {
            //Select the correct layer
            layerManager.setCurrentLayer(activeLayer.getLayerName());
            //Add the measurement Arc
            layerManager.getCurrentLayer().addArc(mArc);
            //put it back on the map
            ArrayList<LatLng> arcPosOnMap = mapCanvasFragment.drawArc(mArc.getMeasurementPositions().get(0), mArc.getMeasurementPositions().get(1), mArc.getMeasurementPositions().get(2),
                    activeLayer.getLayerName(), activeLayer.getColor(), activeLayer.getLineWidth());
            //update the measurement Arc
            mArc.setPositionsOnMap(arcPosOnMap.get(0), arcPosOnMap.get(1), arcPosOnMap.get(2));
        }
        deletedMeasurementArcs.clear();

    }
}
