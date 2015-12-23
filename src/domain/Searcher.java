package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class performs a parallel search for guitar
 * chord fingerings on a provided guitar model and
 * hand model.
 */
public class Searcher
{   //The maximum number of mutes allowed
	private int maxMts;
    //The minimum chord score each candidate must
    //Achieve to be included in the final list
    private double minChrdScr;
    //The maximum distance to search from any
    //depressed finger
    private double maxSrchDst;
    //A model of the guitar being played
	private Guitar guit;
    //A model of the player's hand
	private HandModel hm;
    //The list of results (valid only after searching)
	private List<Fingering> chrdRes;
    //True iff barre chords are enabled
    private boolean enblBarre;

	public void DeleteDuplicates()
	{
        SortChords();
        for(int i = 0; i < chrdRes.size() - 1; ++i)
        {
            if(chrdRes.get(i).Compare(chrdRes.get(i + 1)))
                chrdRes.remove(i-- + 1);
        }
	}

	public boolean[] FindPotentialBars(FretPosition[] fp, FretPosition tonic, List<FretPosition> lfps)
	{   //Positions of the 4 fingers; guaranteed init to false via java spec
		boolean[] shldBar = new boolean[fp.length];
		//Test each finger to see if it would be beneficial to try a barre
		for(int i = 0; i < lfps.size(); ++i)
		{
            boolean skip = false;
            for(FretPosition aFp : fp)
            {
                if (lfps.get(i).str == aFp.str && lfps.get(i).frt < aFp.frt)
                {   //Test to make sure beneficial note is not already blocked by other finger
                    skip = true;
                    break;
                }
            }
            if(skip)
                continue;
            for(int j = 0; j < fp.length; ++j) {
                if(fp[j].frt == lfps.get(i).frt && fp[j].str < lfps.get(i).str)
                    shldBar[j] = true;
            }
		}
		//Test to make sure barres would not overlap
		for(int i = 0; i < shldBar.length; ++i)
		{
            if(fp[i].frt > tonic.frt && fp[i].str <= tonic.str)
                shldBar[i] = false;
			for(int j = i + 1; j < shldBar.length; ++j)
			{
                if(fp[i].frt == fp[j].frt)
                    shldBar[i] = shldBar[j] = false;
                else if(fp[i].str >= fp[j].str)
                    shldBar[j] = false;
			}
		}
		return shldBar;
	}
    
    /**
     * Generates a list of Fingering objects based on the current Guitar,
     * HandModel, and the chord provided. Results are stored in a private data
     * member that can be accessed using GetChords()
     * @param chrd The chord provided
     */
    public void GenerateChords(int[] chrd, int numThrds)
    {
        chrdRes = new LinkedList<>();
        List<FretPosition> fps = guit.FindNotePositions(chrd);
        //Find the tonic positions
        //List<List<FretPosition>> tonicAssignments = AssignTonics(FretPosition.FindFretPos(fps, chrd[0]), numThrds);
        List<FretPosition> tncs = FretPosition.FindFretPos(fps, chrd[0]);
        //Find the non-tonic positions
        List<FretPosition> fPos = new ArrayList<>();
        for(int i = 0; i < fps.size(); ++i)
        {
            if(fps.get(i).frt > 0)
                fPos.add(fps.get(i));
        }
        SearchTask[] srchTsks = new SearchTask[tncs.size()];
        //Create a thread pool to search for fingerings
        ExecutorService xrsrv = Executors.newFixedThreadPool(numThrds);
        for(int i = 0; i < tncs.size(); ++i)
        {	//Add tonic search sub-tasks
        	srchTsks[i] = new SearchTask(chrd, tncs.get(i), fPos);
            xrsrv.submit(srchTsks[i]);
        }
        xrsrv.shutdown();
        try
        {
        	xrsrv.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        }
        catch(InterruptedException e)
        {	//Main thread was interrupted while awating termination
        	e.printStackTrace();
        }
        for(int i = 0; i < srchTsks.length; ++i)
        {
        	List<Fingering> tmp = srchTsks[i].GetChords();
        	if(tmp != null)
        		chrdRes.addAll(tmp);
        }
    }

    /**
     * Get the list of generated chords
     * @return The list of generated chords
     */
	public List<Fingering> GetChords()
	{
		return chrdRes;
	}

    /**
     * Sorts the chrds data member based on Fingering score
     */
    public void SortChords()
    {
        if(chrdRes == null)
            return;
        Collections.sort(chrdRes, new Comparator<Fingering>() {
            public int compare(Fingering f1, Fingering f2) {
                //Switch f1 and f2 for descending order
                return Double.compare(f2.GetScore(), f1.GetScore());
            }

        });
    }

    /**
     * This function searches for potential fingerings
     * @param chrd The chord to search for
     * @param tonic The tonic position
     * @param fPos The list of potential non-tonic positions
     * @return The list of discovered fingerings
     */
    public List<Fingering> Search(int[] chrd, FretPosition tonic, List<FretPosition> fPos)
    {
    	List<Fingering> dscChrds = new ArrayList<>();
        //Initialize first filter to all true
        boolean[] filt = new boolean[fPos.size()];
        for(int i = 0; i < filt.length; ++i)
            filt[i] = true;
        if(hm.GetNumFngr() < 1)    //Using 0 fingers; done searching
            return dscChrds;
        //curI is the tonic
        FretPosition curI = tonic;
        if(curI.frt == 0)
        {   //Current tonic is an open string
            boolean[] filt1 = FretPosition.TonicFilter(fPos, curI, filt);
            //Using one finger
            for (int j = 0; j < fPos.size(); ++j)
            {   //Place first finger
                if(filt1[j])
                    TryCandidate(dscChrds, fPos, chrd, curI, fPos.get(j), null, null, null);
            }
            if(hm.GetNumFngr() < 2)    //Only using 1 finger; done searching
                return dscChrds;
            //Using two fingers
            for(int j = 0; j < fPos.size(); ++j) {    //Place first finger
                if(!filt1[j])
                    continue;
                FretPosition curJ = fPos.get(j);
                boolean[] filt2 = FretPosition.Filter1(fPos, curJ, maxSrchDst, filt1);
                for(int k = j + 1; k < fPos.size(); ++k) {    //Place second finger
                    if(filt2[k])
                        TryCandidate(dscChrds, fPos, chrd, curI, curJ, fPos.get(k), null, null);
                }
            }
            if(hm.GetNumFngr() < 3)    //Only using 2 fingers; done searching
                return dscChrds;
            //Using three fingers
            for(int j = 0; j < fPos.size(); ++j) {    //Place first finger
                if(!filt1[j])
                    continue;
                FretPosition curJ = fPos.get(j);
                boolean[] filt2 = FretPosition.Filter1(fPos, curJ, maxSrchDst, filt1);
                for(int k = j + 1; k < fPos.size(); ++k) {    //Place second finger
                    if(!filt2[k])
                        continue;
                    FretPosition curK = fPos.get(k);
                    boolean[] filt3 = FretPosition.Filter1(fPos, curK, maxSrchDst, filt2);
                    for(int l = k + 1; l < fPos.size(); ++l) {    //Place third finger
                        if(filt3[l])
                            TryCandidate(dscChrds, fPos, chrd, curI, curJ, curK, fPos.get(l), null);
                    }
                }
            }
            if(hm.GetNumFngr() < 4)    //Only using 3 finger; done searching
                return dscChrds;
            //Using four fingers
            for(int j = 0; j < fPos.size(); ++j) {    //Place first finger
                if(!filt1[j])
                    continue;
                FretPosition curJ = fPos.get(j);
                boolean[] filt2 = FretPosition.Filter1(fPos, curJ, maxSrchDst, filt1);
                for(int k = j + 1; k < fPos.size(); ++k) {    //Place second finger
                    if(!filt2[k])
                        continue;
                    FretPosition curK = fPos.get(k);
                    boolean[] filt3 = FretPosition.Filter1(fPos, curK, maxSrchDst, filt2);
                    for(int l = k + 1; l < fPos.size(); ++l) {    //Place third finger
                        if(!filt3[l])
                            continue;
                        FretPosition curL = fPos.get(l);
                        boolean[] filt4 = FretPosition.Filter1(fPos, curL, maxSrchDst, filt3);
                        for(int m = l + 1; m < fPos.size(); ++m) {    //Place fourth finger
                            if(filt4[m])
                                TryCandidate(dscChrds, fPos, chrd, curI, curJ, curK, curL, fPos.get(m));
                        }
                    }
                }
            }
        }
        else
        {   //Current tonic is not an open string
            boolean[] filt1 = FretPosition.Filter2(fPos, curI, maxSrchDst, filt);
            //Using one finger
            TryCandidate(dscChrds, fPos, chrd, curI, null, null, null, null);
            if(hm.GetNumFngr() < 2)    //Only using 1 finger; done searching
                return dscChrds;
            //Using two fingers
            for(int j = 0; j < fPos.size(); ++j) {
                if(filt1[j])
                    TryCandidate(dscChrds, fPos, chrd, curI, null, fPos.get(j), null, null);
            }
            if(hm.GetNumFngr() < 3)    //Only using 2 fingers; done searching
                return dscChrds;
            //Using three fingers
            for(int j = 0; j < fPos.size(); ++j) {    //Place first finger
                if(!filt1[j])
                    continue;
                FretPosition curJ = fPos.get(j);
                boolean[] filt2 = FretPosition.Filter1(fPos, curJ, maxSrchDst, filt1);
                for(int k = j + 1; k < fPos.size(); ++k) {    //Place second finger
                    if(filt2[k])
                        TryCandidate(dscChrds, fPos, chrd, curI, null, curJ, fPos.get(k), null);
                }
            }
            if(hm.GetNumFngr() < 4)    //Only using 3 fingers; done searching
                return dscChrds;
            //Using four fingers
            for (int j = 0; j < fPos.size(); ++j) {    //Place first finger
                if(!filt1[j])
                    continue;
                FretPosition curJ = fPos.get(j);
                boolean[] filt2 = FretPosition.Filter1(fPos, curJ, maxSrchDst, filt1);
                for(int k = j + 1; k < fPos.size(); ++k) {    //Place second finger
                    if(!filt2[k])
                        continue;
                    FretPosition curK = fPos.get(k);
                    boolean[] filt3 = FretPosition.Filter1(fPos, curK, maxSrchDst, filt2);
                    for (int l = k + 1; l < fPos.size(); ++l) {    //Place third finger
                        if(filt3[l])
                            TryCandidate(dscChrds, fPos, chrd, curI, null, curJ, curK, fPos.get(l));
                    }
                }
            }
        }
        return dscChrds;
    }
    
    /**
     * Searcher constructor
     * @param gt The guitar to use
     * @param handMod The hand model to use
     * @param mmts The maximum number of mutes allowed
     * @param mincs The minimum chord score
     * @param maxsd The maximum distance to search
     */
    public Searcher(Guitar gt, HandModel handMod, int mmts, double mincs, double maxsd, boolean barre)
    {
        guit = gt;
        hm = handMod;
        maxMts = mmts;
        minChrdScr = mincs;
        maxSrchDst = maxsd;
        enblBarre = barre;
    }

    /**
     * This function attempts to create a Fingering object
     * based on the current search positions
     * @param chrds The list of fingerings to append results to
     * @param lfps The entire list of FretPositions
     * @param chrd The chord
     * @param tonic The FretPosition of the tonic
     * @param i The 1st selected FretPosition
     * @param j The 2nd selected FretPosition
     * @param k The 3rd selected FretPosition
     * @param l The 4th selected FretPosition
     */
    private void TryCandidate(List<Fingering> chrds, List<FretPosition> lfps, int[] chrd, FretPosition tonic, FretPosition i, FretPosition j, FretPosition k, FretPosition l)
    {
        //Initilize the fingering array with open strings
        FretPosition[] chrdPos = new FretPosition[guit.GetNumStrings()];
        for(int n = 0; n < guit.GetNumStrings(); ++n)
            chrdPos[n] = guit.GuitarFretPos(n, 0).Clone();
        int numPos;
        if(l != null)   //All four fingers are placed
            numPos = 4;
        else if(k != null)  //3 fingers are placed
            numPos = 3;
        else if(j != null)  //2 fingers
            numPos = 2;
        else    //1 finger
            numPos = 1;
        FretPosition[] selPos = new FretPosition[numPos];
        if(tonic.frt != 0)
        {   //Tonic may be out of order; order it correctly
            switch(numPos)
            {
                case 1:
                    selPos[0] = chrdPos[tonic.str];
                    selPos[0].Set(tonic);
                    break;
                case 2:
                    if(tonic.fretId < j.fretId)
                    {
                        selPos[0] = chrdPos[tonic.str];
                        selPos[0].Set(tonic);
                        selPos[1] = chrdPos[j.str];
                        selPos[1].Set(j);
                    }
                    else
                    {
                        selPos[0] = chrdPos[j.str];
                        selPos[0].Set(j);
                        selPos[1] = chrdPos[tonic.str];
                        selPos[1].Set(tonic);
                    }
                    break;
                case 3:
                    //Already know j < k
                    if(tonic.fretId < j.fretId) {
                        selPos[0] = chrdPos[tonic.str];
                        selPos[0].Set(tonic);
                        selPos[1] = chrdPos[j.str];
                        selPos[1].Set(j);
                        selPos[2] = chrdPos[k.str];
                        selPos[2].Set(k);
                    }
                    else if(tonic.fretId < k.fretId)
                    {
                        selPos[0] = chrdPos[j.str];
                        selPos[0].Set(j);
                        selPos[1] = chrdPos[tonic.str];
                        selPos[1].Set(tonic);
                        selPos[2] = chrdPos[k.str];
                        selPos[2].Set(k);
                    }
                    else
                    {
                        selPos[0] = chrdPos[j.str];
                        selPos[0].Set(j);
                        selPos[1] = chrdPos[k.str];
                        selPos[1].Set(k);
                        selPos[2] = chrdPos[tonic.str];
                        selPos[2].Set(tonic);
                    }
                    break;
                case 4:
                    //Already know j < k < l
                    if(tonic.fretId < j.fretId) {
                        selPos[0] = chrdPos[tonic.str];
                        selPos[0].Set(tonic);
                        selPos[1] = chrdPos[j.str];
                        selPos[1].Set(j);
                        selPos[2] = chrdPos[k.str];
                        selPos[2].Set(k);
                        selPos[3] = chrdPos[l.str];
                        selPos[3].Set(l);
                    }
                    else if(tonic.fretId < k.fretId)
                    {
                        selPos[0] = chrdPos[j.str];
                        selPos[0].Set(j);
                        selPos[1] = chrdPos[tonic.str];
                        selPos[1].Set(tonic);
                        selPos[2] = chrdPos[k.str];
                        selPos[2].Set(k);
                        selPos[3] = chrdPos[l.str];
                        selPos[3].Set(l);
                    }
                    else if(tonic.fretId < l.fretId)
                    {
                        selPos[0] = chrdPos[j.str];
                        selPos[0].Set(j);
                        selPos[1] = chrdPos[k.str];
                        selPos[1].Set(k);
                        selPos[2] = chrdPos[tonic.str];
                        selPos[2].Set(tonic);
                        selPos[3] = chrdPos[l.str];
                        selPos[3].Set(l);
                    }
                    else
                    {
                        selPos[0] = chrdPos[j.str];
                        selPos[0].Set(j);
                        selPos[1] = chrdPos[k.str];
                        selPos[1].Set(k);
                        selPos[2] = chrdPos[l.str];
                        selPos[2].Set(l);
                        selPos[3] = chrdPos[tonic.str];
                        selPos[3].Set(tonic);
                    }
                    break;
            }
        }
        else {
            //Set the selected fret positions
            switch (numPos) {
                case 4:
                    selPos[3] = chrdPos[l.str];
                    selPos[3].Set(l);
                case 3:
                    selPos[2] = chrdPos[k.str];
                    selPos[2].Set(k);
                case 2:
                    selPos[1] = chrdPos[j.str];
                    selPos[1].Set(j);
                case 1:
                    selPos[0] = chrdPos[i.str];
                    selPos[0].Set(i);
            }
        }
        //Find the best chord fingering
        double fngrScr = hm.FindBestFingering(selPos);
        Fingering fngr = new Fingering(chrdPos, tonic.str, chrd, fngrScr, false);
        if(fngr.IsValid() && fngr.GetNumMts() <= maxMts && fngr.GetScore() >= minChrdScr)
            chrds.add(fngr);
        if(!enblBarre)  //If barre chords are not enabled return
            return;
        FretPosition[] barrePos = new FretPosition[guit.GetNumStrings()];
        for(int n = 0; n < guit.GetNumStrings(); ++n)
            barrePos[n] = chrdPos[n].Clone();
        //Test if any fingers were actually barred
        if(!hm.FindBarreFingering(barrePos, selPos, FindPotentialBars(selPos, tonic, lfps)))
            return;
        Fingering barreFngr = new Fingering(barrePos, tonic.str, chrd, fngrScr, true);
        if(barreFngr.IsValid() && barreFngr.GetNumMts() <= maxMts && barreFngr.GetScore() >= minChrdScr)
            chrds.add(barreFngr);
    }
    
    /**
     * Class for searching for chords in parallel
     * @author Nicholas
     *
     */
    private class SearchTask implements Runnable
    {
    	private int[] chrd;
    	private List<Fingering> fndFngr;
    	private FretPosition tnc;
    	private List<FretPosition> fPos;
    	
    	/**
    	 * Gets the chords found by this thread
    	 * @return The chords found by this thread
    	 */
    	public List<Fingering> GetChords()
    	{
    		return fndFngr;
    	}

		/**
		 * Search for chords on new thread
		 */
    	@Override
		public void run() 
		{
            fndFngr = Search(chrd, tnc, fPos);
		}
		
    	/**
    	 * Constructor with search parameters
    	 * @param srchChrd The chord to search for
    	 * @param tonic The list of tonics to search
    	 * @param fps The complete list of fret positions
    	 */
    	public SearchTask(int[] srchChrd, FretPosition tonic, List<FretPosition> fps)
    	{
    		chrd = srchChrd;
    		tnc = tonic;
    		fPos = fps;
    	}
    }
}

