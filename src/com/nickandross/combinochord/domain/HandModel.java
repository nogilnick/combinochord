package com.nickandross.combinochord.domain;

/**
 * This class maintains information about the
 * flexibility of the player's hand. The minimum
 * and maximum comfortable ranges for pairs of
 * fingers are maintained.
 */
public class HandModel {
    //Enumerated values for the fingers bit-mapped set
    //For example F123 means enable fingers 1 2 and 3 (disable 4)
    public enum ENBL_FNGR {
        NONE, F1, F2, F12, F3, F13, F23, F123, F4, F14, F24, F124, F34, F134, F234, ALL
    }
    //Used for converting from int to ENBL_FNGR enum
    public static final ENBL_FNGR[] fngrEnumLookup = ENBL_FNGR.values();
    //All possible fingering assignments
    public static final int[][] FNGR_NUM =
            {       {0},            //0
                    {1},            //1
                    {2},            //2
                    {3},            //3
                    {0, 1},         //4
                    {0, 2},         //5
                    {0, 3},         //6
                    {1, 2},         //7
                    {1, 3},         //8
                    {2, 3},         //9
                    {0, 1, 2},      //10
                    {0, 1, 3},      //11
                    {0, 2, 3},      //12
                    {1, 2, 3},      //13
                    {0, 1, 2, 3},   //14
                    {}              //15
            };
    //Look-up table for maximum distances between finger i and j
    private final double[][] maxTbl;
    //Look-up table for minimum distances between finger i and j
    private final double[][] minTbl;
    //BMS to determine if a finger is enabled
    private final int fngrEnbl;
    //Keeps track of the number of available fingers
    private int numFngr;
    //The maximum search distance
    private double maxSearchDist;

    //Fingering assignment score and id pair
    class FAScore
    {
        final double score;
        final int faid;

        public FAScore(double s, int id)
        {
            score = s;
            faid = id;
        }
    }

    /**
     * Create a hand model using max/min distances between fingers allowed.
     * Arrays are ordered as: 1-2, 1-3, 1-4, 2-3, 2-4, 3-4
     *
     * @param fngrBms A bit-mapped set of the enabled fingers
     * @param minFR   An array of minimum distances between fingers in milimeters
     * @param maxFR   An array of max distances between finger in milimeters
     */
    public HandModel(ENBL_FNGR fngrBms, double[] minFR, double[] maxFR) {
        fngrEnbl = GetBMS(fngrBms);
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
        //Get the maximum search distance
        maxSearchDist = maxFR[0];
        maxSearchDist = (maxSearchDist < maxFR[1] ? maxFR[1] : maxSearchDist);
        maxSearchDist = (maxSearchDist < maxFR[2] ? maxFR[2] : maxSearchDist);
        maxSearchDist = (maxSearchDist < maxFR[3] ? maxFR[3] : maxSearchDist);
        maxSearchDist = (maxSearchDist < maxFR[4] ? maxFR[4] : maxSearchDist);
        maxSearchDist = (maxSearchDist < maxFR[5] ? maxFR[5] : maxSearchDist);
    }

    /**
     * Distance score function
     *
     * @param x   The distance
     * @param min The minimum distance between the two fingers acceptable
     * @param max The maximum distance between the two fingers acceptable
     * @return SF(x)
     */
    private static double SF(double x, double min, double max) {
        double a = min * .99;
        double b = max * 1.01;
        if(x < a)
            return 1.0 + (x - a) * (x - a) * (x - a);
        double l = 7.0 * b / 12.0;
        if(x >= a && x <= l)
            return 1.0;
        return 1.0 - ((x - l) / l) * ((x - l) / l);

    }

    /**
     * Converts from an ENBLFNGR enumerated value to a boolean
     * array for convenience
     *
     * @param fngrVal Is the enumerated value
     * @return The boolean array
     */
    private int GetBMS(ENBL_FNGR fngrVal) {
        int bms = fngrVal.ordinal();
        numFngr = 0;
        if ((bms & 0b0001) == 0b0001)
            ++numFngr;  //Finger 0 is enabled
        if ((bms & 0b0010) == 0b0010)
            ++numFngr;  //Finger 1 is enabled
        if ((bms & 0b0100) == 0b0100)
            ++numFngr;  //Finger 2 is enabled
        if ((bms & 0b1000) == 0b1000)
            ++numFngr;  //Finger 3 is enabled
        return bms;
    }

    /**
     * Determines which finger numbers to use given that finger
     * positions are already chosen
     *
     * @param sp The fingering for which to choose finger numbers
     */
    public FAScore FindBestFingering(FingerPlacement[] sp) {
        double fngrScr = 0.0;
        double bestScore = Double.NEGATIVE_INFINITY;
        int faid = 15;
        if (sp.length == 1) {
            //Use first available finger
            if ((fngrEnbl & 0b0001) != 0)
                faid = 0;
            else if ((fngrEnbl & 0b0010) != 0)
                faid = 1;
            else if ((fngrEnbl & 0b0100) != 0)
                faid = 2;
            else if ((fngrEnbl & 0b1000) != 0)
                faid = 3;
            fngrScr = RateFingering(sp, faid);
            //Locate fingers below will ensure fingPos has correct values
        } else if (sp.length == 2) {    //Try all 6 possible ways of choosing the 2 fingers without crossing
            //1-2
            if ((fngrEnbl & 0b0011) == 0b0011) {
                fngrScr = RateFingering(sp, 4);
                if (fngrScr > bestScore) {
                    bestScore = fngrScr;
                    faid = 4;
                }
            }
            //1-3
            if ((fngrEnbl & 0b0101) == 0b0101) {
                fngrScr = RateFingering(sp, 5);
                if (fngrScr > bestScore) {
                    bestScore = fngrScr;
                    faid = 5;
                }
            }
            //1-4
            if ((fngrEnbl & 0b1001) == 0b1001) {
                fngrScr = RateFingering(sp, 6);
                if (fngrScr > bestScore) {
                    bestScore = fngrScr;
                    faid = 6;
                }
            }
            //2-3
            if ((fngrEnbl & 0b0110) == 0b0110) {
                fngrScr = RateFingering(sp, 7);
                if (fngrScr > bestScore) {
                    bestScore = fngrScr;
                    faid = 7;
                }
            }
            //2-4
            if ((fngrEnbl & 0b1010) == 0b1010) {
                fngrScr = RateFingering(sp, 8);
                if (fngrScr > bestScore) {
                    bestScore = fngrScr;
                    faid = 8;
                }
            }
            //3-4
            if ((fngrEnbl & 0b1100) == 0b1100) {
                fngrScr = RateFingering(sp, 9);
                if (fngrScr > bestScore) {
                    bestScore = fngrScr;
                    faid = 9;
                }
            }
            fngrScr = bestScore;
        } else if (sp.length == 3) {    //Try all 4 ways of arranging the fingers without crossing
            //1-2-3
            if ((fngrEnbl & 0b0111) == 0b0111) {
                fngrScr = RateFingering(sp, 10);
                if (fngrScr > bestScore) {
                    bestScore = fngrScr;
                    faid = 10;
                }
            }
            //1-2-4
            if ((fngrEnbl & 0b1011) == 0b1011) {
                fngrScr = RateFingering(sp, 11);
                if (fngrScr > bestScore) {
                    bestScore = fngrScr;
                    faid = 11;
                }
            }
            //1-3-4
            if ((fngrEnbl & 0b1101) == 0b1101) {
                fngrScr = RateFingering(sp, 12);
                if (fngrScr > bestScore) {
                    bestScore = fngrScr;
                    faid = 12;
                }
            }
            //2-3-4
            if ((fngrEnbl & 0b1110) == 0b1110) {
                fngrScr = RateFingering(sp, 13);
                if (fngrScr > bestScore) {
                    bestScore = fngrScr;
                    faid = 13;
                }
            }
            fngrScr = bestScore;
        } else if (sp.length == 4) {    //Use the only possible combination that doesn't cross fingers
            fngrScr = RateFingering(sp, faid = 14);
        }
        //Finally set the finger score to reflect the finalized fingering
        return new FAScore(fngrScr, faid);
    }

    /**
     * Gets the number of fingers being used
     *
     * @return NumFngr
     */
    public int GetNumFngr() {
        return numFngr;
    }

    /**
     * Get the maximum stretching distance
     * @return The maximum stretching/searching distance.
     */
    public double GetMaxSearchDist() { return maxSearchDist; }

    /**
     * Given a fingering object, provides a score for how easy the fingering
     * is to produce
     *
     * @param sp The fingering placement to score
     * @return The score
     */
    private double RateFingering(FingerPlacement[] sp, int faid) {
        double score = 0.0;
        switch (sp.length) {
            case 4:
                score += 1.0 - SF(FretPosition.FretDistance(sp[0].fp, sp[3].fp), minTbl[FNGR_NUM[faid][0]][FNGR_NUM[faid][3]], maxTbl[FNGR_NUM[faid][0]][FNGR_NUM[faid][3]]);
                score += 1.0 - SF(FretPosition.FretDistance(sp[1].fp, sp[3].fp), minTbl[FNGR_NUM[faid][1]][FNGR_NUM[faid][3]], maxTbl[FNGR_NUM[faid][1]][FNGR_NUM[faid][3]]);
                score += 1.0 - SF(FretPosition.FretDistance(sp[2].fp, sp[3].fp), minTbl[FNGR_NUM[faid][2]][FNGR_NUM[faid][3]], maxTbl[FNGR_NUM[faid][2]][FNGR_NUM[faid][3]]);
            case 3:
                score += 1.0 - SF(FretPosition.FretDistance(sp[0].fp, sp[2].fp), minTbl[FNGR_NUM[faid][0]][FNGR_NUM[faid][2]], maxTbl[FNGR_NUM[faid][0]][FNGR_NUM[faid][2]]);
                score += 1.0 - SF(FretPosition.FretDistance(sp[1].fp, sp[2].fp), minTbl[FNGR_NUM[faid][1]][FNGR_NUM[faid][2]], maxTbl[FNGR_NUM[faid][1]][FNGR_NUM[faid][2]]);
            case 2:
                score += 1.0 - SF(FretPosition.FretDistance(sp[0].fp, sp[1].fp), minTbl[FNGR_NUM[faid][0]][FNGR_NUM[faid][1]], maxTbl[FNGR_NUM[faid][0]][FNGR_NUM[faid][1]]);
            default:
        }
        return 1.0 - (score / ((double) Math.max(sp.length * (sp.length - 1) / 2, 1)));
    }
}


