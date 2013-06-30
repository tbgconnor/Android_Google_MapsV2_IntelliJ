package com.Square9.AndroidMapsV2Test;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.*;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class MapTest extends Activity implements MapTypeDialogFragment.MapTypeDialogListener,OnDialogDoneListener, SaveToFile.SaveToFileEvent
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

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(DEBUGTAG, "on Create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
            frag = MapCanvasFragment.newInstance("data naar Mapfragment", layerManager);
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
            photoIntent = false;
            newPhotoId = getLastImageId();
            if(lastPhotoId != newPhotoId && newPhotoId != 0)// if a new photo was taken by the user
            {
                Toast.makeText(MapTest.this, "Please Select a Measurement Point to attach the photo to!", Toast.LENGTH_LONG).show();
                getMapFragment().setActionId(4);
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
    protected void onDestroy()
    {
        Log.d(DEBUGTAG, "on Destroy");
        super.onDestroy();
        //Generally is recommended to close cursors in Activity's life-cycle method either onStop() or onDestroy() method.
        // Cursor for photo 's taken
        imageCursor.close();
    }

    @Override
    protected void onStop()
    {
        Log.d(DEBUGTAG, "on Stop");
        super.onStop();
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
                    int accuracy =  locationProvider.getAccuracy();
                    msg = "Accuracy: " + Integer.toString(accuracy) + " m";
                }
                CustomAlertDialog infoDialog = new CustomAlertDialog(MapTest.this, title, msg, infoPositiveClick);
                infoDialog.changeIconToInformationIcon();
                infoDialog.showDialog();
                return true;
            case R.id.actionBar_openfile:
                return true;
            case R.id.actionBar_saveToFile:
                showSaveToFileDialog();
                return true;
            case R.id.actionBar_maptype:
                showMapTypeDialog();
                return true;
            case R.id.actionBar_measurementPoint:
                Log.d(DEBUGTAG, "Adding measurement point to the map");
                if(gpsSetup && currentLocation != null)
                {
                   getMapFragment().setActionId(1);
                   addMeasurementPoint(currentLocation);
                }
                else
                {
                    Log.d(DEBUGTAG, "Add measurement point while GPS not fixed yet..");
                    Toast.makeText(MapTest.this, "Waiting for GPS FIX...", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.actionBar_addLayer:
                Log.d(DEBUGTAG, "Adding Layer");
                Toast.makeText(MapTest.this, "Add a new layer", Toast.LENGTH_LONG).show();
                showNewLayerSettingsDialog();
                return true;
            case R.id.actionBar_drawLine:
                getMapFragment().setActionId(2);
                Toast.makeText(MapTest.this, "Please Select 2 measurement points and Confirm", Toast.LENGTH_LONG).show();
                return true;
            case R.id.actionBar_drawArc:
                getMapFragment().setActionId(3);
                getMapFragment().confirmedAction();
                return true;
            case R.id.actionBar_takePic:
                lastPhotoId = getLastImageId();
                photoIntent = true;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(intent);
                return true;
            case R.id.actionBar_actionConfirm:
                getMapFragment().confirmedAction();// Action Performed
                return true;
            case R.id.actionBar_actionCancel:
                getMapFragment().cancelAction();
                return true;
        }
        return(super.onOptionsItemSelected(item));
    }

    DialogInterface.OnClickListener infoPositiveClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    private void addMeasurementPoint(LatLng position)
    {
        //Manage the new Measurement point is model structure
        if(layerManager.getCurrentLayer() == null) //test if currentLayer is alive
        {
            Toast.makeText(MapTest.this, "No Layer Active to put this MeasurmentPpoint on!!", Toast.LENGTH_LONG).show();
            return;
        }
        //Create a measurementPoint
        MeasurementPoint mp = new MeasurementPoint(position);
        //add it to the currentlayer
        layerManager.addMeasurementPointToLayer(mp);
        // confirm the action here
        // if the layer was not available then there will be no action confirmed
        getMapFragment().confirmedAction();
    }

    private MapCanvasFragment getMapFragment()
    {
        FragmentManager fm = getFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.main_fragment_container);
        return ((MapCanvasFragment) frag);
    }

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

    @Override
    public void onDialogPositiveClick(DialogFragment dialog)
    {
        Log.d(DEBUGTAG, "call back from dialog received");
        MapTypeDialogFragment dlg = (MapTypeDialogFragment) dialog;
        int requestedType = dlg.getSelectedMapType();
        MapCanvasFragment frag = (MapCanvasFragment) getFragmentManager().findFragmentById(R.id.main_fragment_container);
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

    private void showMapTypeDialog()
    {
        MapCanvasFragment frag = (MapCanvasFragment) getFragmentManager().findFragmentById(R.id.main_fragment_container);
        int mapType = frag.getMapType();
        String type = Integer.toString(mapType);
        Log.d(DEBUGTAG, "Current Map Type = " + type);
        MapTypeDialogFragment mapTypeDlg = MapTypeDialogFragment.newInstance(mapType);
        mapTypeDlg.show(getFragmentManager(), "Map Type Dialog");
    }

    private void showNewLayerSettingsDialog()
    {
            ActiveLayerSettingsDialogFragment alsd = ActiveLayerSettingsDialogFragment.newInstance("Create a new layer:", "New Layer", Color.RED, 3);
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            alsd.show(fm, "NEWLAYERSETTINGS");
    }

    private void showSaveToFileDialog()
    {
        SaveToFileDialogFragment frag = SaveToFileDialogFragment.newInstance();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        frag.show(fm, "SAVETOFILE");
    }

    @Override
    public void onDialogDone(String tag, boolean cancelled, CharSequence message)
    {
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

    /**
     * Method to get the last picture taken on this device.
     * To detect if a picture is taken
     * and to get the reference to this icture if it should by connected to a point
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

    public String getLastPhotoReference()
    {
        String photoRef = null;
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
}

