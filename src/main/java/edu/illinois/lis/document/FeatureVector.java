package edu.illinois.lis.document;


import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;

import edu.illinois.lis.utils.KeyValuePair;
import edu.illinois.lis.utils.ScorableComparator;




/**
 * Simple container mapping term->count pairs grabbed from an input text.
 * 
 * @author Miles Efron
 *
 */
public class FeatureVector  {
	@SuppressWarnings("deprecation")
	private static final StandardAnalyzer STOPPED_ANALYZER = new StandardAnalyzer(Version.LUCENE_41);
	@SuppressWarnings("deprecation")
	private static final WhitespaceAnalyzer RAW_ANALYZER   = new WhitespaceAnalyzer(Version.LUCENE_41);
	private static final Pattern PUNCTUATION = Pattern.compile("\\W");
	
	private Map<String, Double> features;
	double length = 0.0;

	private boolean useStoplist = true;
	
	// CONSTRUCTORS  
	public FeatureVector(String text, boolean useStoplist) {
		this.useStoplist = true;
		features = new HashMap<String, Double>();
		List<String> terms = this.analyze(text);
		Iterator<String> termsIt = terms.iterator();
		while(termsIt.hasNext()) {
			String term = termsIt.next();
			length += 1.0;
			Double val = (Double)features.get(term);
			if(val == null) {
				features.put(term, new Double(1.0));
			} else {
				double v = val.doubleValue() + 1.0;
				features.put(term, new Double(v));
			}
		}
	}

	public FeatureVector(boolean useStoplist) {
		features = new HashMap<String,Double>();
		this.useStoplist = useStoplist;
	}






	// MUTATORS

	/**
	 * Add all the terms in a string to this vector
	 * @param text a space-delimited string where we want to add each word.
	 */
	public void addText(String text) {
		List<String> terms = this.analyze(text);
		Iterator<String> termsIt = terms.iterator();
		while(termsIt.hasNext()) {
			String term = termsIt.next();		
			addTerm(term);
		}
	}

	/**
	 * Add a term to this vector.  if it's already here, increment its count.
	 * @param term
	 */
	public void addTerm(String term) {
		Double freq = ((Double)features.get(term));
		if(freq == null) {
			features.put(term, new Double(1.0));
		} else {
			double f = freq.doubleValue();
			features.put(term, new Double(f+1.0));
		}
		length += 1.0;
	}

	public void setUseStoplist(boolean useStoplist) {
		this.useStoplist = useStoplist;
	}
	
	/**
	 * Add a term to this vector with this weight.  if it's already here, supplement its weight.
	 * @param term
	 * @param weight
	 */
	public void addTerm(String term, double weight) {
		Double w = ((Double)features.get(term));
		if(w == null) {
			features.put(term, new Double(weight));
		} else {
			double f = w.doubleValue();
			features.put(term, new Double(f+weight));
		}
		length += weight;
	}
	
	/**
	 * in case we want to override the derived length.
	 * @param length
	 */
	public void setLength(double length) {
		this.length = length;
	}

	public void pruneToSize(int k) {
		List<KeyValuePair> kvpList = getOrderedFeatures();
		
		Iterator<KeyValuePair> it = kvpList.iterator();
		
		Map<String,Double> newMap = new HashMap<String,Double>(k);
		int i=0;
		while(it.hasNext()) {
			KeyValuePair kvp = it.next();
			newMap.put((String)kvp.getKey(), kvp.getScore());
			if(i++ > k)
				break;
		}

		features = (HashMap<String, Double>) newMap;
		
	}

	public void normalizeToOne() {
		Map<String,Double> f = new HashMap<String,Double>(features.size());
		
		Iterator<String> it = features.keySet().iterator();
		while(it.hasNext()) {
			String feature = it.next();
			double obs = features.get(feature);
			f.put(feature, obs/length);
		}
		
		features = f;
	}

	
	// ACCESSORS

	public Set<String> getFeatures() {
		return features.keySet();
	}
	
	public double getLength() {
		return length;
	}

	public int getDimensions() {
		return features.size();
	}

	public double getFeaturetWeight(String feature) {
		Double w = (Double)features.get(feature);
		return (w==null) ? 0.0 : w.doubleValue();
	}

	public Iterator<String> iterator() {
		return features.keySet().iterator();
	}

	public boolean contains(Object key) {
		return features.containsKey(key);
	}

	public double getVectorNorm() {
		double norm = 0.0;
		Iterator<String> it = features.keySet().iterator();
		while(it.hasNext()) {
			norm += Math.pow(features.get(it.next()), 2.0);
		}
		return Math.sqrt(norm);
	}


	
	// VIEWING
	
	@Override
	public String toString() {
		return this.toString(features.size());
	}
	
	private List<KeyValuePair> getOrderedFeatures() {
		List<KeyValuePair> kvpList = new ArrayList<KeyValuePair>(features.size());
		Iterator<String> featureIterator = features.keySet().iterator();
		while(featureIterator.hasNext()) {
			String feature = featureIterator.next();
			double value   = features.get(feature);
			KeyValuePair keyValuePair = new KeyValuePair(feature, value);
			kvpList.add(keyValuePair);
		}
		ScorableComparator comparator = new ScorableComparator(true);
		Collections.sort(kvpList, comparator);
		
		return kvpList;
	}
	
	public String toString(int k) {
		DecimalFormat format = new DecimalFormat("#.#########");
		StringBuilder b = new StringBuilder();
		List<KeyValuePair> kvpList = getOrderedFeatures();
		Iterator<KeyValuePair> it = kvpList.iterator();
		int i=0;
		while(it.hasNext() && i++ < k) {
			KeyValuePair pair = it.next();
			b.append(format.format(pair.getScore()) + " " + pair.getKey() + "\n");
		}
		return b.toString();

	}
	
	
	// UTILS
	public List<String> analyze(String text) {
		List<String> result = new LinkedList<String>();
		try {
			TokenStream stream = null;
			if(useStoplist) {
				stream = STOPPED_ANALYZER.tokenStream("text", new StringReader(text));
			} else {
				stream = RAW_ANALYZER.tokenStream("text", new StringReader(text));
			}
			OffsetAttribute offsetAttribute = stream.addAttribute(OffsetAttribute.class);
			CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);
			stream.reset();
			while(stream.incrementToken()) {
			    //int startOffset = offsetAttribute.startOffset();
			    //int endOffset = offsetAttribute.endOffset();
			    String term = charTermAttribute.toString();
			    if(!useStoplist) {
			    	term = PUNCTUATION.matcher(term).replaceAll(" ").toLowerCase();
			    }
				result.add(term);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void main(String[] args) {
		String text = "This. This is NOT a test, nor is it better than 666!";
		
		FeatureVector featureVector = new FeatureVector(true);
		List<String> terms = featureVector.analyze(text);
		Iterator<String> termIterator = terms.iterator();
		while(termIterator.hasNext()) {
			System.out.println(termIterator.next());
		}
	}


}
