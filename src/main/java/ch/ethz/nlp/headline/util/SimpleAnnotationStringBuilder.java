package ch.ethz.nlp.headline.util;

import edu.stanford.nlp.pipeline.Annotation;

public class SimpleAnnotationStringBuilder implements AnnotationStringBuilder {

	public static final AnnotationStringBuilder INSTANCE = new SimpleAnnotationStringBuilder();

	@Override
	public String build(Annotation annotation, int maxLength) {
		String result = annotation.toString();
		if (result.length() > maxLength) {
			result = result.substring(0, maxLength);
		}
		return result;
	}

}
