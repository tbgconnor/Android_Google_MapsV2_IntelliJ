package com.Square9.AndroidMapsV2Test;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

/**
 * @author KoEn
 * link: http://developer.android.com/guide/topics/ui/dialogs.html
 * Using DialogFragment to manage the dialog ensures that it correctly handles lifecycle events
 * such as when the user presses the Back button or rotates the screen.
 * A fragment that displays a dialog window, floating on top of its activity's window.
 * This fragment contains a Dialog object, which it displays as appropriate based on the fragment's state.
 */
public class ActiveLayerSettingsDialogFragment extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener
{
    private static String DEBUGTAG = "ActiveLayerSettingsDialogFragement";

    //Fragment Instances
    private String dialogTitle; // Title of the dialog
    private String layerName;
    private int colorNumber;
    private int lineWidth;

    //GUI component Instances

    //Content of GUI Components


    /*
     * Design pattern for instantiate a new dialog Fragment
     * @param String title, title of the dialog fragment
     * @param LayerSettings ls, the layer settings of the layer
     * @return ActiveLayerSettingsDialog the newly created dialog
     */
    public static ActiveLayerSettingsDialogFragment newInstance(String title, String layerName, int colorNum, int lineWidth)
    {
        ActiveLayerSettingsDialogFragment dlg = new ActiveLayerSettingsDialogFragment();
        // Arguments Bundle (initialization arguments)
        //see Apress Android 4.0 PRO p242 last alinea
        Bundle args = new Bundle();
        args.putString("dialogTitle", title);
        args.putString("layerName", layerName);
        args.putInt("colorNumber", colorNum);
        args.putInt("lineWidth", lineWidth);
        dlg.setArguments(args);
        return dlg;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        // If the activity you're being attached to has
        // not implemented the OnDialogDoneListener
        // interface, the following line will throw a
        // ClassCastException. This is the earliest you
        // can test if you have a well-behaved activity.
        try
        {
            OnDialogDoneListener test = (OnDialogDoneListener) activity;
        }
        catch (ClassCastException cce)
        {
            // Here is where we fail gracefully.
            Log.e(DEBUGTAG, "Activity is not listening");
            Log.e(DEBUGTAG, cce.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setCancelable(true);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
        // recreating the dialog
        if(savedInstanceState != null)
        {
            dialogTitle = savedInstanceState.getString("dialogTitle");
            layerName = savedInstanceState.getString("layerName");
            colorNumber =  savedInstanceState.getInt("colorNumber");
            lineWidth = savedInstanceState.getInt("lineWidth");
        }
        else
        {
            // If recreated Android will not use the newInsance() method
            // so the only reliable way to ensure that the instance variables
            // are set to their correct value is here
            dialogTitle = getArguments().getString("dialogTitle");
            layerName = getArguments().getString("layerName");
            colorNumber =  getArguments().getInt("colorNumber");
            lineWidth = getArguments().getInt("lineWidth");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // set Title
        getDialog().setTitle(dialogTitle);
        // create view by inflating layout
        View v = inflater.inflate(R.layout.dialogfragment_active_layer_settings,container);
        // get view components and set values

        // set callbacks


        return v;
    }

    public void onClick(View v)
    {
        OnDialogDoneListener act = (OnDialogDoneListener) getActivity();
        switch (v.getId())
        {

        }

    }

    //This is where you can do final tweaks to the user interface before the user sees it
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    //Now your fragment is visible to the user.
    @Override
    public void onStart()
    {
        super.onStart();
    }

    //The last callback before the user can interact with this fragment
    @Override
    public void onResume()
    {
        super.onResume();
    }

    // When the user presses the Back button while the dialog fragment is
    // displayed
    @Override
    public void onCancel(DialogInterface dialog)
    {
        super.onCancel(dialog);
    }

    // Called when dialog is dismissed via dismiss()
    // Also Called when the device changes state
    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
    }

    /*  To prevent memory problems, be careful about what you save
     *	into this bundle. Only save what you need. If you need to keep a reference to another
     *	fragment, save its tag instead of trying to save the other fragment.
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
/*        outState.putString("dialogTitle", dialogTitle);
        outState.putString("layerName", editText_LayerName.getText().toString().trim());
        outState.putInt("colorNumber", getColorNumber());
        outState.putInt("lineWidth", seekbar_LineWidth.getProgress());*/
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    // GETTERS and SETTERS for instance variables of this fragment
    public void setDialogTitle(String t) {
        dialogTitle = t;
    }

    public String getDialogTitle() {
        return dialogTitle;
    }

    public void setLineWidth(int lw) {
        lineWidth = lw;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLayerName(String n) {
        layerName = n;
    }

    public String getLayerName() {
        return layerName;
    }

    public int getColorNumber() {
        return colorNumber;
    }

    public void setColorNumber(int colorNumber) {
        this.colorNumber = colorNumber;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        this.colorNumber = pos;
    }

    public void onNothingSelected(AdapterView<?> parent) {}

}
