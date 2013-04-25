package com.Square9.AndroidMapsV2Test;

import android.app.Activity;
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
import android.view.WindowManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;




public class MapTest extends Activity
{
    private final static String TAG_MAP_ACTIVITY = "Map Activity";
    private static final LatLng DEFAULTLOCATION = new LatLng(50.879668, 5.309296);  //50.879668,5.309296
    private GoogleMap map;
    private LocationManager locationManager;
    private LocationProvider locationProvider;
    private boolean gpsSetup;
    private Marker currentPositionMarker;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        currentPositionMarker = map.addMarker(new MarkerOptions().position(DEFAULTLOCATION).title("DEFAULT LOCATION"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULTLOCATION, 21));
    }
    @Override
    protected void onStart()
    {
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
                Log.d(TAG_MAP_ACTIVITY, "dialog failed! " + e.toString());
            }

        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
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
            LatLng currentLocation = new LatLng( location.getLatitude(),  location.getLongitude());
            currentPositionMarker.setPosition(currentLocation);
            String pos = currentLocation.toString();
            currentPositionMarker.setTitle("Current Position:");
            currentPositionMarker.setSnippet(pos);
            map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
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

