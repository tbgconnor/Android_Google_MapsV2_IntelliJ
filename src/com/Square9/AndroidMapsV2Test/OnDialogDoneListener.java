package com.Square9.AndroidMapsV2Test;

/**
 * Interface for ActiveLayerSettingsDialogFragment callbacks
 * @author K. Gilissen
 * @version 1.0
 */
public interface OnDialogDoneListener
{
    //TODO CLEAN UP THIS MESS!!
    public void onDialogDone(String tag, boolean cancelled, CharSequence message);
    public void onDialogDone(String tag, boolean cancelled, String ln, int color, int lw);

}
