package com.Square9.AndroidMapsV2Test;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.format.Time;

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
            result.append("<layer>\n");
            result.append("Name: " + layer.getLayerName() + "\n");
            int color = layer.getColor();
            String hexColor ="#" + Integer.toHexString(color);
            result.append("Color: " + hexColor + "\n");
            result.append("Line width: " + layer.getLineWidth() + "px\n");
            // [3] for each measurement point in this layer:
            for(int pointIndex = 0; pointIndex < layer.getNumberOfMeasurementPoints(); pointIndex++)
            {
                result.append("\t<Measurement Point>\n");
                result.append("\tUser Comment: " + layer.getMeasurementPointByIndex(pointIndex).getComment() + "\n");
                result.append("\tLatitude: " + layer.getMeasurementPointByIndex(pointIndex).getPosition().latitude + "\n");
                result.append("\tLongitude: " + layer.getMeasurementPointByIndex(pointIndex).getPosition().longitude + "\n");
                //TODO more data ... pictures ect...
                result.append("\t</Measurement Point>\n");
            }
            // [4] for each line in this layer
            Iterator<MapLine> lineIterator = layer.getMapLineIterator();
            while(lineIterator.hasNext())
            {
                MapLine line = lineIterator.next();
                result.append("\t<Line>\n");
                result.append("\tLatitude 1: " + line.getPointOne().latitude  + "\n");
                result.append("\tLongitude 1: " + line.getPointOne().longitude  + "\n");
                result.append("\tlatitude 2: " + line.getPointTwo().latitude  + "\n");
                result.append("\tlatitude 2: " + line.getPointTwo().longitude  + "\n");
                result.append("\t</Line>\n");
            }
            // [5] for each arc in this layer
            //TODO ....
            result.append("</layer>\n");
        }
        return result;
    }


}
