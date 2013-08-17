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
    private LatLng position;
    private String photoFilePath;
    // Google maps API bug: marker postion read back does not equal the set position.
    // see: https://code.google.com/p/gmaps-api-issues/issues/detail?id=5353
    private LatLng markerPositionOnMap;

    /**
     * Constructor for Measurement Point Object
     * @param position The position on the map
     */
    public MeasurementPoint(LatLng position)
    {
        this.position = position;
        comment = ""; //prevent null pointer errors on simple string object
        photoFilePath = "";
        markerPositionOnMap = position;
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

    /**
     * Getter for photo file path
     * @return String the photo file path
     */
    public String getPhotoFilePath() {
        return photoFilePath;
    }

    /**
     * Setter for photo file path
     * @param photoFilePath  String the photo file path
     */
    public void setPhotoFilePath(String photoFilePath) {
        this.photoFilePath = photoFilePath;
    }

    /**
     * Getter for photo filename
     * @return photo filename if available else an empty string is returned
     */
    public String getPhotoFileName()
    {
        //if filename contains a '/' then there it is assumed that there is a path + filename present
        int lastSlashIndex = photoFilePath.lastIndexOf('/');
        if(lastSlashIndex != -1)
        {
            //return the substring starting from last index
            return photoFilePath.substring(lastSlashIndex);
        }
        else
        {
            return "";
        }

    }


    /**
     * Getter for marker position on map (used for cross ref) (bypassing google play maps v2 api bug)
     * @return the (LatLng) position of the marker on the map
     */
    public LatLng getMarkerPositionOnMap() {
        return markerPositionOnMap;
    }

    /**
     * Setter for marker position on map (used for cross ref) (bypassing google play maps v2 api bug)
     * @param markerPositionOnMap the position of the marker on the map after instantiation of the marker representing this measurement point
     */
    public void setMarkerPositionOnMap(LatLng markerPositionOnMap) {
        this.markerPositionOnMap = markerPositionOnMap;
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
    public MeasurementPoint(Parcel in)
    {
        comment = "";
        position = new LatLng(50.879668, 5.309296);
        photoFilePath = "";
        markerPositionOnMap = new LatLng(50.879668, 5.309296);
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
        dest.writeString(photoFilePath);
        dest.writeParcelable(markerPositionOnMap, 0);
    }

    private void readFromParcel(Parcel in)
    {
        comment = in.readString();
        position = in.readParcelable(LatLng.class.getClassLoader());
        photoFilePath = in.readString();
        markerPositionOnMap = in.readParcelable(LatLng.class.getClassLoader());
    }
}
