package ch.ethz.nlp.headline.util;

import java.util.List;

import edu.stanford.nlp.pipeline.Annotation;

public class RougeNFactory {

	private final int n;

	public RougeNFactory(int n) {
		this.n = n;
	}

	public RougeN make(List<Annotation> references) {
		return new RougeN(references, n);
	}

}
