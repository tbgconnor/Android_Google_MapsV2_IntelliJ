package com.Square9.AndroidMapsV2Test;

/**
 * Interface for Dialog Fragment callbacks
 * @author K. Gilissen
 * @version 1.0
 */
public interface OnDialogDoneListener
{
    void onDialogDone(String tag, boolean cancelled, String message);
    void onDialogDone(String tag, boolean cancelled, String ln, int color, int lw);
    void onDialogDone(String tag, boolean cancelled, int mapType);

}
