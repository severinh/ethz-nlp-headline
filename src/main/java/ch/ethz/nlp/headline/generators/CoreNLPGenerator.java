package ch.ethz.nlp.headline.generators;

import ch.ethz.nlp.headline.cache.AnnotationProvider;
import ch.ethz.nlp.headline.preprocessing.ContentPreprocessor;
import ch.ethz.nlp.headline.preprocessing.NopPreprocessor;
import ch.ethz.nlp.headline.util.AnnotationStringBuilder;
import ch.ethz.nlp.headline.util.SimpleAnnotationStringBuilder;
import edu.stanford.nlp.pipeline.Annotation;

public abstract class CoreNLPGenerator implements Generator {

	private final AnnotationProvider annotationProvider;
	private final ContentPreprocessor preprocessor;
	private final AnnotationStringBuilder resultBuilder;

	public CoreNLPGenerator(AnnotationProvider annotationProvider,
			ContentPreprocessor preprocessor,
			AnnotationStringBuilder resultBuilder) {
		this.annotationProvider = annotationProvider;
		this.preprocessor = preprocessor;
		this.resultBuilder = resultBuilder;
	}

	public CoreNLPGenerator(AnnotationProvider annotationProvider) {
		this(annotationProvider, NopPreprocessor.INSTANCE,
				SimpleAnnotationStringBuilder.INSTANCE);
	}

	@Override
	public String generate(String content) {
		content = preprocessor.preprocess(content);

		Annotation annotation = annotationProvider.getAnnotation(content);
		annotation = generate(annotation);

		String result = resultBuilder.build(annotation, Integer.MAX_VALUE);
		String trimmedResult = resultBuilder.build(annotation, MAX_LENGTH);

		getStatistics().addSummaryResult(result);
		return trimmedResult;
	}

	protected abstract Annotation generate(Annotation annotation);

	private GeneratorStatistics statistics = new GeneratorStatistics(this);

	public GeneratorStatistics getStatistics() {
		return statistics;
	}

}
