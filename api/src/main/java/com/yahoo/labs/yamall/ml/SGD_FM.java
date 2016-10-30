package com.yahoo.labs.yamall.ml;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

import com.yahoo.labs.yamall.core.Instance;
import com.yahoo.labs.yamall.core.SparseVector;


/*
 * This class is an implementation of Factorization Machines.
 *  w - parameters of linear model
 *  v - parameters of interaction parameters(two way interaction) 
 * 
 */
public class SGD_FM implements Learner {
	
	private double eta = .5;
	private double epsilon = Math.exp(-6);
	private Loss lossFnc;
    private double iter = 0;
    private int size_hash = 0;
    private int fmNumberFactors = 0;
    private double[] w;             //linear model parameters
    private double[] s;
    
    private double[][] v;           //interaction parameters
    private double[] sumProd_v;     
    private boolean isInitialized = false;
    
    // Use to store past gradient info for adagrad 
    private double[] gradientSquare_w;  
    private double[][] gradientSquare_v;
    
	
	public SGD_FM(int bits, int fmNumberFactors) {
		size_hash = 1 << bits;
		this.fmNumberFactors = fmNumberFactors;
		w = new double[size_hash];
		s = new double[size_hash];
		
		//TODO: optimize space
		v = new double[size_hash][fmNumberFactors];  
		
		// initialize all v's with gaussian distribution
		init(size_hash, fmNumberFactors);  
		sumProd_v = new double[fmNumberFactors];
		gradientSquare_w = new double[size_hash];
		gradientSquare_v = new double[size_hash][fmNumberFactors];	
	}
	
	/*
	 * Initialize the interaction parameters 
	 *        to avoid gradient to be 0
	 * 
	 */
	public void init(int hash_size, int numFactors) {
		Random r = new Random();
		for (int i = 0 ; i < hash_size; i++) {
			for (int j = 0; j < numFactors; j++) {
				double temp = r.nextGaussian()*0.01;  //Mean = 0, variance = 0.01
				v[i][j] = temp;
			}
		}
		
	}
	
	public double update(Instance sample) {
		/*
		 *  calculate pred => sum(w_i*x_i)
		 */
		double pred = predict_normalized_features(sample);
		
		
		final double negativeGrad = lossFnc.negativeGradient(pred, sample.getLabel(), sample.getWeight());
		
		/*
		 * update weights.
		 * w_i = w_i - eta(t)*gradient(loss)
		 */
		for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
			int key = entry.getIntKey();
			
			double x_i = entry.getDoubleValue();
			double w_i = w[key];
			/*
			 * Adaptive learning rate : eta_grad
			 */
			//gradientSquare_w[key] += Math.pow(negativeGrad*x_i, 2);
			gradientSquare_w[key] += ((negativeGrad*x_i) * (negativeGrad*x_i));
			double eta_grad = eta/(Math.sqrt(gradientSquare_w[key] ) + epsilon);
			
			w_i += (eta_grad*negativeGrad*x_i);
			
			w[key] = w_i;
		}
		for (int i = 0; i < fmNumberFactors; i++) {
				for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
					int key = entry.getIntKey();
					double x_i = entry.getDoubleValue();
					double v_ij = v[key][i];
					double v_grad = (x_i*sumProd_v[i]) - (v_ij * x_i*x_i);
					
					/*
					 * Adaptive learning rate : eta_grad
					 */
					//gradientSquare_v[key][i] += Math.pow(negativeGrad*v_grad, 2);
					gradientSquare_v[key][i] += ((negativeGrad*v_grad) * (negativeGrad*v_grad));
					double eta_grad = eta/(Math.sqrt(gradientSquare_v[key][i]) + epsilon);
					v_ij += eta_grad* negativeGrad*v_grad;
					v[key][i] = v_ij;
				}
		}
		
		return pred;
	}

	public double predict_normalized_features(Instance sample) {
		double pred = 0;
		
		//one-way interaction
		for (Int2DoubleMap.Entry entry : sample.getVector().int2DoubleEntrySet()) {
			int key = entry.getIntKey();
			double s_i = s[key];
			double x_i = entry.getDoubleValue();
			double w_i = w[key];
			if (Math.abs(x_i) > s_i) {
                w_i = w_i * s_i / Math.abs(x_i);
                w[key] = w_i;
                //s_i = Math.abs(x_i);    //I am not changing the max feature value here as it is used in two way 
                						  //     interactions also
                //s[key] = s_i;
            }
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
				double s_i = s[key];
				double x_i = entry.getDoubleValue();
				
				/*
				 * This condition is satisfied only once per feature.
				 * The running time is O(#factors * #features) 
				 */
				if (Math.abs(x_i) > s_i) {
					for (int k = 0 ; k < fmNumberFactors; k++) {
						double v_ij = v[key][k];
						v_ij = v_ij * s_i / Math.abs(x_i);
						v[key][k] = v_ij;
					}
					s_i = Math.abs(x_i);
					s[key] = s_i;
	            }
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
		//eta = 0.01;
		this.eta = eta;
	}

	public SparseVector getWeights() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void writeObject(ObjectOutputStream o) throws IOException {
        o.defaultWriteObject();
    }
    

}
