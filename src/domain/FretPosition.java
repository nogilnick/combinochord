package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a fret position on a guitar
 */
public class FretPosition
{
	public static int MUTE_STR = -1;
	public static int UNDF_FNG = -2;
	//The string number
	public int str;
	//The fret number
	public int frt;
	//The note produced if sounded
	public int note;
	//The finger used to depress (if any)
	public int fingNum;
	//A unique ID for the fret position
	public int fretId;
	//The x and y coordinates of the fret position in 2d space
	private double x;
	private double y;


	/**
	 * Returns an a new identical FretPosition object
	 * @return A new FretPosition object with identical values
	 */
	public FretPosition Clone()
	{
		return new FretPosition(fretId, str, frt, x, y, note, fingNum);
	}

	/**
	 * FretPosition constructor
	 * @param fid The "fret id"
	 * @param s The string number
	 * @param f The fret number
	 * @param x1 The x coordinate on the guitar in euclidean space
	 * @param y1 The y coordinate on the guitar in euclidean space
	 * @param n The note produced
	 * @param fn The number of the finger depressing the position
	 */
	public FretPosition(int fid, int s, int f, double x1, double y1, int n, int fn)
	{
		fretId = fid;
		str = s;
		frt = f;
		x = x1;
		y = y1;
		note = n;
		fingNum = fn;
	}

	/**
	 * Returns the fret distance between two positions
	 * @param f1 Position one
	 * @param f2 Position two
	 * @return The fret distance between the two positions
	 */
	public static double FretDistance(FretPosition f1, FretPosition f2)
	{
		return Math.sqrt((f1.x - f2.x) * (f1.x - f2.x) + (f1.y - f2.y) * (f1.y - f2.y));
	}

	/**
	 * Filters for distace around a given point fp
	 * @param lst The list of FretPositions to filter
	 * @param fp The position to filter based on
	 * @param maxDist The max search distance
	 * @param curFilt The current filter
	 * @return An updated filter
	 */
	public static boolean[] Filter1(List<FretPosition> lst, FretPosition fp, double maxDist, boolean[] curFilt)
	{
		boolean[] newFilt = new boolean[lst.size()];
		for(int i = 0; i < lst.size(); ++i)
		{	//Scan entire fretboard for pts <= maxDist
			newFilt[i] = curFilt[i];
			if(FretPosition.FretDistance(lst.get(i), fp) > maxDist || fp.str == lst.get(i).str)
				newFilt[i] = false;
		}
		return newFilt;
	}

	/**
	 * Filters for a given non-open tonic note fp
	 * @param lst The list of FretPositions to filter
	 * @param fp The position to filter based on
	 * @param maxDist The max search distance
	 * @param curFilt The current filter
	 * @return An updated filter
	 */
	public static boolean[] Filter2(List<FretPosition> lst, FretPosition fp, double maxDist, boolean[] curFilt)
	{
		boolean[] newFilt = new boolean[lst.size()];
		for(int i = 0; i < lst.size(); ++i)
		{	//Scan entire fretboard for pts <= maxDist
			newFilt[i] = curFilt[i];
			if(FretPosition.FretDistance(lst.get(i), fp) > maxDist || lst.get(i).note < fp.note || lst.get(i).str == fp.str)
				newFilt[i] = false;
		}
		return newFilt;
	}

	/**
	 * Find all FretPositions in a list that produce a given note
	 * @param fpl The list of FretPositions
	 * @param n The note
	 * @return A list of FretPositions that produce the given note
	 */
	public static List<FretPosition> FindFretPos(List<FretPosition> fpl, int n)
	{
		List<FretPosition> matchPos = new ArrayList<>();
		for(int i = 0; i < fpl.size(); ++i)
		{
			if((fpl.get(i).note % 12) == n)
				matchPos.add(fpl.get(i));
		}
		return matchPos;
	}

	/**
	 * Tests if the FretPosition is muted
	 * @return True if the FretPosition is muted; false otherwise
	 */
    public boolean IsMute()
    {
        return fingNum == MUTE_STR;
    }

	/**
	 * Mute the current FretPosition object
	 */
	public void Mute()
	{
        note -= frt;
		frt = 0;
        fingNum = MUTE_STR;
	}

	/**
	 * Sets the values of the FretPosition object to another
	 * @param fp The other FretPosition object
	 */
	public void Set(FretPosition fp)
	{
		fretId = fp.fretId;
		str = fp.str;
		frt = fp.frt;
		x = fp.x;
		y = fp.y;
		note = fp.note;
		fingNum = fp.fingNum;
	}

	/**
	 * This function removes all positions beneath the current tonic
	 * @param lst The list to filter
	 * @param fp The position of the tonic
	 * @return An updated filter
	 */
	public static boolean[] TonicFilter(List<FretPosition> lst, FretPosition fp, boolean[] curFilt)
	{
		boolean[] newFilt = new boolean[curFilt.length];
		for(int i = 0; i < curFilt.length; ++i)
		{	//Scan entire fretboard for pts >= tonic
			newFilt[i] = curFilt[i];
			if(lst.get(i).note < fp.note || lst.get(i).str == fp.str)
				newFilt[i] = false;
		}
		return newFilt;
	}

	/**
	 * Function to return a string representation of the object
	 */
	public String toString()
	{
		return "{" + String.format("%.1f", x) + ", " + String.format("%.1f", y) + ", " + str + ", " + frt + ", " + fingNum + "}";
	}
}

