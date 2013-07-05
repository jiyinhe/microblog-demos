package edu.illinois.lis.probabilitydistributions;

import java.util.Arrays;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class KernelDensity implements ContinuousDistribution {
	private double[] data;
	private double[] weights;
	private double h;
	private double k;					// factor to make the weights scale correctly
	private NormalDistribution kernel;



	public KernelDensity(double[] data, double[] weights, double h) {
		this.data    = data;
		this.h       = h;
		this.weights = weights;
		kernel = new NormalDistribution(0.0, 1.0);

		if(h <= 0.0) {
			DescriptiveStatistics ds = new DescriptiveStatistics(data);
			double iqr = ds.getPercentile(75.0) - ds.getPercentile(25);
			iqr /= 1.34;
			double sigma = ds.getStandardDeviation();
			double a = Math.min(iqr, sigma);
			this.h = 0.9 * a * Math.pow((double)data.length, -1.0/5.0);
		}
		
		if(weights==null) {
			weights = new double[data.length];
			Arrays.fill(weights, 1.0/(double)data.length);
		}

		DescriptiveStatistics ss = new DescriptiveStatistics(weights);
		double s = ss.getSum();
		k = (double)weights.length / s;

		for(int i=0; i<weights.length; i++) {
			weights[i] *= k;
		}

	}


	public double density(double x) {
		double f = 0.0;
		for(int i=0; i<data.length; i++) {
			f += weights[i] * kernel.density((data[i]-x)/h);
		}
		f /= ((double)data.length * h);
		return f;
	}



	public double getBandwidth() {
		return h;
	}


}
