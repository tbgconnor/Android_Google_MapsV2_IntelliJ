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
    // Google maps API bug: Map Objects postion read back does not equal the set position.
    // see: https://code.google.com/p/gmaps-api-issues/issues/detail?id=5353
    private LatLng linePositioOnMap01;
    private LatLng linePositioOnMap02;


    public MapLine(LatLng pointOne, LatLng pointTwo, LatLng posOnMap01, LatLng posOnMap02)
    {
        this.pointOne = pointOne;
        this.pointTwo = pointTwo;
        linePositioOnMap01 = posOnMap01;
        linePositioOnMap02 = posOnMap02;
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

    public LatLng getLinePositioOnMap01() {
        return linePositioOnMap01;
    }

    public void setLinePositioOnMap01(LatLng linePositioOnMap01) {
        this.linePositioOnMap01 = linePositioOnMap01;
    }

    public LatLng getLinePositioOnMap02() {
        return linePositioOnMap02;
    }

    public void setLinePositioOnMap02(LatLng linePositioOnMap02) {
        this.linePositioOnMap02 = linePositioOnMap02;
    }

    public MapLine(Parcel in)
    {
        pointOne = new LatLng(50.879668, 5.309296);
        pointTwo = new LatLng(50.879668, 5.309296);
        linePositioOnMap01 = new LatLng(50.879668, 5.309296);
        linePositioOnMap02 = new LatLng(50.879668, 5.309296);

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
        dest.writeParcelable(linePositioOnMap01, 0);
        dest.writeParcelable(linePositioOnMap02, 0);
    }

    private void readFromParcel(Parcel in)
    {
        pointOne = in.readParcelable(LatLng.class.getClassLoader());
        pointTwo = in.readParcelable(LatLng.class.getClassLoader());
        linePositioOnMap01 = in.readParcelable(LatLng.class.getClassLoader());
        linePositioOnMap02 = in.readParcelable(LatLng.class.getClassLoader());
    }
}
