package ch.ethz.nlp.headline.learning;

import edu.stanford.nlp.ling.IndexedWord;

public interface Feature {

	public double computeValue(IndexedWord label);

}
