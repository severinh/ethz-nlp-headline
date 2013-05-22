package ch.ethz.nlp.headline.selection;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PriorityQueue;

public class TfIdfSentencesSelector extends SentencesSelector {

	private final TfIdfProvider tfIdfProvider;

	private static final int NUM_SENTENCES = 3;
	private static final int MIN_LENGTH = 10;
	private static final int POS_THRESHOLD = 1;

	private static final double LENGTH_WEIGHT = 2.0;
	private static final double POS_WEIGHT = 6.0;
	private static final double TFIDF_WEIGHT = 0.20;

	public TfIdfSentencesSelector(TfIdfProvider tfIdfProvider) {
		this.tfIdfProvider = tfIdfProvider;
	}

	@Override
	protected List<CoreMap> selectImpl(Annotation annotation) {
		PriorityQueue<String> tfIdfMap = tfIdfProvider.getTfIdfMap(annotation);

		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		java.util.PriorityQueue<SentenceScores> allSentenceScores = new java.util.PriorityQueue<>(
				10, Collections.reverseOrder());

		for (int i = 0; i < sentences.size(); i++) {
			CoreMap sentence = sentences.get(i);
			SentenceScores scores = new SentenceScores(sentence);

			// tf-idf
			double sentenceTfIdfScore = 0.0;
			List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
			for (CoreLabel label : labels) {
				String lemma = label.lemma();
				double tfIdf = tfIdfMap.getPriority(lemma);
				sentenceTfIdfScore += tfIdf;
			}
			sentenceTfIdfScore /= labels.size();
			scores.setTfIdfScore(sentenceTfIdfScore);

			// pos
			scores.setPositionScore(i < POS_THRESHOLD ? 1.0 : 0.0);

			// length
			int len = labels.size();
			if (len >= MIN_LENGTH) {
				scores.setLengthScore(0);
			} else {
				scores.setLengthScore(len - MIN_LENGTH);
			}

			allSentenceScores.add(scores);
		}

		List<CoreMap> bestSentences = new LinkedList<>();

		for (int s = 0; s < NUM_SENTENCES && !allSentenceScores.isEmpty(); s++) {
			SentenceScores score = allSentenceScores.remove();
			CoreMap currentSentence = score.getSentence();
			bestSentences.add(currentSentence);
		}

		return bestSentences;
	}

	public class SentenceScores implements Comparable<SentenceScores> {

		private final CoreMap sentence;

		private double tfIdfScore;
		private double positionScore;
		private double lengthScore;

		public double getTfIdfScore() {
			return tfIdfScore;
		}

		public void setTfIdfScore(double tfIdfScore) {
			this.tfIdfScore = tfIdfScore;
		}

		public double getPositionScore() {
			return positionScore;
		}

		public void setPositionScore(double positionScore) {
			this.positionScore = positionScore;
		}

		public double getLengthScore() {
			return lengthScore;
		}

		public void setLengthScore(double lengthScore) {
			this.lengthScore = lengthScore;
		}

		public CoreMap getSentence() {
			return sentence;
		}

		public SentenceScores(CoreMap sentence) {
			this.sentence = sentence;
		}

		public double getCombinedScore() {
			return TFIDF_WEIGHT * tfIdfScore + POS_WEIGHT * positionScore
					+ LENGTH_WEIGHT * lengthScore;
		}

		@Override
		public int compareTo(SentenceScores other) {
			double thisScore = getCombinedScore();
			double otherScore = other.getCombinedScore();
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
			return "total: " + getCombinedScore() + ", tfidf: " + tfIdfScore
					+ ", pos: " + positionScore + ", " + lengthScore;
		}

	}

}
