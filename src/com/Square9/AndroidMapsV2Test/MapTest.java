package com.Square9.AndroidMapsV2Test;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.*;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MapTest extends Activity implements OnDialogDoneListener, SaveToFile.SaveToFileEvent, ReadFromFile.ReadFromFileEvent, onMapFragmentEventListener
{
    private final static String DEBUGTAG = "MapTestAct";

    private LocationManager locationManager;
    private LocationProvider locationProvider;
    private boolean gpsSetup;
    private boolean gpsFix;
    private ProgressDialog pdGPSFix;

    //actionbar
    private ActionBar actionBar;
    private ArrayList<String> actionBarLayers;

    //Measurement data structure members
    private LayerManager layerManager;
    private LatLng currentLocation;

    // Cursor ref related to photo's on the device
    private Cursor imageCursor;
    private boolean photoIntent;
    private int lastPhotoId;
    private int newPhotoId;

    //Command Design Pattern Instances
    private UndoRedo commandBuffer;

    /*
     * Actions:
     * 0:
     * 1: add measurement point to current layer
     * 2: draw line
     * 3: draw arc
     * 4: attach photo to measurement point
     * 5: add/change user comment of measurement point
     */
    private int actionId;
    private ArrayList<Marker> selectedMarkers;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(DEBUGTAG, "on Create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if(savedInstanceState != null) // Recreating the previously destroyed instance of the activity
        {
            Log.d(DEBUGTAG, "Recreating the previously destroyed instance of the activity");
            layerManager = savedInstanceState.getParcelable("layerManager");
            //TODO there is more persistant data ...
        }
        // Actionbar dependency
        actionBarLayers = new ArrayList<String>();
        if(layerManager == null)
        {
            Log.d(DEBUGTAG, "Creating new Instance of LayerManager");
            layerManager = new LayerManager();
            String layerName = layerManager.getCurrentLayer().getLayerName();
            actionBarLayers.add(layerName);
        }

        //Map fragment
        FragmentManager fm = getFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.main_fragment_container);
        if(frag == null)
        {
            Log.d(DEBUGTAG, "Creating new Instance of MapCanvasFragment");
            frag = MapCanvasFragment.newInstance();
            fm.beginTransaction().add(R.id.main_fragment_container, frag).commit();
        }

        //actionbar
        actionBar = getActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);


        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,actionBarLayers);
        actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId)
            {
                layerManager.setCurrentLayerByIndex(itemPosition);
                String newCurrentLayer = layerManager.getCurrentLayer().getLayerName();
                Toast.makeText(MapTest.this, "Active Layer: " + newCurrentLayer, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        actionBar.show();
        photoIntent = false;
        lastPhotoId = 0;
        newPhotoId = 0;
        actionId = 0;
        selectedMarkers = new ArrayList<Marker>(0);
        commandBuffer = new UndoRedo();
    }

    @Override
    protected void onStart()
    {
        Log.d(DEBUGTAG, "on Start");
        super.onStart();
        // keep screen on, do not put screen into sleep!
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // setup GPS location provider service...
        if(!gpsSetup)
        {
            this.setupGpsController();
        }
        // Check the current enabled/disabled status of the GPS provider.
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            // create dialog which leads to Location source settings...
            try
            {
                DialogInterface.OnClickListener posOnClick = new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(settingsIntent);
                    }
                };

                // using Activity Context Here!!
                CustomAlertDialog dlgNoGpsProvivder = new CustomAlertDialog(this, getString(R.string.Alert_ttl_GPS_not_enabled), getString(R.string.Alert_msg_GPS_not_enabled), posOnClick);
                dlgNoGpsProvivder.showDialog();
            }
            catch(Exception e)
            {
                Log.d(DEBUGTAG, "dialog failed! " + e.toString());
            }

        }
        if(!gpsFix && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            //pdGPSFix = ProgressDialog.show(MapTest.this, "GPS", "Waiting for GPS Fix");
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(DEBUGTAG, "on Resume");
        // if resuming the activity because we are returning from the camera activity
        //TODO saveInstance state of photoIntent/newPhotoId/lastPhotoId ??
        if(photoIntent)
        {
            photoIntent = false; //Reset variable
            newPhotoId = getLastImageId(); //Get the id of last image taken
            if(lastPhotoId != newPhotoId && newPhotoId != 0)// if a new photo was taken by the user
            {
                Toast.makeText(MapTest.this, "Please Select a Measurement Point, and confirm, to attach the photo to the measurement point!", Toast.LENGTH_LONG).show();
                actionId = 4;
            }
            else
            {
                Toast.makeText(MapTest.this, "No new Photo taken", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause()
    {
        Log.d(DEBUGTAG, "on Pause");
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        Log.d(DEBUGTAG, "on Stop");
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Log.d(DEBUGTAG, "on Destroy");
        super.onDestroy();
        //Generally is recommended to close cursors in Activity's life-cycle method either onStop() or onDestroy() method.
        // Cursor for photo 's taken
        if(imageCursor != null)// If no photo taken, then imageCursor is not initialized
        {
            imageCursor.close();
        }
        // Clean up location based services
        locationManager.removeUpdates(locationListener);
    }


    /*
     * Only Called if Killed by the OS...
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.d(DEBUGTAG, "Saving act instance state");
        // Save all measurements ect....
        outState.putParcelable("layerManager", layerManager);
        // Save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        Log.d(DEBUGTAG, "Restoring instance state");
        layerManager = savedInstanceState.getParcelable("layerManager");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInfl = getMenuInflater();
        menuInfl.inflate(R.menu.actionbar_mapactivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.actionBar_info:
                String title = "Information";
                String msg = "No Gps Fix";
                if(gpsFix)
                {
                    msg = "Accuracy: ???";
                }
                CustomAlertDialog infoDialog = new CustomAlertDialog(MapTest.this, title, msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
                infoDialog.changeIconToInformationIcon();
                infoDialog.showDialog();
                return true;
            case R.id.actionBar_openfile:
                showOpenFileDialog();
                return true;
            case R.id.actionBar_saveToFile:
                showSaveToFileDialog();
                return true;
            case R.id.actionBar_maptype:
                showMapTypeDialog();
                return true;
            case R.id.actionBar_undo:
                if(!commandBuffer.undo())
                {
                    Toast.makeText(MapTest.this, "Nothing to undo", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.actionBar_redo:
                if(!commandBuffer.redo())
                {
                    Toast.makeText(MapTest.this, "Nothing to redo", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.actionBar_measurementPoint:
                if(gpsSetup && currentLocation != null)
                {
                    CommandAddMeasurementPoint addMeasurementPoint = new CommandAddMeasurementPoint(layerManager, currentLocation, getMapFragment());
                    commandBuffer.addToUndoBuffer(addMeasurementPoint);
                }
                else
                {
                    Toast.makeText(MapTest.this, "Waiting for GPS FIX...", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.actionBar_addLayer:
                Toast.makeText(MapTest.this, "Add a new layer", Toast.LENGTH_LONG).show();
                showNewLayerSettingsDialog();
                return true;
            case R.id.actionBar_drawLine:
                actionId = 2;
                Toast.makeText(MapTest.this, "Please Select 2 measurement points and Confirm", Toast.LENGTH_LONG).show();
                return true;
            case R.id.actionBar_drawArc:
                actionId = 3;
                confirmedAction();
                return true;
            case R.id.actionBar_takePic:
                lastPhotoId = getLastImageId();
                photoIntent = true;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(intent);
                return true;
            case R.id.actionBar_text:
                actionId = 5;
                Log.d(DEBUGTAG, "add text button pushed");
                Log.d(DEBUGTAG, "ActionId = " + Integer.toString(actionId) + "#Selected points = " + Integer.toString(selectedMarkers.size()));
                Toast.makeText(MapTest.this, "Please Select 1 measurement point and confirm", Toast.LENGTH_LONG).show();
                return true;
            case R.id.actionBar_actionConfirm:
                Log.d(DEBUGTAG, "Confirm Action button pushed");
                Log.d(DEBUGTAG, "ActionId = " + Integer.toString(actionId) + "#Selected points = " + Integer.toString(selectedMarkers.size()));
                confirmedAction();// Action Performed
                return true;
            case R.id.actionBar_actionCancel:
                finishAction(true);
                return true;
        }
        return(super.onOptionsItemSelected(item));
    }

    /**
     * Method to setup the GPS controller
     */
    public void setupGpsController()
    {
        try
        {
            //According to: http://developer.android.com/training/basics/location/locationmanager.html
            locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
            locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
            locationManager.addGpsStatusListener(gpsStatusListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener);
            gpsSetup = true;
        }
        catch(Exception exp)
        {
            Log.d(DEBUGTAG,"Error: failed setupGpsController: " + exp.toString());
            gpsSetup = false;
        }
    }

    /**
     * Location Listener for on-board GPS
     */
    private LocationListener locationListener = new LocationListener()
    {

        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            // TODO Auto-generated method stub
        }

        public void onProviderEnabled(String provider)
        {
            // TODO Auto-generated method stub
        }

        public void onProviderDisabled(String provider)
        {
            // TODO Auto-generated method stub
        }

        public void onLocationChanged(Location location)
        {
            currentLocation = new LatLng( location.getLatitude(),  location.getLongitude());
            MapCanvasFragment frag = (MapCanvasFragment) getFragmentManager().findFragmentById(R.id.main_fragment_container);
            frag.moveCurrentPositionMarker(currentLocation);
        }
    };

    /**
     * On-board GPS Status Listener
     */
    // http://developer.android.com/reference/android/location/GpsStatus.Listener.html
    private GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener()
    {

        public void onGpsStatusChanged(int event)
        {
            switch(event)
            {
                case(GpsStatus.GPS_EVENT_FIRST_FIX):
                    gpsFix = true;
                   // pdGPSFix.dismiss();
                   // pdGPSFix = null;
                    break;
                case(GpsStatus.GPS_EVENT_SATELLITE_STATUS):
                    break;
                case(GpsStatus.GPS_EVENT_STARTED):
                    break;
                case(GpsStatus.GPS_EVENT_STOPPED):
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Method to create a new measurementPoint instance
     * This method also adds a marker on the map
     * @param position The position (LatLng) of the new Measurement Point
     */
    private void addMeasurementPoint(LatLng position)
    {
        LatLng measurementPosition = new LatLng(position.latitude, position.longitude);
        //Manage the new Measurement point is model structure
        if(layerManager.getCurrentLayer() == null) //test if currentLayer is alive
        {
            Toast.makeText(MapTest.this, "No Layer Active to put this MeasurmentPpoint on!!", Toast.LENGTH_LONG).show();
            return;
        }
        //Create a measurementPoint
        MeasurementPoint mp = new MeasurementPoint(measurementPosition);
        //Add marker to the map
        LatLng markerPos = getMapFragment().addMarker(measurementPosition, layerManager.getCurrentLayer().getLayerName(), mp.getComment(), layerManager.getCurrentLayer().getColor());
        //Add marker position to measurment point
        mp.setMarkerPositioOnMap(markerPos);
        //add it to the currentlayer
        layerManager.addMeasurementPointToLayer(mp);
    }

    /**
     * Method to get the mapcanvasfragment
     * @return the MapCanvasFragment Instance
     */
    private MapCanvasFragment getMapFragment()
    {
        FragmentManager fm = getFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.main_fragment_container);
        return ((MapCanvasFragment) frag);
    }


    /**
     * Method to get the last picture taken on this device.
     * To detect if a picture is taken
     * and to get the reference to this picture if it should by connected to a point
     * Code adapted from: http://stackoverflow.com/questions/7636697/get-path-and-filename-from-camera-intent-result
     * @return the id of the photo or '0' if any problem occurred
     */
    private int getLastImageId()
    {
        final String[] imageColumns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
        final String imageOrderBy = MediaStore.Images.Media._ID+" DESC";
        imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);
        if(imageCursor.moveToFirst())
        {
            int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
            String fullPath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            Log.d(DEBUGTAG, "getLastImageId::id " + id);
            Log.d(DEBUGTAG, "getLastImageId::path " + fullPath);
            return id;
        }
        else
        {
            return 0;
        }
    }

    /**
     * Method to get the full path of the last photo taken
     * @return (String) null if failed Path if ok
     */
    public String getLastPhotoReference()
    {
        final String[] imageColumns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
        final String imageOrderBy = MediaStore.Images.Media._ID+" DESC";
        imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);
        if(imageCursor.moveToFirst())
        {
            int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
            String fullPath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            Log.d(DEBUGTAG, "getLastImageId::id " + id);
            Log.d(DEBUGTAG, "getLastImageId::path " + fullPath);
            return fullPath;
        }
        else
        {
            return null;
        }
    }

    /**
     * Method to launch show map type dialog
     */
    private void showMapTypeDialog()
    {
        MapCanvasFragment frag = (MapCanvasFragment) getFragmentManager().findFragmentById(R.id.main_fragment_container);
        int mapType = frag.getMapType();
        MapTypeDialogFragment mapTypeDlg = MapTypeDialogFragment.newInstance(mapType);
        mapTypeDlg.show(getFragmentManager(), "MAPTYPE");
    }

    /**
     * Method to show the newLayerSettingDialog (Fragement)
     */
    private void showNewLayerSettingsDialog()
    {
            ActiveLayerSettingsDialogFragment alsd = ActiveLayerSettingsDialogFragment.newInstance("Create a new layer:", "New Layer", Color.RED, 3);
            FragmentManager fm = getFragmentManager();
            //TODO is ft needed ?
            FragmentTransaction ft = fm.beginTransaction();
            alsd.show(fm, "NEWLAYERSETTINGS");
    }

    /**
     * Method to launch Save to file Dialog Fragment
     */
    private void showSaveToFileDialog()
    {
        SaveToFileDialogFragment frag = SaveToFileDialogFragment.newInstance();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        frag.show(fm, "SAVETOFILE");
    }

    /**
     * Method to launch add/modify text of a measurement point
     * @param comment The comment of the measurement point
     */
    private void showAddTextDialog(String comment)
    {
        AddTextDialogFragment frag = AddTextDialogFragment.newInstance(comment);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        frag.show(fm, "ADDTEXT");
    }

    /**
     * Method to launch open file dialog
     */
    private void showOpenFileDialog()
    {
        OpenFileDialogFragment dlg = OpenFileDialogFragment.newInstance();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        dlg.show(manager, "OPENFILE");
    }

    @Override
    public void onDialogDone(String tag, boolean cancelled, String message)
    {
        Log.d(DEBUGTAG, "on Dialog Done Call Back...");
        Log.d(DEBUGTAG, "ActionId = " + Integer.toString(actionId) + "#Selected points = " + Integer.toString(selectedMarkers.size()));
        if(!cancelled && tag.equals("SAVETOFILE"))
        {
            if(!FileHandler.checkMediaAvailability()) //if public external storage is not available do nothing
                Toast.makeText(MapTest.this, "Error, could not reach storage media",Toast.LENGTH_LONG).show();
            else
            {
                FileHandler outPutFile = new FileHandler( message.toString().trim(), MapTest.this);
                SaveToFile stf = new SaveToFile(MapTest.this, outPutFile, MapTest.this);
                stf.execute(layerManager);
            }
        }
        else if(!cancelled && tag.equals("ADDTEXT"))
        {
            Log.d("DEBUGTAG", "# selected markers: " + Integer.toString(selectedMarkers.size()));

            if(message != null && selectedMarkers.size() == 1)
            {
                LatLng markerPos = selectedMarkers.get(0).getPosition();
                MeasurementPoint mp = layerManager.getCurrentLayer().getMeasurementPointByMarkerPosition(markerPos);
                mp.setComment(message);
                finishAction(false);
            }
            else
            {
                Toast.makeText(MapTest.this, "Something went very wrong...", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onDialogDone(String tag, boolean cancelled, String ln, int color, int lw)
    {
        //dialog done from NewLayerSettingsDialog
        if(!cancelled && tag.equals("NEWLAYERSETTINGS"))
        {
            //add a new layer to the drop down spinner in actionbar
            actionBarLayers.add(ln);
            int newLayerPosition = actionBarLayers.size() - 1;
            // create a new layer:
            layerManager.addNewLayer(ln, color, lw);
            // update the actionbar spinner to set the new layer as selected on the top
            actionBar.setSelectedNavigationItem(newLayerPosition);
            // Toast.makeText(MapTest.this, "New Layer: " + ln + " is now active!", Toast.LENGTH_LONG).show();
            Log.d(DEBUGTAG, "Added New Layer: ");
            Log.d(DEBUGTAG, "Layer Name: " + ln);
            Log.d(DEBUGTAG, "Color Number: " + Float.toString(color));
            Log.d(DEBUGTAG, "Line Width: " + Integer.toString(lw));
        }
    }

    @Override
    public void onDialogDone(String tag, boolean cancelled, int mapType)
    {
        if(tag.equals("MAPTYPE")) //MapTypeDialog
        {
            int requestedType = mapType;
            MapCanvasFragment frag = getMapFragment();
            int currentType = frag.getMap().getMapType();
            if(currentType != requestedType)
            {
                Log.d(DEBUGTAG, "User selected other map type...");
                frag.setMapType(requestedType);
            }
            else
            {
                Log.d(DEBUGTAG, "Map type selected same as previous so not worth the effort...");
                // Professionally doing nothing ;-)
            }
        }
    }

    @Override
    public void onDialogDone(String tag, boolean cancelled, File selectedFile)
    {
        if(tag.equals("OPENFILE") && selectedFile != null)
        {
           //start AsyncTask openFile
            ReadFromFile rff = new ReadFromFile(MapTest.this, MapTest.this);
            rff.execute(selectedFile);
            // MeanWhile
            // explicit dereference previous layer manager
            layerManager = null;
            // Clear Map
            getMapFragment().clearMap();
            // Clear Layer list from actionbar spinner
            actionBarLayers.clear();
        }
        else
        {
            Toast.makeText(MapTest.this, "Error: Invalid file", Toast.LENGTH_LONG).show();
            Log.d(DEBUGTAG, "Error: callback from open file dialog failed -> incorrect tag or file = null");
        }
    }

    @Override
    public void onSaveToFileCompleted(Integer result)
    {
        String msg;

        switch(result.intValue())
        {
            case 0:
                msg = "Data Successfully Saved\n";
                break;
            case -1:
                msg = "media not available\n";
                break;
            case -2:
                msg = "unable to create file\n";
                break;
            case -3:
                msg = "Error writing file\n";
                break;
            default:
                msg = "Unknown Error\n";
                break;
        }
        Toast.makeText(MapTest.this, msg, Toast.LENGTH_LONG).show();
    }



    @Override
    public void onMarkerClicked(Marker marker)
    {
        String layerName = marker.getTitle();
        String snippet = marker.getSnippet();
        LatLng markerPosition = marker.getPosition();
        String selectedSnippet = getResources().getString(R.string.marker_snippet_selected);

        //Check if it is a "selected" marker or a measurement point marker to be selected ...
        if(snippet.equals(selectedSnippet)) // deselect the marker
        {
            MeasurementLayer layer = layerManager.getLayerByName(layerName);
            if(layer != null)
            {
                MeasurementPoint mp = layer.getMeasurementPointByMarkerPosition(markerPosition);
                if(mp != null)
                {
                    Log.d(DEBUGTAG, "Deselecting marker");
                    getMapFragment().deselectMarkerAt(markerPosition, mp.getPosition(), layerName, mp.getComment(), layer.getColor() );
                }
                else
                {
                    Log.d(DEBUGTAG, "Error: while deselecting marker: measurement point not found in layer: " + layerName);
                }
            }
            else
            {
                Log.d(DEBUGTAG, "Error: while deselecting marker:  Layer not found...");
            }

        }
        else // Select the marker
        {
            MeasurementLayer layer = layerManager.getLayerByName(layerName);
            if(layer != null)
            {
                MeasurementPoint mp = layer.getMeasurementPointByMarkerPosition(markerPosition);
                if(mp != null)
                {
                    Log.d(DEBUGTAG, "Selecting marker");
                    getMapFragment().selectMarker(mp.getMarkerPositioOnMap(), layerName, selectedSnippet);
                }
                else
                {
                    Log.d(DEBUGTAG, "Error: while selecting marker: measurement point not found in layer: " + layerName);
                }
            }
            else
            {
                Log.d(DEBUGTAG, "Error: while selecting marker:  Layer not found...");
            }
        }
    }

    @Override
    public void onMapClicked(LatLng clickPosition)
    {

    }

    @Override
    public void onMapLongClicked(LatLng longClickPosition)
    {
        getMapFragment().clearAllSelectedMarkers();
    }

    @Override
    public void onReadFromFileCompleted(LayerManager layerManager)
    {
        if(layerManager != null)
        {
            // New LayerManager Instance:
            this.layerManager = layerManager;
            //Populate the map
            populateMap();
        }
        else
        {
            Log.d(DEBUGTAG, "Error: Parsing error in file");
            Toast.makeText(MapTest.this, "Error: something went wrong while reading the file", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * User can perform 'actions' on the map such as draw lines ....
     * These actions are confirmed by an action bar button  but of the activity.
     * This method performs the desired action
     */
    public void confirmedAction()
    {
        switch(actionId)
        {
            case 0: // NO ACTION
                //DO NOTHING
                return;
            case 1://add marker to map

                return;
            case 2: // Draw Line
                if(selectedMarkers.size() == 2) //need 2 markers selected to draw a line no more, no less
                {
                    //get the marker positions
                    LatLng mapPos01 = selectedMarkers.get(0).getPosition();
                    LatLng mapPos02 = selectedMarkers.get(1).getPosition();
                    //get the associated measurement points from the layer manager
                    MeasurementPoint mp01 = layerManager.getCurrentLayer().getMeasurementPointByMarkerPosition(mapPos01);
                    MeasurementPoint mp02 = layerManager.getCurrentLayer().getMeasurementPointByMarkerPosition(mapPos02);
                    //Create polylineOptions instance
                    PolylineOptions lineOptions = new PolylineOptions();
                    //Line point 1
                    lineOptions.add(mapPos01); //use Map positions here ?!
                    //Line point 2
                    lineOptions.add(mapPos02); //use Map positions here ?!
                    lineOptions.color(layerManager.getCurrentLayer().getColor());
                    lineOptions.width((float) layerManager.getCurrentLayer().getLineWidth());
                    //Draw the line on the map
                    List<LatLng> mapPoints =  getMapFragment().drawLine(lineOptions);
                    //Create a new Mapline(REAL POSITION1, REAL POSITION2, MAP POSITION1, MAP POSITION2)
                    MapLine line = new MapLine(mp01.getPosition(), mp02.getPosition(), mapPoints.get(0), mapPoints.get(1));
                    layerManager.getCurrentLayer().addLine(line);
                    // reset variables
                    // ->> User Comment to snippet of marker
                    selectedMarkers.get(0).setSnippet(mp01.getComment());
                    selectedMarkers.get(1).setSnippet(mp02.getComment());
                    // Cancel Action
                    finishAction(false);
                }
                else
                {
                    Toast.makeText(MapTest.this, "There are not enough points selected to draw the line CANCELING THE ACTION", Toast.LENGTH_LONG).show();
                    finishAction(true);
                }
                return;
            case 3: // Draw Arc
                Toast.makeText(MapTest.this, "Drawing Arc", Toast.LENGTH_LONG).show();
                actionId = 0; // Reset action id
                return;
            case 4: // add photo
                if(selectedMarkers.size() == 1) // Correct #points selected (1)
                {
                    String photoPath = getLastPhotoReference();
                    LatLng markerPos = selectedMarkers.get(0).getPosition();
                    MeasurementPoint mp = layerManager.getCurrentLayer().getMeasurementPointByMarkerPosition(markerPos);
                    mp.setPhotoFilePath(photoPath);
                    Toast.makeText(MapTest.this, "Photo Added!", Toast.LENGTH_LONG).show();
                    finishAction(false);
                }
                else if(selectedMarkers.size() == 0) // no Point Selected
                {
                    Toast.makeText(MapTest.this, "no Measurement Point Selected, Please Selected a Point and Confirm!", Toast.LENGTH_LONG).show();
                    selectedMarkers.clear();
                }
                else //too many points selected
                {
                    Toast.makeText(MapTest.this, "Too Many Measurement Points Selected,  Please Selected ONE Point and Confirm!", Toast.LENGTH_LONG).show();
                    selectedMarkers.clear();
                }
                return;
            case 5: // Add/Change User Comment of measurement point
                if(selectedMarkers.size() == 1) // Correct #points selected (1)
                {
                    Log.d(DEBUGTAG, "Action Confirmed... Launching dialog");
                    Log.d(DEBUGTAG, "ActionId = " + Integer.toString(actionId) + "#Selected points = " + Integer.toString(selectedMarkers.size()));
                    LatLng markerPos = selectedMarkers.get(0).getPosition();
                    MeasurementPoint mp = layerManager.getCurrentLayer().getMeasurementPointByMarkerPosition(markerPos);
                    String userComment = mp.getComment();
                    showAddTextDialog(userComment);
                }
                else if(selectedMarkers.size() == 0) // no Point Selected
                {
                    Toast.makeText(MapTest.this, "no Measurement Point Selected, Please Selected a Point and Confirm!", Toast.LENGTH_LONG).show();
                    selectedMarkers.clear();
                }
                else //too many points selected
                {
                    Toast.makeText(MapTest.this, "Too Many Measurement Points Selected,  Please Selected ONE Point and Confirm!", Toast.LENGTH_LONG).show();
                    selectedMarkers.clear();
                }
                return;
            default:
                return;
        }
    }

    /**
     * Method for cancelling the Action
     */
    public void finishAction(Boolean cancelled)
    {
        // Reset User Comment to snippet
        for(Marker m : selectedMarkers)
        {
            MeasurementPoint mp = layerManager.getCurrentLayer().getMeasurementPointByMarkerPosition(m.getPosition());
            if(mp != null)
            {
                m.setSnippet(mp.getComment());
            }
        }
        //dereference selected markers
        selectedMarkers.clear();
        //clear user selected action
        actionId = 0;
        if(cancelled)
            Toast.makeText(MapTest.this, "Action Canceled", Toast.LENGTH_SHORT).show();
    }

    public void populateMap()
    {
        Iterator<MeasurementLayer> layerIterator = layerManager.getMeasurementLayerIterator();
        LatLng markerPos = new LatLng(51.759275,5.738796); //ergens in Nederland
        while(layerIterator.hasNext())
        {
            MeasurementLayer layer = layerIterator.next();
            //Add to actionbar spinner:
            actionBarLayers.add(layer.getLayerName());
            //Set this layer as current layer:
            layerManager.setCurrentLayer(layer.getLayerName());
            // update the actionbar spinner to set the new layer as selected on the top
            int newLayerPosition = actionBarLayers.size() - 1;
            actionBar.setSelectedNavigationItem(newLayerPosition);
            int currentColor = layer.getColor();
            int currentLineWidth = layer.getLineWidth();
            for(int pointIndex = 0; pointIndex < layer.getNumberOfMeasurementPoints(); pointIndex++)
            {
               // Add the marker to the map
                //Create a temp measurementPoint
                MeasurementPoint mp = layer.getMeasurementPointByIndex(pointIndex);
                //Add marker to the map
                markerPos = getMapFragment().addMarker(mp.getPosition(), layerManager.getCurrentLayer().getLayerName(), mp.getComment(), layerManager.getCurrentLayer().getColor());
                //Add marker position to measurement point
                mp.setMarkerPositioOnMap(markerPos);
            }
            Iterator<MapLine> lineIterator = layer.getMapLineIterator();
            while(lineIterator.hasNext())
            {
                MapLine line = lineIterator.next();
                //Create polylineOptions instance
                PolylineOptions lineOptions = new PolylineOptions();
                //Line point 1
                lineOptions.add(line.getPointOne());
                //Line point 2
                lineOptions.add(line.getPointTwo());
                lineOptions.color(layerManager.getCurrentLayer().getColor());
                lineOptions.width((float) layerManager.getCurrentLayer().getLineWidth());
                //Draw the line on the map
                List<LatLng> mapPoints = getMapFragment().drawLine(lineOptions);
                // Update the line position on the map
                line.setLinePositioOnMap01(mapPoints.get(0));
                line.setLinePositioOnMap02(mapPoints.get(1));
            }
            //TODO More Map elements

        }
        //restore current position marker to last point added:
        getMapFragment().restoreCurrentPositionMarker(markerPos);
    }
}

