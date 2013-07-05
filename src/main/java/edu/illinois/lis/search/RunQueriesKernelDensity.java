package edu.illinois.lis.search;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;



import cc.twittertools.search.api.TrecSearchThriftClient;
import cc.twittertools.thrift.gen.TResult;
import edu.illinois.lis.query.TrecTemporalTopicSet;
import edu.illinois.lis.rerank.KernelDensityReranker;
import edu.illinois.lis.temporal.TimestampOracle;
import edu.illinois.lis.utils.ListUtils;
import edu.illinois.lis.utils.ParameterBroker;
import edu.illinois.lis.utils.TimeUtils;

public class RunQueriesKernelDensity {
	private static final String DEFAULT_RUNTAG = "lucene4lm";
	private static final double DAY = 60.0 * 60.0 * 24.0;;

	private static final String HOST_OPTION = "host";
	private static final String PORT_OPTION = "port";
	private static final String QUERIES_OPTION = "queries";
	private static final String NUM_RESULTS_OPTION = "num_results";
	private static final String NUM_RERANK_OPTION = "num_rerank";
	private static final String GROUP_OPTION = "group";
	private static final String TOKEN_OPTION = "token";
	private static final String RUNTAG_OPTION = "runtag";
	private static final String ORACLE_FILE_OPTION = "oracle_epochs";
	private static final String QRELS_FILE_OPTION = "qrels";
	private static final String USE_ORACLE_OPTION = "use_oracle";


	private RunQueriesKernelDensity() {}

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

		// max number of docs to analyze for re-ranking
		int numRerank = numResults;
		try {
			if (params.getParamValue(NUM_RERANK_OPTION) != null) {
				numResults = Integer.parseInt(params.getParamValue(NUM_RERANK_OPTION));
			}
		} catch (NumberFormatException e) {
			err.println("Invalid " + NUM_RERANK_OPTION + ": " + params.getParamValue(NUM_RERANK_OPTION));
			System.exit(-1);
		}
		if(numRerank > numResults) {
			err.println("num_rerank must by <= num_results");
			System.exit(-1);
		}

		boolean useOracle = false;
		if(params.getParamValue(USE_ORACLE_OPTION) != null) {
			try {
				useOracle = Boolean.parseBoolean(params.getParamValue(USE_ORACLE_OPTION));
			} catch (Exception e)  {
				err.println("Invalid " + USE_ORACLE_OPTION + ": must be either true/false or blank.");
				System.exit(-1);
			}
		}
		if(useOracle==true)
			err.println("using oracle condition");

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

		// if we're using the oracle condition
		TimestampOracle oracle = new TimestampOracle(params.getParamValue(QRELS_FILE_OPTION),
				params.getParamValue(ORACLE_FILE_OPTION));

		for(edu.illinois.lis.query.TrecTemporalTopic query : topicsFile) {
			err.println(query.getId());

			List<TResult> results = client.search(query.getQuery(),
					query.getQueryTweetTime(), numResults);



			// get the query epoch
			double queryEpoch = query.getEpoch();

			List<Double> rawEpochs = TimeUtils.extractEpochsFromResults(results);


			// groom our hit times wrt to query time
			List<Double> scaledEpochs = TimeUtils.adjustEpochsToLandmark(rawEpochs, queryEpoch, DAY);
			double[] densityTrainingData = ListUtils.listToArray(scaledEpochs);
			double[] densityWeights = new double[densityTrainingData.length];
			Arrays.fill(densityWeights, 1.0/(double)densityWeights.length);

			// if we're using our oracle, we need the right training data
			if(useOracle) {
				List<Double> oracleRawEpochs = oracle.getEpochsForQuery(query.getId());
				List<Double> oracleScaledEpochs = TimeUtils.adjustEpochsToLandmark(oracleRawEpochs, queryEpoch, DAY);
				densityTrainingData = ListUtils.listToArray(oracleScaledEpochs);
				densityWeights = new double[densityTrainingData.length];
				Arrays.fill(densityWeights, 1.0/(double)densityWeights.length);
			}

			if(densityTrainingData != null && densityTrainingData.length > 2) {
				KernelDensityReranker reranker = new KernelDensityReranker(results, scaledEpochs,
						densityTrainingData, densityWeights);
				results = reranker.getReranked();
			}
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
