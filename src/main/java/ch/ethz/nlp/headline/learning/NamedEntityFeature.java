package ch.ethz.nlp.headline.learning;

import java.util.Objects;

import edu.stanford.nlp.ling.IndexedWord;

public class NamedEntityFeature implements Feature {

	private final String tag;

	public NamedEntityFeature(String namedEntityTag) {
		super();
		this.tag = namedEntityTag;
	}

	@Override
	public double computeValue(IndexedWord label) {
		double result = (Objects.equals(label.ner(), tag)) ? 1.0 : 0.0;
		return result;
	}

}
