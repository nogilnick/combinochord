package ui;

import java.util.ArrayList;
import java.util.List;
import domain.Constants;
import domain.Constants.GuitTypes;
import domain.Fingering;
import domain.Guitar;
import domain.HandModel;
import domain.Searcher;
import technical.HeuristicOptimizer;

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
		Guitar git = new Guitar(Constants.standard, 12, Constants.defNutSpc[gitType], Constants.defBrgSpc[gitType], Constants.defFrstFrtWdth[gitType], Constants.defSclLen[gitType]);
		//Create the searcher object that searches for non-barre chords with
		//a max of 2 mutes, a heuristic score >= 0.6 (60%) with a max
		//search distance of 110mm.
		List<Fingering> max = new ArrayList<Fingering>();
		List<Fingering> min = new ArrayList<Fingering>();
		Searcher srchr = new Searcher(git, hm, 2, 0.5, 110.0, false);
		for(int i = 0; i < 12; ++i)
		{
			int[] chrd = Constants.ChordToKey(Constants.genericChords[7], i);
			//Search for E Major chords with 4 threads
			srchr.GenerateChords(chrd, 6);
			srchr.SortChords();
			//Retrieve the chords
			List<Fingering> fngrs = srchr.GetChords();
			max.addAll(fngrs.subList(0, 4));
			min.addAll(fngrs.subList(fngrs.size() - 5, fngrs.size()));
		}
		System.out.print("Running GA...\n");
		HeuristicOptimizer ho = new HeuristicOptimizer(min, max, 100);
		ho.RunGE();
		return;
	}
}
