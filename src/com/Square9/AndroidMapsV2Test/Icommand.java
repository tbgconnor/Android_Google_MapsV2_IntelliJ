package com.Square9.AndroidMapsV2Test;

/**
 * Icommand interface to define command class methods
 * @author K. Gilissen
 * @version 1.0
 * adapted from http://www.codeproject.com/Articles/33384/Multilevel-Undo-and-Redo-Implementation-in-C-Part
 */
public interface Icommand
{
    void execute();
    void unexecute();

}
