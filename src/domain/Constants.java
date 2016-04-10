package domain;

import java.security.InvalidParameterException;

/**
 * This class contains default values, constant
 * values, and static helper functions.
 */
public class Constants {
    //Default guitar setup
    public static final double[] defSclLen = {620, 660, 630};
    public static final double[] defFrstFrtWdth = {38, 36.0, 34.0};
    public static final double[] defBrgSpc = {58.7375, 56, 52};
    public static final double[] defNutSpc = {44.45, 48, 46};
    public static final String[] guitTypeStrs = {"Acoustic", "Classical", "Electric"};
    public static final GuitTypes[] guitTypes = GuitTypes.values();
    //Default maximum distances between fingers
    //1-2, 1-3, 1-4, 2-3, 2-4, 3-4
    public static final double[] defMaxFR = {80.0, 95.0, 110.0, 52.0, 69.0, 47.0};
    //Default minimum distances between fingers
    //1-2, 1-3, 1-4, 2-3, 2-4, 3-4
    public static final double[] defMinFR = {5.0, 15.0, 25.0, 6.0, 12, 8.5};
    //Maximum distance between any two fingers
    public static final double maxDist = 110.0;
    //Tunings
    public static final int[] standard = {40, 45, 50, 55, 59, 64};
    public static final int[] dropD = {38, 45, 50, 55, 59, 64};
    public static final int[] baritone = {35, 40, 45, 50, 54, 59};
    public static final int[] standard7 = {35, 40, 45, 50, 55, 59, 64};
    public static final int[] standard8 = {28, 35, 40, 45, 50, 55, 59, 64};
    //Chords to be shifted a specific key
    public static final int[][] genericChords = {
            {},               //Empty chord
            {0, 4, 7, 9},      //6th chord
            {0, 2, 9},         //6th (no 5th)
            {0, 2, 4, 9},      //6/9
            {0, 4, 8},         //Augmented
            {0, 3, 6},         //Diminished
            {0, 3, 9},         //Diminished 7
            {0, 3, 6, 9},      //Diminished 7 + flat 5th
            {0, 4, 7},         //Major
            {0, 4},            //Major 3rd
            {0, 4, 11},        //Major 7
            {0, 4, 7, 11},     //Major 7 + 5th
            {0, 2, 4, 11},     //Major 9th
            {0, 2, 4, 7, 11},  //Major 9 + 5th
            {0, 2, 4, 7},      //Major Add 9
            {0, 4, 10},        //Major Dominant 7th
            {0, 4, 7, 10},     //Major Dominant 7th + 5th
            {0, 4, 6, 10},     //Major 7b5
            {0, 4, 8, 10},     //Major 7/5
            {0, 2, 4, 10},     //Major 9th
            {0, 1, 4, 10},     //Major 7b9
            {0, 3, 4, 10},     //Major 7/9
            {0, 4, 9, 10},     //Major 13th
            {0, 3, 7},         //Minor
            {0, 3, 8},         //Minor 6th
            {0, 3, 7, 8},      //Minor 6th + 5th
            {0, 2, 3, 7},      //Minor 9th
            {0, 3, 5, 7},      //Minor 11th
            {0, 3, 7, 8},      //Minor 13th
            {0, 2, 3, 7, 8},   //Minor 13th + 9th
            {0, 3, 10},        //Minor 7th
            {0, 3, 7, 10},     //Minor 7th + 5th
            {0, 3, 6, 10},     //Minor 7b5
            {0, 3, 8, 10},     //Minor 7/5
            {0, 2, 3, 10},     //Minor 9
            {0, 1, 3, 10},     //Minor 7b9
            {0, 7},            //Power chord
            {0, 5, 7},         //Sus
            {0, 2, 7}          //Sus2
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
            "6th chord",
            "6th (no 5th)",
            "6/9",
            "Augmented",
            "Diminished",
            "Diminished 7",
            "Diminished 7 + flat 5th",
            "Major",
            "Major 3rd",
            "Major 7",
            "Major 7 + 5th",
            "Major 9th",
            "Major 9 + 5th",
            "Major Add 9",
            "Major Dominant 7th",
            "Major Dominant 7th + 5th",
            "Major 7b5",
            "Major 7/5",
            "Major 9th",
            "Major 7b9",
            "Major 7/9",
            "Major 13th",
            "Minor",
            "Minor 6th",
            "Minor 6th + 5th",
            "Minor 9th",
            "Minor 11th",
            "Minor 13th",
            "Minor 13th + 9th",
            "Minor 7th",
            "Minor 7th + 5th",
            "Minor 7b5",
            "Minor 7/5",
            "Minor 9",
            "Minor 7b9",
            "Power chord",
            "Sus",
            "Sus2"};

    /**
     * Shifts a generic chord to a specific key
     *
     * @param chord The generic chord
     * @param tonic The new tonic
     * @return The semi-tones (0-11) of the new chord
     */
    public static int[] ChordToKey(int[] chord, int tonic) {
        int[] newChrd = new int[chord.length];
        for (int i = 0; i < newChrd.length; ++i)
            newChrd[i] = (chord[i] + tonic) % 12;
        return newChrd;
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
    public static boolean NoteInChord(int pitch, int[] chrd) {
        for (int aChrd : chrd) {
            if (aChrd == (pitch % 12))
                return true;
        }
        return false;
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

    //Guitar types
    public enum GuitTypes {
        Acoustic, Classical, Electric
    }

}
