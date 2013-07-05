package edu.illinois.lis.probabilitydistributions;

public class LocalExponentialDistribution implements ContinuousDistribution {
	private double lambda = 0.01;
	
	public LocalExponentialDistribution(double lambda) {
		this.lambda = lambda;
	}
	
	public double density(double x) {
		return lambda * Math.exp(-1.0 * lambda * x);
	}
	
}
