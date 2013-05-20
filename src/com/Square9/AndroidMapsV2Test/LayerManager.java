package com.Square9.AndroidMapsV2Test;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.ArrayList;

/**
 * Layer Manager Class to manage the measurement points
 * @version 1.0
 * @author  K. Gilissen
 */
public class LayerManager
{
    private ArrayList<MeasurementLayer> measurementLayers;
    private MeasurementLayer currentLayer;

    /**
     * default constructor for layerManager object
     * initializes arraylist and adds a "DEFAULT" layer to the arraylist and sets it as the current layer to use
     */
    public LayerManager()
    {
        measurementLayers = new ArrayList<MeasurementLayer>();
        currentLayer = new MeasurementLayer("default", BitmapDescriptorFactory.HUE_AZURE, 3);
        measurementLayers.add(currentLayer);
    }

    /**
     * constructor for layerManager object
     * @param layerName the name for the first & current layer in the newly created layerManager
     * @param color the color of the layer
     */
    public LayerManager(String layerName, float color, int lineWidth)
    {
        measurementLayers = new ArrayList<MeasurementLayer>();
        currentLayer = new MeasurementLayer(layerName, color, lineWidth);
        measurementLayers.add(currentLayer);
    }

    /**
     * Add a new Layer and set it to the current
     * @param name the new layer name
     * @param color the color of the layer
     * @return (boolean) returns false: the layer with the name all ready exists if there is no duplicate it returns true
     */
    public boolean addNewLayer(String name, float color, int lineWidth)
    {
        int index;
        boolean duplicateNameFound = false;
        for(index = 0; index < measurementLayers.size(); index++)
        {
            if(measurementLayers.get(index).getLayerName().equals(name))
            {
                duplicateNameFound = true;
            }
        }
        if(!duplicateNameFound)
        {
            MeasurementLayer ml = new MeasurementLayer(name, color, lineWidth);
            measurementLayers.add(ml);
            currentLayer = ml;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * get the number of layers available
     * @return (int) #layers
     */
    public int getNumberOfLayers()
    {
        return measurementLayers.size();
    }

    /**
     * (private) method to get a reference to a layer by name
     * @param name the name of the layer to be found
     * @return if layer is found returns the ref to the layer otherwise it returns null
     */
    private MeasurementLayer findLayerByName(String name)
    {
        int index = 0;
        MeasurementLayer mL = null;
        for(index = 0; index < measurementLayers.size(); index++)
        {
            if(measurementLayers.get(index).getLayerName().equals(name))
            {
                    mL = measurementLayers.get(index);
            }
        }
        return mL;
    }

    /**
     * add a measurement point to the CURRENT LAYER
     * @param mP the Measurement Point you wish to add
     * @return  returns true if layer was found else it will return false
     */
    public boolean addMeasurementPointToLayer(MeasurementPoint mP)
    {
        if(currentLayer != null)
        {
            currentLayer.addMeasurementPoint(mP);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * method to get the current layer used
     * @return (String) the name of the current Layer
     */
    public MeasurementLayer getCurrentLayer()
    {
        if(currentLayer != null)
            return currentLayer;
        else
            return null;
    }

    /**
     * method to set the current layer
     * @param name (String) name of current layer
     * @return boolean true if ok false if failed to set current layer
     */
    public boolean setCurrentLayer(String name)
    {
        int index = 0;
        boolean layerFound = false;
        for(index = 0; index < measurementLayers.size(); index++)
        {
            if(measurementLayers.get(index).getLayerName().equals(name))
            {
                layerFound = true;
                currentLayer = measurementLayers.get(index);
                break;
            }
        }
        return layerFound;
    }
}
