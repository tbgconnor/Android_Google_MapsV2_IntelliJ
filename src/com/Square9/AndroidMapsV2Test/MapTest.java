package com.Square9.AndroidMapsV2Test;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;


public class MapTest extends Activity
{
    private final static String TAG_MAP_ACTIVITY = "MapTestAct";

    private LocationManager locationManager;
    private LocationProvider locationProvider;
    private boolean gpsSetup;

   //actionbar
    private ActionBar actionBar;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG_MAP_ACTIVITY, "on Create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Map fragment
        FragmentManager fm = getFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.main_fragment_container);
        if(frag == null)
        {
            frag = new MapCanvasFragment();
            fm.beginTransaction().add(R.id.main_fragment_container, frag).commit();
        }


        //actionbar
        actionBar = getActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.action_bar_list_maptype, android.R.layout.simple_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(mSpinnerAdapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId)
            {
                switch(itemPosition)
                {
                    case 0:

                        return true;
                    case 1:

                        return true;
                    case 2:

                        return true;
                    case 3:

                        return true;
                    case 4:

                        return true;
                    default:
                        return true;
                }
            }
        });

        actionBar.show();

    }
    @Override
    protected void onStart()
    {
        Log.i(TAG_MAP_ACTIVITY, "on Start");
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
                Log.i(TAG_MAP_ACTIVITY, "dialog failed! " + e.toString());
            }

        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG_MAP_ACTIVITY, "on Resume");
    }

    @Override
    protected void onPause()
    {
        Log.i(TAG_MAP_ACTIVITY, "on Pause");
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        Log.i(TAG_MAP_ACTIVITY, "on Destroy");
        super.onDestroy();
    }

    @Override
    protected void onStop()
    {
        Log.i(TAG_MAP_ACTIVITY, "on Stop");
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInfl = getMenuInflater();
        menuInfl.inflate(R.layout.actionbar_mapactivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.actionBar_maptype:
                //map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                Toast.makeText(this, "yip", Toast.LENGTH_LONG).show();
                return true;
            case R.id.actionBar_measurementPoint:
                Toast.makeText(this, "yap", Toast.LENGTH_LONG).show();
                //map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
        }
        return(super.onOptionsItemSelected(item));
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
            Log.d(TAG_MAP_ACTIVITY,"Error: failed setupGpsController: " + exp.toString());
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


}

