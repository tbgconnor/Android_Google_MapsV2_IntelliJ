package com.Square9.AndroidMapsV2Test;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;

/**
 * Class represents a model for a line added to the map by the user
 */
public class MapLine implements Parcelable
{
    private LatLng pointOne;
    private LatLng pointTwo;


    public MapLine(LatLng pointOne, LatLng pointTwo) {
        this.pointOne = pointOne;
        this.pointTwo = pointTwo;
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

    public void setPointTwo(LatLng pointTwo) {
        this.pointTwo = pointTwo;
    }

    public MapLine(Parcel in)
    {
        pointOne = new LatLng(50.879668, 5.309296);
        pointTwo = new LatLng(50.879668, 5.309296);
        readFromParcel(in);
    }

    public static final Parcelable.Creator<MapLine> CREATOR
            = new Parcelable.Creator<MapLine>() {
        public MapLine createFromParcel(Parcel in) {
            return new MapLine(in);
        }

        public MapLine[] newArray(int size) {
            return new MapLine[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(pointOne, 0);
        dest.writeParcelable(pointTwo, 0);
    }

    private void readFromParcel(Parcel in)
    {
        pointOne = in.readParcelable(LatLng.class.getClassLoader());
        pointTwo = in.readParcelable(LatLng.class.getClassLoader());
    }
}
