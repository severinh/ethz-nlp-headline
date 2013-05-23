package ch.ethz.nlp.headline.selection;

import edu.stanford.nlp.util.CoreMap;

public interface SentenceFeature {
	public double extract(CoreMap sentence);
	
	public String getName();
}
