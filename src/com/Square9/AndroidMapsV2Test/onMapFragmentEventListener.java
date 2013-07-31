package com.Square9.AndroidMapsV2Test;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Interface for marker click events call-back to activity
 * @author Koen Gilissen
 * @version 1.0
 */
public interface onMapFragmentEventListener
{
    void onMarkerClicked(Marker marker);
    void onMapClicked(LatLng clickPosition);
    void onMapLongClicked(LatLng longClickPosition);
    void onInfoWindowClicked(Marker marker);
}
