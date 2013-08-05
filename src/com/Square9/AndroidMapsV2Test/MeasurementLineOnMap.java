package com.Square9.AndroidMapsV2Test;

import com.google.android.gms.maps.model.Polyline;

/**
 * Class representing a polyline on the map
 * @author K. Gilissen
 * @version 1.0
 */
public class MeasurementLineOnMap
{
    private Polyline line;
    private String layerName;

    public MeasurementLineOnMap(Polyline line, String layerName)
    {
        this.line = line;
        this.layerName = layerName;
    }

    public Polyline getLine() {
        return line;
    }

    public void setLine(Polyline line) {
        this.line = line;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }
}
