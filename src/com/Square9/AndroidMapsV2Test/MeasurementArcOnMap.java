package com.Square9.AndroidMapsV2Test;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

/**
 *
 * Measurement Arc On Map reference
 * @author K. Gilissen
 * @version 1.0
 */
public class MeasurementArcOnMap
{
    private Polyline arc;
    private String layerName;
    private LatLng position01;
    private LatLng position02;
    private LatLng position03;

    public MeasurementArcOnMap(Polyline arc, String layerName, LatLng position01, LatLng position02, LatLng position03) {
        this.arc = arc;
        this.layerName = layerName;
        this.position01 = position01;
        this.position02 = position02;
        this.position03 = position03;
    }

    public Polyline getArc() {
        return arc;
    }

    public String getLayerName() {
        return layerName;
    }

    public LatLng getPosition01() {
        return position01;
    }

    public LatLng getPosition02() {
        return position02;
    }

    public LatLng getPosition03() {
        return position03;
    }
}
