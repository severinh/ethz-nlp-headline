package ch.ethz.nlp.headline.generators;

import java.util.ArrayList;
import java.util.List;

public class GeneratorStatistics {

	private final Generator generator;

	public GeneratorStatistics(Generator generator) {
		this.generator = generator;
	}

	private List<Integer> summaryLengths = new ArrayList<>();

	public void addSummaryResult(String summary) {
		summaryLengths.add(summary.length());
	}

	@Override
	public String toString() {
		int summaries = summaryLengths.size();
		int tooLong = 0;
		double totalLength = 0;
		for (Integer i : summaryLengths) {
			if (i > CoreNLPGenerator.MAX_LENGTH) {
				tooLong++;
			}
			totalLength += i;
		}
		double avgLength = totalLength / summaries;

		StringBuilder sb = new StringBuilder();
		sb.append(generator.getId() + " statistics:\n");
		sb.append(String.format("Too long: %d/%d\n", tooLong, summaries));
		sb.append(String.format("Average length: %.2f\n", avgLength));
		return sb.toString();
	}

}