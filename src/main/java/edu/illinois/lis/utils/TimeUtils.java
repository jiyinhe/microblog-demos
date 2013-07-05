package edu.illinois.lis.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cc.twittertools.thrift.gen.TResult;

public class TimeUtils {

	public static List<Double> extractEpochsFromResults(List<TResult> results) {
		List<Double> epochs = new ArrayList<Double>(results.size());
		Iterator<TResult> it = results.iterator();
		while(it.hasNext()) {
			epochs.add((double)it.next().getEpoch());
		}
		return epochs;
	}
	
	public static List<Double> adjustEpochsToLandmark(List<Double> epochs, double landmark, double scaleDenominator) {
		List<Double> scaled = new ArrayList<Double>(epochs.size());
		Iterator<Double> it = epochs.iterator();
		while(it.hasNext()) {
			double rawEpoch = it.next();
			scaled.add(TimeUtils.adjustEpochToLandmark(rawEpoch, landmark, scaleDenominator));
		}
		return scaled;
	}
	
	public static double adjustEpochToLandmark(double rawEpoch, double landmark, double scaleDenominator) {
		return (landmark - rawEpoch) / scaleDenominator;
	}
	
	public static List<TResult> updateResultTimes(List<TResult> rawResults, double landmark, double scaleDenominator) {
		List<TResult> scaledResults = new ArrayList<TResult>(rawResults.size());
		Iterator<TResult> it = rawResults.iterator();
		while(it.hasNext()) {
			TResult rawResult = it.next();
			double rawEpoch = (double)rawResult.epoch;
			double scaledTime = TimeUtils.adjustEpochToLandmark(rawEpoch, landmark, scaleDenominator);
			// do we need a deeper copy?
			TResult newResult = rawResult;
			newResult.epoch = (long)scaledTime;
			scaledResults.add(newResult);
		}
		return scaledResults;
	}
}
