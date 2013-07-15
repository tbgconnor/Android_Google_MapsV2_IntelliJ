package com.Square9.AndroidMapsV2Test;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Async Task to read from file
 * @author Koen Gilissen
 * @version 1.0
 */
public class ReadFromFile extends AsyncTask<File, Integer, LayerManager>
{
    private final static String DEBUGTAG = "ReadFromFile";
    private ReadFromFileEvent listener;
    private Context actCtx;

    // Parsing Tags
    private final String OPEN_LAYER_TAG = "<layer>";
    private final String LAYER_NAME = "Name:";
    private final String LAYER_COLOR = "Color:";
    private final String LAYER_LINEWIDTH = "Line width:";
    private final String OPEN_MEASUREMENTPOINT_TAG = "<Measurement Point>";
    private final String USER_COMMENT = "User Comment:";
    private final String LATITUDE = "Latitude:";
    private final String LONGITUDE = "Longitude:";
    private final String PHOTO = "Photo:";
    private final String CLOSE__MEASUREMENTPOINT_TAG = "</Measurement Point>";
    private final String OPEN_LINE_TAG = "<Line>";
    private final String LINE_LAT1 = "Latitude 1:";
    private final String LINE_LON1 = "Longitude 1:";
    private final String LINE_LAT2 = "Latitude 2:";
    private final String LINE_LON2 = "Longitude 2:";
    private final String CLOSE_LINE_TAG = "</Line>";

    public interface ReadFromFileEvent
    {
        void onReadFromFileCompleted(LayerManager layerManager);
    }

    public ReadFromFile(Context ctx, ReadFromFileEvent listener)
    {
        actCtx = ctx;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onPostExecute(LayerManager lm)
    {
        listener.onReadFromFileCompleted(lm);
    }

    @Override
    protected LayerManager doInBackground(File... params)
    {
        // Get FileHandler Object
        File file = params[0];
        Log.d(DEBUGTAG, "Reading file in async task : " + file.getName());
        // Create a new LayerManager
        LayerManager result = new LayerManager();
        // Check if the file reachable and readable
        if(!file.canRead())
        {
            Log.d(DEBUGTAG, "ERROR: Async Task can't Read file");
            return null;
        }
        // Read the file
        try
        {
            //Create an InputStream
            FileInputStream in = new FileInputStream(file);
            //Create Reader to read the InputStream
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            //Create a buffered Reader
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            //TODO: fix buggy parsing

            while ((line = bufferedReader.readLine()) != null)
            {
                Log.d(DEBUGTAG, "Reading Line: " + line);
                if(line.contains(OPEN_LAYER_TAG))
                {
                    Log.d(DEBUGTAG, "Open Layer Tag Found");
                    String layerName = "";
                    int layerColor = 0;
                    int layerLineWidth = 0;

                    String nextLine = bufferedReader.readLine();
                    Log.d(DEBUGTAG, "Reading Next line: " + nextLine);
                    if(nextLine.contains(LAYER_NAME))
                        layerName = nextLine.substring(nextLine.indexOf(':')+1).trim();
                    Log.d(DEBUGTAG, "LayerName: " + layerName);
                    nextLine = bufferedReader.readLine();
                    Log.d(DEBUGTAG, "Reading Next line: " + nextLine);
                    if(nextLine.contains(LAYER_COLOR))
                        layerColor = stringColorToIntColor(nextLine.substring(nextLine.indexOf(':')+1).trim());
                    Log.d(DEBUGTAG, "LayerColor: " + Integer.toString(layerColor));
                     nextLine = bufferedReader.readLine();
                    Log.d(DEBUGTAG, "Reading Next line: " + nextLine);
                    if(nextLine.contains(LAYER_LINEWIDTH))
                        layerLineWidth = stringLineWidthToIntLineWidth(nextLine);
                    Log.d(DEBUGTAG, "Line Width: " + layerLineWidth);
                    // Create a new Measurement layer and set is as current layer of the layer manager
                    MeasurementLayer ml = new MeasurementLayer(layerName, layerColor, layerLineWidth);
                    Log.d(DEBUGTAG, "Created new Layer");
                    result.addNewLayer(ml);
                    Log.d(DEBUGTAG, "Added Layer to LayerManager");
                }
                if(line.contains(OPEN_MEASUREMENTPOINT_TAG))
                {
                    String userComment = "";
                    double lat = 0.0;
                    double lon = 0.0;
                    String photoRef = "";
                    String nextLine = bufferedReader.readLine();
                    if(nextLine.contains(USER_COMMENT))
                        userComment = nextLine.substring(nextLine.indexOf(':') + 1).trim();
                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LATITUDE))
                    {
                        String sLat = nextLine.substring(nextLine.indexOf(":")+1);
                        lat = Double.parseDouble(sLat);
                    }
                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LONGITUDE))
                    {
                        String sLon = nextLine.substring(nextLine.indexOf(":")+1);
                        lon = Double.parseDouble(sLon);
                    }
                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(PHOTO))
                        photoRef = nextLine.substring(nextLine.indexOf(":")+1);
                    //Create a new Measurement Point
                    LatLng pos = new LatLng(lat, lon);
                    MeasurementPoint mp = new MeasurementPoint(pos);
                    mp.setComment(userComment);
                    mp.setPhotoFilePath(photoRef);
                    //Add the measurement point to current Layer of LayerManager
                    result.addMeasurementPointToLayer(mp);
                }
                if(line.contains(OPEN_LINE_TAG))
                {
                    double lat1 = 0.0;
                    double lon1 = 0.0;
                    double lat2= 0.0;
                    double lon2 = 0.0;
                    String nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LINE_LAT1))
                    {
                        String sLat = nextLine.substring(nextLine.indexOf(':')+1).trim();
                        lat1 = Double.parseDouble(sLat);
                    }
                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LINE_LON1))
                    {
                        String sLon = nextLine.substring(nextLine.indexOf(':')+1).trim();
                        lon1 = Double.parseDouble(sLon);
                    }
                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LINE_LAT2))
                    {
                        String sLat = nextLine.substring(nextLine.indexOf(':')+1).trim();
                        lat2 = Double.parseDouble(sLat);
                    }
                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LINE_LON2))
                    {
                        String sLon = nextLine.substring(nextLine.indexOf(':')+1).trim();
                        lon2 = Double.parseDouble(sLon);
                    }
                    // Create a new line instance
                    LatLng point1 = new LatLng(lat1, lon1);
                    LatLng point2 = new LatLng(lat2, lon2);
                    // Constructor of MapLine requires the Position on map,
                    // A this point this can not be passed, so i'm passing the measurement data points
                    // They should be set when map is populated for each line
                    MapLine lineElement = new MapLine(point1, point2, point1, point2);
                    // add Map line to current layer in layermanager
                    result.getCurrentLayer().addLine(lineElement);
                }



            }
            bufferedReader.close();
            inputStreamReader.close();
            in.close();
        }
        catch(Exception e)
        {
            result = null;
        }
        return result;
    }


    /**
     * Method to convert #AARRGGBB to 3452233
     * @param color string representation of color
     * @return integer representation of color
     */
    public int stringColorToIntColor(String color)
    {
        Log.d(DEBUGTAG, "Converting Color: " + color);
        // Strip '#'
        String strippedColor = color.substring(color.indexOf('#')+1).trim();
        Log.d(DEBUGTAG, "Stripped Color: " + strippedColor);
        // Parse Int with radix 16
        int colorValue = (int) Long.parseLong(strippedColor, 16);
        Log.d(DEBUGTAG, "Color Value: " +  Integer.toString(colorValue));
        return colorValue;
    }

    /**
     * Method to convert "Line width: 3px" (string) to 3 (int)
     * @param lw lineWidth line (String) e.g.: "Line width: 3px"
     * @return int value for lineWidth e.g.: 3
     */
    public int stringLineWidthToIntLineWidth(String lw)
    {
        //start "Line width: 3px"
        //pixel => "3px"
        String pixels = lw.substring(lw.indexOf(':')+1).trim();
        String[] lwSplit = pixels.split("p");
        String lwValue = lwSplit[0];
        return Integer.parseInt(lwValue, 10);
    }

}