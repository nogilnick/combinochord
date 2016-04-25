package com.nickandross.combinochord.domain;

import java.io.Serializable;

/**
 * This class represents a fret position on a guitar
 */
public class FretPosition implements Serializable {
    public static final int MUTE_STR = -1;
    public static final int UNDF_FNG = -2;
    //The string number
    public int string;
    //The fret number
    public int fret;
    //The note produced if sounded
    public int note;
    //The finger used to depress (if any)
    public int fingNum;
    //A unique ID for the fret position
    public int fretId;
    //The x and y coordinates of the fret position in 2d space
    private double x;
    private double y;

    public FretPosition() { }

    /**
     * FretPosition constructor
     *
     * @param fid The "fret id"
     * @param s   The string number
     * @param f   The fret number
     * @param x1  The x coordinate on the guitar in euclidean space
     * @param y1  The y coordinate on the guitar in euclidean space
     * @param n   The note produced
     * @param fn  The number of the finger depressing the position
     */
    public FretPosition(int fid, int s, int f, double x1, double y1, int n, int fn) {
        fretId = fid;
        string = s;
        fret = f;
        x = x1;
        y = y1;
        note = n;
        fingNum = fn;
    }

    /**
     * Returns the fret distance between two positions
     *
     * @param f1 Position one
     * @param f2 Position two
     * @return The fret distance between the two positions
     */
    public static double FretDistance(FretPosition f1, FretPosition f2) {
        return Math.sqrt((f1.x - f2.x) * (f1.x - f2.x) + (f1.y - f2.y) * (f1.y - f2.y));
    }

    /**
     * Tests if the FretPosition is muted
     *
     * @return True if the FretPosition is muted; false otherwise
     */
    public boolean IsMute() {
        return fingNum == MUTE_STR;
    }

    /**
     * Mute the current FretPosition object
     */
    public void Mute() {
        note -= fret;
        fret = 0;
        fingNum = MUTE_STR;
    }

    /**
     * Copies the values from a fret position
     * into the calling object
     * @param fp The object having the values to copy
     */
    public void Set(FretPosition fp)
    {
        string = fp.string;
        fret = fp.fret;
        note = fp.note;
        fingNum = fp.fingNum;
        fretId = fp.fretId;
        x = fp.x;
        y = fp.y;
    }

    /**
     * Function to return a string representation of the object
     */
    public String toString() {
        return "{" + String.format("%.1f", x) + ", " + String.format("%.1f", y) + ", " + string + ", " + fret + ", " + fingNum + ", " + Constants.NoteToString(note, false) + "}";
    }
}

