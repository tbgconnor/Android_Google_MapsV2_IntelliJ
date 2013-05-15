package com.Square9.AndroidMapsV2Test;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.*;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class MapTest extends Activity implements MapTypeDialogFragment.MapTypeDialogListener,OnDialogDoneListener
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



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(DEBUGTAG, "on Create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Actionbar dependency
        actionBarLayers = new ArrayList<String>();

        //Map fragment
        FragmentManager fm = getFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.main_fragment_container);
        if(frag == null)
        {
            Log.d(DEBUGTAG, "Creating new Instance of MapCanvasFragment");
            frag = MapCanvasFragment.newInstance("data naar Mapfragment");
            fm.beginTransaction().add(R.id.main_fragment_container, frag).commit();
        }
        if(layerManager == null)
        {
            Log.d(DEBUGTAG, "Creating new Instance of LayerManager");
            layerManager = new LayerManager();
            String layerName = layerManager.getCurrentLayer().getLayerName();
            actionBarLayers.add(layerName);
        }

        //actionbar
        actionBar = getActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);


        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        //SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.maptype_string_array, android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,actionBarLayers);

        actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId)
            {
                String ip = Integer.toString(itemPosition);
                String ii = Long.toString(itemId);
                Toast.makeText(MapTest.this, "Position: " + ip + " itemId: " + ii, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        actionBar.show();
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
            case R.id.actionBar_maptype:
                showMapTypeDialog();
                return true;
            case R.id.actionBar_measurementPoint:
                Log.d(DEBUGTAG, "Adding measurement point to the map");
                if(gpsSetup && currentLocation != null)
                {
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
                Toast.makeText(MapTest.this, "pushed add layer", Toast.LENGTH_LONG).show();
                actionBarLayers.add("newLayer");
                showNewLayerSettingsDialog();
                return true;
        }
        return(super.onOptionsItemSelected(item));
    }

    private void addMeasurementPoint(LatLng position)
    {
        //Manage the new Measurement point is model structure
        if(layerManager.getCurrentLayer() == null) //test if currentLayer is alive
        {
            layerManager.addNewLayer("New Layer", BitmapDescriptorFactory.HUE_AZURE); // if not creat a default new Layer
        }
        //Create a measurementPoint
        MeasurementPoint mp = new MeasurementPoint(layerManager.getCurrentLayer().getLayerName(), position, layerManager.getCurrentLayer().getColor());

        //Put a Marker on the map
        FragmentManager fm = getFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.main_fragment_container);
        String mpTitle = "Layer: " + layerManager.getCurrentLayer();
        ((MapCanvasFragment) frag).addMarker(position, mpTitle, "comment", layerManager.getCurrentLayer().getColor());
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
        try
        {
            ActiveLayerSettingsDialogFragment alsd = ActiveLayerSettingsDialogFragment.newInstance("Create a new layer:", "Layer", 0, 0);
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            alsd.show(fm, "NEWLAYERSETTINGS");
        }
        catch(Exception e)
        {
            Log.e(DEBUGTAG, e.toString());
        }
    }


    @Override
    public void onDialogDone(String tag, boolean cancelled, CharSequence message) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onDialogDone(String tag, boolean cancelled, String ln, int color, int lw) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

