package com.Square9.AndroidMapsV2Test;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Class representing an Arc measurement
 * @author K. Gilissen
 * @version 1.0
 */
public class MeasurementArc implements Parcelable
{
    private LatLng position01;
    private LatLng position02;
    private LatLng position03;
    private double heightOne;
    private double heightTwo;
    private double heightThree;
    // Google maps API bug: Map Objects postion read back does not equal the set position.
    // see: https://code.google.com/p/gmaps-api-issues/issues/detail?id=5353
    private LatLng position01OnMap;
    private LatLng position02OnMap;
    private LatLng position03OnMap;

    /**
     * Constructor for Measurement Arc
     * @param pos1 measured position 1
     * @param pos2 measured position 2
     * @param pos3 measured position 3
     * @param pos1Map Position on Map 1
     * @param pos2Map Position on Map 2
     * @param pos3Map Position on Map 3
     */
    public MeasurementArc(LatLng pos1, LatLng pos2, LatLng pos3, LatLng pos1Map, LatLng pos2Map, LatLng pos3Map)
    {
        position01 = pos1;
        position02 = pos2;
        position03 = pos3;

        heightOne = 0.0;
        heightTwo = 0.0;
        heightThree = 0.0;

        position01OnMap = pos1Map;
        position02OnMap = pos2Map;
        position03OnMap = pos3Map;
    }

    /**
     * Method to get the Measured Positions of the arc
     * @return [Arraylist] Measured positions
     */
    public ArrayList<LatLng> getMeasurementPositions()
    {
        ArrayList<LatLng> result = new ArrayList<LatLng>(3);
        result.add(position01);
        result.add(position02);
        result.add(position03);
        return result;
    }

    /**
     * setter for measured positions of the arc
     * @param pos1 [LatLng] Measured Position01
     * @param pos2 [LatLng] Measured Position02
     * @param pos3 [LatLng] Measured Position03
     */
    public void setMeasurementPositions(LatLng pos1, LatLng pos2, LatLng pos3)
    {
        position01 = pos1;
        position02 = pos2;
        position03 = pos3;
    }

    public double getHeightOne() {
        return heightOne;
    }

    public void setHeightOne(double heightOne) {
        this.heightOne = heightOne;
    }

    public double getHeightTwo() {
        return heightTwo;
    }

    public void setHeightTwo(double heightTwo) {
        this.heightTwo = heightTwo;
    }

    public double getHeightThree() {
        return heightThree;
    }

    public void setHeightThree(double heightThree) {
        this.heightThree = heightThree;
    }

    /**
     * Method to get the Positions on Map of the arc
     * @return [Arraylist] positions
     */
    public ArrayList<LatLng> getPositionsOnMap()
    {
        ArrayList<LatLng> result = new ArrayList<LatLng>(3);
        result.add(position01OnMap);
        result.add(position02OnMap);
        result.add(position03OnMap);
        return result;
    }

    /**
     * setter for the positions on Map of the arc
     * @param pos1 [LatLng] Position01 on Map
     * @param pos2 [LatLng] Position02 on Map
     * @param pos3 [LatLng] Position02 on Map
     */
    public void setPositionsOnMap(LatLng pos1, LatLng pos2, LatLng pos3)
    {
        position01OnMap = pos1;
        position02OnMap = pos2;
        position03OnMap = pos3;
    }

    public MeasurementArc(Parcel in)
    {
        position01 = new LatLng(50.879668, 5.309296);
        position02 = new LatLng(50.879668, 5.309296);
        position03 = new LatLng(50.879668, 5.309296);
        heightOne = 0.0;
        heightTwo = 0.0;
        heightThree = 0.0;
        position01OnMap = new LatLng(50.879668, 5.309296);
        position02OnMap = new LatLng(50.879668, 5.309296);
        position03OnMap = new LatLng(50.879668, 5.309296);

        readFromParcel(in);
    }

    public static final Parcelable.Creator<MeasurementArc> CREATOR = new Creator<MeasurementArc>()
    {
        @Override
        public MeasurementArc createFromParcel(Parcel source)
        {
            return new MeasurementArc(source);
        }

        @Override
        public MeasurementArc[] newArray(int size)
        {
            return new MeasurementArc[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(position01, 0);
        dest.writeParcelable(position02, 0);
        dest.writeParcelable(position03, 0);
        dest.writeDouble(heightOne);
        dest.writeDouble(heightTwo);
        dest.writeDouble(heightThree);
        dest.writeParcelable(position01OnMap, 0);
        dest.writeParcelable(position02OnMap, 0);
        dest.writeParcelable(position03OnMap, 0);
    }

    private void readFromParcel(Parcel in)
    {
        position01 = in.readParcelable(LatLng.class.getClassLoader());
        position02 = in.readParcelable(LatLng.class.getClassLoader());
        position03 = in.readParcelable(LatLng.class.getClassLoader());
        heightOne = in.readDouble();
        heightTwo = in.readDouble();
        heightThree = in.readDouble();
        position01OnMap = in.readParcelable(LatLng.class.getClassLoader());
        position02OnMap = in.readParcelable(LatLng.class.getClassLoader());
        position03OnMap = in.readParcelable(LatLng.class.getClassLoader());
    }
}
