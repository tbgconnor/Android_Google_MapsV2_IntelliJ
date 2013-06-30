package com.Square9.AndroidMapsV2Test;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Layer Manager Class to manage the measurement points
 * @version 1.0
 * @author  K. Gilissen
 */
public class LayerManager implements Iterable<MeasurementPoint>, Parcelable
{
    private ArrayList<MeasurementLayer> measurementLayers;
    private MeasurementLayer currentLayer;

    /**
     * default constructor for layerManager object
     * Creates a new layerManager Object including a (MeasurementLayer) DEFAULT LAYER OBJECT and set it as CURRENT
     * initializes arraylist and adds a "DEFAULT" layer to the arraylist and sets it as the current layer to use
     */
    public LayerManager()
    {
        measurementLayers = new ArrayList<MeasurementLayer>();
        currentLayer = new MeasurementLayer("default", Color.RED, 3);
        measurementLayers.add(currentLayer);
    }

    /**
     * constructor for layerManager object
     * Creates a new layerManager Object including a (MeasurementLayer) with constructor parameters provided and set it as CURRENT
     * @param layerName the name for the first & current layer in the newly created layerManager
     * @param color the color of the layer
     * @param lineWidth the line width of the layer
     */
    public LayerManager(String layerName, int color, int lineWidth)
    {
        measurementLayers = new ArrayList<MeasurementLayer>();
        currentLayer = new MeasurementLayer(layerName, color, lineWidth);
        measurementLayers.add(currentLayer);
    }


    /**
     * Add a new Layer and set it to the current
     * @param name the new layer name
     * @param color the color of the layer
     * @param lineWidth the line width of the new layer to add
     * @return (boolean) returns false: the layer with the name all ready exists if there is no duplicate it returns true
     */
    public boolean addNewLayer(String name, int color, int lineWidth)
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
     * get the number of layers in the layer manager
     * @return (int) Number of layers
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
     * @return true if layer was found else it will return false
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
        MeasurementLayer layerToFind = null;
        if( (layerToFind = findLayerByName(name)) != null )
        {
            currentLayer = layerToFind;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Change the current layer by index
     * @param index the index of the layer to set to current layer in the list
     * @return true: index is not out of bounds, current layer changed false: failed, index out of bounds, current layer not changed
     */
    public boolean setCurrentLayerByIndex(int index)
    {
        boolean indexWithinListBounds = false;
        if(index < measurementLayers.size())
        {
            indexWithinListBounds = true;
            currentLayer = measurementLayers.get(index);
        }
        return indexWithinListBounds;
    }

    /**
     * get Iterator object of list
     * @return Iterator instance to navigate through the list
     */
    public Iterator<MeasurementLayer> getMeasurementLayerIterator()
    {
        return measurementLayers.iterator();
    }


    /**
     *  Anonymous Inner Class implementing a custom Iterator Object
     *  Iterator implementation joins all layers so that all MeasurementPoint objects can be accessed in one continuous fashion
     * @return  Iterator<MeasurementPoint>
     */
    @Override
    public Iterator<MeasurementPoint> iterator() {
      return new Iterator<MeasurementPoint>()
      {
          int iteratorCounter;

          @Override
          public boolean hasNext()
          {
              int totalOfMeasurementPoints = 0;
              for(MeasurementLayer layer : measurementLayers)
              {
                  totalOfMeasurementPoints = totalOfMeasurementPoints + layer.getNumberOfMeasurementPoints();
              }
              return iteratorCounter < totalOfMeasurementPoints;
          }

          @Override
          public MeasurementPoint next()
          {
              int lowerLimit = 0;
              MeasurementPoint mP = new MeasurementPoint(new LatLng(50.879752,5.308601));
              for(MeasurementLayer layer : measurementLayers)
              {
                  if(iteratorCounter >= lowerLimit && iteratorCounter <= (lowerLimit+layer.getNumberOfMeasurementPoints()-1))
                  {
                      mP = layer.getMeasurementPointByIndex((iteratorCounter - lowerLimit));

                  }
                  else
                  {
                      lowerLimit = lowerLimit + layer.getNumberOfMeasurementPoints();
                  }
              }
              iteratorCounter++;
              return mP;
          }

          @Override
          public void remove(){}
      };
    }

    /**
     * Overloaded constructor which expects a Parcel as a parameter and calls the readFromParcel() utility method
     * @param in (Parcel)
     */
    public LayerManager(Parcel in)
    {
        measurementLayers = new ArrayList<MeasurementLayer>();
        currentLayer = new MeasurementLayer("default", Color.RED, 3);
        readFromParcel(in);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeTypedList(measurementLayers);
        dest.writeParcelable(currentLayer, 0);
    }

    private void readFromParcel(Parcel in)
    {
        in.readTypedList(measurementLayers, MeasurementLayer.CREATOR);
        currentLayer = in.readParcelable(MeasurementLayer.class.getClassLoader());
    }

    public static final Parcelable.Creator<LayerManager> CREATOR = new Parcelable.Creator<LayerManager>()
    {
        public LayerManager createFromParcel(Parcel in) {
            return new LayerManager(in);
        }

        public LayerManager[] newArray(int size) {
            return new LayerManager[size];
        }
    };
}
