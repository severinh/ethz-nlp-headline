package ch.ethz.nlp.headline.cache;

import java.util.List;

import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Builds annotations where tokenization, sentence splitting, POS recognition,
 * lemmatizing, named entity recognition and parsing have already been
 * performed.
 * 
 * Parsing is the most expensive task of the above, so it is only applied to the
 * first sentence.
 */
public class RichAnnotationProvider implements AnnotationProvider {

	private static final int DEFAULT_NUM_PARSED_SENTENCES = 1;

	private final int numParsedSentences;

	public RichAnnotationProvider(int numParsedSentences) {
		super();
		this.numParsedSentences = numParsedSentences;
	}

	public RichAnnotationProvider() {
		this(DEFAULT_NUM_PARSED_SENTENCES);
	}

	@Override
	public Annotation getAnnotation(String content) {
		Annotation annotation = new Annotation(content);
		CoreNLPUtil.ensureLemmaAnnotation(annotation);
		CoreNLPUtil.ensureNamedEntityTagAnnotation(annotation);

		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		int numSentences = Math.min(numParsedSentences, sentences.size());
		List<CoreMap> sentencesToParse = sentences.subList(0, numSentences);

		Annotation annotationToParse = CoreNLPUtil
				.sentencesToAnnotation(sentencesToParse);
		CoreNLPUtil.ensureTreeAnnotation(annotationToParse);

		return annotation;
	}

	@Override
	public String getId() {
		return "rich";
	}

}
