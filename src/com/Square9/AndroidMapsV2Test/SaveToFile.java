package com.Square9.AndroidMapsV2Test;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.format.Time;
import com.google.android.gms.internal.bj;
import com.google.android.gms.maps.model.LatLng;

import java.util.Iterator;

/**
 * Async Task to save
 */
public class SaveToFile extends AsyncTask<LayerManager, Integer, Integer>
{
    private final static String DEBUGTAG = "AsyncTask_SaveToFile";
    private Context ctx;
    private ProgressDialog pd = null;
    private SaveToFileEvent saveToDiskEventListener;
    private FileHandler fileHandler;

     /**
     *  Callback Interface for sending callbacks to the host of the async task (e.g.: the activity)
     */
    public interface SaveToFileEvent
    {
        // Callback to be used in onPostExecute() to transfer the Boolean 'result' to the activity
        void onSaveToFileCompleted(Integer result);
    }

    public SaveToFile(Context c, FileHandler fh, SaveToFileEvent listener)
    {
        ctx = c;
        saveToDiskEventListener = listener;
        fileHandler = fh;
    }

    /**
     * background thread for saving file
     * Return codes:
     * -1: media not available
     * -2: unable to create file
     * -3: Error writing file
     *  0: successful written file
     * @param params
     * @return
     */
    @Override
    protected Integer doInBackground(LayerManager... params)
    {
        //Check if Storage is available
        if(!FileHandler.checkMediaAvailability())
            return -1;
        //Create file to write to:
        if(!fileHandler.createFile())
            return -2;
        // String parsedLayerManager
        String data = parseLayerManager(params[0]).toString();
        //write data to file
        if(!fileHandler.write(data))
                return -3;
        return 0; //successful write
    }

    @Override
    protected void onPreExecute()
    {

    }

    @Override
    protected void onPostExecute(Integer i)
    {
        saveToDiskEventListener.onSaveToFileCompleted(i);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Method for parsing model (layerManager) to world readable string
     */
    private StringBuilder parseLayerManager(LayerManager lm)
    {
        // Parsing Tags
        final String OPEN_LAYER_TAG = "<Layer>";
        final String CLOSE_LAYER_TAG = "</Layer>";
        final String OPEN_LAYER_NAME_TAG = "<Name>";
        final String CLOSE_LAYER_NAME_TAG = "</Name>";
        final String OPEN_LAYER_COLOR_TAG = "<Color>";
        final String CLOSE_LAYER_COLOR_TAG = "</Color>";
        final String OPEN_LAYER_LINEWIDTH_TAG = "<LineWidth>";
        final String CLOSE_LAYER_LINEWIDTH_TAG = "</LineWidth>";
        final String OPEN_MEASUREMENTPOINT_TAG = "<MeasurementPoint>";
        final String CLOSE_MEASUREMENTPOINT_TAG = "</MeasurementPoint>";
        final String OPEN_USER_COMMENT_TAG = "<UserComment>";
        final String CLOSE_USER_COMMENT_TAG = "</UserComment>";
        final String OPEN_MP_LATITUDE_TAG = "<Latitude>";
        final String CLOSE_MP_LATITUDE_TAG = "</Latitude>";
        final String OPEN_MP_LONGITUDE_TAG = "<Longitude>";
        final String CLOSE_MP_LONGITUDE_TAG = "</Longitude>";
        final String OPEN_MP_X_TAG = "<X>";
        final String CLOSE_MP_X_TAG = "</X>";
        final String OPEN_MP_Y_TAG = "<Y>";
        final String CLOSE_MP_Y_TAG = "</Y>";
        final String OPEN_PHOTO_TAG = "<Photo>";
        final String CLOSE_PHOTO_TAG = "</Photo>";
        final String OPEN_LINE_TAG = "<Line>";
        final String CLOSE_LINE_TAG = "</Line>";
        final String OPEN_LINE_LAT1_TAG = "<Latitude1>";
        final String CLOSE_LINE_LAT1_TAG = "</Latitude1>";
        final String OPEN_LINE_LON1_TAG = "<Longitude1>";
        final String CLOSE_LINE_LON1_TAG = "</Longitude1>";
        final String OPEN_LINE_LAT2_TAG = "<Latitude2>";
        final String CLOSE_LINE_LAT2_TAG = "</Latitude2>";
        final String OPEN_LINE_LON2_TAG = "<Longitude2>";
        final String CLOSE_LINE_LON2_TAG = "</Longitude2>";
        final String OPEN_LINE_X1_TAG = "<X1>";
        final String CLOSE_LINE_X1_TAG = "</X1>";
        final String OPEN_LINE_Y1_TAG = "<Y1>";
        final String CLOSE_LINE_Y1_TAG = "</Y1>";
        final String OPEN_LINE_X2_TAG = "<X2>";
        final String CLOSE_LINE_X2_TAG = "</X2>";
        final String OPEN_LINE_Y2_TAG = "<Y2>";
        final String CLOSE_LINE_Y2_TAG = "</Y2>";
        final String OPEN_ARC_TAG = "<Arc>";
        final String OPEN_ARC_LAT1_TAG = "<Latitude1>";
        final String OPEN_ARC_LON1_TAG = "<Longitude1>";
        final String OPEN_ARC_LAT2_TAG = "<Latitude2>";
        final String OPEN_ARC_LON2_TAG = "<Longitude2>";
        final String OPEN_ARC_LAT3_TAG = "<Latitude3>";
        final String OPEN_ARC_LON3_TAG = "<Longitude3>";
        final String OPEN_ARC_X1_TAG = "<X1>";
        final String OPEN_ARC_Y1_TAG = "<Y1>";
        final String OPEN_ARC_X2_TAG = "<X2>";
        final String OPEN_ARC_Y2_TAG = "<Y2>";
        final String OPEN_ARC_X3_TAG = "<X3>";
        final String OPEN_ARC_Y3_TAG = "<Y3>";
        final String CLOSE_ARC_LAT1_TAG = "</Latitude1>";
        final String CLOSE_ARC_LON1_TAG = "</Longitude1>";
        final String CLOSE_ARC_LAT2_TAG = "</Latitude2>";
        final String CLOSE_ARC_LON2_TAG = "</Longitude2>";
        final String CLOSE_ARC_LAT3_TAG = "</Latitude3>";
        final String CLOSE_ARC_LON3_TAG = "</Longitude3>";
        final String CLOSE_ARC_X1_TAG = "</X1>";
        final String CLOSE_ARC_Y1_TAG = "</Y1>";
        final String CLOSE_ARC_X2_TAG = "</X2>";
        final String CLOSE_ARC_Y2_TAG = "</Y2>";
        final String CLOSE_ARC_X3_TAG = "</X3>";
        final String CLOSE_ARC_Y3_TAG = "</Y3>";
        final String CLOSE_ARC_TAG = "</ARC>";

        StringBuilder result = new StringBuilder();
        // [1] Create a time stamp
        String format = "%e/%m/%Y - %H:%M:%S";
        Time timeStamp = new Time();
        timeStamp.setToNow();
        String time = timeStamp.format(format) + "\n";
        result.append(time);
        // [2] for each layer in LayerManager
        Iterator<MeasurementLayer> layerIterator = lm.getMeasurementLayerIterator();
        while(layerIterator.hasNext())
        {
            MeasurementLayer layer = layerIterator.next();
            result.append(OPEN_LAYER_TAG + "\n");
            result.append("\t" + OPEN_LAYER_NAME_TAG + layer.getLayerName() + CLOSE_LAYER_NAME_TAG +"\n");
            int color = layer.getColor();
            String hexColor ="#" + Integer.toHexString(color);
            result.append("\t" + OPEN_LAYER_COLOR_TAG + hexColor + CLOSE_LAYER_COLOR_TAG + "\n");
            result.append("\t" + OPEN_LAYER_LINEWIDTH_TAG + layer.getLineWidth() + "px" + CLOSE_LAYER_LINEWIDTH_TAG + "\n");
            // [3] for each measurement point in this layer:
            for(int pointIndex = 0; pointIndex < layer.getNumberOfMeasurementPoints(); pointIndex++)
            {
                double mpXY[] = conversionLatLngToXYNed(layer.getMeasurementPointByIndex(pointIndex).getPosition());
                result.append("\t" + OPEN_MEASUREMENTPOINT_TAG + "\n");
                result.append("\t\t" + OPEN_USER_COMMENT_TAG + layer.getMeasurementPointByIndex(pointIndex).getComment() + CLOSE_USER_COMMENT_TAG + "\n");
                result.append("\t\t" + OPEN_MP_LATITUDE_TAG + layer.getMeasurementPointByIndex(pointIndex).getPosition().latitude + CLOSE_MP_LATITUDE_TAG + "\n");
                result.append("\t\t" + OPEN_MP_LONGITUDE_TAG + layer.getMeasurementPointByIndex(pointIndex).getPosition().longitude + CLOSE_MP_LONGITUDE_TAG + "\n");
                result.append("\t\t" + OPEN_PHOTO_TAG +  layer.getMeasurementPointByIndex(pointIndex).getPhotoFilePath() + CLOSE_PHOTO_TAG + "\n");
                result.append("\t\t" + OPEN_MP_X_TAG + Double.toString(mpXY[0]) + CLOSE_MP_X_TAG + "\n");
                result.append("\t\t" + OPEN_MP_Y_TAG + Double.toString(mpXY[1]) + CLOSE_MP_Y_TAG + "\n");
                result.append("\t" + CLOSE_MEASUREMENTPOINT_TAG +"\n");
            }
            // [4] for each line in this layer
            Iterator<MeasurementLine> lineIterator = layer.getMeasurementLineIterator();
            while(lineIterator.hasNext())
            {
                MeasurementLine line = lineIterator.next();
                double lineP1[] = conversionLatLngToXYNed(line.getPointOne());
                double lineP2[] = conversionLatLngToXYNed(line.getPointTwo());
                result.append("\t" + OPEN_LINE_TAG + "\n");
                result.append("\t\t" + OPEN_LINE_LAT1_TAG + line.getPointOne().latitude  + CLOSE_LINE_LAT1_TAG + "\n");
                result.append("\t\t" + OPEN_LINE_LON1_TAG + line.getPointOne().longitude  + CLOSE_LINE_LON1_TAG +"\n");
                result.append("\t\t" + OPEN_LINE_LAT2_TAG + line.getPointTwo().latitude + CLOSE_LINE_LAT2_TAG + "\n");
                result.append("\t\t" + OPEN_LINE_LON2_TAG + line.getPointTwo().longitude  + CLOSE_LINE_LON2_TAG + "\n");
                result.append("\t\t" + OPEN_LINE_X1_TAG + lineP1[0] + CLOSE_LINE_X1_TAG + "\n");
                result.append("\t\t" + OPEN_LINE_Y1_TAG + lineP1[1] + CLOSE_LINE_Y1_TAG + "\n");
                result.append("\t\t" + OPEN_LINE_X2_TAG + lineP2[0] + CLOSE_LINE_X2_TAG + "\n");
                result.append("\t\t" + OPEN_LINE_Y2_TAG + lineP2[1] + CLOSE_LINE_Y2_TAG + "\n");
                result.append("\t" + CLOSE_LINE_TAG + "\n");
            }
            // [5] for each arc in this layer
            Iterator<MeasurementArc> arcIterator = layer.getMeasurementArcIterator();
            while(arcIterator.hasNext())
            {
                MeasurementArc arc = arcIterator.next();
                LatLng mPos1 = arc.getMeasurementPositions().get(0);
                LatLng mPos2 = arc.getMeasurementPositions().get(1);
                LatLng mPos3 = arc.getMeasurementPositions().get(2);
                double arcP1[] = conversionLatLngToXYNed(mPos1);
                double arcP2[] = conversionLatLngToXYNed(mPos2);
                double arcP3[] = conversionLatLngToXYNed(mPos3);
                result.append("\t" + OPEN_ARC_TAG + "\n");
                result.append("\t\t" + OPEN_ARC_LAT1_TAG + mPos1.latitude + CLOSE_ARC_LAT1_TAG + "\n");
                result.append("\t\t" + OPEN_ARC_LON1_TAG + mPos1.longitude + CLOSE_ARC_LON1_TAG + "\n");
                result.append("\t\t" + OPEN_ARC_LAT2_TAG + mPos2.latitude + CLOSE_ARC_LAT2_TAG + "\n");
                result.append("\t\t" + OPEN_ARC_LON2_TAG + mPos2.longitude + CLOSE_ARC_LON2_TAG + "\n");
                result.append("\t\t" + OPEN_ARC_LAT3_TAG + mPos3.latitude + CLOSE_ARC_LAT3_TAG + "\n");
                result.append("\t\t" + OPEN_ARC_LON3_TAG + mPos3.longitude + CLOSE_ARC_LON3_TAG + "\n");
                result.append("\t\t" + OPEN_ARC_X1_TAG + arcP1[0] + CLOSE_ARC_X1_TAG + "\n");
                result.append("\t\t" + OPEN_ARC_Y1_TAG + arcP1[1] + CLOSE_ARC_Y1_TAG + "\n");
                result.append("\t\t" + OPEN_ARC_X2_TAG + arcP2[0] + CLOSE_ARC_X2_TAG + "\n");
                result.append("\t\t" + OPEN_ARC_Y2_TAG + arcP2[1] + CLOSE_ARC_Y2_TAG + "\n");
                result.append("\t\t" + OPEN_ARC_X3_TAG + arcP3[0] + CLOSE_ARC_X3_TAG + "\n");
                result.append("\t\t" + OPEN_ARC_Y3_TAG + arcP3[1] + CLOSE_ARC_Y3_TAG + "\n");
                result.append("\t" + CLOSE_ARC_TAG + "\n");

            }
            result.append(CLOSE_LAYER_TAG+"\n");
        }
        return result;
    }


    private double[] conversionLatLngToXYNed(LatLng posToConvert)
    {
        double[] result = {0.0 , 0.0};
        double somX = 0.0;
        double somY = 0.0;
        double dF = 0.0;
        double dL = 0.0;
        double x = 0.0;
        double y = 0.0;
        final double latitudeOffset = 52.15517440;
        final double longitudeOffset = 5.38720621;

        dF = 0.36*(posToConvert.latitude - latitudeOffset);
        dL = 0.36*(posToConvert.longitude - longitudeOffset);

        somX = (190094.945 * dL) + (-11832.228 * dF * dL) + (-144.221 * Math.pow(dF, 2) * dL) + (-32.391 * Math.pow(dL, 3))
                + (-0.705 * dF) + (-2.340 * Math.pow(dF, 3) * dL) + (-0.608 * dF * Math.pow(dL, 3) ) + (-0.008 * Math.pow(dL, 2) ) + (0.148 * Math.pow(dF, 2) * Math.pow(dL, 3));

        somY = (309056.544 * dF) + (3638.893 * Math.pow(dL, 2)) + (73.077 * Math.pow(dF, 2)) + (-157.984 * dF * Math.pow(dL, 2)) + (59.788 * Math.pow(dF, 3) ) + (0.433 * dL)
                + (-6.439 * Math.pow(dF, 2) * Math.pow(dL, 2)) + (-0.032 * dF * dL) + (0.092 * Math.pow(dL, 4)) + (-0.054 * dF * Math.pow(dL,4));

        x = 155000 + somX;
        y = 463000 + somY;

        result[0] = x;
        result[1] = y;

        return result;
    }

}
