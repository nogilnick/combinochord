package com.nickandross.combinochord.learn;

import com.nickandross.combinochord.domain.Fingering;
import com.nickandross.jamal.Matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Created by nicholas on 4/12/16.
 * This class uses machine learning to provide a heuristic
 * rating on a chord fingering
 */
public class FingeringRater
{   //Current MSE for the ridge regression model
    private double curMSE;
    private Ridge rr;

    public FingeringRater()
    {   //TODO handle loading default values if none exist
        double[][] tmp = new double[Fingering.NUM_SCORES][1];
        tmp[0][0] = 0.09;
        tmp[1][0] = 0.28;
        tmp[2][0] = 0.28;
        tmp[3][0] = 0.18;
        tmp[4][0] = 0.03;
        tmp[5][0] = 0.03;
        tmp[6][0] = 0.04;
        tmp[7][0] = 0.07;
        Matrix coef = new Matrix(tmp, Fingering.NUM_SCORES, 1);
        Matrix intcpt = new Matrix(new double[][] {{0.0}}, 1, 1);
        rr = new Ridge(1.0, coef, intcpt);
    }

    /**
     * Converts guitar chord fingerings to a Matrix
     * for use with prediction/learning
     * @param fingerings The chord fingerings to convert
     * @return A matrix of features extracted from the fingerings
     */
    private Matrix[] FingeringToMatrices(Collection<Fingering> fingerings)
    {
        int m = fingerings.size();
        int n = Fingering.NUM_SCORES;
        double[][] ADat = new double[m][n];
        double[][] yDat = new double[m][1];
        int i = 0;
        for (Fingering f : fingerings) {
            System.arraycopy(f.GetCategoryScores(), 0, ADat[i], 0, n);
            yDat[i][0] = f.GetRating();
            ++i;
        }
        Matrix[] datTarg = new Matrix[2];
        //Wrap arrays in Matrix; no copying is performed
        datTarg[0] = new Matrix(ADat, m, n);
        datTarg[1] = new Matrix(yDat, m, 1);
        return datTarg;
    }

    /**
     * Get the current MSE of the model
     * @return The MSE
     */
    public double GetMSE()
    {
        return curMSE;
    }

    /**
     * Fit the model to the current fingerings and ratings
     * @param fingerings The fingerings and ratings to learn
     * @return True if successful false otherwise
     */
    public boolean Learn(Collection<Fingering> fingerings) {
        //Handle empty case
        if(fingerings == null || fingerings.size() == 0)
            return false;
        //Create the data matrix and the target matrix
        Matrix A;
        Matrix y;
        {   //Get the matrix data
            Matrix[] tmp = FingeringToMatrices(fingerings);
            A = tmp[0];
            y = tmp[1];
        }
        rr = new Ridge();
        //Fit the data
        rr.Fit(A, y);
        Matrix x = rr.GetCoef();
        if(x == null)
        {   //failed
            return false;
        }
        String myStr = "{ ";
        for(int i = 0; i < x.getRowDimension(); ++i) {
            myStr += x.get(i, 0) + "  ";
        }
        myStr += "} + " + rr.GetInt().get(0, 0);
        System.out.println(myStr);
        curMSE = rr.Score(y, rr.Predict(A));
        return true;
    }

    /**
     * Rate a single fingering
     * @param f The fingering to rate
     */
    public void Rate(Fingering f)
    {
        List<Fingering> tmp = new ArrayList<>(1);
        tmp.add(f);
        Rate(tmp);
    }

    /**
     * Rate multiple fingerings simultaneously
     * @param fingerings The fingerings to rate
     */
    public void Rate(Collection<Fingering> fingerings)
    {
        Matrix A;
        {   //Get the matrix data
            Matrix[] tmp = FingeringToMatrices(fingerings);
            A = tmp[0];
        }
        Matrix yHat = rr.Predict(A);
        int i = 0;
        //Set scores (assuming scores is single value)
        for(Fingering f : fingerings)
            f.SetScore(yHat.get(i++, 0));
    }
}
