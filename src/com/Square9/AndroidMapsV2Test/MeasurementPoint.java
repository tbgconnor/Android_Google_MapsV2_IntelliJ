package com.Square9.AndroidMapsV2Test;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;

/**
 * Object that represents a single measurement point
 *
 * @author K. Gilissen
 * @version 1.0
 */
public class MeasurementPoint implements Parcelable
{
    private final static String DEBUGTAG = "MeasurementPoint";

    private String comment;
    private LatLng position;  // LatLng object extends from parcelable, GREAT!!

    /**
     * Constructor for Measurement Point Object
     * @param position The position on the map
     */
    public MeasurementPoint(LatLng position)
    {
        this.position = position;
        comment = ""; //prevent null pointer errors on simple string object
    }

    /**
     * getter for comment
     * @return comment of the measurement point
     */
    public String getComment() {
        return comment;
    }

    /**
     * setter for comment
     * @param comment (String) comment for this measurement point
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }

    /**
     * Getter for the position of the measurement point
     * @return position of the measurement point
     */
    public LatLng getPosition()
    {
        return position;
    }

    /**
     * Setter for the position of the measurement point
     * @param position the new position of the measurement point
     */
    public void setPosition(LatLng position)
    {
        this.position = position;
    }

    public static final Parcelable.Creator<MeasurementPoint> CREATOR
            = new Parcelable.Creator<MeasurementPoint>() {
        public MeasurementPoint createFromParcel(Parcel in) {
            return new MeasurementPoint(in);
        }

        public MeasurementPoint[] newArray(int size) {
            return new MeasurementPoint[size];
        }
    };


    /**
     * Constructs a new instance of {@code Object}.
     */
    public MeasurementPoint(Parcel in) {
        readFromParcel(in);
    }

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
        dest.writeString(comment);
        dest.writeParcelable(position, 0);
    }

    private void readFromParcel(Parcel in)
    {
        comment = in.readString();
        position = in.readParcelable(LatLng.class.getClassLoader());
    }
}
