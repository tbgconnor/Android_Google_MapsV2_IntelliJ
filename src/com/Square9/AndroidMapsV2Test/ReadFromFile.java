package com.Square9.AndroidMapsV2Test;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
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
    private final String OPEN_ARC_TAG = "<Arc>";
    private final String ARC_LAT1 = "Latitude 1:";
    private final String ARC_LON1 = "Longitude 1:";
    private final String ARC_LAT2 = "Latitude 2:";
    private final String ARC_LON2 = "Longitude 2:";
    private final String ARC_LAT3 = "Latitude 3:";
    private final String ARC_LON3 = "Longitude 3:";
    private final String CLOSE_ARC_TAG = "</ARC>";



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
            while ((line = bufferedReader.readLine()) != null)
            {
                Log.d(DEBUGTAG, "Reading Line: " + line);
                if(line.contains(OPEN_LAYER_TAG))
                {
                    String layerName = "";
                    int layerColor = 0;
                    int layerLineWidth = 0;

                    String nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LAYER_NAME))
                    {
                        layerName = getSubStringBehindToken(nextLine, ':');
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LAYER_COLOR))
                    {
                        int color = stringLineColorToIntColor(nextLine);
                        if(color == -1)
                        {
                            // Parsing error if return -1
                            color = 16777215; // 0xFFFFFF (white)
                        }
                        layerColor = color;
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LAYER_LINEWIDTH))
                    {
                        layerLineWidth = stringLineWidthToIntLineWidth(nextLine);
                    }
                    // Create a new Measurement layer and set is as current layer of the layer manager
                    Log.d(DEBUGTAG, "LayerName: " + layerName);
                    Log.d(DEBUGTAG, "LayerColor: " + Integer.toString(layerColor));
                    Log.d(DEBUGTAG, "Line Width: " + layerLineWidth);
                    MeasurementLayer ml = new MeasurementLayer(layerName, layerColor, layerLineWidth);
                    result.addNewLayer(ml);
                }
                if(line.contains(OPEN_MEASUREMENTPOINT_TAG))
                {
                    String userComment = "";
                    double lat = 0.0;
                    double lon = 0.0;
                    String photoRef = "";

                    String nextLine = bufferedReader.readLine();
                    if(nextLine.contains(USER_COMMENT))   //	User Comment: test
                        userComment = getSubStringBehindToken(nextLine, ':');

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LATITUDE))      // 	Latitude: 50.87406459824764
                    {
                        String sLat = getSubStringBehindToken(nextLine, ':');
                        lat = decimalNumberStringToDouble(sLat);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LONGITUDE)) // Longitude: 5.273974682895753
                    {
                        String sLon = getSubStringBehindToken(nextLine, ':');
                        lon = decimalNumberStringToDouble(sLon);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(PHOTO))
                        photoRef = getSubStringBehindToken(nextLine, ':');

                    //Create a new Measurement Point
                    Log.d(DEBUGTAG, "User Comment: " + userComment);
                    Log.d(DEBUGTAG, "Latitude: " + Double.toString(lat));
                    Log.d(DEBUGTAG, "Longitude: " + Double.toString(lon));
                    Log.d(DEBUGTAG, "Photo: " + photoRef);
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
                    double lat2 = 0.0;
                    double lon2 = 0.0;

                    String nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LINE_LAT1))
                    {
                        String sLat = getSubStringBehindToken(nextLine, ':');
                        lat1 = decimalNumberStringToDouble(sLat);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LINE_LON1))
                    {
                        String sLon = getSubStringBehindToken(nextLine, ':');
                        lon1 = decimalNumberStringToDouble(sLon);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LINE_LAT2))
                    {
                        String sLat =  getSubStringBehindToken(nextLine, ':');
                        lat2 = decimalNumberStringToDouble(sLat);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(LINE_LON2))
                    {
                        String sLon = getSubStringBehindToken(nextLine, ':');
                        lon2 = decimalNumberStringToDouble(sLon);
                    }
                    // Create a new line instance
                    Log.d(DEBUGTAG, "Lat 1: " + Double.toString(lat1));
                    Log.d(DEBUGTAG, "Lon 1: " + Double.toString(lon1));
                    Log.d(DEBUGTAG, "Lat 2: " + Double.toString(lat2));
                    Log.d(DEBUGTAG, "Lon 2: " + Double.toString(lon2));
                    LatLng point1 = new LatLng(lat1, lon1);
                    LatLng point2 = new LatLng(lat2, lon2);
                    // Constructor of MeasurementLine requires the Position on map,
                    // A this point this can not be passed, so i'm passing the measurement data points
                    // They should be set when map is populated for each line
                    MeasurementLine lineElement = new MeasurementLine(point1, point2, point1, point2);
                    // add Map line to current layer in layermanager
                    result.getCurrentLayer().addLine(lineElement);
                }
                if(line.contains(OPEN_ARC_TAG))
                {
                    double lat1 = 0.0;
                    double lon1 = 0.0;
                    double lat2 = 0.0;
                    double lon2 = 0.0;
                    double lat3 = 0.0;
                    double lon3 = 0.0;


                    String nextLine = bufferedReader.readLine();
                    if(nextLine.contains(ARC_LAT1))
                    {
                        String sLat = getSubStringBehindToken(nextLine, ':');
                        lat1 = decimalNumberStringToDouble(sLat);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(ARC_LON1))
                    {
                        String sLon = getSubStringBehindToken(nextLine, ':');
                        lon1 = decimalNumberStringToDouble(sLon);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(ARC_LAT2))
                    {
                        String sLat =  getSubStringBehindToken(nextLine, ':');
                        lat2 = decimalNumberStringToDouble(sLat);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(ARC_LON2))
                    {
                        String sLon = getSubStringBehindToken(nextLine, ':');
                        lon2 = decimalNumberStringToDouble(sLon);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(ARC_LAT3))
                    {
                        String sLat =  getSubStringBehindToken(nextLine, ':');
                        lat3 = decimalNumberStringToDouble(sLat);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(ARC_LON3))
                    {
                        String sLon = getSubStringBehindToken(nextLine, ':');
                        lon3 = decimalNumberStringToDouble(sLon);
                    }

                    // Create a new Arc instance
                    LatLng point1 = new LatLng(lat1, lon1);
                    LatLng point2 = new LatLng(lat2, lon2);
                    LatLng point3 = new LatLng(lat3, lon3);
                    // Constructor of MeasurementArc requires the Position on map,
                    // A this point this can not be passed, so i'm passing the measurement data points
                    // They should be set, when map is populated, for each arc
                    MeasurementArc arc = new MeasurementArc(point1, point2, point3, point1, point2, point3);
                    // add Arc to current layer in layermanager
                    result.getCurrentLayer().addArc(arc);
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
     * Method to convert Color: #ff007fff ->  4278222847
     * @param lineColor string representation of Line color
     * @return integer representation of color
     */
    public int stringLineColorToIntColor(String lineColor)
    {
        //get substring behind '#' ->> Color: #ff007fff
        String strippedColor = getSubStringBehindToken(lineColor, '#');     // e.g.: Color: #ff007fff
        strippedColor = strippedColor.toLowerCase();
        if(strippedColor.length() == 8 && strippedColor.matches("[0-9a-f]+"))    //if the substring color is exactly 8 chars long (4 -2digit- hex values) AND is a hexadecimal value
        {
            int colorValue = (int) Long.parseLong(strippedColor, 16);
            return colorValue;
        }
        else
        {
            return -1;
        }
    }

    /**
     * Method to convert "Line width: 3px" (string) to 3 (int)
     * @param lw lineWidth line (String) e.g.: "Line width: 3px"
     * @return int value for lineWidth e.g.: 3
     */
    public int stringLineWidthToIntLineWidth(String lw)
    {
        //start "Line width: 3px"
        //pixels => "3px"
        String pixels = getSubStringBehindToken(lw, ':');
        //pixelValue => "3"
        String pixelValue[] = pixels.split("p");
        if(pixelValue[0].matches("[0-9]+")) //if it only contains numbers its ok
        {
            return  Integer.parseInt(pixelValue[0], 10);
        }
        else
        {
            return -1; //invalid value
        }
    }

    /**
     * Method to retrieve the substring behind a char token in the property declaration line
     * e.g.:  token = ':' --> line = Latitude: 50.87421464954512 --> returns --> 50.87421464954512
     * @param line String line containing a property declaration
     * @return  the substring behind the token
     */
    private String getSubStringBehindToken(String line, char token)
    {
        int colonPos = line.indexOf(token);
        String subString;
        // colonPos = -1 if the colon did not occur in string line.
        if(colonPos != -1)
        {
            subString = line.substring(colonPos+1).trim();
            if(subString.length() > 0)
            {
                return  subString;
            }
            else
            {
                return "";
            }
        }
        else
        {
            return "missing token in property declaration";
        }
    }

    /**
     * Method to convert decimal number string to double
     * @param value Can only contain number values and decimal point [0-9 && .]
     * @return double value of string or 0.0 if failed to convert -> due to not obeying the regex rule ;-)
     */
    private double decimalNumberStringToDouble(String value)
    {
        if(value.matches("[0-9]*\\.?[0-9]+"))
        {
            return  Double.parseDouble(value);
        }
        else
        {
            return 0.0;
        }
    }

}