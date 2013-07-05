package edu.illinois.lis.rerank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cc.twittertools.thrift.gen.TResult;
import edu.illinois.lis.probabilitydistributions.LocalExponentialDistribution;

public class RecencyReranker extends SearchReranker {
	private double lambda = 0.01;
	private List<Double> scaledEpochs;
	
	public RecencyReranker(List<TResult> results, List<Double> scaledEpochs, double lambda) {
		this.results = results;
		this.scaledEpochs = scaledEpochs;
		this.lambda = lambda;
		this.score();
	}
	
	protected void score() {
		LocalExponentialDistribution dexp = new LocalExponentialDistribution(lambda);
		Iterator<TResult> resultIt = results.iterator();
		Iterator<Double> epochIt   = scaledEpochs.iterator();
		
		List<TResult> updatedResults = new ArrayList<TResult>(results.size());
		while(resultIt.hasNext()) {
			TResult origResult = resultIt.next();
			double scaledEpoch = epochIt.next();
			double recency = Math.log(dexp.density(scaledEpoch));
			if(Double.isInfinite(recency) || Double.isNaN(recency)) 
				recency = -100.0;
			TResult updatedResult = new TResult();
			updatedResult.setId(origResult.getId());
			updatedResult.setRsv(origResult.getRsv() + recency);
			updatedResults.add(updatedResult);
		}
		results = updatedResults;
	}
	
}
