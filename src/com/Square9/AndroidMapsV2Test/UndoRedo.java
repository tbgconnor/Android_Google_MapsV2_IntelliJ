package com.Square9.AndroidMapsV2Test;

import android.util.Log;

import java.util.Stack;

/**
 * This class is the undo - redo buffer
 * @version 1.0
 * @author K. Gilissen
 */
public class UndoRedo
{
    private static final String DEBUGTAG = "UndoRedo";
    private Stack<Icommand> undoStack;
    private Stack<Icommand> redoStack;

    public UndoRedo()
    {
        undoStack = new Stack<Icommand>();
        redoStack = new Stack<Icommand>();
    }

    /**
     * Method to undo an Icommand
     * @return true if command is undone | false if there is nothing to undo
     */
    public boolean undo()
    {
        Log.d(DEBUGTAG, "Undo: size of undo buffer: " + undoStack.size() + " size of redo buffer:  " + redoStack.size());
        //First check whether UndoStack is empty or not. If empty, then return otherwise proceed.
        if(undoStack.empty())
        {
            return false;
        }
        else
        {
            // Pop the command to undo
            Icommand cmdToUndo = undoStack.pop();
            //Command Delete can NOT be REDONE
            if(cmdToUndo instanceof CommandDeleteMeasurementItems)
            {
                //Swallow the push of this command to the redo stack.... njam njam
                //
            }
            else
            {
                // Push this command to RedoStack.
                redoStack.push(cmdToUndo);
            }
            // Then invoke the Unexecute method of the Icommand object.
            cmdToUndo.unexecute();
            return true;
        }
    }

    /**
     * Method to redo the last undone command
     * @return true if command was redone | false if nothing to be redone
     */
    public boolean redo()
    {
        Log.d(DEBUGTAG, "Redo: size of undo buffer: " + undoStack.size() + " size of redo buffer:  " + redoStack.size());
        // check whether there is something to redo
        if(redoStack.empty())
        {
            return false; //Nothing to redo ...
        }
        else
        {
            // get the command to redo
            Icommand cmdToRedo = redoStack.pop();
            // Put the command on top of the undo stack
            undoStack.push(cmdToRedo);
            // invoke the execute method of the Icommand instance
            cmdToRedo.execute();
            return true;
        }
    }

    /**
     * Method to add an Icommand to the undo stack (on top of)
     * @param cmd the Icommand to add
     */
    public void addToUndoBuffer(Icommand cmd)
    {
        //add cmd to undo buffer
        undoStack.push(cmd);
        //clear redo buffer
        redoStack.clear();

    }

    /**
     * Method to clear the buffers
     */
    public void clearBuffers()
    {
        undoStack.clear();
        redoStack.clear();
    }
}
