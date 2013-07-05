package edu.illinois.lis.temporal;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import edu.illinois.lis.utils.Qrels;


public class TimestampOracle {
	public static final Pattern SPACE_PATTERN = Pattern.compile(" ", Pattern.DOTALL);
	
	public static final int DOCNO_COLUMN = 0;
	public static final int EPOCH_COLUMN = 1;
	
	private Qrels qrels;
	private Map<String,Double> docnosToEpochs;
	
	public TimestampOracle(String pathToQrels, String pathToDocnoEpochs) {
		qrels = new Qrels(pathToQrels);
		docnosToEpochs = new HashMap<String,Double>();
		try {
			List<String> tuples = IOUtils.readLines(new FileReader(new File(pathToDocnoEpochs)));
			Iterator<String> tupleIterator = tuples.iterator();
			while(tupleIterator.hasNext()) {
				String[] toks = SPACE_PATTERN.split(tupleIterator.next());
				docnosToEpochs.put(toks[DOCNO_COLUMN], Double.parseDouble(toks[EPOCH_COLUMN]));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public List<Double> getEpochsForQuery(String queryName) {
		List<Double> epochs = new LinkedList<Double>();
		Set<String> relDocnos = qrels.getRelDocs(queryName);
		if(relDocnos==null || relDocnos.size()==0) {
			return epochs;
		}
		Iterator<String> relDocIt = relDocnos.iterator();
		while(relDocIt.hasNext()) {
			String relDocno = relDocIt.next();
			if(!docnosToEpochs.containsKey(relDocno)) 
				continue;
			epochs.add(docnosToEpochs.get(relDocno));
		}
		return epochs;
	}
}
