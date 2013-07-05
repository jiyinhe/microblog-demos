package edu.illinois.lis.rerank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import cc.twittertools.thrift.gen.TResult;
import edu.illinois.lis.probabilitydistributions.KernelDensity;

public class KernelDensityReranker extends SearchReranker {
	private List<Double> scaledEpochs;
	private KernelDensity kernelDensity;
	
	public KernelDensityReranker(List<TResult> results, List<Double> scaledEpochs, double[] trainingData,
			double[] trainingWeights) {
		this.results = results;
		this.scaledEpochs = scaledEpochs;
		kernelDensity = new KernelDensity(trainingData, trainingWeights, -1.0);

		this.score();
	}
	
	protected void score() {		
		Iterator<TResult> resultIt = results.iterator();
		Iterator<Double> epochIt   = scaledEpochs.iterator();
		
		List<TResult> updatedResults = new ArrayList<TResult>(results.size());
		while(resultIt.hasNext()) {
			TResult origResult = resultIt.next();
			double scaledEpoch = epochIt.next();
			double density = Math.log(kernelDensity.density(scaledEpoch));
			if(Double.isInfinite(density) || Double.isNaN(density)) 
				density = -100.0;
			TResult updatedResult = new TResult();
			updatedResult.setId(origResult.getId());
			updatedResult.setRsv(origResult.getRsv() + density);
			updatedResults.add(updatedResult);
		}
		results = updatedResults;
	}
	

}
