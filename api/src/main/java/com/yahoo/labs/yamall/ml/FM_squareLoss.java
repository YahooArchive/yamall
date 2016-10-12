package com.yahoo.labs.yamall.ml;

public class FM_squareLoss implements Loss {

	public double lossValue(double pred, double label) {
		return (pred - label) * (pred - label);
	}

	public double negativeGradient(double pred, double label, double importance) {
		
		//return pred*(1.0  - 1.0/(1.0 + Math.exp(-pred*label)));
		return 2.0 * (label - pred) * importance;  //This is for regression
	}

	public double negativeGradientInvariant(double pred, double label,
			double importance, double h_normx) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double lossConstantBinaryLabels(double sPlus, double sMinus) {
		// TODO Auto-generated method stub
		return 0;
	}

}
