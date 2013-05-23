package ch.ethz.nlp.headline.selection;

import java.util.List;

import edu.stanford.nlp.util.CoreMap;

public class SentenceScore implements Comparable<SentenceScore> {

	private final CoreMap sentence;
	private final List<String> names;
	private final List<Double> values;
	private final List<Double> weights;

	public SentenceScore(CoreMap sentence, List<String> names, List<Double> values, List<Double> weights) {
		this.sentence = sentence;
		this.names = names;
		this.values = values;
		this.weights = weights;
	}
	
	public double getTotalScore() {
		double totalScore = 0.0;
		for (int i = 0; i < values.size(); i++) {
			totalScore += weights.get(i)*values.get(i);
		}
		return totalScore;
	}

	@Override
	public int compareTo(SentenceScore other) {
		double thisScore = getTotalScore();
		double otherScore = other.getTotalScore();
		if (thisScore < otherScore) {
			return -1;
		} else if (thisScore > otherScore) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getTotalScore());
		builder.append(" = [");
		for (int i = 0; i < values.size(); i++) {
			String entry = String.format("%.2f*(%s %.2f", weights.get(i), names.get(i), values.get(i));
			builder.append(entry);
		}
		builder.append("]");
		return builder.toString();
	}

	public CoreMap getSentence() {
		return sentence;
	}
	
	public List<String> getNames() {
		return names;
	}

	public List<Double> getValues() {
		return values;
	}

	public List<Double> getWeights() {
		return weights;
	}


}