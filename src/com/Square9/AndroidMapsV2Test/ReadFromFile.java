package com.Square9.AndroidMapsV2Test;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Async Task to read from file
 * @author Koen Gilissen
 * @version 1.0
 */
public class ReadFromFile extends AsyncTask<FileHandler, Integer, String>
{
    private ReadFromFileEvent listener;
    private Context actCtx;


    public interface ReadFromFileEvent
    {
        void onReadFromFileCompleted(String result);
    }

    public ReadFromFile(Context ctx, ReadFromFileEvent listener)
    {
        actCtx = ctx;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onPostExecute(String s)
    {
        listener.onReadFromFileCompleted(s);
    }

    @Override
    protected String doInBackground(FileHandler... params)
    {
        //get FileHandler Object
        FileHandler fh = params[0];
        //Check if public external storage is read-writeable and reachable
        if(!FileHandler.checkMediaAvailability())
            return null;
        //Check if file needs to be created:
        if(fh.getFile() == null)
            if(!fh.createFile())
                return null;
        // Read the file
        String result;
        try
        {
            result = fh.getStringFromFile(fh.getAbsoluteFile());
        }
        catch(Exception e)
        {
            result = null;
        }
        return result;
    }
}