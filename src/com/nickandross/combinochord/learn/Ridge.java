package com.nickandross.combinochord.learn;

import com.nickandross.jamal.Matrix;
import com.nickandross.jamal.SingularValueDecomposition;

/**
 * Created by nicholas on 4/12/16.
 * Implements ridge regression. Allows
 * fitting data, predicting outputs, and
 * scoring the prediction using the MSE.
 */
public class Ridge
{
    private final double a;
    private Matrix coef;
    private Matrix intercept;

    /**
     * Centers the data matrix A
     * @param A The data matrix
     * @return The mean of the data matrix
     * along the row axis
     */
    private Matrix Center(Matrix A)
    {
        int m = A.getRowDimension();
        int n = A.getColumnDimension();
        double[][] mean = new double[1][n];
        //Sum the columns
        for(int i = 0; i < m; ++i)
        {
            for(int j = 0; j < n; ++j)
                mean[0][j] += A.get(i, j);
        }
        //Compute mean
        for(int i = 0; i < n; ++i)
            mean[0][i] /= m;
        //Subtract the mean
        for(int i = 0; i < m; ++i)
        {
            for(int j = 0; j < n; ++j)
                A.set(i, j, A.get(i, j) - mean[0][j]);
        }
        return new Matrix(mean, 1, n);
    }

    /**
     * Fits the ridge regression model to the data
     * @param A The sample data. Each row corresponds
     *          to a sample. The data is centered (modifies
     *          the calling object).
     * @param y The target data. Each row corresponds to
     *          a sample. (Calling object is modified)
     */
    public void Fit(Matrix A, Matrix y)
    {   //Perform SVD on A
        Matrix aOffset = Center(A);
        Matrix yOffset = Center(y);
        SingularValueDecomposition svd = new SingularValueDecomposition(A);
        Matrix U = svd.getU();
        Matrix S = svd.getS();
        Matrix Vt = svd.getV();
        //Modify diagonal matrix S to s_i / (s_i^2+a^2)
        for(int i = 0; i < S.getRowDimension(); ++i)
        {
            double s_i = S.get(i, i);
            S.set(i, i, s_i / (s_i * s_i + a * a));
        }
        //The solution is then VS(U^T)y
        coef = Vt.transpose().times(S.times(U.transpose())).times(y);
        intercept = yOffset.minus(aOffset.times(coef));
    }

    public Matrix GetCoef()
    {
        return coef;
    }

    public Matrix GetInt() { return intercept; }

    /**
     * Predict the output given input matrix A
     * @param A Each row corresponds to a sample
     * @return Each row corresponds to prediction for
     * corresponding sample in A
     */
    public Matrix Predict(Matrix A)
    {
        return A.times(coef).BAdd(intercept);
    }

    /**
     * Initialize a RidgeRegression class  
     */
    public Ridge() {
        this(1.0);
    }

    /**
     * Initialize a RidgeRegression class
     * @param alpha Weight of the L2 regularization term
     */
    public Ridge(double alpha)
    {
        a = alpha; coef = null;
    }

    /**
     * Initialize a Ridge regression model
     * with an existing coefficient and
     * intercept matrix
     * @param alpha Weight of the L2 regularization term
     * @param w The coefficient matrix
     * @param i The intercept matrix
     */
    public Ridge(double alpha, Matrix w, Matrix i)
    {
        a = alpha;
        coef = w;
        intercept = i;
    }

    /**
     * This MSE of the prediction yHat
     * @param y The actual values
     * @param yHat The predicted values
     * @return The MSE
     */
    public double Score(Matrix y, Matrix yHat)
    {
        return y.minus(yHat).norm2();
    }

}
