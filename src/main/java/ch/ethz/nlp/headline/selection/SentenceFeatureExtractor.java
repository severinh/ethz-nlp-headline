package ch.ethz.nlp.headline.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PriorityQueue;

public class SentenceFeatureExtractor {
	private static final double LENGTH_WEIGHT = 2.0;
	private static final double POS_WEIGHT = 6.0;
	private static final double TFIDF_WEIGHT = 0.20;
	//private static final double NNP_WEIGHT = 1;
	
	private final PriorityQueue<String> tfIdfMap;
	private Annotation documentAnnotation;
	private List<SentenceFeature> features = new ArrayList<>();
	private List<Double> weights = new ArrayList<>();


	public SentenceFeatureExtractor(TfIdfProvider tfIdfProvider,
			Annotation documentAnnotation) {
		this.documentAnnotation = documentAnnotation;
		this.tfIdfMap = tfIdfProvider.getTfIdfMap(documentAnnotation);
		
		addFeature(new TfIdfFeature(), TFIDF_WEIGHT);
		addFeature(new PosFeature(), POS_WEIGHT);
		//addFeature(new NNPFeature(), NNP_WEIGHT);
		addFeature(new LengthFeature(), LENGTH_WEIGHT);
	}
	
	private void addFeature(SentenceFeature feature, double weight) {
		features.add(feature);
		weights.add(weight);
	}

	public SentenceScore extractFeaturesForSentence(CoreMap sentence) {
		List<String> names = new ArrayList<>();
		List<Double> values = new ArrayList<>();
		for (SentenceFeature feature : features) {
			names.add(feature.getName());
			values.add(feature.extract(sentence));
		}
		return new SentenceScore(sentence, names, values, weights);
	}

	protected class TfIdfFeature implements SentenceFeature {

		@Override
		public double extract(CoreMap sentence) {
			double sentenceTfIdfScore = 0.0;
			List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
			for (CoreLabel label : labels) {
				String lemma = label.lemma();
				double tfIdf = tfIdfMap.getPriority(lemma);
				sentenceTfIdfScore += tfIdf;
			}
			sentenceTfIdfScore /= labels.size();
			return sentenceTfIdfScore;
		}

		@Override
		public String getName() {
			return "TF-IDF";
		}
	}

	protected class PosFeature implements SentenceFeature {
		private static final int POS_THRESHOLD = 1;

		@Override
		public double extract(CoreMap sentence) {
			List<CoreMap> sentences = documentAnnotation
					.get(SentencesAnnotation.class);
			int pos = sentences.indexOf(sentence);
			assert (pos >= 0);
			return pos < POS_THRESHOLD ? 1.0 : 0.0;
		}

		@Override
		public String getName() {
			return "POS";
		}
	}

	protected class NNPFeature implements SentenceFeature {
		private static final int MIN_NNP = 3;

		@Override
		public double extract(CoreMap sentence) {
			// NNP (proper nouns)
			List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
			int numberOfNNP = 0;
			for (CoreLabel label : labels) {
				String tag = label.tag();
				if (Objects.equals(tag, "NNP")) {
					numberOfNNP++;
				}
			}
			return numberOfNNP < MIN_NNP ? -5 : 0;
		}

		@Override
		public String getName() {
			return "NNP";
		}
	}

	protected class LengthFeature implements SentenceFeature {
		private static final int MIN_LENGTH = 10;

		@Override
		public double extract(CoreMap sentence) {
			List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
			int len = labels.size();
			if (len >= MIN_LENGTH) {
				return 0.0;
			} else {
				return len - MIN_LENGTH;
			}
		}

		@Override
		public String getName() {
			return "LENGTH";
		}
	}

}