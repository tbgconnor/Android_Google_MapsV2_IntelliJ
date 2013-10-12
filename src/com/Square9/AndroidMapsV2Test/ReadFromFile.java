package com.Square9.AndroidMapsV2Test;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;

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
                if(line.contains(OPEN_LAYER_TAG))
                {
                    String layerName = "";
                    int layerColor = 0;
                    int layerLineWidth = 0;

                    String nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_LAYER_NAME_TAG))
                    {
                        layerName = parseXmlString(nextLine);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_LAYER_COLOR_TAG))
                    {
                        String layerColorParsed = parseXmlString(nextLine); //   Strip value from tags

                        int color = stringLineColorToIntColor(layerColorParsed);
                        if(color == -1)
                        {
                            // Parsing error if return -1
                            color = 16777215; // 0xFFFFFF (white)
                        }
                        layerColor = color;
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_LAYER_LINEWIDTH_TAG))
                    {
                        layerLineWidth = stringLineWidthToIntLineWidth(nextLine);
                        if(layerLineWidth == -1)
                        {
                            //If parsing error occurred, make it default:
                            layerLineWidth = 3;
                            Log.d(DEBUGTAG, "ERROR while parsing the line width of the layer, set to default: " + Integer.toString(layerLineWidth));
                        }
                    }
                    // Create a new Measurement layer and set is as current layer of the layer manager
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
                    if(nextLine.contains(OPEN_USER_COMMENT_TAG))   //	<User Comment>xyz</User Comment>
                        userComment = parseXmlString(nextLine);

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_MP_LATITUDE_TAG))      // 	<Latitude>50.874207705232884</Latitude>
                    {
                        String sLat = parseXmlString(nextLine); //50.874207705232884
                        lat = decimalNumberStringToDouble(sLat);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_MP_LONGITUDE_TAG)) // <Longitude>5.274354991470152</Longitude>
                    {
                        String sLon = parseXmlString(nextLine);
                        lon = decimalNumberStringToDouble(sLon);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_PHOTO_TAG)) // <PHOTO>xyz</PHOTO>
                    {
                        photoRef = parseXmlString(nextLine);
                    }


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
                    double lat2 = 0.0;
                    double lon2 = 0.0;
                    String nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_LINE_LAT1_TAG)) //<Latitude 1>50.87416954036876</Latitude 1>
                    {
                        String sLat = parseXmlString(nextLine);
                        lat1 = decimalNumberStringToDouble(sLat);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_LINE_LON1_TAG)) //<Longitude 1>5.274158805922624</Longitude 1>
                    {
                        String sLon = parseXmlString(nextLine);
                        lon1 = decimalNumberStringToDouble(sLon);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_LINE_LAT2_TAG)) //<Latitude 2>50.87418850654149</Latitude 2>
                    {
                        String sLat =  parseXmlString(nextLine);
                        lat2 = decimalNumberStringToDouble(sLat);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_LINE_LON2_TAG)) //<Longitude 2>5.2744467368496855</Longitude 2>
                    {
                        String sLon = parseXmlString(nextLine);
                        lon2 = decimalNumberStringToDouble(sLon);
                    }
                    // Create a new line instance
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
                    if(nextLine.contains(OPEN_ARC_LAT1_TAG))
                    {
                        String sLat = parseXmlString(nextLine);
                        lat1 = decimalNumberStringToDouble(sLat);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_ARC_LON1_TAG))
                    {
                        String sLon = parseXmlString(nextLine);
                        lon1 = decimalNumberStringToDouble(sLon);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_ARC_LAT2_TAG))
                    {
                        String sLat =  parseXmlString(nextLine);
                        lat2 = decimalNumberStringToDouble(sLat);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_ARC_LON2_TAG))
                    {
                        String sLon = parseXmlString(nextLine);
                        lon2 = decimalNumberStringToDouble(sLon);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_ARC_LAT3_TAG))
                    {
                        String sLat =  parseXmlString(nextLine);
                        lat3 = decimalNumberStringToDouble(sLat);
                    }

                    nextLine = bufferedReader.readLine();
                    if(nextLine.contains(OPEN_ARC_LON3_TAG))
                    {
                        String sLon = parseXmlString(nextLine);
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
        //#ffffff00
        //get substring behind '#' ->> Color: #ff007fff --> ff007fff
        String strippedColor = getSubStringBehindToken(lineColor, '#');     // e.g.: Color:  ff007fff
        strippedColor = strippedColor.toLowerCase();
        if(strippedColor.length() == 8 && strippedColor.matches("[0-9a-f]+"))    //if the substring color is exactly 8 chars long (4 -2digit- hex values) AND is a hexadecimal value
        {
            int colorValue = (int) Long.parseLong(strippedColor, 16);
            return colorValue;
        }
        else
        {
            Log.d(DEBUGTAG, "ERROR: while parsing color");
            return -1;
        }
    }

    /**
     * Method to convert "<Line width>4 px </Line width>" (string) to 4 (int)
     * @param lw lineWidth line (String) e.g.: "<Line width>4 px </Line width>"
     * @return int value for lineWidth e.g.: 4
     */
    public int stringLineWidthToIntLineWidth(String lw)
    {
        //<Line width>4px</Line width>
        String xmlValue = parseXmlString(lw);
        //4px
        String pixelValue[] = xmlValue.split("p");
        //4
        String lineWidthNumber = pixelValue[0].trim();
        if(lineWidthNumber.matches("[0-9]+")) //if it only contains numbers its ok
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

    /**
     * Method to parse strings like: <tag1>xyz</tag1>
     * Using XML pull parser as suggested by android documentation: http://developer.android.com/reference/org/xmlpull/v1/XmlPullParser.html
     * @param xml the string to parser  <tag1>xyz</tag1>
     * @return value between the tags "xyz"
     */
    private String parseXmlString(String xml)
    {
        String xmlValue = null;
        try
        {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); //Create a new instance of the parser factory
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput( new StringReader (xml) );
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                if(eventType == XmlPullParser.TEXT)
                {
                    xmlValue = xpp.getText();
                }
                eventType = xpp.next();
            }
        }
        catch(Exception e)
        {
          // Swallow the exception
        }
        // If there is no text between the tags -> empty string
        if(xmlValue == null)
        {
            xmlValue = "";
        }

        return xmlValue;
    }

}