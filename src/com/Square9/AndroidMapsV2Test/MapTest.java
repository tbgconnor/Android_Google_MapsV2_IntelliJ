package com.Square9.AndroidMapsV2Test;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MapTest extends Activity implements IonDialogDoneListener, SaveToFile.SaveToFileEvent, ReadFromFile.ReadFromFileEvent, IonMapFragmentEventListener
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

    // Selection mode
    private boolean selectionMode;
    private MeasurementPoint mpOninfoWindowClicked;
    private MeasurementPoint mpPhotoAttach;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(DEBUGTAG, "on Create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        layerManager = new LayerManager();

        // Actionbar
        actionBarLayers = new ArrayList<String>();
        String layerName = layerManager.getCurrentLayer().getLayerName();
        actionBarLayers.add(layerName);
        actionBar = getActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,actionBarLayers);
        actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId)
            {
                // Deselect all measurement items of previous current layer
                if(getMapFragment().getNumberOfSelectedMarkers() > 0)
                {
                    deSelectAllMeasurementItems();
                    selectionMode = false;
                }
                layerManager.setCurrentLayerByIndex(itemPosition);
                String newCurrentLayer = layerManager.getCurrentLayer().getLayerName();
                Toast.makeText(MapTest.this, "Active Layer: " + newCurrentLayer, Toast.LENGTH_LONG).show();
                return true;
            }
        });
        actionBar.show();
        //Map fragment
        FragmentManager fm = getFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.main_fragment_container);
        if(frag == null)
        {
            Log.d(DEBUGTAG, "Creating new Instance of MapCanvasFragment");
            frag = MapCanvasFragment.newInstance();
            fm.beginTransaction().add(R.id.main_fragment_container, frag).commit();
        }

        photoIntent = false;
        lastPhotoId = 0;
        newPhotoId = 0;
        commandBuffer = new UndoRedo();
        selectionMode = false;
        mpOninfoWindowClicked = null;
        mpPhotoAttach = null;
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
                mpPhotoAttach.setPhotoFilePath(getLastPhotoReference());
                Toast.makeText(MapTest.this, "Photo added", Toast.LENGTH_LONG).show();
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
        populateMap();
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
            case R.id.actionBar_selectionMode:
                selectionMode = !selectionMode;
                if(selectionMode)
                    Toast.makeText(MapTest.this, "Selection mode ON", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(MapTest.this, "Selection mode OFF", Toast.LENGTH_LONG).show();
                return true;
            case R.id.actionBar_addLayer:
                Toast.makeText(MapTest.this, "Add a new layer", Toast.LENGTH_LONG).show();
                showNewLayerSettingsDialog();
                return true;
            case R.id.actionBar_drawLine:
                if(getMapFragment().getNumberOfSelectedMarkers() == 2)
                {
                    //get current layer
                    MeasurementLayer currentLayer = layerManager.getCurrentLayer();
                    //get the position of the markers
                    LatLng positionOfSelectedMarker01 = getMapFragment().getSelectedMarkerPositionAtIndex(0);
                    LatLng positionOfSelectedMarker02 = getMapFragment().getSelectedMarkerPositionAtIndex(1);
                    //find the measurement point position associated with the markers
                    LatLng mpPosition01 = currentLayer.getMeasurementPointByMarkerPosition(positionOfSelectedMarker01).getPosition();
                    LatLng mpPosition02 = currentLayer.getMeasurementPointByMarkerPosition(positionOfSelectedMarker02).getPosition();
                    if(mpPosition01 != null && mpPosition02 != null)
                    {
                        CommandAddMeasurementLine addMeasurementLine = new CommandAddMeasurementLine(layerManager, mpPosition01, mpPosition02, getMapFragment());
                        commandBuffer.addToUndoBuffer(addMeasurementLine);
                    }
                    else
                    {
                        Log.d(DEBUGTAG, "Error: [Line] selected marker positions not found in measurment point layer! (positions do not match)");
                    }
                }
                else
                {
                     CustomAlertDialog selectionAlert = new CustomAlertDialog(MapTest.this, "Draw Line", "Select 2 measurement points to draw a Line!", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                         }
                     });
                    selectionAlert.showDialog();
                }
                return true;
            case R.id.actionBar_drawArc:
                if(getMapFragment().getNumberOfSelectedMarkers() == 3)
                {
                    //Get current Layer
                    MeasurementLayer currentLayer = layerManager.getCurrentLayer();
                    //Get the positions of the selected markers
                    LatLng positionOfSelectedMarker01 = getMapFragment().getSelectedMarkerPositionAtIndex(0);
                    LatLng positionOfSelectedMarker02 = getMapFragment().getSelectedMarkerPositionAtIndex(1);
                    LatLng positionOfSelectedMarker03 = getMapFragment().getSelectedMarkerPositionAtIndex(2);
                    //Get the corresponding positions of the associated measurement points
                    LatLng mpPosition01 = currentLayer.getMeasurementPointByMarkerPosition(positionOfSelectedMarker01).getPosition();
                    LatLng mpPosition02 = currentLayer.getMeasurementPointByMarkerPosition(positionOfSelectedMarker02).getPosition();
                    LatLng mpPosition03 = currentLayer.getMeasurementPointByMarkerPosition(positionOfSelectedMarker03).getPosition();
                    if(mpPosition01 != null && mpPosition02 != null && mpPosition03 != null)
                    {
                        CommandAddMeasurementArc cmd = new CommandAddMeasurementArc(layerManager, mpPosition01, mpPosition02, mpPosition03, getMapFragment());
                        commandBuffer.addToUndoBuffer(cmd);
                    }
                    else
                    {
                        Log.d(DEBUGTAG, "Error: [Arc] selected marker positions not found in measurment point layer! (positions do not match)");
                    }
                }
                else
                {
                    CustomAlertDialog selectionAlert = new CustomAlertDialog(MapTest.this, "Draw Arc", "Select 3 measurement points to draw an Arc!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    selectionAlert.showDialog();
                }

                return true;
            case R.id.actionBar_takePic:
                if(getMapFragment().getNumberOfSelectedMarkers() == 1)
                {
                    // Get the position of the marker to add the photo to
                    LatLng photoPosition = getMapFragment().getSelectedMarkerPositionAtIndex(0);
                    // Find corresponding measurement Point
                    mpPhotoAttach = layerManager.getCurrentLayer().getMeasurementPointByMarkerPosition(photoPosition);
                    if(mpPhotoAttach != null)
                    {
                        // Deselect the marker
                        getMapFragment().deselectMarker(getMapFragment().getSelectedMarkerAtIndex(0), mpPhotoAttach.getComment(), layerManager.getCurrentLayer().getColor());
                        // Get the last image id
                        lastPhotoId = getLastImageId();
                        // Start the intent
                        photoIntent = true;
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivity(intent);
                    }
                    else
                    {
                        Log.d(DEBUGTAG, "Could not find the corresponding Measurement Point for the selected marker to attach a Photo");
                    }
                }
                else
                {
                    CustomAlertDialog selectionAlert = new CustomAlertDialog(MapTest.this, "Add Photo", "Select 1 measurement point to attach the photo to!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    selectionAlert.showDialog();
                }
                return true;
            case R.id.actionBar_delete:

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
        //Add marker position to measurement point
        mp.setMarkerPositionOnMap(markerPos);
        //add it to the current layer
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
        if(!cancelled && tag.equals("SAVETOFILE"))
        {
            Log.d(DEBUGTAG, "Saving to file...");
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
            Log.d(DEBUGTAG, "received callback from add text dialog: " + message.trim());
            mpOninfoWindowClicked.setComment(message.trim());
            getMapFragment().updateMarkerSnippet(mpOninfoWindowClicked.getMarkerPositionOnMap(), message.trim());
            mpOninfoWindowClicked = null;
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
            //Clear undo redo buffers
            commandBuffer.clearBuffers();
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
        Log.d(DEBUGTAG, "On Marker Clicked Event Received in Main Activity " + marker.getId());
        String currentPositionMarkerTitle = getResources().getString(R.string.currentPositionMarkerTitle);

        if(selectionMode && !marker.getTitle().equals(currentPositionMarkerTitle))
        {
            String layerName = marker.getTitle();
            String snippet = marker.getSnippet();
            LatLng markerPosition = marker.getPosition();
            String selectedSnippet = getResources().getString(R.string.marker_snippet_selected);
            Log.d(DEBUGTAG, "Marker Title: " + layerName);
            Log.d(DEBUGTAG, "Marker Snippet: " + snippet);

            //Check if it is a "selected" marker or a measurement point marker to be selected ...
            if(snippet.equals(selectedSnippet)) // deselect the marker
            {
                MeasurementLayer layer = layerManager.getCurrentLayer();
                MeasurementPoint mp = layer.getMeasurementPointByMarkerPosition(markerPosition);
                if(mp != null)
                {
                    Log.d(DEBUGTAG, "Deselecting marker");
                    getMapFragment().deselectMarker(marker, mp.getComment(), layer.getColor());
                }
                else
                {
                    Log.d(DEBUGTAG, "Error: while deselecting marker: measurement point not found in layer: " + layerName);
                }
            }
            else // Select the marker
            {
                // ONLY select markers of current active layer! (to limit the search)
                if(layerManager.getCurrentLayer().getLayerName().equals(layerName))
                {
                    Log.d(DEBUGTAG, "Selecting Marker");
                    getMapFragment().selectMarker(marker);
                }
                else
                {
                    marker.showInfoWindow();
                    Toast.makeText(MapTest.this, "Measurement Point not in active Layer!", Toast.LENGTH_LONG).show();
                }
            }
        }
        else if(!selectionMode && !marker.getTitle().equals(currentPositionMarkerTitle))
        {
            marker.showInfoWindow();
        }
    }

    @Override
    public void onMapClicked(LatLng clickPosition)
    {
        if(selectionMode)
        {
            // Point conversion of clickPosition LatLng -->> XY
            Point pointClicked = getMapFragment().getMapProjection(clickPosition);
            // Line Points in Current Layer [XY]
            ArrayList<Point> layerLinePoints = getMapFragment().getLineProjectionPointsOfLayer(layerManager.getCurrentLayer().getLayerName());
            // If the current layer contains LINES:
            if(!layerLinePoints.isEmpty())
            {
                //Check if we got an even number of points (every lines has to points so the size must be even...)
                int numberOfLinePoints = layerLinePoints.size();
                int remainder = numberOfLinePoints % 2;
                if(remainder == 0) // Even #LinePoints is required
                {
                    // lines are at paired indexes: [0,1] [2,3] ... [n, n+1] --> 0 is point1 of line0 and 1 is point2 of line0 --> 2 is point1 of line1 and 3 is point2 of line1 --> ....
                    // Line Index :                  0     1    .... n/2     --> ever pair n and n+1 is a line so the position of the line in array would be n/2
                    int lineIndex = 0;
                    for(int index = 0; index < layerLinePoints.size()-1; index=index+2)
                    {
                        Point pOne = layerLinePoints.get(index);
                        Point pTwo = layerLinePoints.get(index+1);
                        // y = ax + c
                        double a = getLineSlope(pOne, pTwo);
                        double c = getLineOffset(pOne, a);
                        int errorMargin = 10; // Only for Y
                        // Determine X bounds
                        int xUpperBound = 0;
                        int xLowerBound = 0;
                        if(pOne.x > pTwo.x)
                        {
                            xUpperBound = pOne.x;
                            xLowerBound = pTwo.x;
                        }
                        else
                        {
                            xUpperBound = pTwo.x;
                            xLowerBound = pOne.x;
                        }
                        // Check if the click is within the bounds (x and y)
                        if( (pointClicked.y + errorMargin ) >= (a*pointClicked.x+c) && (pointClicked.y - errorMargin ) <= (a*pointClicked.x+c)
                                && pointClicked.x < xUpperBound && pointClicked.x > xLowerBound )
                        {
                            // Calc lineIndex
                            lineIndex = index / 2;
                            // Select the line by this index
                            getMapFragment().selectLineByIndex(layerManager.getCurrentLayer().getLayerName(), layerManager.getCurrentLayer().getColor(), lineIndex);
                            break;
                        }
                    }
                }
                else
                {
                    Log.d(DEBUGTAG, "Error: (Map Clicked) uneven number of line points received!");
                }
            }
            else
            {
                Log.d(DEBUGTAG, "(Map Clicked) No lines in current layer");
            }
        }
    }

    @Override
    public void onMapLongClicked(LatLng longClickPosition)
    {
        deSelectAllMeasurementItems();
    }

    @Override
    public void onInfoWindowClicked(Marker marker)
    {
        String snippet = marker.getSnippet();
        // Infowindow clicked is only for not selected markers and should not be the current position marker :-)
        if(!snippet.equals(getResources().getString(R.string.marker_snippet_selected)) && layerManager.getLayerByName(marker.getTitle()) != null)
        {
            String layerName = marker.getTitle();
            MeasurementLayer layer = layerManager.getLayerByName(layerName);
            String userComment;
            if( layer != null)
            {
                mpOninfoWindowClicked = layer.getMeasurementPointByMarkerPosition(marker.getPosition());
                if(mpOninfoWindowClicked  != null)
                {
                    userComment = mpOninfoWindowClicked.getComment();
                    showAddTextDialog(userComment);
                }
                else
                {
                    Log.d(DEBUGTAG, "Error: infowindow Clicked could not find measurement point by marker position on map");
                }
            }
            else
            {
                Log.d(DEBUGTAG, "Error: infowindow Clicked could not find layer from marker title");
            }
        }
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

    public void populateMap()
    {
        Log.d(DEBUGTAG, "Populating Map!");
        // Clear all layers in Actionbar spinner
        actionBarLayers.clear();
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
                mp.setMarkerPositionOnMap(markerPos);
            }
            Iterator<MeasurementLine> lineIterator = layer.getMeasurementLineIterator();
            while(lineIterator.hasNext())
            {
                MeasurementLine line = lineIterator.next();
                String layerName = layerManager.getCurrentLayer().getLayerName();
                LatLng p1 = line.getPointOne();
                LatLng p2 = line.getPointTwo();
                int color = layerManager.getCurrentLayer().getColor();
                int lineWidth = layerManager.getCurrentLayer().getLineWidth();
                //Draw Line on Map
                List<LatLng> pointsOnMap = getMapFragment().drawLine(p1, p2, layerName, color, lineWidth);
                // Update the line position on the map
                line.setLinePositionOnMap01(pointsOnMap.get(0));
                line.setLinePositionOnMap02(pointsOnMap.get(1));
            }
            Iterator<MeasurementArc> arcIterator =layer.getMeasurementArcIterator();
            while (arcIterator.hasNext())
            {
                MeasurementArc arc = arcIterator.next();
                String layerName = layerManager.getCurrentLayer().getLayerName();
                int color = layerManager.getCurrentLayer().getColor();
                int lineWidth = layerManager.getCurrentLayer().getLineWidth();
                LatLng mP1 = arc.getMeasurementPositions().get(0);
                LatLng mP2 = arc.getMeasurementPositions().get(1);
                LatLng mP3 = arc.getMeasurementPositions().get(2);
                getMapFragment().drawArc(mP1, mP2, mP3, layerName, color, lineWidth);
                //TODO Draw the ARC on the map
                //TODO get the positions on the map of the arc
                //TODO Update Arc instance in the model
            }

        }
        //restore current position marker to last point added:
        getMapFragment().restoreCurrentPositionMarker(markerPos);
    }

    public void deSelectAllMeasurementItems()
    {
        int numberOfSelectedMarkers = getMapFragment().getNumberOfSelectedMarkers();
        MeasurementLayer layer = layerManager.getCurrentLayer();
        //Deselect all measurementpoints
        for(int index = numberOfSelectedMarkers-1; index >= 0; index--)
        {
            Log.d(DEBUGTAG, "Index: " + index);
            Marker selectedMarker = getMapFragment().getSelectedMarkerAtIndex(index);
            if(selectedMarker != null)
            {
                MeasurementPoint mp = layer.getMeasurementPointByMarkerPosition(selectedMarker.getPosition());
                if(mp != null)
                {
                    getMapFragment().deselectMarker(selectedMarker, mp.getComment(), layer.getColor());
                }
                else
                {
                    Log.d(DEBUGTAG, "Measurement Point is NULL!");
                }
            }
            else
            {
                Log.d(DEBUGTAG, "Selected Marker is null");
            }

        }
        // Deselect all measurmentLines
        getMapFragment().deselectAllMeasurementLinesOnMap(layerManager.getCurrentLayer().getColor());
        //TODO Deselect all MeasurementArcs
    }

    /**
     * Method to calculate a lines slope based on 2 points ( 2D orthogonal space XY)
     * in terms of y = a*x +c --> a = slope
     * @param one a point of the line
     * @param two another point of the line
     * @return the slope
     */
    public double getLineSlope(Point one, Point two)
    {
        double deltaX = (double) (two.x - one.x);
        double deltaY = (double) (two.y - one.y);
        double result = deltaY/deltaX;
        return result;
    }

    /**
     * Method to calculate a lines offset based on 1 points and the slope of the line ( 2D orthogonal space XY)
     * @param one a point of the line
     * @param slope the slope of the line
     * @return the offset of the line
     */
    public double getLineOffset(Point one, double slope)
    {
        // y = ax+c => c = y - ax
        double result = one.y - (slope*one.x);
        return result;
    }


}

