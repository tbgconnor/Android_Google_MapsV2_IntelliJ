package com.Square9.AndroidMapsV2Test;

import java.io.File;

/**
 * Interface for Dialog Fragment callbacks
 * @author K. Gilissen
 * @version 1.0
 */
public interface IonDialogDoneListener
{
    void onDialogDone(String tag, boolean cancelled, String message);
    void onDialogDone(String tag, boolean cancelled, String ln, int color, int lw);
    void onDialogDone(String tag, boolean cancelled, int mapType);
    void onDialogDone(String tag, boolean cancelled, File selectedFile);

}
