package com.Square9.AndroidMapsV2Test;
import android.content.Context;
import android.os.Environment;

import java.io.*;

/**
 *  FileHandler Class
 *  @author Koen Gilissen
 *  @version v1.0
 */
public class FileHandler
{
    private final static String DEBUGTAG = "FileHandler";
    private String fileName;
    private String absoluteFilePath;
    private boolean newDir;
    private Context context;
    private File file;

    /**
     * Public Constructor creating an instance of FileHandler object
     * @param fileName String user defined name of the file WITHOUT the file extension
     * @param ctx activity context to reach resources sub-directory and fileExt are defined in Resources string
     */
    public FileHandler(String fileName, Context ctx)
    {
        this.fileName = fileName;
        absoluteFilePath = "not available";
        newDir = false;
        context = ctx;
        file = null;
    }


    /**
     * Method to check if the 'external' storage is reachable
     * from: http://developer.android.com/guide/topics/data/data-storage.html
     * @return true if external storage is available and Writable otherwise false
     */
    public static boolean checkMediaAvailability()
    {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWritable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWritable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWritable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWritable = false;
        }
        return mExternalStorageAvailable && mExternalStorageWritable;
    }

    /**
     * method to create a file in PUBLIC external storage based on fileName
     * // http://developer.android.com/training/basics/data-storage/files.html#WriteExternalStorage
     * @return the file created
     */
    public boolean createFile()
    {
        boolean fileSuccessfulCreated = true;
        String dirName = context.getString(R.string.master_data_dir);
        File dir = new File (Environment.getExternalStorageDirectory() + "/" + dirName);
        absoluteFilePath = dir.getAbsolutePath();

        if(dir.mkdirs())
        {
            newDir = true;
        }
        try
        {
            file = new File(dir, fileName + context.getString(R.string.file_extension));
        }
        catch(Exception e)
        {
            fileSuccessfulCreated = false;
        }
        return fileSuccessfulCreated;
    }

    /**
     * Method to write a string to a file
     * @param text the text to write
     * @return true successful write false error occurred
     */
    public boolean write(String text)
    {
        boolean succesfullWrite = true;
        try
        {
            FileOutputStream f = new FileOutputStream(file, true); //APPEND Mode
            PrintWriter pw = new PrintWriter(f);
            pw.print(text);
            pw.flush();
            pw.close();
            f.close();
        }
        catch(Exception e)
        {
            succesfullWrite = false;
        }
        return succesfullWrite;
    }

    /**
     * Method to write a line
     * @param text the text to write
     * @return  true successful write false error occurred
     */
    public boolean writeLine(String text)
    {
        boolean succesfullWrite = true;
        try
        {
            FileOutputStream f = new FileOutputStream(file, true);  //APPEND Mode
            PrintWriter pw = new PrintWriter(f);
            pw.println(text);
            pw.flush();
            pw.close();
            f.close();
        }
        catch(Exception e)
        {
            succesfullWrite = false;
        }
        return succesfullWrite;
    }

    /**
     * Private method to convert a (Character) InputStream into a String
     * @param is character oriented input stream
     * @return string representation of the input stream (Buffered IO)
     * @throws Exception
     */
    private String convertStreamToString(InputStream is) throws Exception
    {
        // Using buffer IO
        // BufferedReader creates a buffered CHARACTER stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        //Stringbuilder is more efficient then stringBuffer
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    /**
     * Method to convert a file to a string
     * @param filePath the absolute filepath  e.g.: /storage/sdcard0/measurement_data/TestFileIo.txt
     * @return string representation of the content of the file
     * @throws Exception
     */
    public String getStringFromFile(String filePath) throws Exception
    {
        File fl;
        FileInputStream fin = null;
        String ret;
        try
        {
            fl = new File(filePath);
            fin = new FileInputStream(fl);
            ret = convertStreamToString(fin);
        }
        finally
        {
            if(fin != null)
            {
                fin.close();
            }
        }
        return ret;
    }


    /**
     * Method to get absolute file path e.g.: '/storage/sdcard0/measurement_data'
     * @return absolute file path (String)
     */
    public String getAbsoluteFilePath()
    {
        return absoluteFilePath;
    }

    /**
     * Method to get absolute file absPath/name.ext e.g.: /storage/sdcard0/measurement_data/measurement_data/TestFileIo.txt
     * @return string representation of absPath/name.ext
     */
    public String getAbsoluteFile()
    {
        return file.toString();
    }

    /**
     * Method to get file name e.g.: TestFileIo
     * @return  string representation of filename
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Method to get the directory state
     * @return  true if the directory was created, false on failure or if the directory already existed.
     */
    public boolean isNewDir() {
        return newDir;
    }

    public File getFile()
    {
        return file;
    }
}