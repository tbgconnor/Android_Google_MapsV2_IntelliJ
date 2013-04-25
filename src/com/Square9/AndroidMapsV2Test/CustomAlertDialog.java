package com.Square9.AndroidMapsV2Test;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class CustomAlertDialog
{
    private AlertDialog dialog;
    private String title;
    private String message;

    public CustomAlertDialog(Context context, String title, String message, DialogInterface.OnClickListener posClick)
    {
        dialog = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK).create();
        dialog.setIcon(R.drawable.icon_alerts_and_states_warning);
        // Setting Dialog Title
        this.title = title;
        dialog.setTitle(title);

        // Setting Dialog Message
        this.message = message;
        dialog.setMessage(message);

        // Setting OK Button
        dialog.setButton("OK", posClick);
    }

    public void showDialog()
    {
        // Showing Alert Message
        if(dialog != null)
        {
            dialog.show();
        }
    }

    public void changeIconToInformationIcon()
    {
        dialog.setIcon(R.drawable.ic_information);
    }

    public String getTitle()
    {
        if(title != null)
        {
            return title;
        }
        else
        {
            return "null!!!";
        }
    }

    public String getMessage()
    {
        if(message != null)
        {
            return message;
        }
        else
        {
            return "null!!!";
        }
    }

    public void setTitle(String t)
    {
        title = t;
    }

    public void setMessage(String m)
    {
        message = m;
    }

}