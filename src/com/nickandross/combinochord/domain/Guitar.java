package com.nickandross.combinochord.domain;

import java.util.LinkedList;

/**
 * This class represents a guitar.
 */
public class Guitar {    //All measurements are assumed to be in mm
    //The width of the nut
    private final double nutWidth;
    //The width of the bridge
    private final double bridgeWidth;
    //The number of frets on the guitar
    private final int numFrets;
    //The number of strings on the guitar
    private final int numStrings;
    //The tuning of the strings as integer semi-tone numbers
    private final int[] tuning;
    //The width of the first fret
    private final double firstFretWidth;
    //The scale length of the guitar
    private final double scaleLen;
    //All fret positions on the fret board
    private FretPosition[] fretboard;

    /**
     * Constructor specifying addition guitar setup info
     *
     * @param tun Array of notes representing guitar tuning from low to high
     * @param nf  The number of frets of the guitar
     * @param nw  The width of the nut
     * @param bw  The width of the bridge
     * @param ffw The width of the first fret
     * @param sl  The scale length of the guitar
     */
    public Guitar(int[] tun, int nf, double nw, double bw, double ffw, double sl) {
        tuning = tun;
        numStrings = tun.length;
        numFrets = nf;
        nutWidth = nw;
        bridgeWidth = bw;
        firstFretWidth = ffw;
        scaleLen = sl;
        CreateFretboard();
    }

    /**
     * This function creates a model of the positions of the fret positions in Euclidean space
     */
    private void CreateFretboard() {
        double nutOffset = (bridgeWidth - nutWidth) / 2;
        double nutStrWidth = nutWidth / numStrings;
        double brgStrWidth = bridgeWidth / numStrings;
        fretboard = new FretPosition[(numFrets + 1) * numStrings];
        for (int i = 0; i < (numFrets + 1) * numStrings; ++i) {
            double tmp = scaleLen - Constants.FretToDistance(firstFretWidth, 0, i / numStrings);
            fretboard[i] = new FretPosition(i, i % numStrings, i / numStrings, tmp, ((nutOffset + (i % numStrings) * nutStrWidth) - ((i % numStrings) * brgStrWidth)) / scaleLen * tmp + (i % numStrings) * brgStrWidth, tuning[i % numStrings] + (i / numStrings), FretPosition.UNDF_FNG);
        }

    }

    /**
     * Finds all possible finger placements of the given notes
     *
     * @param notes The BMS representing the chord
     * @param barreEnabled  True if barre chords are enabled
     * @return A list of fretpositions whose notes are in nts
     */
    public LinkedList<FingerPlacement> FindPositions(int notes, boolean barreEnabled)
    {
        LinkedList<FingerPlacement> lfp = new LinkedList<>();
        int curNotes = 0;
        int curFret = -1;
        boolean isBarre;
        for(int i = fretboard.length - 1; i >= 0; --i)
        {   //If the fret position is not in the chord; skip it
            if(Constants.NoteNotInChord(fretboard[i].note, notes))
                continue;
            if(curFret != fretboard[i].fret || curFret == 0 || !barreEnabled)
            {   //This note cannot be included in a barre with the previous
                isBarre = false;
                curFret = fretboard[i].fret;
                curNotes = (1 << (fretboard[i].note % 12));
            }
            else
            {   //This note can be included in a barre with the previous one
                curNotes |= (1 << (fretboard[i].note % 12));
                isBarre = true;
            }
            if(isBarre) //Add the non-barred version
                lfp.addFirst(new FingerPlacement(fretboard[i], 1 << (fretboard[i].note % 12), false));
            //Add the barred/non-barred fingering position
            lfp.addFirst(new FingerPlacement(fretboard[i], curNotes, isBarre));
        }
        return lfp;
    }

    /**
     * Finds the FretPosition object at a specific fret and string
     *
     * @param strNum The string number
     * @param frtNum The fret number
     * @return The FretPosition object
     */
    public FretPosition GuitarFretPos(int strNum, int frtNum) {
        return fretboard[frtNum * this.numStrings + strNum];
    }

    /**
     * Getter function for numStrings
     *
     * @return numStrings
     */
    public int GetNumStrings() {
        return numStrings;
    }


    /**
     * Function to find the notes produced in the chord
     * provided
     *
     * @return The notes produced by playing an open (with mutes)
     */
    public int OpenStrings(int chord, int tonic) {
        int notes = 0;
        for(int i = 0; i < numStrings; ++i) {
            if(fretboard[i].note >= tonic)
                notes |= (1 << (fretboard[i].note % 12));
        }
        //By muting bad strings, only keep notes in the chord
        return notes & chord;
    }

}
