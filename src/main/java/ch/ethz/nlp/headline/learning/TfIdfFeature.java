package ch.ethz.nlp.headline.learning;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.util.PriorityQueue;

public class TfIdfFeature implements Feature {

	private final PriorityQueue<String> tfIdfMap;

	public TfIdfFeature(PriorityQueue<String> tfIdfMap) {
		super();
		this.tfIdfMap = tfIdfMap;
	}

	@Override
	public double computeValue(IndexedWord label) {
		double tfIdfScore = tfIdfMap.getPriority(label.lemma());
		return Math.max(0.0, tfIdfScore);
	}

}
