package com.Square9.AndroidMapsV2Test;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;


/**
 *  DialogFragment sub-class for changing the map type through a dialog
 *  Host activity/fragment should implement the 'MapTypeDialogListener' Interface to receive a callback when the OK button is pushed
 *  @version 1.0
 *  @author  K. Gilissen
 */
public class MapTypeDialogFragment extends DialogFragment
{
    private final static String DEBUGTAG = "MapTypeDialogFragment";
    private int selectedMapType;
    private int currentMapType;
    private OnDialogDoneListener mListener;

    public static MapTypeDialogFragment newInstance(int currentMapType)
    {
        Log.d(DEBUGTAG, "MapTypeDialogFragment newInstance...");
        MapTypeDialogFragment dlg = new MapTypeDialogFragment();

        //create bundle
        Bundle args = new Bundle();
        //put data in bundle
        args.putInt("currentMapType", currentMapType);
        //push set data
        dlg.setArguments(args);
        return dlg;
    }

    @Override
    public void onAttach(Activity activity)
    {
        Log.d(DEBUGTAG, "MapTypeDialogFragment onAttach...");
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try
        {
            // Instantiate the MapTypeDialogListener so we can send events to the host
            mListener = (OnDialogDoneListener) activity;
            Log.d(DEBUGTAG, "mListener instantiated...");
        }
        catch(Exception e)
        {
            // Here is where we fail gracefully.
            Log.d(DEBUGTAG, "activity did NOT implement Interface ... ");
        }
    }

    /*
    *  The system calls this when creating the fragment.
    *  Within your implementation,
    *  you should initialize essential components of the fragment
    * that you want to retain when the fragment is paused or stopped, then resumed.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(DEBUGTAG, "MapTypeDialogFragment onCreate...");
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
        {
           currentMapType = savedInstanceState.getInt("currentMapType");

        }
        else
        {
            // If recreated Android will not use the newInsance() method
            // so the only reliable way to ensure that the instance variables
            // are set to their correct value is here
            // getArguments(): Returns the arguments supplied when the fragment was instantiated, if any.
            currentMapType = getArguments().getInt("currentMapType");
        }
        // Set instance variable to default value
        // 1 = map type normal
        selectedMapType = 1;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Log.d(DEBUGTAG, "MapTypeDialogFragment onCreateDialog...");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
        builder.setTitle("Select a Map Type");
        builder.setSingleChoiceItems(R.array.maptype_string_array, currentMapType, onSingleChoiceClick);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                mListener.onDialogDone(getTag(), false, selectedMapType);
            }
        });
        return builder.create();
    }

    /*  To prevent memory problems, be careful about what you save
    *	into this bundle. Only save what you need. If you need to keep a reference to another
    *	fragment, save its tag instead of trying to save the other fragment.
    */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.d(DEBUGTAG, "MapTypeDialogFragment onSaveInstanceState...");
        super.onSaveInstanceState(outState);
        outState.putInt("currentMapType", currentMapType);

    }

    private DialogInterface.OnClickListener onSingleChoiceClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            String mapType = Integer.toString(which);
            Log.d(DEBUGTAG, "Map Type Selected = " + mapType);
            selectedMapType = which;
        }
    };

    public int getSelectedMapType()
    {
        return selectedMapType;
    }
}

