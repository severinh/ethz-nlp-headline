package ch.ethz.nlp.headline.generators;

import ch.ethz.nlp.headline.selection.TfIdfProvider;
import ch.ethz.nlp.headline.selection.TfIdfSentencesSelector;
import edu.stanford.nlp.pipeline.Annotation;

/**
 * Uses the sentence whose words have the highest average tf-idf score.
 */
public class CombinedSentenceGenerator extends CoreNLPGenerator {

	private final TfIdfSentencesSelector sentencesSelector;

	public CombinedSentenceGenerator(TfIdfProvider tfIdfProvider) {
		this.sentencesSelector = new TfIdfSentencesSelector(tfIdfProvider);
	}

	@Override
	public String getId() {
		return "COMBINED-SENTENCE";
	}

	@Override
	protected Annotation generate(Annotation annotation) {
		return sentencesSelector.select(annotation);
	}

}
