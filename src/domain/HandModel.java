package domain;

/**
 * This class maintains information about the
 * flexibility of the player's hand. The minimum
 * and maximum comfortable ranges for pairs of
 * fingers are maintained.
 */
public class HandModel
{
	//Enumerated values for the fingers bit-mapped set
	//For example F123 means enable fingers 1 2 and 3 (disable 4)
	public enum ENBL_FNGR {NONE, F1, F2, F12, F3, F13, F23, F123, F4, F14, F24, F124, F34, F134, F234, ALL};
	//Look-up table for maximum distances between finger i and j
	private double[][] maxTbl;
	//Look-up table for minimum distances between finger i and j
	private double[][] minTbl;
	//Array to determine if a finger is enabled
	private boolean[] fngrEnbl;
	//Keeps track of the number of available fingers
	private int numFngr;

	/**
	 * Converts from an ENBLFNGR enumerated value to a boolean
	 * array for convenience
	 * @param fngrVal Is the enumerated value
	 * @return The boolean array
	 */
	public boolean[] BMSToBoolArr(ENBL_FNGR fngrVal)
	{
		int bms = fngrVal.ordinal();
		boolean[] arr = new boolean[4];
		numFngr = 0;
		if((bms & 1) == 1)
		{
			arr[0] = true;
			++numFngr;
		}
		if(((bms >> 1) & 1) == 1)
		{
			arr[1] = true;
			++numFngr;
		}
		if(((bms >> 2) & 1) == 1)
		{
			arr[2] = true;
			++numFngr;
		}
		if(((bms >> 3) & 1) == 1)
		{
			arr[3] = true;
			++numFngr;
		}
		return arr;
	}

	public boolean FindBarreFingering(FretPosition[] fp, FretPosition[] sp, boolean[] barArr)
	{	//TODO score fingering
		boolean isBarre = false;
		for(int i = 0; i < barArr.length; ++i)
		{
			if(barArr[i])
			{
				for(int j = sp[i].str + 1; j < fp.length; ++j)
				{
					if(fp[j].frt == 0)
					{
						isBarre = true;
						fp[j].fingNum = sp[i].fingNum;
						fp[j].note += sp[i].frt;
						fp[j].frt = sp[i].frt;
					}
					else if(fp[j].frt <= sp[i].frt)
					{	//New barred finger covers previously barred finger
						isBarre = true;
						fp[j].fingNum = sp[i].fingNum;
						fp[j].note += sp[i].frt - fp[j].frt;
						fp[j].frt = sp[i].frt;
					}
				}

			}
		}
		return isBarre;
	}

	/**
	 * Determines which finger numbers to use given that finger
	 * positions are already chosen
	 * @param fp The selected fret positions
	 */
	public double FindBestFingering(FretPosition[] fp)
	{
		double fngrScr = 0.0;
		if(fp.length == 1)
		{
			//Use first available finger
			if(fngrEnbl[0])
				fp[0].fingNum = 0;
			else if(fngrEnbl[1])
				fp[0].fingNum = 1;
			else if(fngrEnbl[2])
				fp[0].fingNum = 2;
			else if(fngrEnbl[3])
				fp[0].fingNum = 3;
			fngrScr = RateFingering(fp);
			//Locate fingers below will ensure fingPos has correct values
		}
		else if(fp.length == 2)
		{	//Try all 6 possible ways of choosing the 2 fingers without crossing
			int best = -1;
			double bestScore = Double.NEGATIVE_INFINITY;
			//1-2
			if(fngrEnbl[0] && fngrEnbl[1]) {
				fp[0].fingNum = 0;
				fp[1].fingNum = 1;
				fngrScr = RateFingering(fp);
				if (fngrScr > bestScore) {
					bestScore = fngrScr;
					best = 1;
				}
			}
			//1-3
			if(fngrEnbl[0] && fngrEnbl[2]) {
				fp[0].fingNum = 0;
				fp[1].fingNum = 2;
				fngrScr = RateFingering(fp);
				if (fngrScr > bestScore) {
					bestScore = fngrScr;
					best = 2;
				}
			}
			//1-4
			if(fngrEnbl[0] && fngrEnbl[3]) {
				fp[0].fingNum = 0;
				fp[1].fingNum = 3;
				fngrScr = RateFingering(fp);
				if (fngrScr > bestScore) {
					bestScore = fngrScr;
					best = 3;
				}
			}
			//2-3
			if(fngrEnbl[1] && fngrEnbl[2]) {
				fp[0].fingNum = 1;
				fp[1].fingNum = 2;
				fngrScr = RateFingering(fp);
				if (fngrScr > bestScore) {
					bestScore = fngrScr;
					best = 4;
				}
			}
			//2-4
			if(fngrEnbl[1] && fngrEnbl[3]) {
				fp[0].fingNum = 1;
				fp[1].fingNum = 3;
				fngrScr = RateFingering(fp);
				if (fngrScr > bestScore) {
					bestScore = fngrScr;
					best = 5;
				}
			}
			//3-4
			if(fngrEnbl[2] && fngrEnbl[3]) {
				fp[0].fingNum = 2;
				fp[1].fingNum = 3;
				fngrScr = RateFingering(fp);
				if (fngrScr > bestScore) {
					bestScore = fngrScr;
					best = 6;
				}
			}
			//Locate fingers below will ensure fingPos has correct values
			switch(best)
			{
				case 1: //1-2
					fp[0].fingNum = 0;
					fp[1].fingNum = 1;
					break;
				case 2: //1-3
					fp[0].fingNum = 0;
					fp[1].fingNum = 2;
					break;
				case 3: //1-4
					fp[0].fingNum = 0;
					fp[1].fingNum = 3;
					break;
				case 4: //2-3
					fp[0].fingNum = 1;
					fp[1].fingNum = 2;
					break;
				case 5: //2-4
					fp[0].fingNum = 1;
					fp[1].fingNum = 3;
					break;
				case 6: //3-4
					//The fingering is already correct from above
					break;
			}
			fngrScr = bestScore;
		}
		else if(fp.length == 3)
		{	//Try all 4 ways of arranging the fingers without crossing
			int best = -1;
			double bestScore = Double.NEGATIVE_INFINITY;
			//1-2-3
			if(fngrEnbl[0] && fngrEnbl[1] && fngrEnbl[2]) {
				fp[0].fingNum = 0;
				fp[1].fingNum = 1;
				fp[2].fingNum = 2;
				bestScore = RateFingering(fp);
				if(fngrScr > bestScore) {
					bestScore = fngrScr;
					best = 1;
				}
			}
			//1-2-4
			if(fngrEnbl[0] && fngrEnbl[1] && fngrEnbl[3]) {
				fp[0].fingNum = 0;
				fp[1].fingNum = 1;
				fp[2].fingNum = 3;
				fngrScr = RateFingering(fp);
				if(fngrScr > bestScore) {
					bestScore = fngrScr;
					best = 2;
				}
			}
			//1-3-4
			if(fngrEnbl[0] && fngrEnbl[2] && fngrEnbl[3]) {
				fp[0].fingNum = 0;
				fp[1].fingNum = 2;
				fp[2].fingNum = 3;
				fngrScr = RateFingering(fp);
				if(fngrScr > bestScore) {
					bestScore = fngrScr;
					best = 3;
				}
			}
			//2-3-4
			if(fngrEnbl[1] && fngrEnbl[2] && fngrEnbl[3]) {
				fp[0].fingNum = 1;
				fp[1].fingNum = 2;
				fp[2].fingNum = 3;
				fngrScr = RateFingering(fp);
				if(fngrScr > bestScore) {
					bestScore = fngrScr;
					best = 4;
				}
			}
			//Locate fingers below will ensure fingPos has correct values
			switch(best)
			{
				case 1:     //1-2-3
					fp[0].fingNum = 0;
					fp[1].fingNum = 1;
					fp[2].fingNum = 2;
					break;
				case 2:     //1-2-4
					fp[0].fingNum = 0;
					fp[1].fingNum = 1;
					fp[2].fingNum = 3;
					break;
				case 3:     //1-3-4
					fp[0].fingNum = 0;
					fp[1].fingNum = 2;
					fp[2].fingNum = 3;
					break;
				case 4:
					//The fingering is already correct from above
					break;
			}
			fngrScr = bestScore;
		}
		else if(fp.length == 4)
		{	//Use the only possible combination that doesn't cross fingers
			fp[0].fingNum = 0;
			fp[1].fingNum = 1;
			fp[2].fingNum = 2;
			fp[3].fingNum = 3;
			//Locate fingers below will ensure fingPos has correct values
			fngrScr = RateFingering(fp);
		}
		return fngrScr;
	}

	/**
	 * Gets the number of fingers being used
	 * @return NumFngr
	 */
	public int GetNumFngr()
	{
		return numFngr;
	}

	/**
	 * Create a hand model using max/min distances between fingers allowed.
	 * Arrays are ordered as: 1-2, 1-3, 1-4, 2-3, 2-4, 3-4
	 * @param fngrBms A bit-mapped set of the enabled fingers
	 * @param minFR An array of minimum distances between fingers in milimeters
	 * @param maxFR An array of max distances between finger in milimeters
	 */
	public HandModel(ENBL_FNGR fngrBms, double[] minFR, double[] maxFR)
	{
		fngrEnbl = BMSToBoolArr(fngrBms);
		maxTbl = new double[4][4];
		minTbl = new double[4][4];
		//Set diagonals to 0
		maxTbl[0][0] = maxTbl[1][1] = maxTbl[2][2] = maxTbl[3][3] = 0.0;
		minTbl[0][0] = minTbl[1][1] = minTbl[2][2] = minTbl[3][3] = 0.0;
		//Fill in maximum entries
		maxTbl[1][0] = maxTbl[0][1] = maxFR[0];
		maxTbl[2][0] = maxTbl[0][2] = maxFR[1];
		maxTbl[3][0] = maxTbl[0][3] = maxFR[2];
		maxTbl[2][1] = maxTbl[1][2] = maxFR[3];
		maxTbl[3][1] = maxTbl[1][3] = maxFR[4];
		maxTbl[3][2] = maxTbl[2][3] = maxFR[5];
		//Fill in minimum entries
		minTbl[1][0] = minTbl[0][1] = minFR[0];
		minTbl[2][0] = minTbl[0][2] = minFR[1];
		minTbl[3][0] = minTbl[0][3] = minFR[2];
		minTbl[2][1] = minTbl[1][2] = minFR[3];
		minTbl[3][1] = minTbl[1][3] = minFR[4];
		minTbl[3][2] = minTbl[2][3] = minFR[5];
	}

	/**
	 * Given a fingering object, provides a score for how easy the fingering
	 * is to produce
	 * @param fp The array of FretPosition objects to score
	 * @return The score
	 */
	public double RateFingering(FretPosition[] fp)
	{
		double score = 0.0;
		switch(fp.length)
		{
			case 4:
				score += 1.0 - SF(FretPosition.FretDistance(fp[0], fp[3]), minTbl[fp[0].fingNum][fp[3].fingNum], maxTbl[fp[0].fingNum][fp[3].fingNum]);
				score += 1.0 - SF(FretPosition.FretDistance(fp[1], fp[3]), minTbl[fp[1].fingNum][fp[3].fingNum], maxTbl[fp[1].fingNum][fp[3].fingNum]);
				score += 1.0 - SF(FretPosition.FretDistance(fp[2], fp[3]), minTbl[fp[2].fingNum][fp[3].fingNum], maxTbl[fp[2].fingNum][fp[3].fingNum]);
			case 3:
				score += 1.0 - SF(FretPosition.FretDistance(fp[0], fp[2]), minTbl[fp[0].fingNum][fp[2].fingNum], maxTbl[fp[0].fingNum][fp[2].fingNum]);
				score += 1.0 - SF(FretPosition.FretDistance(fp[1], fp[2]), minTbl[fp[1].fingNum][fp[2].fingNum], maxTbl[fp[1].fingNum][fp[2].fingNum]);
			case 2:
				score += 1.0 - SF(FretPosition.FretDistance(fp[0], fp[1]), minTbl[fp[0].fingNum][fp[1].fingNum], maxTbl[fp[0].fingNum][fp[1].fingNum]);
			default:
		}
		return 1.0 - (score / ((double) Math.max(fp.length * (fp.length - 1) / 2, 1)));
	}

	/**
	 * Distance score function
	 * @param x The distance
	 * @param min The minimum distance between the two fingers acceptable
	 * @param max The maximum distance between the two fingers acceptable
	 * @return SF(x)
	 */
	public static double SF(double x, double min, double max)
	{
        double a = min * .99;
        double b = max * 1.01;
		if(x < a)
			return 1 + (x - a) * (x - a) * (x - a);
		return 1 - ((x - a) / (b - a)) * ((x - a) / (b - a));
	}
}


