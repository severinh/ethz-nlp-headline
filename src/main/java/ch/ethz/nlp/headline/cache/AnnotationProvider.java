package ch.ethz.nlp.headline.cache;

import edu.stanford.nlp.pipeline.Annotation;

public interface AnnotationProvider {

	public Annotation getAnnotation(String content);

	public String getId();

}
