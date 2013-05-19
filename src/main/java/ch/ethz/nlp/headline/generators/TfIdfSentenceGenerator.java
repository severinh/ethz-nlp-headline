package ch.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.List;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.DocumentId;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PriorityQueue;

/**
 * Uses the sentence whose words have the highest average tf-idf score.
 */
public class TfIdfSentenceGenerator extends TfIdfGenerator {

	public TfIdfSentenceGenerator(Dataset dataset) throws IOException {
		super(dataset, "ssplit");
	}

	@Override
	public String getId() {
		return "TF-IDF-SENTENCE";
	}

	@Override
	public String generate(Document document) {
		DocumentId documentId = document.getId();
		PriorityQueue<String> tfIdfMap = getTfIdfMap(documentId);
		Annotation annotation = getAnnotation(documentId);

		CoreMap bestSentence = null;
		double bestSentenceScore = Float.MIN_VALUE;

		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			double sentenceScore = 0.0;
			List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
			for (CoreLabel label : labels) {
				String word = label.word();
				double tfIdf = tfIdfMap.getPriority(word);
				sentenceScore += tfIdf;
			}
			sentenceScore /= labels.size();
			if (bestSentence == null || bestSentenceScore < sentenceScore) {
				bestSentence = sentence;
				bestSentenceScore = sentenceScore;
			}
		}

		return bestSentence.toString();
	}

}
