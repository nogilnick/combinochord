package domain;

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
    private final FretPosition[] fp;
    //The chord produced
    private final int chord;
    //The anatomical distance-based score
    private final double fngrScr;
    //The maximum number of fingers allowed
    private final int maxFngr;
    //The index of the tonic in the fp array
    private final int tonicPos;
    //True iff the fingering has at least 1 barred finger
    private final boolean isBarre;
    //Array to locate the finger positions in the fp array
    private final int[] fingPos = new int[]{FretPosition.UNDF_FNG, FretPosition.UNDF_FNG, FretPosition.UNDF_FNG, FretPosition.UNDF_FNG};
    //The heuristic score
    private double scr;
    //Number of fingers used
    private int numFngrsUsed;
    //The number of mutes in the fingering
    private int numMts;
    //The lowest sounding string
    private int lwstSndStr;
    //True iff all notes in chord are produced and scr >= threshold
    private boolean isValid;
    //The fret number of the lowest fretted fret position
    private int minFrt;
    //The fret number of the highest fretted fret position
    private int maxFrt;
    //The number of unisons produce by this fingering
    private int numUnison;
    //Vector of various scores
    public static final int NUM_SCORES = 5;
    private double[] scores;
    //Weight vector
    public static final double[] w = {0.05, 0.35, 0.35, 0.2, 0.05};
    //User applied rating to chord
    private double rating;

    /**
     * Constructor for a Fingering object
     *
     * @param frtLst An array of FretPosition for the strings of a guitar
     * @param tp     The index of the tonic in frtLst
     * @param chrd   The chord to be produced
     * @param scr    The fingering score
     * @param isB    True if the Fingering is to be a barre chord
     */
    public Fingering(FretPosition[] frtLst, int tp, int chrd, double scr, boolean isB) {
        isBarre = isB;
        fp = frtLst;
        chord = chrd;
        maxFngr = 4;
        fngrScr = scr;
        tonicPos = tp;
        Update();
    }

    /**
     * Tests if this Fingering object is equivalent to another Fingering object
     *
     * @param f1 The other Fingering object
     * @return True if they are equivalent false otherwise
     */
    public boolean Compare(Fingering f1) {
        if (f1.scr != scr || f1.numFngrsUsed != numFngrsUsed || f1.numMts != numMts || f1.lwstSndStr != lwstSndStr || f1.tonicPos != tonicPos || f1.Size() != Size())
            return false;
        for (int i = 0; i < f1.Size(); ++i) {
            if (f1.GetFrt(i).IsMute() && GetFrt(i).IsMute())
                continue;
            if (f1.GetFrt(i).frt != GetFrt(i).frt || f1.GetFrt(i).str != GetFrt(i).str)
                return false;
        }
        return true;
    }

    /**
     * Update component scores and compute the final score
     */
    private void ComputeScore()
    {
        scores = new double[NUM_SCORES];
        //Unison score
        scores[0] = (1.0 / (1.0 + numUnison));
        //Number of mutes score
        scores[1] = (1 / ((double) (numMts + 1) * (numMts + 1)));
        //Anatomical distance-based scores
        scores[2] = fngrScr;
        //Number of strings used
        scores[3] = (((double) (fp.length - lwstSndStr)) / ((double) fp.length));
        //Number of fingers used
        scores[4] = ((double) (maxFngr - numFngrsUsed)) / ((double) maxFngr);
        //Compute the final score (weights * category scores)
        scr = 0.0;
        for(int i = 0; i < NUM_SCORES; ++i)
            scr += scores[i] * w[i];
    }

    public double[] GetCategoryScores() { return scores; }

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
        return numMts;
    }

    public double GetRating() { return rating; }

    public double GetScore() {
        return scr;
    }

    public boolean IsValid() {
        return isValid;
    }

    public int Size() {
        return fp.length;
    }

    public void SetRating(double val) { rating = val; }

    public void SetW(double[] newW)
    {
        System.arraycopy(newW, 0, w, 0, w.length);
    }

    public String toString() {
        String ret = "{";
        for (FretPosition i : fp)
            ret += i.toString();
        return ret + "}: " + isBarre + ", " + numMts + ", " + numFngrsUsed + ", " + minFrt + ", " + maxFrt + ", " + tonicPos + ", " + numUnison + ", " + String.format("%.4f", scr);
    }

    /**
     * This function synchronizes the Fingering object's data members
     * with the current contents of the fp array
     */
    private void Update() {    //Note checklist (bit-mapped set); initialized to all off
        int noteCl = 0;
        isValid = true;
        numFngrsUsed = 0;
        lwstSndStr = 0;
        numMts = 0;
        minFrt = Integer.MAX_VALUE;
        maxFrt = 0;
        numUnison = 0;
        //Used to count the number of unisons
        Set<Integer> noteRep = new HashSet<>();
        for (int i = fp.length - 1; i >= 0; --i) {   //Mute the string if it is beneath the tonic or not in the chord
            if (fp[i].note < fp[tonicPos].note || !Constants.NoteInChord(GetFrt(i).note, chord)) {
                fp[i].Mute();
                ++numMts;
            } else {   //The note will sound
                noteCl |= (1 << (fp[i].note % 12));
                lwstSndStr = i;
                if (fp[i].frt > 0) {   //String is depressed by a finger
                    ++numFngrsUsed;
                    fingPos[fp[i].fingNum] = i;
                    if (minFrt > fp[i].frt)
                        minFrt = fp[i].frt;
                    if (maxFrt < fp[i].frt)
                        maxFrt = fp[i].frt;
                }
                if (noteRep.contains(fp[i].note))
                    ++numUnison;
                else
                    noteRep.add(fp[i].note);
            }
        }
        //Check if minFrt was never set
        if (minFrt == Integer.MAX_VALUE)
            minFrt = 0;
        numMts -= lwstSndStr;
        //Check if all notes are sounding
        if(noteCl != chord)
        	isValid = false;
        //Final heuristic calculation
        ComputeScore();
    }

}
