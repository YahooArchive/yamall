package com.yahoo.labs.yamall.ml;

import java.util.Random;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

import com.yahoo.labs.yamall.core.Instance;
import com.yahoo.labs.yamall.core.SparseVector;

public class SGD_FM implements Learner {
	
	private double eta = .5;
	private Loss lossFnc;
    private double iter = 0;
    private int size_hash = 0;
    private int fmNumberFactors = 0;
    private transient double[] w;
    private transient double[] s;
    
    private transient double[][] v;
    private transient double[] sumProd_v;
    private boolean isInitialized = false;
	
	public SGD_FM(int bits, int fmNumberFactors) {
		size_hash = 1 << bits;
		this.fmNumberFactors = fmNumberFactors;
		w = new double[size_hash];
		s = new double[size_hash];
		
		v = new double[size_hash][fmNumberFactors]; //Too much space wastage. TODO: optimize space
		sumProd_v = new double[fmNumberFactors];
	}
	
	public void init(Instance sample) {
		Random r = new Random();
		for (int i = 0 ; i < fmNumberFactors; i++) {
			for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
				int key = entry.getIntKey();
				double temp = r.nextGaussian()*0.01;
				v[key][i] = temp;
			}
		}
		
	}
	
	public double update(Instance sample) {
		
		
		if (!isInitialized) {
			init(sample);
			isInitialized = true;
		}
		/*
		 *  calculate pred => sum(w_i*x_i)
		 */
		double pred = predict(sample);
		
		final double negativeGrad = lossFnc.negativeGradient(pred, sample.getLabel(), sample.getWeight());
		
		/*
		 * update weights.
		 * w_i = w_i - eta*gradient(loss)
		 */
		for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
			int key = entry.getIntKey();
			double x_i = entry.getDoubleValue();
			double w_i = w[key];
			w_i += (eta*negativeGrad*x_i);
			w[key] = w_i;
		}
		
		for (int i = 0; i < fmNumberFactors; i++) {
				for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
					int key = entry.getIntKey();
					double x_i = entry.getDoubleValue();
					double v_ij = v[key][i];
					double v_grad = (x_i*sumProd_v[i]) - (v_ij * x_i*x_i);
					v_ij += eta* negativeGrad*v_grad;
					v[key][i] = v_ij;
				}
		}
		
		return pred;
	}

	public double predict(Instance sample) {
		double pred = 0;
		
		//one-way interaction
		for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
			int key = entry.getIntKey();
			double x_i = entry.getDoubleValue();
			double w_i = w[key];
			pred += (x_i * w_i);
		}
		
		
		/*
		 * Calculating two way interaction: O(nk)
		 *  
		 */
		for (int i = 0; i < fmNumberFactors; i++) {
			double linearSum = 0;
			double squareSum = 0;
			for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
				int key = entry.getIntKey();
				double x_i = entry.getDoubleValue();
				double v_ij = v[key][i];
				double prod = v_ij*x_i;
				linearSum += prod;
				squareSum += prod*prod;
			}
			sumProd_v[i] = linearSum;
			pred += 0.5*(linearSum*linearSum - squareSum);
		}
		
		return pred;
	}

	public void setLoss(Loss lossFnc) {
		this.lossFnc = lossFnc;
		
	}

	public Loss getLoss() {
		return lossFnc;
	}

	public void setLearningRate(double eta) {
		eta = 0.01;
		this.eta = eta;
	}

	public SparseVector getWeights() {
		// TODO Auto-generated method stub
		return null;
	}

}
