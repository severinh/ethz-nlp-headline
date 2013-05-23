package ch.ethz.nlp.headline.generators;

import ch.ethz.nlp.headline.cache.AnnotationProvider;
import ch.ethz.nlp.headline.selection.FirstSentenceSelector;
import ch.ethz.nlp.headline.selection.SentencesSelector;
import edu.stanford.nlp.pipeline.Annotation;

/**
 * Generator that simply extracts the first sentence from the given text.
 */
public class BaselineGenerator extends CoreNLPGenerator {

	private final SentencesSelector sentencesSelector = new FirstSentenceSelector();

	public BaselineGenerator(AnnotationProvider annotationProvider) {
		super(annotationProvider);
	}

	@Override
	public String getId() {
		return "BASE";
	}

	@Override
	protected Annotation generate(Annotation annotation) {
		return sentencesSelector.select(annotation);
	}

}
