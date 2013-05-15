package com.Square9.AndroidMapsV2Test;

import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Object that represents a single measurement point
 *
 * @author K. Gilissen
 * @version 1.0
 */
public class MeasurementPoint
{
    private final static String DEBUGTAG = "MeasurementPoint";

    private String layer;
    private String comment;
    private LatLng position;
    private boolean onMap;

    /**
     * Constructor for Measurement Point Object
     * @param layer The layer where this measurement point belongs to.
     * @param position The position on the map
     * @param hue The Color of the (default) icon.
     */
    public MeasurementPoint(String layer, LatLng position, float hue)
    {
        this.layer = layer;
        this.position = position;
        comment = ""; //prevent null pointer errors on simple string object
        onMap = false; //placed on map ?
    }

    /**
     * getter for layer
     * @return layer where the measurement point belongs to.
     */
    public String getLayer() {
        return layer;
    }

    /**
     * setter for layer
     * @param layer  where the measurement point belongs to.
     */
    public void setLayer(String layer)
    {
        //TODO can a measurement point change layer ? ...
    }

    /**
     * getter for comment
     * @return comment of the measurement point
     */
    public String getComment() {
        return comment;
    }


    /**
     * Getter for the position of the measurement point
     * @return position of the measurement point
     */
    public LatLng getPosition()
    {
        return position;
    }

    /**
     * Setter for the position of the measurement point
     * @param position the new position of the measurement point
     */
    public void setPosition(LatLng position)
    {
        this.position = position;
    }
}
