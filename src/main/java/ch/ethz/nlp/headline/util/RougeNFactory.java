package ch.ethz.nlp.headline.util;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.nlp.headline.Model;
import ch.ethz.nlp.headline.cache.AnnotationProvider;
import edu.stanford.nlp.pipeline.Annotation;

public class RougeNFactory {

	private final int n;

	public RougeNFactory(int n) {
		this.n = n;
	}

	public RougeN make(List<Annotation> models) {
		return new RougeN(models, n);
	}

	public RougeN make(List<Model> models, AnnotationProvider annotationProvider) {
		List<Annotation> annotations = new ArrayList<>();
		for (Model model : models) {
			String content = model.getContent();
			annotations.add(annotationProvider.getAnnotation(content));
		}

		return make(annotations);
	}

}
