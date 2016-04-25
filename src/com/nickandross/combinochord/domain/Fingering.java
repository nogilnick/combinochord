package com.nickandross.combinochord.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This class representing a guitar chord fingering. A fingering
 * contains an array of fret positions as well as additional
 * information about the chord produced, the number of fingers
 * used and additional values for the heuristic computation.
 */
public class Fingering implements Serializable {
    //All the fret positions in the fingering
    private final FretPosition[] fp;
    //The fret positions depressed with fingers
    private final FretPosition[] sp;
    //The chord that is actually produced
    private int chord;
    //The tonic note of the chord
    private final int tonic;
    //The heuristic score
    private double scr;
    //The number of mutes in the fingering
    private int numMutes;
    //The fret number of the lowest fretted fret position
    private int minFrt;
    //The fret number of the highest fretted fret position
    private int maxFrt;
    //Vector of various scores
    public static final int NUM_SCORES = 8;
    private double[] scores;
    //User applied rating to chord
    private double rating;

    /**
     * Tests if this Fingering object is equivalent to another Fingering object
     *
     * @param f1 The other Fingering object (not null)
     * @return True if they are equivalent false otherwise
     */
    public boolean Compare(Fingering f1) {
        if (f1.scr != scr || f1.chord != chord || f1.tonic != tonic || f1.scores[0] != scores[0] ||
            f1.scores[1] != scores[1] || f1.scores[2] != scores[2] || f1.scores[3] != scores[3] ||
            f1.scores[4] != scores[4] || f1.scores[5] != scores[5] || f1.scores[6] != scores[6] ||
            f1.scores[7] != scores[7])
            return false;
        for (int i = 0; i < f1.Size(); ++i) {
            FretPosition f1Fp = f1.GetFrt(i);
            FretPosition fp = GetFrt(i);
            if (f1Fp.IsMute() && fp.IsMute())
                continue;
            if (f1Fp.fret != fp.fret || f1Fp.string != fp.string || f1Fp.fingNum != fp.fingNum)
                return false;
        }
        return true;
    }

    /**
     * Update component scores and compute the final score
     */
    private void ComputeScores(int numUnison, double fngrScr, int lwstSndStr, int maxFngr, int numChordNotes, int numBarres)
    {
        scores = new double[NUM_SCORES];
        //Unison score
        scores[0] = 1.0 / (1.0 + numUnison);
        //Number of mutes score
        scores[1] = 1.0 / ((numMutes + 1.0) * (numMutes + 1.0));
        //Anatomical distance-based scores
        scores[2] = fngrScr;
        //Number of strings used
        scores[3] = (((double) (fp.length - lwstSndStr)) / ((double) fp.length));
        //Number of fingers used
        scores[4] = ((double) (maxFngr - sp.length)) / ((double) maxFngr);
        //Number of frets covered
        scores[5] = 1.0 / (maxFrt - minFrt + 1);
        //Number of unique notes in the chord
        scores[6] = 1.0 - 1.0 / numChordNotes;
        //Number of barred fingers
        scores[7] = 1.0 / (1.0 + numBarres);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass() && Compare((Fingering) o);
    }

    /**
     * Constructor for a Fingering object
     *
     * @param frtLst An array of FretPosition for the strings of a guitar
     * @param tonicNote     The index of the tonic in frtLst
     */
    private Fingering(FretPosition[] frtLst, FretPosition[] selPos, int tonicNote) {
        fp = frtLst;
        sp = selPos;
        tonic = tonicNote;
        chord = 0;
        numMutes = maxFrt;
        minFrt = Integer.MAX_VALUE;
    }

    public double[] GetCategoryScores() {
        return scores;
    }

    /**
     * Get the i-th fret position in this fingering
     * @param i The index
     * @return The i-th fret position
     */
    public FretPosition GetFrt(int i) {
        return fp[i];
    }

    public int GetMaxFrt() {
        return maxFrt;
    }

    public int GetMinFrt() {
        return minFrt;
    }

    public int GetNumMts() {
        return numMutes;
    }

    public double GetRating() { return rating; }

    public double GetScore() {
        return scr;
    }

    @Override
    public int hashCode()
    {   //Top digits represent the chord
        int hc = chord;
        int mult = 10;
        for(FretPosition f : fp)
        {   //One digit for each additional fret position
            hc *= mult;
            hc += (f.fingNum + f.string + f.fret) % 10;
        }
        return hc;
    }

    /**
     * Create a fingering given a guitar, the positions of the fingers,
     * the chord to produce, and the location of the tonic
     * @param guit          The guitar to produce a fingering for
     * @param selPos        The positions of the fingers
     * @param chord         The chord to produce
     * @param tonicNote     The tonic fret position
     * @param faid          The id of the fingering numbers
     * @param faScore       The anatomical finger assignment score
     * @param maxNumFngr    The maximum number of fingers available
     * @param numBarre      The number of barred fingers
     * @return              The fingering
     */
    public static Fingering Make(Guitar guit, FingerPlacement[] selPos, int chord, int tonicNote, int faid, double faScore, int maxNumFngr, int numBarre)
    {
        int numStrs = guit.GetNumStrings();
        Fingering f = new Fingering(new FretPosition[numStrs], new FretPosition[selPos.length], tonicNote);
        //Used to count the number of unisons (same note in the same octave)
        Set<Integer> noteRep = new HashSet<>();
        //Used to count the number of unisons (same note in the same octave)
        //Identifies the current barred fret number (0 if open)
        int openFret = 0;
        int barreFinger = FretPosition.UNDF_FNG;
        int curPlacement;
        //Positions of the fingers; and minimum/maximum fret
        int finger1 = -1;
        int finger2 = -1;
        int finger3 = -1;
        int finger4 = -1;
        f.minFrt = Integer.MAX_VALUE;
        f.maxFrt = 0;
        switch(selPos.length) {
            case 4:
                finger4 = selPos[3].fp.string;
                f.minFrt = (f.minFrt > selPos[3].fp.fret ? selPos[3].fp.fret : f.minFrt);
                f.maxFrt = (f.maxFrt < selPos[3].fp.fret ? selPos[3].fp.fret : f.maxFrt);
            case 3:
                finger3 = selPos[2].fp.string;
                f.minFrt = (f.minFrt > selPos[2].fp.fret ? selPos[2].fp.fret : f.minFrt);
                f.maxFrt = (f.maxFrt < selPos[2].fp.fret ? selPos[2].fp.fret : f.maxFrt);
            case 2:
                finger2 = selPos[1].fp.string;
                f.minFrt = (f.minFrt > selPos[1].fp.fret ? selPos[1].fp.fret : f.minFrt);
                f.maxFrt = (f.maxFrt < selPos[1].fp.fret ? selPos[1].fp.fret : f.maxFrt);
            case 1:
                finger1 = selPos[0].fp.string;
                f.minFrt = (f.minFrt > selPos[0].fp.fret ? selPos[0].fp.fret : f.minFrt);
                f.maxFrt = (f.maxFrt < selPos[0].fp.fret ? selPos[0].fp.fret : f.maxFrt);
                break;
            default:
                //Minimum fret was never set
                f.minFrt = 0;
        }
        //Data variables for computing the heuristic score
        int numUnison = 0;
        int lwstSndStr = -1;
        for(int i = 0; i < numStrs; ++i)
        {   //Create a new fret position to prevent modification of guitar's fret positions
            f.fp[i] = new FretPosition();
            curPlacement = -1;
            //Determine if this string is depressed by a finger and find which one
            if(finger1 == i)
                curPlacement = 0;
            else if(finger2 == i)
                curPlacement = 1;
            else if(finger3 == i)
                curPlacement = 2;
            else if(finger4 == i)
                curPlacement = 3;
            else {  //Default to lowest sounding open fret (open string or barred position)
                f.fp[i].Set(guit.GuitarFretPos(i, openFret));
                f.fp[i].fingNum = barreFinger;
            }
            //Test if the current string is depressed with a finger
            if(curPlacement != -1) {
                //Keep track of the current selected finger
                f.sp[curPlacement] = f.fp[i];
                //Update the new fret position with correct position information
                f.fp[i].Set(selPos[curPlacement].fp);
                //Set the finger number for the finger assignment from the hand model
                f.fp[i].fingNum = HandModel.FNGR_NUM[faid][curPlacement];
                //Check for barre placement and update the barre fret and finger number if needed
                if (selPos[curPlacement].isBarre && f.fp[i].fret > openFret) {
                    openFret = f.fp[i].fret;
                    barreFinger = f.fp[i].fingNum;
                }
            }
            //Check if the note is in the chord; if it isn't mute the string
            if (f.fp[i].note < tonicNote || Constants.NoteNotInChord(f.fp[i].note, chord)) {
                f.fp[i].Mute();
                ++f.numMutes;
            } else {   //The string will sound
                f.chord |= (1 << (f.fp[i].note % 12));
                if(lwstSndStr == -1)
                    lwstSndStr = i; //Hasn't been set; found the lowest string
                //Keep track of the current notes and their repetitions
                if(!noteRep.add(f.fp[i].note))
                    ++numUnison;
            }
        }
        //Chord does not produce the correct notes
        if(f.chord != chord)
            return null;
        //Strings beneath the lowest sounding string don't count as mutes
        f.numMutes -= lwstSndStr;
        //Make the category scores
        f.ComputeScores(numUnison, faScore, lwstSndStr, maxNumFngr, noteRep.size(), numBarre);
        return f;
    }

    public int Size() {
        return fp.length;
    }

    public void SetRating(double val) { rating = val; }

    public void SetScore(double val) { scr = val; }

    public String toString() {
        String ret = "{";
        for (FretPosition i : fp)
            ret += i.toString();
        return ret + "}: " + ", " + numMutes + ", " + sp.length + ", " + minFrt + ", " + maxFrt + ", " + tonic + ", " + ", " + String.format("%.4f", scr);
    }
}
