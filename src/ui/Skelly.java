package ui;

import java.util.ArrayList;
import java.util.List;
import domain.Constants;
import domain.Constants.GuitTypes;
import domain.Fingering;
import domain.Guitar;
import domain.HandModel;
import domain.Searcher;

/**
 * A skeleton UI class to demonstrate the use
 * of the chord searching functionality
 *
 */
public class Skelly 
{
	public static void main(String[] args)
	{
		Skelly s = new Skelly();
		s.Test();
	}
	
	public void Test()
	{	//Create the guitar and hand model
		//Create a hand model with all fingers enabled with the default min and max finger ranges
		HandModel hm = new HandModel(HandModel.ENBL_FNGR.ALL, Constants.defMinFR, Constants.defMaxFR);
		//The guitar type
		int gitType = GuitTypes.Acoustic.ordinal();
		//Create a standard tuned guitar with 12 frets and default setup measurements
		Guitar git = new Guitar(Constants.standard, 20, Constants.defNutSpc[gitType], Constants.defBrgSpc[gitType], Constants.defFrstFrtWdth[gitType], Constants.defSclLen[gitType]);
		//Create the searcher object that searches for non-barre chords with
		//a max of 2 mutes, a heuristic score >= 0.6 (60%) with a max
		//search distance of 110mm.
		Searcher srchr = new Searcher(git, hm, 2, 0.7, 110.0, false);
		long s = System.currentTimeMillis();
		for(int i = 0; i < 12; ++i)
		{
			for(int j = 0; j < Constants.genericChords.length; ++j)
			{
				int[] chrd = Constants.ChordToKey(Constants.genericChords[j], i);
				//Search for E Major chords with 4 threads
				srchr.GenerateChords(chrd, 6);
			}
		}
		long e = System.currentTimeMillis();
		//Test Several chords
		srchr.GenerateChords(Constants.ChordToKey(Constants.genericChords[8], 4), 6);
		srchr.SortChords();
		List<Fingering> res = srchr.GetChords();
		for(Fingering f : res)
			System.out.println(f.toString());
		System.out.print("Total time: " + (e - s) + "ms\n");
		return;
	}
}
