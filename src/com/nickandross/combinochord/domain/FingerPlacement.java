package com.nickandross.combinochord.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nicholas on 4/21/16.
 * This class contains information about a finger
 * placed on the fretboard.
 */
public class FingerPlacement
{   //The fret position depressed by the ball of the finger
    final FretPosition fp;
    //All the notes sounded (including barred notes)
    final int notes;
    //Boolean value if this is a barre placement
    final boolean isBarre;

    /**
     * Filters for distance around a given point fp
     *
     * @param lst       The list of FingerPlacements to filter
     * @param fpIndex   The index of the position to filter based on
     * @param maxDist   The max search distance
     * @param canBarre  If fingers can still be barred
     * @return An updated filter
     */
    public static List<FingerPlacement> Filter1(List<FingerPlacement> lst, int fpIndex, double maxDist, boolean canBarre) {
        List<FingerPlacement> fPos = new ArrayList<>(lst.size() - fpIndex - 1);
        FingerPlacement fp = lst.get(fpIndex);
        for (int i = fpIndex + 1; i < lst.size(); ++i) //Scan only later positions for pts <= maxDist
        {	//Iterate over the list and add only those fret positions meeting the requirements
            FingerPlacement curFp = lst.get(i);
            if (T1Test(curFp, fp, maxDist, canBarre))
                fPos.add(curFp);
        }
        return fPos;
    }

    /**
     *
     * @param fretPos           The fret position depressed by the ball of the finger
     * @param notesSounded      All the notes sounded (including barred notes)
     * @param barre             Boolean value if this is a barre placement
     */
    public FingerPlacement(FretPosition fretPos, int notesSounded, boolean barre)
    {
        fp = fretPos;
        notes = notesSounded;
        isBarre = barre;
    }

    /**
     * Find all FingerPlacements in a list that produce a given note
     *
     * @param fpl The list of FingerPlacements
     * @param n   The note
     * @return A list of FingerPlacements that produce the given note
     */
    public static List<FingerPlacement> FindFretPos(List<FingerPlacement> fpl, int n) {
        List<FingerPlacement> matchPos = new ArrayList<>();
        for (FingerPlacement fp : fpl) {
            if ((fp.fp.note % 12) == n)
                matchPos.add(fp);
        }
        return matchPos;
    }

    /**
     * This function removes all positions beneath the current tonic
     *
     * @param lst The list to filter
     * @param tonicFp  The position of the tonic
     * @return An updated list of fret positions
     */
    public static List<FingerPlacement> TonicFilter(List<FingerPlacement> lst, FingerPlacement tonicFp) {
        List<FingerPlacement> fPos = new ArrayList<>(lst.size());
        for (FingerPlacement curFp : lst)
        {	//Iterate over the list and add only those fret positions meeting the requirements
            if (curFp.fp.note >= tonicFp.fp.note && curFp.fp.string != tonicFp.fp.string &&
                (!curFp.isBarre || curFp.fp.string > tonicFp.fp.string || curFp.fp.fret < tonicFp.fp.fret))
                fPos.add(curFp);
        }
        return fPos;
    }

    /**
     * Filters for a given non-open tonic note fp
     *
     * @param lst     The list of FingerPlacements to filter
     * @param fp      The position to filter based on
     * @param maxDist The max search distance
     * @return An updated filter
     */
    public static List<FingerPlacement> TonicFilterNO(List<FingerPlacement> lst, FingerPlacement fp, double maxDist) {
        List<FingerPlacement> fPos = new ArrayList<>(lst.size());
        for (FingerPlacement curFp : lst)
        {	//Iterate over the list and only add valid elements;
            if (curFp.fp.note >= fp.fp.note && T1Test(curFp, fp, maxDist, true))
                fPos.add(curFp);
        }
        return fPos;
    }

    @Override
    public String toString()
    {
        return "{" + Constants.BMSToNotes(notes) + ", " + fp.string + ", " + fp.fret + ", " + isBarre + "}";
    }

    /**
     * Filter1 test for determining if a fret position curFp
     * is valid given that fret position fp has already been
     * selected
     * @param curFp     The fret position to test
     * @param fp        The already selected fret position
     * @param maxDist   The maximum stretching distance
     * @param canBarre  True if barres are allowed; false otherwise
     * @return          True if the fret position is valid; false otherwise
     */
    public static boolean T1Test(FingerPlacement curFp, FingerPlacement fp, double maxDist, boolean canBarre)
    {
        return (fp.fp.string != curFp.fp.string) && //The position isn't on the same string
                (!curFp.isBarre || (canBarre && curFp.fp.fret != fp.fp.fret && ((curFp.fp.string > fp.fp.string) || (curFp.fp.fret < fp.fp.fret)))) &&
                (!fp.isBarre || (fp.fp.fret < curFp.fp.fret) || (fp.fp.string > curFp.fp.string)) && //The position isn't covered by fp (and fp is a barre)
                (FretPosition.FretDistance(curFp.fp, fp.fp) <= maxDist);  //The position is within reach
    }
}
