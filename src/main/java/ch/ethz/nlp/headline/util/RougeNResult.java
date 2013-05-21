package ch.ethz.nlp.headline.util;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Holds the recall, precision and F score from a ROUGE-N evaluation.
 * 
 * Currently not used because {@link RougeN} only computes the recall.
 */
public class RougeNResult {

	private final double recall;
	private final double precision;
	private final double fScore;

	public RougeNResult(double recall, double precision) {
		super();

		checkArgument(recall >= 0.0 && recall <= 1.0);
		checkArgument(precision >= 0.0 && recall <= 1.0);

		this.recall = recall;
		this.precision = precision;
		this.fScore = 2 * precision * recall / (precision + recall);
	}

	public double getRecall() {
		return recall;
	}

	public double getPrecision() {
		return precision;
	}

	public double getfScore() {
		return fScore;
	}

	@Override
	public String toString() {
		return "RougeResult [recall=" + recall + ", precision=" + precision
				+ ", fScore=" + fScore + "]";
	}

}