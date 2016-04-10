package domain;

import java.util.ArrayList;
import java.util.List;

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
    private final int numStrs;
    //The tuning of the strings as integer semi-tone numbers
    private final int[] tuning;
    //The width of the first fret
    private final double firstFretWidth;
    //The scale length of the guitar
    private final double scaleLen;
    //All fret positions on the fret board
    private List<FretPosition> fretboard;

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
        numStrs = tun.length;
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
        double nutStrWidth = nutWidth / numStrs;
        double brgStrWidth = bridgeWidth / numStrs;
        fretboard = new ArrayList<>((numFrets + 1) * numStrs);
        for (int i = 0; i < (numFrets + 1) * numStrs; ++i) {
            double tmp = scaleLen - Constants.FretToDistance(firstFretWidth, 0, i / numStrs);
            fretboard.add(new FretPosition(i, i % numStrs, i / numStrs, tmp, ((nutOffset + (i % numStrs) * nutStrWidth) - ((i % numStrs) * brgStrWidth)) / scaleLen * tmp + (i % numStrs) * brgStrWidth, tuning[i % numStrs] + (i / numStrs), FretPosition.UNDF_FNG));
        }

    }


    /**
     * Finds the (string, fret) positions of all given notes
     *
     * @param nts The array of notes
     * @return A list of fretpositions whose notes are in nts
     */
    public List<FretPosition> FindNotePositions(int[] nts) {
        List<FretPosition> notePos = new ArrayList<>();
        for (int i = 0; i < fretboard.size(); ++i) {
            if (Constants.NoteInChord(fretboard.get(i).note, nts))
                notePos.add(fretboard.get(i));
        }
        return notePos;
    }

    /**
     * Finds the FretPosition object at a specific fret and string
     *
     * @param strNum The string number
     * @param frtNum The fret number
     * @return The FretPosition object
     */
    public FretPosition GuitarFretPos(int strNum, int frtNum) {
        if (fretboard == null || frtNum * this.numStrs + strNum >= fretboard.size())
            return null;
        return fretboard.get(frtNum * this.numStrs + strNum);
    }

    /**
     * Getter function for numStrs
     *
     * @return numStrs
     */
    public int GetNumStrings() {
        return this.numStrs;
    }

    /**
     * Function to get the open string FretPositions
     *
     * @return The fret positions of the open strings on the guitar
     */
    public FretPosition[] OpenStrings() {
        FretPosition[] opens = new FretPosition[numStrs];
        for (int i = 0; i < numStrs; ++i)
            opens[i] = fretboard.get(i).Clone();
        return opens;
    }

}
