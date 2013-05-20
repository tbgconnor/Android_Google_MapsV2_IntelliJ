package com.Square9.AndroidMapsV2Test;

import java.util.ArrayList;

/**
 *  Measurement Layer to group and contain common information for a number of measurement points
 *  @author K. Gilissen
 *  @version 1.0
 */
public class MeasurementLayer
{
    private ArrayList<MeasurementPoint> measurementPoints;
    private String layerName;
    private float color;
    private int lineWidth;

    /**
     * MeasurementLayer constructor
     * Construct an new MeasurementLayer Object to group measurement points in a common construct and reference
     * @param name the name of the layer
     * @param color the color of the layer (for the markers and drawables)
     */
    public MeasurementLayer(String name, float color, int lineWidth)
    {
        this.layerName = name;
        measurementPoints = new ArrayList<MeasurementPoint>();
        this.color = color;
        this.lineWidth = lineWidth;
    }

    /**
     * getter for the name of the MeasurementLayer
     * @return (String) name of the layer
     */
    public String getLayerName()
    {
        return layerName;
    }

    /**
     * setter for the name of the MeasurementLayer
     * @param layerName Change the name of the layer
     */
    public void setLayerName(String layerName)
    {
        this.layerName = layerName;
    }

    /**
     * get the number of measurement point in the layer
     * @return #measurementPoint objects referenced by this layer
     */
    public int getNumberOfMeasurementPointsInLayer()
    {
        return measurementPoints.size();
    }

    /**
     * add a measurementPoint to the layer
     * @param point object to add to the layer
     */
    public void addMeasurementPoint(MeasurementPoint point)
    {
        measurementPoints.add(point);
    }

    /**
     * get the color of the layer
     * @return color (float) value of BitmapDescriptionFactory.HUE_****
     */
    public float getColor()
    {
        return color;
    }

    /**
     * get the line width of the layer
     * @return line width (int) of the layer
     */
    public int getLineWidth() {
        return lineWidth;
    }

    /**
     * set the line width of the layer
     * @param lineWidth  line width of the layer
     */
    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }
}

