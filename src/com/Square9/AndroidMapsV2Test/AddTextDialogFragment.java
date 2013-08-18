package com.Square9.AndroidMapsV2Test;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Class Dialog Fragment to add text to measurement point
 * @author Koen Gilissen
 * @version 1.0
 */
public class AddTextDialogFragment extends DialogFragment
{
    private static final String DEBUGTAG = "AddTextDialogFragment";
    private String userComment;
    private IonDialogDoneListener mListener;
    private EditText editTextUserComment;
    private Button buttonSave;
    private Button buttonCancel;

    public static AddTextDialogFragment newInstance(String comment)
    {
        AddTextDialogFragment dlg = new AddTextDialogFragment();
        Bundle args = new Bundle();
        args.putString("userComment", comment);
        dlg.setArguments(args);
        Log.d(DEBUGTAG, "New Add Text Fragment Created");
        return dlg;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            Log.d(DEBUGTAG, "Activity checks out ;-)");
            mListener = (IonDialogDoneListener) activity;
        }
        catch (ClassCastException cce)
        {
            // Here is where we fail gracefully.
            Log.d(DEBUGTAG, "Activity is not listening");
            Log.d(DEBUGTAG, cce.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setCancelable(false);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
        if(savedInstanceState != null)
        {
            userComment = savedInstanceState.getString("userComment");
        }
        else
        {
            userComment = getArguments().getString("userComment");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().setTitle(R.string.dlgfrag_addtext_title);
        View v = inflater.inflate(R.layout.dialogfragment_add_text, container);
        editTextUserComment = (EditText) v.findViewById(R.id.editText_addtext_text);
        buttonSave = (Button) v.findViewById(R.id.button_addText_save);
        buttonCancel = (Button) v.findViewById(R.id.button_addText_Cancel);

        if(userComment.equals("") || userComment == null)
        {
            editTextUserComment.setHint(R.string.dlgfrag_addtext_hint);
        }
        else
        {
            editTextUserComment.setText(userComment);
        }
        buttonSave.setOnClickListener(onButtonClickListener);
        buttonCancel.setOnClickListener(onButtonClickListener);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString("userComment", userComment);
    }

    private View.OnClickListener onButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            userComment = editTextUserComment.getText().toString();
            if(v.getId() == R.id.button_addText_save)
            {
                mListener.onDialogDone(AddTextDialogFragment.this.getTag(), false, userComment);
                AddTextDialogFragment.this.dismiss();
            }
            else if( v.getId() == R.id.button_addText_Cancel)
            {
                AddTextDialogFragment.this.dismiss();
            }
        }
    };

}
