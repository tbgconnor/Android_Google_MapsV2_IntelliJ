package com.Square9.AndroidMapsV2Test;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;

/**
 * Class represents a model for a line added to the map by the user
 */
public class MeasurementLine implements Parcelable
{
    private LatLng pointOne;
    private LatLng pointTwo;
    private double heightOne;
    private double heightTwo;
    // Google maps API bug: Map Objects postion read back does not equal the set position.
    // see: https://code.google.com/p/gmaps-api-issues/issues/detail?id=5353
    private LatLng linePositionOnMap01;
    private LatLng linePositionOnMap02;


    public MeasurementLine(LatLng pointOne, LatLng pointTwo, LatLng posOnMap01, LatLng posOnMap02)
    {
        this.pointOne = pointOne;
        this.pointTwo = pointTwo;
        heightOne = 0.0;
        heightTwo = 0.0;
        linePositionOnMap01 = posOnMap01;
        linePositionOnMap02 = posOnMap02;
    }

    public LatLng getPointOne() {
        return pointOne;
    }

    public void setPointOne(LatLng pointOne) {
        this.pointOne = pointOne;
    }

    public LatLng getPointTwo() {
        return pointTwo;
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

    public void setPointTwo(LatLng pointTwo) {
        this.pointTwo = pointTwo;
    }

    public LatLng getLinePositionOnMap01() {
        return linePositionOnMap01;
    }

    public void setLinePositionOnMap01(LatLng linePositionOnMap01) {
        this.linePositionOnMap01 = linePositionOnMap01;
    }

    public LatLng getLinePositionOnMap02() {
        return linePositionOnMap02;
    }

    public void setLinePositionOnMap02(LatLng linePositionOnMap02) {
        this.linePositionOnMap02 = linePositionOnMap02;
    }

    public MeasurementLine(Parcel in)
    {
        pointOne = new LatLng(50.879668, 5.309296);
        pointTwo = new LatLng(50.879668, 5.309296);
        heightOne = 0.0;
        heightTwo = 0.0;
        linePositionOnMap01 = new LatLng(50.879668, 5.309296);
        linePositionOnMap02 = new LatLng(50.879668, 5.309296);

        readFromParcel(in);
    }

    public static final Parcelable.Creator<MeasurementLine> CREATOR = new Parcelable.Creator<MeasurementLine>()
    {
        public MeasurementLine createFromParcel(Parcel in)
        {
            return new MeasurementLine(in);
        }

        public MeasurementLine[] newArray(int size)
        {
            return new MeasurementLine[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(pointOne, 0);
        dest.writeParcelable(pointTwo, 0);
        dest.writeDouble(heightOne);
        dest.writeDouble(heightTwo);
        dest.writeParcelable(linePositionOnMap01, 0);
        dest.writeParcelable(linePositionOnMap02, 0);
    }

    private void readFromParcel(Parcel in)
    {
        pointOne = in.readParcelable(LatLng.class.getClassLoader());
        pointTwo = in.readParcelable(LatLng.class.getClassLoader());
        heightOne = in.readDouble();
        heightTwo = in.readDouble();
        linePositionOnMap01 = in.readParcelable(LatLng.class.getClassLoader());
        linePositionOnMap02 = in.readParcelable(LatLng.class.getClassLoader());
    }
}
