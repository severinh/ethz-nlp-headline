package ch.ethz.nlp.headline.generators;

import java.util.ArrayList;
import java.util.List;

public abstract class CoreNLPGenerator implements Generator {

	private GeneratorStatistics statistics = new GeneratorStatistics();

	public GeneratorStatistics getStatistics() {
		return statistics;
	}

	protected String truncate(String headline) {
		if (headline.length() > MAX_LENGTH) {
			headline = headline.substring(0, MAX_LENGTH);
		}
		return headline;
	}

	public class GeneratorStatistics {
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
				if (i > MAX_LENGTH) {
					tooLong++;
				}
				totalLength += i;
			}
			double avgLength = totalLength / summaries;

			StringBuilder sb = new StringBuilder();
			sb.append(getId() + " statistics:\n");
			sb.append(String.format("Too long: %d/%d\n", tooLong, summaries));
			sb.append(String.format("Average length: %.2f\n", avgLength));
			return sb.toString();
		}
	}

}
