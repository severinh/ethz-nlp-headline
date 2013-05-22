package ch.ethz.nlp.headline.util;

import edu.stanford.nlp.pipeline.Annotation;

public interface AnnotationStringBuilder {

	public String build(Annotation annotation, int maxLength);

}
