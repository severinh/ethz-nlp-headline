package ch.ethz.nlp.headline.generators;

import ch.ethz.nlp.headline.preprocessing.ContentPreprocessor;
import ch.ethz.nlp.headline.preprocessing.NopPreprocessor;
import ch.ethz.nlp.headline.util.AnnotationCache;
import ch.ethz.nlp.headline.util.AnnotationStringBuilder;
import ch.ethz.nlp.headline.util.SimpleAnnotationStringBuilder;
import edu.stanford.nlp.pipeline.Annotation;

public abstract class CoreNLPGenerator implements Generator {

	private final AnnotationCache cache;
	private final ContentPreprocessor preprocessor;
	private final AnnotationStringBuilder resultBuilder;

	public CoreNLPGenerator(AnnotationCache cache,
			ContentPreprocessor preprocessor,
			AnnotationStringBuilder resultBuilder) {
		this.cache = cache;
		this.preprocessor = preprocessor;
		this.resultBuilder = resultBuilder;
	}

	public CoreNLPGenerator(AnnotationCache cache) {
		this(cache, NopPreprocessor.INSTANCE,
				SimpleAnnotationStringBuilder.INSTANCE);
	}

	@Override
	public String generate(String content) {
		content = preprocessor.preprocess(content);

		Annotation annotation = cache.getAnnotation(content);
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
