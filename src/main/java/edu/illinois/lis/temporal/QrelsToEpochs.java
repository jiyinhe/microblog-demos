package edu.illinois.lis.temporal;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class QrelsToEpochs {
	public static final Pattern SPACE_PATTERN = Pattern.compile("\\s", Pattern.DOTALL);

	private Map<Long,Long> map=null;
	private Iterator<Long> mapIterator;
	
	public QrelsToEpochs(String pathToMap) {
		try {
			List<String> lines = IOUtils.readLines(new FileReader(pathToMap));
			map = new HashMap<Long,Long>(lines.size());
			Iterator<String> lineIterator = lines.iterator();
			while(lineIterator.hasNext()) {
				String[] toks = SPACE_PATTERN.split(lineIterator.next());
				map.put(Long.parseLong(toks[0]), Long.parseLong(toks[1]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public double epochForDocno(long docno) {
		if(!map.containsKey(docno)) {
			System.err.println("can't find epoch for docno " + docno);
			return Double.NEGATIVE_INFINITY;
		}
		return map.get(docno);
	}
	
	public Iterator<Long> iterator() {
		mapIterator = map.keySet().iterator();
		return mapIterator;
	}
}
