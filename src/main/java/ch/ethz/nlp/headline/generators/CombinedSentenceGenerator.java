package ch.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.DocumentId;
import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PriorityQueue;

/**
 * Uses the sentence whose words have the highest average tf-idf score.
 */
public class CombinedSentenceGenerator extends TfIdfGenerator {

	private static final Logger LOG = LoggerFactory
			.getLogger(CombinedSentenceGenerator.class);

	private final Set<String> openTags;

	private static final int MIN_LEN = 10;
	private static final int LOW_POS = 1;

	private static final int LENGTH_WEIGHT = 2;
	private static final int POS_WEIGHT = 6;
	private static final double TFIDF_WEIGHT = 0.20;

	public CombinedSentenceGenerator(Dataset dataset) throws IOException,
			ClassNotFoundException {
		super(dataset);

		// Temporarily create a tagger to gain access to the list of open tags
		MaxentTagger tagger = new MaxentTagger(MaxentTagger.DEFAULT_JAR_PATH);
		openTags = tagger.getTags().getOpenTags();
	}

	@Override
	public String getId() {
		return "COMBINED-SENTENCE";
	}

	@Override
	public String generate(Document document) {
		DocumentId documentId = document.getId();
		PriorityQueue<String> tfIdfMap = getTfIdfMap(documentId);
		Annotation annotation = annotations.get(documentId);

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
				String word = label.word();
				double tfIdf = tfIdfMap.getPriority(word);
				sentenceTfIdfScore += tfIdf;
			}
			sentenceTfIdfScore /= labels.size();
			scores.setTfIdfScore(sentenceTfIdfScore);

			// pos
			scores.setPositionScore(i < LOW_POS ? 1 : 0);

			// length
			int len = labels.size();
			if (len >= MIN_LEN) {
				scores.setLengthScore(0);
			} else {
				scores.setLengthScore(len - MIN_LEN);
			}

			allSentenceScores.add(scores);
		}

		List<CoreMap> bestSentences = new LinkedList<>();
		List<SentenceScores> bestScores = new LinkedList<>();
		List<String> bestWords = new LinkedList<>();
		for (int s = 0; s < 3 && !allSentenceScores.isEmpty(); s++) {
			SentenceScores score = allSentenceScores.remove();
			CoreMap currentSentence = score.getSentence();
			bestSentences.add(currentSentence);
			bestScores.add(score);

			Annotation singleSentenceAnnotation = new Annotation(
					currentSentence.get(TextAnnotation.class));
			List<CoreMap> singletonList = new LinkedList<>();
			singletonList.add(currentSentence);
			singleSentenceAnnotation.set(SentencesAnnotation.class,
					singletonList);
			CoreNLPUtil.getPosTagger().annotate(singleSentenceAnnotation);
			List<CoreLabel> labels = currentSentence
					.get(TokensAnnotation.class);
			for (CoreLabel label : labels) {
				String tag = label.get(PartOfSpeechAnnotation.class);
				if (openTags.contains(tag)) {
					bestWords.add(label.word());
				}
			}
		}

		LOG.debug(bestScores.get(0).toString());

		StringBuilder builder = new StringBuilder();
		for (String sortedTerm : bestWords) {
			if (builder.length() + sortedTerm.length() > MAX_LENGTH) {
				break;
			}
			builder.append(" " + sortedTerm);
		}

		String result = builder.toString().trim();
		LOG.debug(result);
		LOG.debug("---");
		return result;
	}

	public class SentenceScores implements Comparable<SentenceScores> {
		private final CoreMap sentence;
		double tfIdfScore;
		double positionScore;
		double lengthScore;

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
