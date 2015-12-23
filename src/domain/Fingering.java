package domain;

import java.util.HashSet;
import java.util.Set;

/**
 * This class representing a guitar chord fingering. A fingering
 * contains an array of fret positions as well as additional
 * information about the chord produced, the number of fingers
 * used and additional values for the heuristic computation.
 */
public class Fingering
{	//Default heuristic weights
	private static final double[] defW = {0.05, 0.35, 0.35, 0.2, 0.05};
	public double[] weight;
	private FretPosition[] fp;
	//The chord produced
	private int[] chord;
	//The heuristic score
	private double scr;
	//The anitomical distance-based score
	private double fngrScr;
	//Number of fingers used
	private int numFngrsUsed;
	//The number of mutes in the fingering
	private int numMts;
	//The lowest sounding string
    private int lwstSndStr;
	//The maximum number of fingers allowed
    private int maxFngr;
	//True iff all notes in chord are produced and scr >= threshold
	private boolean isValid;
	//The fret number of the lowest fretted fret position
	private int minFrt;
	//The fret number of the highest fretted fret position
	private int maxFrt;
	//The index of the tonic in the fp array
	private int tonicPos;
	//The number of unisons produce by this fingering
    private int numUnison;
	//True iff the fingering has at least 1 barred finger
	private boolean isBarre;
	//The index of the finger positions in fp
	private int[] fingPos = {FretPosition.UNDF_FNG , FretPosition.UNDF_FNG , FretPosition.UNDF_FNG , FretPosition.UNDF_FNG};

	/**
	 * Tests if this Fingering object is equivalent to another Fingering object
	 * @param f1 The other Fingering object
	 * @return True if they are equivalent false otherwise
	 */
	public boolean Compare(Fingering f1)
	{
		if(f1.scr != scr || f1.numFngrsUsed != numFngrsUsed || f1.numMts != numMts || f1.lwstSndStr != lwstSndStr || f1.tonicPos != tonicPos || f1.Size() != Size())
			return false;
		for(int i = 0; i < f1.Size(); ++i)
		{
			if(f1.GetFrt(i).IsMute() && GetFrt(i).IsMute())
				continue;
			if(f1.GetFrt(i).frt != GetFrt(i).frt || f1.GetFrt(i).str != GetFrt(i).str)
				return false;
		}
		return true;
	}
	
	public Fingering Clone()
	{
		FretPosition[] fps = new FretPosition[fp.length];
		for(int i = 0; i < fp.length; ++i)
			fps[i] = fp[i].Clone();
		return new Fingering(fps, tonicPos, chord, fngrScr, isBarre);
	}

	/**
	 * Constructor for a Fingering object
	 * @param frtLst An array of FretPosition for the strings of a guitar
	 * @param tp The index of the tonic in frtLst
	 * @param chrd The chord to be produced
	 * @param scr The fingering score
	 * @param isB True if the Fingering is to be a barre chord
	 */
	public Fingering(FretPosition[] frtLst, int tp, int[] chrd, double scr, boolean isB)
	{
		isBarre = isB;
		fp = frtLst;
		chord = chrd;
		maxFngr = 4;
        fngrScr = scr;
		tonicPos = tp;
		weight = defW;
        Update();
	}


    /**
     * This function synchronizes the Fingering object's data members
     * with the current contents of the fp array
     */
	private void Update()
	{	//Note checklist; initialized to false via java spec
        boolean[] noteCl = new boolean[12];
		//Array to locate the finger positions in the fp array
		fingPos = new int[] {FretPosition.UNDF_FNG, FretPosition.UNDF_FNG, FretPosition.UNDF_FNG, FretPosition.UNDF_FNG};
        isValid = true;
        numFngrsUsed = 0;
		lwstSndStr = 0;
        numMts = 0;
        minFrt = Integer.MAX_VALUE;
        maxFrt = Integer.MIN_VALUE;
        numUnison = 0;
        //Used to count the number of unisons
        Set<Integer> noteRep = new HashSet<>();
		for(int i = fp.length - 1; i >= 0; --i)
        {   //Mute the string if it is beneath the tonic or not in the chord
            if(fp[i].note < fp[tonicPos].note || !Constants.NoteInChord(GetFrt(i).note, chord)) {
                fp[i].Mute();
                ++numMts;
            }
            else
            {   //The note will sound
                noteCl[fp[i].note % 12] = true;
                lwstSndStr = i;
                if(fp[i].frt > 0)
                {   //String is depressed by a finger
                    ++numFngrsUsed;
                    fingPos[fp[i].fingNum] = i;
                    if(minFrt > fp[i].frt)
                        minFrt = fp[i].frt;
                    if(maxFrt < fp[i].frt)
                        maxFrt = fp[i].frt;
                }
                if(noteRep.contains(fp[i].note))
                    ++numUnison;
                else
                    noteRep.add(fp[i].note);
            }
        }
        numMts -= lwstSndStr;
		for(int aChord : chord)
		{
			if (!noteCl[aChord])
			{
				isValid = false;
				break;
			}
		}
		//Final heuristic calculation
        UpdateScore();
    }
	
	/**
	 * Compute the hueristic score
	 */
	public void UpdateScore()
	{
		scr = F(weight);
	}
	
	/**
	 * The weights of the criteria by index:
	 * 0. The number of unison notes
	 * 1. The number of mutes
	 * 2. The finger score
	 * 3. The number of strings played
	 * 4. The number of fingers used
	 * @param w The weights for the above values
	 * @return The heuristic score
	 */
	public double F(double[] w)
	{
		return w[0] * (1.0 / (1.0 + numUnison)) + w[1] * (1 / ((double) (numMts + 1) * (numMts + 1))) + 
			   w[2] * fngrScr + w[3] * (((double) (fp.length - lwstSndStr)) / ((double) fp.length)) + 
			   w[4] * ((double) (maxFngr - numFngrsUsed)) / ((double) maxFngr);
	}

	public FretPosition GetFrt(int i)
	{
		return fp[i];
	}

	public int GetMaxFrt() { return maxFrt; }

	public int GetMinFrt() { return minFrt; }

    public int GetNumMts() { return numMts; }

	public double GetScore()
	{
		return scr;
	}

	public void SetW(double[] w)
	{
		weight = w;
	}
	
	public boolean IsValid() { return isValid; }

	public int Size()
	{
		return fp.length;
	}


	public String toString()
	{
		String ret = "{";
		for(FretPosition i : fp)
			ret += i.toString();
		return ret + "}: " + isBarre + ", " + numMts + ", " + numFngrsUsed  + ", " + minFrt + ", " + maxFrt + ", " + tonicPos + ", "  + numUnison + ", " + String.format("%.4f", scr);
	}
}
