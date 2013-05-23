package ch.ethz.nlp.headline.selection;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class ScoredSentencesSelector extends SentencesSelector {

	private final TfIdfProvider tfIdfProvider;

	private static final int TAKE_MAX_SENTENCES = 1;

	public ScoredSentencesSelector(TfIdfProvider tfIdfProvider) {
		this.tfIdfProvider = tfIdfProvider;
	}

	@Override
	protected List<CoreMap> selectImpl(Annotation documentAnnotation) {
		List<CoreMap> sentences = documentAnnotation.get(SentencesAnnotation.class);
		java.util.PriorityQueue<SentenceScore> allSentenceScores = new java.util.PriorityQueue<>(
				10, Collections.reverseOrder());
		SentenceFeatureExtractor featureExtractor = new SentenceFeatureExtractor(tfIdfProvider, documentAnnotation);

		for (int i = 0; i < sentences.size(); i++) {
			CoreMap sentence = sentences.get(i);
			SentenceScore score = featureExtractor.extractFeaturesForSentence(sentence);
			allSentenceScores.add(score);
		}

		List<CoreMap> bestSentences = new LinkedList<>();
		for (int s = 0; s < TAKE_MAX_SENTENCES && !allSentenceScores.isEmpty(); s++) {
			SentenceScore score = allSentenceScores.remove();
			CoreMap currentSentence = score.getSentence();
			bestSentences.add(currentSentence);
		}

		return bestSentences;
	}

}
