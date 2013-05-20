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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

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
    private float colorNumber;
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
    // From http://developer.android.com/reference/com/google/android/gms/maps/model/BitmapDescriptorFactory.html#HUE_AZURE
    private final static int COLOR_AZURE = 210;
    private final static int COLOR_BLUE = 240;
    private final static int COLOR_CYAN = 180;
    private final static int COLOR_GREEN = 120;
    private final static int COLOR_MAGENTA = 300;
    private final static int COLOR_ORANGE = 30;
    private final static int COLOR_RED = 0;
    private final static int COLOR_ROSE = 330;
    private final static int COLOR_VIOLET = 270;
    private final static int COLOR_YELLOW = 60;


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
    public static ActiveLayerSettingsDialogFragment newInstance(String title, String layerName, float colorNum, int lineWidth)
    {
        ActiveLayerSettingsDialogFragment dlg = new ActiveLayerSettingsDialogFragment();
        Bundle args = new Bundle();
        args.putString("dialogTitle", title);
        args.putString("layerName", layerName);
        args.putFloat("colorNumber", colorNum);
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
            colorNumber =  savedInstanceState.getFloat("colorNumber");
            lineWidth = savedInstanceState.getInt("lineWidth");
        }
        else
        {
            // If recreated Android will not use the newInsance() method
            // so the only reliable way to ensure that the instance variables
            // are set to their correct value is here
            dialogTitle = getArguments().getString("dialogTitle");
            layerName = getArguments().getString("layerName");
            colorNumber =  getArguments().getFloat("colorNumber");
            lineWidth = getArguments().getInt("lineWidth");
        }

        Log.d(DEBUGTAG, "dialog title: " + dialogTitle);
        Log.d(DEBUGTAG, "Layer Name: " + layerName);
        Log.d(DEBUGTAG, "Color Number: " + Float.toString(colorNumber));
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
        // set the layer name as passed by the host activity
        editTextLayerName.setText(this.layerName);
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
        //TODO buttons color
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
                    String tag = ActiveLayerSettingsDialogFragment.this.getTag();
                    act.onDialogDone(tag, false, layerName, colorNumber, lineWidth); // NOT CANCELLED!
                    ActiveLayerSettingsDialogFragment.this.dismiss();
                    return;
                case(R.id.button_cancel_layer_settings):
                    ActiveLayerSettingsDialogFragment.this.dismiss();
                    return;
                case(R.id.button_azure):
                    colorNumber = (float) COLOR_AZURE;
                    updateLineColor();
                    return;
                case(R.id.button_blue):
                    colorNumber = (float) COLOR_BLUE;
                    updateLineColor();
                    return;
                case(R.id.button_cyan):
                    colorNumber = (float) COLOR_CYAN;
                    updateLineColor();
                    return;
                case(R.id.button_green):
                    colorNumber = (float) COLOR_GREEN;
                    updateLineColor();
                    return;
                case(R.id.button_magenta):
                    colorNumber = (float) COLOR_MAGENTA;
                    updateLineColor();
                    return;
                case(R.id.button_orange):
                    colorNumber = (float) COLOR_ORANGE;
                    updateLineColor();
                    return;
                case(R.id.button_red):
                    colorNumber = (float) COLOR_RED;
                    updateLineColor();
                    return;
                case(R.id.button_rose):
                    colorNumber = (float) COLOR_ROSE;
                    updateLineColor();
                    return;
                case(R.id.button_violet):
                    colorNumber = (float) COLOR_VIOLET;
                    updateLineColor();
                    return;
                case(R.id.button_yellow):
                    colorNumber = (float) COLOR_YELLOW;
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
        outState.putFloat("colorNumber", colorNumber);
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
        setColor(linePreviewPaint);
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
        setColor(linePreviewPaint);
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

    private void setColor(Paint p)
    {
        switch((int) colorNumber)
        {
            case COLOR_AZURE:
                p.setColor(Color.rgb(0, 0x7f, 0xff));
                return;
            case COLOR_BLUE:
                p.setColor(Color.BLUE);
                return;
            case COLOR_CYAN:
                p.setColor(Color.CYAN);
                return;
            case COLOR_GREEN:
                p.setColor(Color.GREEN);
                return;
            case COLOR_MAGENTA:
                p.setColor(Color.MAGENTA);
                return;
            case COLOR_ORANGE:
                p.setColor(Color.rgb(0xff, 0x86, 0x00));
                return;
            case COLOR_RED:
                p.setColor(Color.RED);
                return;
            case COLOR_ROSE:
                p.setColor(Color.rgb(0xff, 0x8c, 0xe6));
                return;
            case COLOR_VIOLET:
                p.setColor(Color.rgb(0x8f, 0x00, 0xff));
                return;
            case COLOR_YELLOW:
                p.setColor(Color.YELLOW);
                return;
            default:
                p.setColor(Color.RED);
                return;
        }

    }
}
