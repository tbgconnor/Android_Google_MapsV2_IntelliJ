package com.Square9.AndroidMapsV2Test;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *  Measurement Layer to group and contain common information for a number of measurement points
 *  @author K. Gilissen
 *  @version 1.0
 */
public class MeasurementLayer implements Parcelable
{
    private ArrayList<MeasurementPoint> measurementPoints;
    private String layerName;
    private int color;
    private int lineWidth;

    private ArrayList<MapLine> lines;

    /**
     * MeasurementLayer constructor
     * Construct an new MeasurementLayer Object to group measurement points in a common construct and reference
     * @param name the name of the layer
     * @param color the color of the layer (for the markers and drawables)
     */
    public MeasurementLayer(String name, int color, int lineWidth)
    {
        this.layerName = name;
        measurementPoints = new ArrayList<MeasurementPoint>();
        this.color = color;
        this.lineWidth = lineWidth;
        lines = new ArrayList<MapLine>();
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
    public int getNumberOfMeasurementPoints()
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
     * get a measurementPoint object from the layer by its index in de list
     * @param index the index of the measurementPoint from 0 .. layer size - 1
     * @return the MeasurementPoint Object at index "index"
     */
    public MeasurementPoint getMeasurementPointByIndex(int index)
    {
        if(index < measurementPoints.size())
        {
            return measurementPoints.get(index);
        }
        else
        {
            return null;
        }
    }


    /**
     * get a MeasurementPoint object from the layer by its position
     * @param position the position of the measurment point
     * @return the measurement point object or null if it was not found
     */
    public MeasurementPoint getMeasurementPointByPosition(LatLng position)
    {
        MeasurementPoint mP = null;
        for( MeasurementPoint point : measurementPoints)
        {
            if(point.getPosition().equals(position))
            {
                mP = point;
            }
        }
        return mP;
    }

    /**
     * Method for removing a measurement point from the layer
     * @param position the position of the measurementPoint
     * @return true successful removal of the measurementPoint at position
     */
    public boolean removeMeasurementPointByPosition(LatLng position)
    {
        boolean succesfullRemoved = false;
        int i = 0;
        for(i = 0; i < measurementPoints.size(); i++)
        {
            if(measurementPoints.get(i).getPosition().equals(position))
            {
                measurementPoints.remove(i);
                succesfullRemoved = true;

            }
        }
        return succesfullRemoved;
    }

    /**
     * set method for color instance variable
     * @param color the color for the layer
     */
    public void setColor(int color)
    {
        //TODO: change Color possible ??
    }

    /**
     * get the color of the layer
     * @return color (int) value of resource defined in xml
     */
    public int getColor()
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

    /**
     * Method to get the number of lines in this layer
     * @return the number of lines in this layer (int)
     */
    public int getNumberOfLines()
    {
        return lines.size();
    }

    /**
     * Method to add a line to the layer
     * @param line lineObject
     */
    public void addLine(MapLine line)
    {
        lines.add(line);
    }

    /**
     * Method to remove a line from the layer
     * @param line the line object to remove
     * @return boolean success or fail
     */
    public boolean removeLine(MapLine line)
    {
        return lines.remove(line);
    }


    public Iterator<MapLine> getMapLineIterator()
    {
        return lines.iterator();
    }

    public MeasurementLayer(Parcel in)
    {
        measurementPoints = new ArrayList<MeasurementPoint>();
        layerName = new String();
        color = Color.RED;
        lineWidth = 3;
        lines = new ArrayList<MapLine>();
        readFromParcel(in);
    }

    public static final Parcelable.Creator<MeasurementLayer> CREATOR = new Parcelable.Creator<MeasurementLayer>()
    {
        /**
         * Create a new instance of the Parcelable class, instantiating it
         * from the given Parcel whose data had previously been written by
         * {@link android.os.Parcelable#writeToParcel Parcelable.writeToParcel()}.
         *
         * @param source The Parcel to read the object's data from.
         * @return Returns a new instance of the Parcelable class.
         */
        @Override
        public MeasurementLayer createFromParcel(Parcel source) {
            return new MeasurementLayer(source);
        }

        /**
         * Create a new array of the Parcelable class.
         * @param size Size of the array.
         * @return Returns an array of the Parcelable class, with every entry
         *         initialized to null.
         */
        @Override
        public MeasurementLayer[] newArray(int size) {
            return new MeasurementLayer[0];
        }
    };


    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     *         by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(layerName);
        dest.writeInt(color);
        dest.writeInt(lineWidth);
        dest.writeTypedList(measurementPoints);
        dest.writeTypedList(lines);
    }

    private void readFromParcel(Parcel in)
    {
        layerName = in.readString();
        color = in.readInt();
        lineWidth = in.readInt();
        in.readTypedList(measurementPoints, MeasurementPoint.CREATOR);
        in.readTypedList(lines, MapLine.CREATOR);
    }
}

