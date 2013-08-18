package com.Square9.AndroidMapsV2Test;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog Fragment for browsing a directory
 * @author K. Gilissen
 * @version 1.0
 * Partially based on: http://stackoverflow.com/questions/6909846/open-file-dialog-in-android
 */
public class OpenFileDialogFragment extends DialogFragment
{
    private static final String DEBUGTAG = "OpenFileDialogFragment";
    private final String EMPTYDIR = "[No Files Active Directory]";
    private TextView textViewPath;
    private ListView listViewFiles;
    private File[] files;
    private File selected;
    private Button buttonOpen;
    private Button buttonCancel;
    private IonDialogDoneListener onDialogDoneListener;

    public static OpenFileDialogFragment newInstance()
    {
        OpenFileDialogFragment dlg = new OpenFileDialogFragment();
        return dlg;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            onDialogDoneListener = (IonDialogDoneListener) activity;
        }
        catch(ClassCastException cce)
        {
            Log.d(DEBUGTAG, "Activity is not listening");
            Log.d(DEBUGTAG, cce.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().setTitle(R.string.dlgfrag_openfile_title);
        View v = inflater.inflate(R.layout.dialog_fragment_open_file, container);

        textViewPath = (TextView) v.findViewById(R.id.textView_openfile_path);
        // List view
        listViewFiles = (ListView) v.findViewById(R.id.listView_dlgfrag_openfile_folderview);
        // Only ONE filename can be selected to be opened...
        // Define the choice behavior for the view:
        // Does not trigger the event ()
        listViewFiles.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //Set event listeners:
        listViewFiles.setOnItemClickListener(onFileClick);

        buttonOpen = (Button) v.findViewById(R.id.button_dlgfrag_openfile_open);
        buttonCancel = (Button) v.findViewById(R.id.button_dlgfrag_openfile_cancel);

        buttonOpen.setOnClickListener(onButtonClickListener);
        buttonCancel.setOnClickListener(onButtonClickListener);

        populateFileList(getFilesFromDirectory());

        return v;
    }

    private View.OnClickListener onButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if(R.id.button_dlgfrag_openfile_open == v.getId())
            {
                // If no file is selected, do nothing...
                // Keep dialog alive, if the user wants to dismiss the dialog, 'cancel' should be pushed
                if (selected != null)
                {
                    onDialogDoneListener.onDialogDone(getTag(), false, selected);
                    OpenFileDialogFragment.this.dismiss();
                }
            }
            else
            {
                OpenFileDialogFragment.this.dismiss();
            }
        }
    };

    /**
     * Method to get the default app directory
     * (Same as in Class Filehandler (Method: createFile())
     * @return Absolute path to project directory {String}
     */
    private String getAbsolutePathToExternalProjectData()
    {
        String dirName = getActivity().getResources().getString(R.string.master_data_dir);
        return new File (Environment.getExternalStorageDirectory() + "/" + dirName).getAbsolutePath();
    }


    /**
     * Method to get an ArrayList of the file names (String) in the App directory
     * If the are no files in the directory the the arraylist will be empty not null!
     * @return ArrayList of file names (string)
     */
    private ArrayList<String> getFilesFromDirectory()
    {
        // Get Absolute Path
        String absolutePath = getAbsolutePathToExternalProjectData();
        // Update the textview
        textViewPath.setText("Path: " + absolutePath);
        // Create an Arraylist
        ArrayList<String> fileNameList = new ArrayList<String>();
        // Get reference to App directory
        File f = new File(absolutePath);
        // Get an array of files contained in the directory represented by this file
        files = f.listFiles();
        Log.d(DEBUGTAG, "#Files: " + Integer.toString(files.length));

        for (int i = 0; i < files.length; i++)
        {
            File file = files[i];

            if (file.isDirectory())
                fileNameList.add(file.getName() + "/");
            else
                fileNameList.add(file.getName());

        }
        // DO NOT SORT THIS ARRAY LIST!
        return fileNameList;
    }


    /**
     * Method to populate the list view
     * @param files the list of files (items)
     */
    private void populateFileList(List<String> files)
    {
        if(files.size() == 0)
        {
            // Display 1 entry to list as msg to user
            files.add(EMPTYDIR);
            // Nothing to select
            listViewFiles.setChoiceMode(ListView.CHOICE_MODE_NONE);
        }
        //TODO change list layout
        ArrayAdapter<String> filesList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1, files);
        listViewFiles.setAdapter(filesList);
    }

    private AdapterView.OnItemClickListener onFileClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            String fileNameFromAdapter = (String) parent.getItemAtPosition(position);
            if( fileNameFromAdapter.equals(files[position].getName()))
            {
                selected = files[position];
            }
            else
            {
                Toast.makeText(getActivity(), "Something went wrong, selected file name does not match referenced file", Toast.LENGTH_LONG).show();
            }
        }
    };
}