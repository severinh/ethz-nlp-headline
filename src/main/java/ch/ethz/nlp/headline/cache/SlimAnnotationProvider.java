package ch.ethz.nlp.headline.cache;

import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.pipeline.Annotation;

/**
 * Builds annotations where tokenization, sentence splitting, POS recognition,
 * lemmatizing have already been performed.
 */
public class SlimAnnotationProvider implements AnnotationProvider {

	@Override
	public Annotation getAnnotation(String content) {
		Annotation annotation = new Annotation(content);
		CoreNLPUtil.ensureLemmaAnnotation(annotation);
		return annotation;
	}

	@Override
	public String getId() {
		return "slim";
	}

}
