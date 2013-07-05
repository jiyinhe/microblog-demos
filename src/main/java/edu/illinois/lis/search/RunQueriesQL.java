package edu.illinois.lis.search;

import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;


import cc.twittertools.search.api.TrecSearchThriftClient;
import cc.twittertools.thrift.gen.TResult;
import edu.illinois.lis.query.TrecTemporalTopicSet;
import edu.illinois.lis.utils.ParameterBroker;

public class RunQueriesQL {
	private static final String DEFAULT_RUNTAG = "lucene4lm";

	private static final String HOST_OPTION = "host";
	private static final String PORT_OPTION = "port";
	private static final String QUERIES_OPTION = "queries";
	private static final String NUM_RESULTS_OPTION = "num_results";
	private static final String GROUP_OPTION = "group";
	private static final String TOKEN_OPTION = "token";
	private static final String RUNTAG_OPTION = "runtag";
		
	private RunQueriesQL() {}

	public static void main(String[] args) throws Exception {
		ParameterBroker params = new ParameterBroker(args[0]);

		PrintStream out = new PrintStream(System.out, true, "UTF-8");
		PrintStream err = new PrintStream(System.err, true, "UTF-8");

		TrecTemporalTopicSet topicsFile = TrecTemporalTopicSet.fromFile(new File(params.getParamValue(QUERIES_OPTION)));

		// max number of docs to send to output
		int numResults = 1000;
		try {
			if (params.getParamValue(NUM_RESULTS_OPTION) != null) {
				numResults = Integer.parseInt(params.getParamValue(NUM_RESULTS_OPTION));
			}
		} catch (NumberFormatException e) {
			err.println("Invalid " + NUM_RESULTS_OPTION + ": " + params.getParamValue(NUM_RESULTS_OPTION));
			System.exit(-1);
		}



		// authentication credentials
		String group = params.getParamValue(GROUP_OPTION);
		if(group==null) {
			err.println("Invalid " + GROUP_OPTION + ": must set a valid group ID");
			System.exit(-1);
		}
		String token = params.getParamValue(TOKEN_OPTION);
		if(group==null) {
			err.println("Invalid " + TOKEN_OPTION + ": must set a valid authentication token");
			System.exit(-1);
		}

		TrecSearchThriftClient client = new TrecSearchThriftClient(params.getParamValue(HOST_OPTION),
				Integer.parseInt(params.getParamValue(PORT_OPTION)), group, token);

		for(edu.illinois.lis.query.TrecTemporalTopic query : topicsFile) {
			err.println(query.getId());
			List<TResult> results = client.search(query.getQuery(), query.getQueryTweetTime(), numResults);
			String runTag = params.getParamValue(RUNTAG_OPTION);
			if(runTag==null) 
				runTag = DEFAULT_RUNTAG;

			int i = 1;
			Iterator<TResult> hitIterator = results.iterator();
			while(hitIterator.hasNext()) {
				TResult hit = hitIterator.next();
				out.println(String.format("%s Q0 %s %d %f %s", query.getId(), hit.getId(), i,
						hit.getRsv(), runTag));

				if(i++ >= numResults)
					break;
			}

		}
		out.close();
	}
}
