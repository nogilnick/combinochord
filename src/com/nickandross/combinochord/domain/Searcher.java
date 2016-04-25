package com.nickandross.combinochord.domain;

import com.nickandross.combinochord.learn.FingeringRater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class performs a parallel search for guitar
 * chord fingerings on a provided guitar model and
 * hand model.
 */
public class Searcher {   //The maximum number of mutes allowed
    private final int maxMutes;
    //The minimum chord score each candidate must
    //achieve to be included in the final list
    private final double minChordScore;
    //The maximum distance to search from any
    //depressed finger
    private final double maxSearchDst;
    //The maximum number of barred fingers
    private final int maxBarre;
    //A model of the guitar being played
    private final Guitar guit;
    //A model of the player's hand
    private final HandModel hm;
    //True iff barre chords are enabled
    private final boolean enblBarre;
    //The list of results (valid only after searching)
    private List<Fingering> chordRes;
    //Used to rate fingerings with heuristic scores
    private final FingeringRater fr;

    /**
     * Searcher constructor
     *
     * @param gt            The guitar to use
     * @param handMod       The hand model to use
     * @param mmts          The maximum number of mutes allowed
     * @param mincs         The minimum chord score
     * @param barre         If barre chords are enabled
     * @param maxNumBarre   The maximum number of barres allowed
     */
    public Searcher(Guitar gt, HandModel handMod, FingeringRater fingRat, int mmts, double mincs, boolean barre, int maxNumBarre) {
        guit = gt;
        hm = handMod;
        fr = fingRat;
        maxMutes = mmts;
        minChordScore = mincs;
        maxSearchDst = hm.GetMaxSearchDist();
        enblBarre = barre;
        maxBarre = maxNumBarre;
    }

    /**
     * Function for getting an empty fingering
     *
     * @return A fingering corresponding to strumming all
     * open strings.
     */
    public Fingering GetOpen() {
        return Fingering.Make(guit, new FingerPlacement[0], 0, 0, 0, 0.0, 4, 0);
    }

    /**
     * Generates a list of Fingering objects based on the current Guitar,
     * HandModel, and the chord provided. Results are stored in a private data
     * member that can be accessed using GetChords()
     *
     * @param chord The generic chord to search for
     * @param key The key of the chord
     * @param numThreads The number of searcher threads to use
     */
    public void GenerateChords(int chord, int key, int numThreads) {
        chordRes = new ArrayList<>();
        //Handle case of empty chord
        if (chord == 0)
            return;
        //Shift the chord to the appropriate key
        chord = Constants.ChordToKey(chord, key);
        //Get a linked-list of all the fret positions
        List<FingerPlacement> fps = guit.FindPositions(chord, enblBarre);
        //Find which fret positions are useful to barre if enabled
        //Find the tonic positions
        List<FingerPlacement> tonics = FingerPlacement.FindFretPos(fps, key);
        //Find the non-open positions
        List<FingerPlacement> fPos = new ArrayList<>();
        for(FingerPlacement fp : fps)
        {   //Fingers cannot be placed on open strings so remove them
            if(fp.fp.fret > 0)
                fPos.add(fp);
        }
        SearchTask[] srchTsks = new SearchTask[tonics.size()];
        //Create a thread pool to search for fingerings
        ExecutorService xrsrv = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < tonics.size(); ++i) {    //Add tonic search sub-tasks
            srchTsks[i] = new SearchTask(chord, tonics.get(i), fPos);
            xrsrv.submit(srchTsks[i]);
        }
        xrsrv.shutdown();
        try {
            xrsrv.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {    //Main thread was interrupted while awaiting termination
            e.printStackTrace();
        }
        for (SearchTask srchTsk : srchTsks) {
            List<Fingering> tmp = srchTsk.GetChords();
            if (tmp != null)
                chordRes.addAll(tmp);
        }
    }

    /**
     * Get the list of generated chords.
     *
     * @return The list of generated chords
     */
    public List<Fingering> GetChords() {
        return chordRes;
    }

    /**
     * Given j, k, and l that are in order. Places the tonic
     * and other fret positions into an array such that it is ordered
     * @param numPos The number of (non null) positions
     * @param tonic The tonic position (not an open string)
     * @param j The j fret position (can be null)
     * @param k The k fret position (can be null)
     * @param l The l fret position (can be null)
     * @return An ordered array of the above up to 4 fret positions
     */
    private FingerPlacement[] OrderPositions(int numPos, FingerPlacement tonic, FingerPlacement j, FingerPlacement k, FingerPlacement l)
    {
        FingerPlacement[] selPos = new FingerPlacement[numPos];
        switch (numPos) {
            case 1:
                selPos[0] = tonic;
                break;
            case 2:
                if (tonic.fp.fretId < j.fp.fretId) {
                    selPos[0] = tonic;
                    selPos[1] = j;
                } else {
                    selPos[0] = j;
                    selPos[1] = tonic;
                }
                break;
            case 3:
                //Already know j < k
                if (tonic.fp.fretId < j.fp.fretId) {
                    selPos[0] = tonic;
                    selPos[1] = j;
                    selPos[2] = k;
                } else if (tonic.fp.fretId < k.fp.fretId) {
                    selPos[0] = j;
                    selPos[1] = tonic;
                    selPos[2] = k;
                } else {
                    selPos[0] = j;
                    selPos[1] = k;
                    selPos[2] = tonic;
                }
                break;
            case 4:
                //Already know j < k < l
                if (tonic.fp.fretId < j.fp.fretId) {
                    selPos[0] = tonic;
                    selPos[1] = j;
                    selPos[2] = k;
                    selPos[3] = l;
                } else if (tonic.fp.fretId < k.fp.fretId) {
                    selPos[0] = j;
                    selPos[1] = tonic;
                    selPos[2] = k;
                    selPos[3] = l;
                } else if (tonic.fp.fretId < l.fp.fretId) {
                    selPos[0] = j;
                    selPos[1] = k;
                    selPos[2] = tonic;
                    selPos[3] = l;
                } else {
                    selPos[0] = j;
                    selPos[1] = k;
                    selPos[2] = l;
                    selPos[3] = tonic;
                }
                break;
        }
        return selPos;
    }

    /**
     * Sorts the chrds data member based on Fingering score
     */
    public void SortChords() {
        if (chordRes == null)
            return;
        Collections.sort(chordRes, new Comparator<Fingering>() {
            public int compare(Fingering f1, Fingering f2) {
                //Switch f1 and f2 for descending order
                return Double.compare(f2.GetScore(), f1.GetScore());
            }

        });
    }

    /**
     * This function searches for potential fingerings.
     *
     * @param chord  The chord to search for
     * @param tonic The tonic position
     * @param notePos The list of potential non-tonic positions
     * @return The list of discovered fingerings
     */
    private List<Fingering> Search(int chord, FingerPlacement tonic, List<FingerPlacement> notePos) {
        List<Fingering> fingerings = new ArrayList<>();
        //Temporary variable for storing the number of fingers
        final int numFingers = hm.GetNumFngr();
        if (numFingers < 1)    //Using 0 fingers; done searching
            return fingerings;
        //Find the notes produced by the open strings that are in the chord
        int openNotes = guit.OpenStrings(chord, tonic.fp.note);
        //Temporary variable for storing the candidate fingering
        Fingering c;
        //tonic is the first selection
        if (tonic.fp.fret == 0) {   //Current tonic is an open string
            List<FingerPlacement> fPos = FingerPlacement.TonicFilter(notePos, tonic);
            for (int j = 0; j < fPos.size(); ++j) {   //Place first finger
                FingerPlacement curJ = fPos.get(j);
                //The current set of notes produced
                int curNoteJ = openNotes | curJ.notes;
                //The current number of barres
                int numBarreJ = (curJ.isBarre ? 1 : 0);
                //Try candidate using only 1 finger
                if(curNoteJ == chord && (c = TryCandidate(chord, tonic.fp, new FingerPlacement[] {curJ}, numBarreJ)) != null)
                    fingerings.add(c);
                if(numFingers < 2)	//Only using 1 finger; don't consider further fingerings
                    continue;
                List<FingerPlacement> fPos2 = FingerPlacement.Filter1(fPos, j, maxSearchDst, numBarreJ < maxBarre);
                for (int k = 0; k < fPos2.size(); ++k) {
                    //Place second finger
                    FingerPlacement curK = fPos2.get(k);
                    //The current number of barres
                    int numBarreK = (curK.isBarre ? 1 : 0) + numBarreJ;
                    //The current set of notes produced
                    int curNoteK = curNoteJ | curK.notes;
                    //Try candidate using 2 fingers
                    if(curNoteK == chord && (c = TryCandidate(chord, tonic.fp, new FingerPlacement[] {curJ, curK}, numBarreK)) != null)
                        fingerings.add(c);
                    if (numFingers < 3)    //Only using 2 fingers; don't consider further fingerings
                        continue;
                    List<FingerPlacement> fPos3 = FingerPlacement.Filter1(fPos2, k, maxSearchDst, numBarreK < maxBarre);
                    for (int l = 0; l < fPos3.size(); ++l) {    //Place third finger
                        FingerPlacement curL = fPos3.get(l);
                        //The current set of notes produced
                        int curNoteL = curNoteK | curL.notes;
                        //The current number of barres
                        int numBarreL = (curL.isBarre ? 1 : 0) + numBarreK;
                        //Try candidate using 3 fingers
                        if(curNoteL == chord && (c = TryCandidate(chord, tonic.fp, new FingerPlacement[] {curJ, curK, curL}, numBarreL)) != null)
                            fingerings.add(c);
                        if (numFingers < 4)    //Only using 3 finger; don't consider further fingerings
                            continue;
                        //Filter the list in place on last loop
                        for (int m = l + 1; m < fPos3.size(); ++m) {    //Place fourth finger
                            FingerPlacement curM = fPos3.get(m);
                            if(FingerPlacement.T1Test(curM, curL, maxSearchDst, numBarreL < maxBarre))
                            {   //Position is not filtered; attempt fingering
                                //The current set of notes produced
                                int curNoteM = curNoteL | curM.notes;
                                int numBarreM = numBarreL + (curM.isBarre ? 1 : 0);
                                if(curNoteM == chord && (c = TryCandidate(chord, tonic.fp, new FingerPlacement[]{curJ, curK, curL, curM}, numBarreM)) != null)
                                    fingerings.add(c);
                            }
                        }
                    }
                }
            }
        } else {   //Current tonic is not an open string
            List<FingerPlacement> fPos = FingerPlacement.TonicFilterNO(notePos, tonic, maxSearchDst);
            //The current set of notes produced
            int curNoteT = openNotes | tonic.notes;
            //The current number of barres
            int numBarreT = (tonic.isBarre ? 1 : 0);
            //Try candidate using only 1 finger
            if(curNoteT == chord && (c = TryCandidate(chord, tonic.fp, new FingerPlacement[] {tonic}, numBarreT)) != null)
                fingerings.add(c);
            if (numFingers < 2)    //Only using 1 finger; done searching
                return fingerings;
            //Using two fingers
            for (int j = 0; j < fPos.size(); ++j) {
                FingerPlacement curJ = fPos.get(j);
                //The current set of notes produced
                int curNoteJ = curNoteT | curJ.notes;
                //The current number of barres
                int numBarreJ = (curJ.isBarre ? 1 : 0) + numBarreT;
                //Try candidate using 2 fingers
                if(curNoteJ == chord && (c = TryCandidate(chord, tonic.fp, OrderPositions(2, tonic, curJ, null, null), numBarreJ)) != null)
                    fingerings.add(c);
                if (numFingers < 3)    //Only using 2 fingers; don't consider further fingerings
                    continue;
                List<FingerPlacement> fPos2 = FingerPlacement.Filter1(fPos, j, maxSearchDst, numBarreJ < maxBarre);
                for (int k = 0; k < fPos2.size(); ++k) {
                    //Place second finger
                    FingerPlacement curK = fPos2.get(k);
                    //The current set of notes produced
                    int curNoteK = curNoteJ | curK.notes;
                    //The current number of barres
                    int numBarreK = (curK.isBarre ? 1 : 0) + numBarreJ;
                    //Try candidate using 3 fingerings
                    if(curNoteK == chord && (c = TryCandidate(chord, tonic.fp, OrderPositions(3, tonic, curJ, curK, null), numBarreK)) != null)
                        fingerings.add(c);
                    if (numFingers < 4)    //Only using 3 fingers; don't consider further fingerings
                        continue;
                    //Filter the list in place for the last finger
                    for (int l = k + 1; l < fPos2.size(); ++l) {    //Place third finger
                        FingerPlacement curL = fPos2.get(l);
                        if(FingerPlacement.T1Test(curL, curK, maxSearchDst, numBarreK < maxBarre))
                        {   //Position is not filtered; attempt fingering
                            //The current set of notes produced
                            int curNoteL = curNoteK | curL.notes;
                            //Current number of barres
                            int numBarreL = (curL.isBarre ? 1 : 0) + numBarreK;
                            //Try candidate using 4 fingers
                            if(curNoteL == chord && (c = TryCandidate(chord, tonic.fp, OrderPositions(4, tonic, curJ, curK, curL), numBarreL)) != null)
                                fingerings.add(c);
                        }
                    }
                }
            }
        }
        return fingerings;
    }


    /**
     * This function attempts to create a Fingering object
     * based on the current search positions
     *
     * @param tonic         The FretPosition of the tonic (not modified)
     * @param selPos        The selected fret positions to place fingers
     */
    private Fingering TryCandidate(int chord, FretPosition tonic, FingerPlacement[] selPos, int numBarre) {
        HandModel.FAScore faScore = hm.FindBestFingering(selPos);
        //Create the fingering
        Fingering fngr = Fingering.Make(guit, selPos, chord, tonic.note, faScore.faid, faScore.score, hm.GetNumFngr(), numBarre);
        //Fngr can be null if it didn't produce the correct notes
        if(fngr == null)
            return null;
        //Give the fingering a heuristic score
        fr.Rate(fngr);
        if (fngr.GetNumMts() > maxMutes || fngr.GetScore() < minChordScore)
            return null;
        //All tests passed
        return fngr;
    }

    /**
     * Class for searching for chords in parallel
     *
     * @author Nicholas
     */
    private class SearchTask implements Runnable {
        private final int chord;
        //The discovered fingerings
        private List<Fingering> fingerings;
        //List of finger placements
        private final List<FingerPlacement> fpList;
        //The tonic position
        private final FingerPlacement fpTonic;

        /**
         * Constructor with search parameters
         *
         * @param searchChord   The chord to search for
         * @param tonic         The list of tonics to search
         * @param fps           The complete list of fret positions
         */
        public SearchTask(int searchChord, FingerPlacement tonic, List<FingerPlacement> fps)
        {
            chord = searchChord;
            fpList = fps;
            fpTonic = tonic;
        }

        /**
         * Gets the chords found by this thread
         *
         * @return The chords found by this thread
         */
        public List<Fingering> GetChords() {
            return fingerings;
        }

        /**
         * Search for chords on new thread
         */
        @Override
        public void run() {
            fingerings = Search(chord, fpTonic, fpList);
        }
    }
}

