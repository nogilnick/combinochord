package com.nickandross.combinochord.domain;

import java.security.InvalidParameterException;

/**
 * This class contains default values, constant
 * values, and static helper functions.
 */
public class Constants {
    //Default guitar setup
    public static final double[] defSclLen = {620, 660, 630};
    public static final double[] defFret1Size = {38, 36.0, 34.0};
    public static final double[] defBridgeWidth = {58.7375, 56, 52};
    public static final double[] defNutWidth = {44.45, 48, 46};
    public static final String[] guitTypeStrs = {"Acoustic", "Classical", "Electric"};
    //Guitar types
    public enum GuitTypes { Acoustic, Classical, Electric }
    public static final GuitTypes[] guitTypes = GuitTypes.values();
    //Default maximum distances between fingers
    //1-2, 1-3, 1-4, 2-3, 2-4, 3-4
    public static final double[] defMaxFR = {72.0, 93.0, 100.0, 45.0, 70.0, 45.0};
    //Default minimum distances between fingers
    //1-2, 1-3, 1-4, 2-3, 2-4, 3-4
    public static final double[] defMinFR = {5.0, 10.0, 20.0, 5.0, 10.0, 5.0};
    //Tunings
    public static final int[] standard = {40, 45, 50, 55, 59, 64};
    public static final int[] dropD = {38, 45, 50, 55, 59, 64};
    public static final int[] baritone = {35, 40, 45, 50, 54, 59};
    public static final int[] standard7 = {35, 40, 45, 50, 55, 59, 64};
    public static final int[] standard8 = {28, 35, 40, 45, 50, 55, 59, 64};
    //12-bit bit-mapped sets representing generic chords
    //To be shifted to a specific key
    //Bit 0 : C
    //Bit 1 : C#/Db
    //...
    //Bit 11 : B
    public static final int[] genericChords = {
            0b000000000000,	//Empty chord
            0b001010010001,	//6th chord
            0b001000000101,	//6th (no 5th)
            0b001000010101,	//6/9
            0b000100010001,	//Augmented
            0b000001001001,	//Diminished
            0b001000001001,	//Diminished 7
            0b001001001001,	//Diminished 7 + flat 5th
            0b000010010001,	//Major
            0b000000010001,	//Major 3rd
            0b100000010001,	//Major 7
            0b100010010001,	//Major 7 + 5th
            0b100000010101,	//Major 9th
            0b100010010101,	//Major 9 + 5th
            0b000010010101,	//Major Add 9
            0b010000010001,	//Major Dominant 7th
            0b010010010001,	//Major Dominant 7th + 5th
            0b010001010001,	//Major 7b5
            0b010100010001,	//Major 7/5
            0b010000010101,	//Major 9th
            0b010000010011,	//Major 7b9
            0b010000011001,	//Major 7/9
            0b011000010001,	//Major 13th
            0b000010001001,	//Minor
            0b000100001001,	//Minor 6th
            0b000110001001,	//Minor 6th + 5th
            0b000010001101,	//Minor 9th
            0b000010101001,	//Minor 11th
            0b000110001001,	//Minor 13th
            0b000110001101,	//Minor 13th + 9th
            0b010000001001,	//Minor 7th
            0b010010001001,	//Minor 7th + 5th
            0b010001001001,	//Minor 7b5
            0b010100001001,	//Minor 7/5
            0b010000001101,	//Minor 9
            0b010000001011,	//Minor 7b9
            0b000010000001,	//Power chord
            0b000010100001,	//Sus
            0b000010000101	//Sus2
    };
    
    //Strings corresponding to the different keys in western music
    //Indexed by their semi-tone number - 1
    public static final String[] KEYS = {
            "-",
            "C",
            "C#/Db",
            "D",
            "D#/Eb",
            "E",
            "F",
            "F#/Gb",
            "G",
            "G#/Ab",
            "A",
            "A#/Bb",
            "B"};
    
    //Chords names ordered as their interval
    //representation above
    public static final String[] CHRD = {
            "-",
            "6th",
            "6th (no 5th)",
            "6/9",
            "Aug",
            "Dim",
            "Dim 7",
            "Dim 7 + b5",
            "Maj",
            "Maj 3rd",
            "Maj 7",
            "Maj 7 + 5th",
            "Maj 9th",
            "Maj 9 + 5th",
            "Maj Add 9",
            "Maj Dom 7th",
            "Maj Dom 7th + 5th",
            "Maj 7b5",
            "Maj 7/5",
            "Maj 9th",
            "Maj 7b9",
            "Maj 7/9",
            "Maj 13th",
            "Min",
            "Min 6th",
            "Min 6th + 5th",
            "Min 9th",
            "Min 11th",
            "Min 13th",
            "Min 13th + 9th",
            "Min 7th",
            "Min 7th + 5th",
            "Min 7b5",
            "Min 7/5",
            "Min 9",
            "Min 7b9",
            "5th",
            "Sus",
            "Sus2"};

    /**
     * Takes a note bit-mapped set and returns
     * a string representation of it
     * @param noteBMS The not bit-mapped set
     * @return A string representation of noteBMS
     */
    public static String BMSToNotes(int noteBMS)
    {
        int c = 0;
        String ret = "{ ";
        while(noteBMS > 0)
        {
            if((noteBMS & 1) != 0)
                ret += NoteToString(c + 12, false) + " ";
            noteBMS >>= 1;
            ++c;
        }
        return ret + "}";
    }

    /**
     * Shifts a 12-bit chord BMS to a key (performing
     * rotation as needed)
     * @param chrd The chord BMS
     * @param key The key to shift to
     * @return The shifted chord
     */
    public static int ChordToKey(int chrd, int key)
    {	//Perform bit rotation (0xFFF is 12 bits turned on)
    	return ((chrd << key) & 0xFFF) | (chrd >> (12 - key));
    }

    /**
     * This function computes the actual distance between the m-th and n-th fret
     *
     * @param a Is the width of the first fret
     * @param m Is the starting fret
     * @param n Is the ending fret
     * @return The distance between the m-th and n-th fret
     */
    public static double FretToDistance(double a, int m, int n) {
        return -18.876616839465076 * a * (Math.exp(-0.057762265046662105 * n) - Math.exp(-0.057762265046662105 * m));
    }

    /**
     * Test if a note belong in a chord
     *
     * @param pitch The note
     * @param chrd  The chord
     * @return True if pitch belongs in chrd
     */
    public static boolean NoteNotInChord(int pitch, int chrd) {
    	return ((1 << (pitch % 12)) & chrd) == 0;
    }

    /**
     * Function to get the unique integer for a given note
     *
     * @param pitch  The pitch (integer mod 12)
     * @param octave The octave number
     * @return A unique integer denoting the note
     */
    private static int NoteToInt(int pitch, int octave) {
        return (octave + 1) * 12 + pitch;
    }

    /**
     * Used to convert from semi-tone number to a string
     *
     * @param note   The note between 0-11 starting from C
     * @param isShrp True if sharps are being used
     * @return A string corresponding to the note
     */
    public static String NoteToString(int note, boolean isShrp) {
        String ret = "";
        int temp = note % 12;
        if (temp == 0)
            ret = "C";
        else if (temp == 1) {
            if (isShrp)
                ret = "C#";
            else
                ret = "Db";
        } else if (temp == 2)
            ret = "D";
        else if (temp == 3) {
            if (isShrp)
                ret = "D#";
            else
                ret = "Eb";
        } else if (temp == 4)
            ret = "E";
        else if (temp == 5)
            ret = "F";
        else if (temp == 6) {
            if (isShrp)
                ret = "F#";
            else
                ret = "Gb";
        } else if (temp == 7)
            ret = "G";
        else if (temp == 8) {
            if (isShrp)
                ret = "G#";
            else
                ret = "Ab";
        } else if (temp == 9)
            ret = "A";
        else if (temp == 10) {
            if (isShrp)
                ret = "A#";
            else
                ret = "Bb";
        } else if (temp == 11)
            ret = "B";
        return ret + ((note / 12) - 1);
    }

    public static int ParseNoteString(String str) {
        if (str.length() <= 1)
            throw new InvalidParameterException("Invalid format!");
        int note, oct;
        if (str.charAt(1) == '#' || str.charAt(1) == 'b') {
            note = StringToNote(str.substring(0, 2));
            oct = Integer.parseInt(str.substring(2));
        } else {
            note = StringToNote(str.substring(0, 1));
            oct = Integer.parseInt(str.substring(1));
        }
        if (note == -1)
            throw new InvalidParameterException("Invalid note name!");
        return NoteToInt(note, oct);

    }

    /**
     * Converts a string into a integer between 0-11
     *
     * @param str The string
     * @return The semitone represented as an integer between 0-11
     * -1 is returned on failure
     */
    private static int StringToNote(String str) {
        int ret;
        if (str.length() == 1)
            str = str.toUpperCase();
        else if (str.length() == 2)
            str = new String(new char[]{Character.toUpperCase(str.charAt(0)), str.charAt(1)});
        else
            return -1;
        if (str.compareTo("C") == 0)
            ret = 0;
        else if (str.compareTo("C#") == 0)
            ret = 1;
        else if (str.compareTo("Db") == 0)
            ret = 1;
        else if (str.compareTo("D") == 0)
            ret = 2;
        else if (str.compareTo("D#") == 0)
            ret = 3;
        else if (str.compareTo("Eb") == 0)
            ret = 3;
        else if (str.compareTo("E") == 0)
            ret = 4;
        else if (str.compareTo("F") == 0)
            ret = 5;
        else if (str.compareTo("F#") == 0)
            ret = 6;
        else if (str.compareTo("Gb") == 0)
            ret = 6;
        else if (str.compareTo("G") == 0)
            ret = 7;
        else if (str.compareTo("G#") == 0)
            ret = 8;
        else if (str.compareTo("Ab") == 0)
            ret = 8;
        else if (str.compareTo("A") == 0)
            ret = 9;
        else if (str.compareTo("A#") == 0)
            ret = 10;
        else if (str.compareTo("Bb") == 0)
            ret = 10;
        else if (str.compareTo("B") == 0)
            ret = 11;
        else
            ret = -1;
        return ret;
    }
}
