package com.Square9.AndroidMapsV2Test;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

/**
 * @author KoEn
 * link: http://developer.android.com/guide/topics/ui/dialogs.html
 * Using DialogFragment to manage the dialog ensures that it correctly handles lifecycle events
 * such as when the user presses the Back button or rotates the screen.
 * A fragment that displays a dialog window, floating on top of its activity's window.
 * This fragment contains a Dialog object, which it displays as appropriate based on the fragment's state.
 */
public class ActiveLayerSettingsDialogFragment extends DialogFragment
{
    private static final String DEBUGTAG = "ActiveLayerSettingsDialogFragement";

    //Fragment Instances
    private String dialogTitle; // Title of the dialog
    private String layerName;
    private int colorNumber;
    private int lineWidth;

    //GUI component Instances
    private EditText editTextLayerName;
    private SeekBar seekBarLineWidth;
    private TextView textViewLineWidthValue;
    //Color Buttons:
    private Button cbAzure;
    private Button cbBlue;
    private Button cbCyan;
    private Button cbGreen;
    private Button cbMagenta;
    private Button cbOrange;
    private Button cbRed;
    private Button cbRose;
    private Button cbViolet;
    private Button cbYellow;
    //Line preview elements
    ImageView imageViewLineWidth;
    Bitmap linePreviewBitmap;
    Canvas linePreviewCanvas;
    Paint  linePreviewPaint;

    Button buttonApply;
    Button buttonCancel;

    /*
     * Design pattern for instantiate a new dialog Fragment
     * @param String title, title of the dialog fragment
     * @param LayerSettings ls, the layer settings of the layer
     * @return ActiveLayerSettingsDialog the newly created dialog
     */
    public static ActiveLayerSettingsDialogFragment newInstance(String title, String layerName, int colorNum, int lineWidth)
    {
        ActiveLayerSettingsDialogFragment dlg = new ActiveLayerSettingsDialogFragment();
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
        this.setCancelable(false);
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

        Log.d(DEBUGTAG, "dialog title: " + dialogTitle);
        Log.d(DEBUGTAG, "Layer Name: " + layerName);
        Log.d(DEBUGTAG, "Color Number: " + Integer.toString(colorNumber));
        Log.d(DEBUGTAG, "Line Width: " + Integer.toString(lineWidth));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // set Title
        getDialog().setTitle(dialogTitle);
        // create view by inflating layout
        View v = inflater.inflate(R.layout.dialogfragment_active_layer_settings,container);
        // get view components and set values
        editTextLayerName = (EditText) v.findViewById(R.id.editText_layer_name);
        //buttons color:
        cbAzure = (Button) v.findViewById(R.id.button_azure);
        cbBlue = (Button) v.findViewById(R.id.button_blue);
        cbCyan = (Button) v.findViewById(R.id.button_cyan);
        cbGreen = (Button) v.findViewById(R.id.button_green);
        cbMagenta = (Button) v.findViewById(R.id.button_magenta);
        cbOrange = (Button) v.findViewById(R.id.button_orange);
        cbRed = (Button) v.findViewById(R.id.button_red);
        cbRose = (Button) v.findViewById(R.id.button_rose);
        cbViolet = (Button) v.findViewById(R.id.button_violet);
        cbYellow = (Button) v.findViewById(R.id.button_yellow);
        // LineWidth views (textview / seekbar / bitmap line)
        seekBarLineWidth = (SeekBar) v.findViewById(R.id.seekBar_line_width);
        textViewLineWidthValue = (TextView) v.findViewById(R.id.textView_lineWidth_value);
        imageViewLineWidth = (ImageView) v.findViewById(R.id.ImageView_line_width);
        // set the seekbar progress, the textView value and the bitmap representation thickness + color to the line width value passed by the host activity
        initLineWidthElements();
        updateLineThickness(seekBarLineWidth, false);
        buttonApply = (Button) v.findViewById(R.id.button_apply_layer_settings);
        buttonCancel = (Button) v.findViewById(R.id.button_cancel_layer_settings);
        // set callbacks
        buttonApply.setOnClickListener(onButtonClick);
        buttonCancel.setOnClickListener(onButtonClick);
        seekBarLineWidth.setOnSeekBarChangeListener(onSeekBarChangeListener);
        //Buttons for color selection
        cbAzure.setOnClickListener(onButtonClick);
        cbBlue.setOnClickListener(onButtonClick);
        cbCyan.setOnClickListener(onButtonClick);
        cbGreen.setOnClickListener(onButtonClick);
        cbMagenta.setOnClickListener(onButtonClick);
        cbOrange.setOnClickListener(onButtonClick);
        cbRed.setOnClickListener(onButtonClick);
        cbRose.setOnClickListener(onButtonClick);
        cbViolet.setOnClickListener(onButtonClick);
        cbYellow.setOnClickListener(onButtonClick);
        return v;
    }

    // Anonymous Inner Class That Implements Interface View.OnClickListener to Process Button Click events
    private View.OnClickListener onButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            OnDialogDoneListener act = (OnDialogDoneListener) getActivity();
            switch(v.getId())
            {
                case(R.id.button_apply_layer_settings):
                    layerName = editTextLayerName.getText().toString().trim();
                    if(!layerName.equals(""))
                    {
                        String tag = ActiveLayerSettingsDialogFragment.this.getTag();
                        act.onDialogDone(tag, false, layerName, colorNumber, lineWidth); // NOT CANCELLED!
                        ActiveLayerSettingsDialogFragment.this.dismiss();
                    }
                    else
                    {
                        ActiveLayerSettingsDialogFragment.this.dismiss(); // no layer name = Cancel
                    }
                    return;
                case(R.id.button_cancel_layer_settings):
                    ActiveLayerSettingsDialogFragment.this.dismiss();
                    return;
                case(R.id.button_azure):
                    colorNumber = getActivity().getResources().getColor(R.color.azure);
                    updateLineColor();
                    return;
                case(R.id.button_blue):
                    colorNumber = getActivity().getResources().getColor(R.color.blue);
                    updateLineColor();
                    return;
                case(R.id.button_cyan):
                    colorNumber = getActivity().getResources().getColor(R.color.cyan);
                    updateLineColor();
                    return;
                case(R.id.button_green):
                    colorNumber = getActivity().getResources().getColor(R.color.green);
                    updateLineColor();
                    return;
                case(R.id.button_magenta):
                    colorNumber = getActivity().getResources().getColor(R.color.magenta);
                    updateLineColor();
                    return;
                case(R.id.button_orange):
                    colorNumber = getActivity().getResources().getColor(R.color.orange);
                    updateLineColor();
                    return;
                case(R.id.button_red):
                    colorNumber = getActivity().getResources().getColor(R.color.red);
                    updateLineColor();
                    return;
                case(R.id.button_rose):
                    colorNumber = getActivity().getResources().getColor(R.color.rose);
                    updateLineColor();
                    return;
                case(R.id.button_violet):
                    colorNumber = getActivity().getResources().getColor(R.color.violet);
                    updateLineColor();
                    return;
                case(R.id.button_yellow):
                    colorNumber = getActivity().getResources().getColor(R.color.yellow);
                    updateLineColor();
                    return;
            }
        }
    };

    // Anonymous Inner Class That Implements Interface SeekBar.OnSeekBarChangeListener to Process seekbar progress change events
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
    {
        //TODO see page 245 ANDROID™ FOR PROGRAMMERS AN APP-DRIVEN APPROACH
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
           if(fromUser)
           {
               lineWidth = progress;
           }
           updateLineThickness(seekBar, fromUser);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };
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
        outState.putString("dialogTitle", dialogTitle);
        outState.putString("layerName", editTextLayerName.getText().toString().trim());
        outState.putInt("colorNumber", colorNumber);
        outState.putInt("lineWidth",  seekBarLineWidth.getProgress());
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    /**
     * method for updating the line width elements :
     * SeekBar seekBarLineWidth;
     * TextView textViewLineWidthValue;
     * ImageView imageViewLineWidth;
     * @param seekBar the dailogfragment's seekbar
     */
    private void updateLineThickness(SeekBar seekBar, boolean fromUser)
    {
        //Erase Bitmap (with transparent paint)
        linePreviewBitmap.eraseColor(Color.TRANSPARENT);
        // if not from user (from onProgressChanged)
        if(!fromUser)
        {
            seekBar.setProgress(lineWidth); // Set the Progress of the seekbar (e.g.: at start-up)
        }
        //Change the numeric value in the text view
        textViewLineWidthValue.setText(" " + Integer.toString(seekBar.getProgress()) + " px");
        // Set Color of the paint according to the value of colorNumber!
        linePreviewPaint.setColor(colorNumber);
        // Get the stroke width ... (can be adapted by the seekbar)
        linePreviewPaint.setStrokeWidth((float) lineWidth);
        // Draw the line
        //TODO: change dimensions
        linePreviewCanvas.drawLine(162, 25, 362, 25, linePreviewPaint);
        //set the bitmap to the imageview
        imageViewLineWidth.setImageBitmap(linePreviewBitmap);
    }

    private void updateLineColor()
    {
        //Erase Bitmap (with transparent paint)
        linePreviewBitmap.eraseColor(Color.TRANSPARENT);
        // Set Color of the paint according to the value of colorNumber!
        linePreviewPaint.setColor(colorNumber);
        // Get the stroke width ...
        linePreviewPaint.setStrokeWidth((float) lineWidth);
        // Draw the line
        //TODO: change dimensions
        linePreviewCanvas.drawLine(162, 25, 362, 25, linePreviewPaint);
        //set the bitmap to the imageview
        imageViewLineWidth.setImageBitmap(linePreviewBitmap);
    }

    private void initLineWidthElements()
    {
        //TODO: Find a solution for the size of the bitmap size ...
        //Create the bitmap
        linePreviewBitmap = Bitmap.createBitmap(524, 50, Bitmap.Config.ARGB_8888);
        //Associate with the Canvas
        linePreviewCanvas = new Canvas(linePreviewBitmap);
        //Create the Paint
        linePreviewPaint = new Paint();
        linePreviewPaint.setStrokeCap(Paint.Cap.ROUND);
        //Set the bitmap to the imageView
        imageViewLineWidth.setImageBitmap(linePreviewBitmap);
    }
}
