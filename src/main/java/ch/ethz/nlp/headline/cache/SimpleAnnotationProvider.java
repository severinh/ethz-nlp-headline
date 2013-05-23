package ch.ethz.nlp.headline.cache;

import edu.stanford.nlp.pipeline.Annotation;

public class SimpleAnnotationProvider implements AnnotationProvider {

	@Override
	public Annotation getAnnotation(String content) {
		return new Annotation(content);
	}

}
