package com.Square9.AndroidMapsV2Test;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.format.Time;
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
        final String OPEN_LAYER_TAG = "<layer>";
        final String LAYER_NAME = "Name:";
        final String LAYER_COLOR = "Color:";
        final String LAYER_LINEWIDTH = "Line width:";
        final String OPEN_MEASUREMENTPOINT_TAG = "<Measurement Point>";
        final String USER_COMMENT = "User Comment:";
        final String LATITUDE = "Latitude:";
        final String LONGITUDE = "Longitude:";
        final String PHOTO = "Photo:";
        final String CLOSE_MEASUREMENTPOINT_TAG = "</Measurement Point>";
        final String OPEN_LINE_TAG = "<Line>";
        final String LINE_LAT1 = "Latitude 1:";
        final String LINE_LON1 = "Longitude 1:";
        final String LINE_LAT2 = "Latitude 2:";
        final String LINE_LON2 = "Longitude 2:";
        final String CLOSE_LINE_TAG = "</Line>";
        final String OPEN_ARC_TAG = "<Arc>";
        final String ARC_LAT1 = "Latitude 1:";
        final String ARC_LON1 = "Longitude 1:";
        final String ARC_LAT2 = "Latitude 2:";
        final String ARC_LON2 = "Longitude 2:";
        final String ARC_LAT3 = "Latitude 3:";
        final String ARC_LON3 = "Longitude 3:";
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
            result.append(LAYER_NAME + layer.getLayerName() + "\n");
            int color = layer.getColor();
            String hexColor ="#" + Integer.toHexString(color);
            result.append(LAYER_COLOR + hexColor + "\n");
            result.append(LAYER_LINEWIDTH + layer.getLineWidth() + "px\n");
            // [3] for each measurement point in this layer:
            for(int pointIndex = 0; pointIndex < layer.getNumberOfMeasurementPoints(); pointIndex++)
            {
                result.append("\t" + OPEN_MEASUREMENTPOINT_TAG + "\n");
                result.append("\t" + USER_COMMENT + layer.getMeasurementPointByIndex(pointIndex).getComment() + "\n");
                result.append("\t" + LATITUDE + layer.getMeasurementPointByIndex(pointIndex).getPosition().latitude + "\n");
                result.append("\t" + LONGITUDE + layer.getMeasurementPointByIndex(pointIndex).getPosition().longitude + "\n");
                result.append("\t" + PHOTO +  layer.getMeasurementPointByIndex(pointIndex).getPhotoFilePath() + "\n");
                result.append("\t" + CLOSE_MEASUREMENTPOINT_TAG +"\n");
            }
            // [4] for each line in this layer
            Iterator<MeasurementLine> lineIterator = layer.getMeasurementLineIterator();
            while(lineIterator.hasNext())
            {
                MeasurementLine line = lineIterator.next();
                result.append("\t" + OPEN_LINE_TAG + "\n");
                result.append("\t" + LINE_LAT1 + line.getPointOne().latitude  + "\n");
                result.append("\t" + LINE_LON1 + line.getPointOne().longitude  + "\n");
                result.append("\t" + LINE_LAT2 + line.getPointTwo().latitude  + "\n");
                result.append("\t" + LINE_LON2 + line.getPointTwo().longitude  + "\n");
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
                result.append("\t" + OPEN_ARC_TAG + "\n");
                result.append("\t" + ARC_LAT1 + mPos1.latitude +"\n");
                result.append("\t" + ARC_LON1 + mPos1.longitude +"\n");
                result.append("\t" + ARC_LAT2 + mPos2.latitude +"\n");
                result.append("\t" + ARC_LON2 + mPos2.longitude +"\n");
                result.append("\t" + ARC_LAT3 + mPos3.latitude +"\n");
                result.append("\t" + ARC_LON3 + mPos3.longitude +"\n");
                result.append("\t" + CLOSE_ARC_TAG + "\n");

            }
            result.append("</layer>\n");
        }
        return result;
    }


}
