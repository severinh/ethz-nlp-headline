package ch.ethz.nlp.headline.generators;

import java.io.IOException;

import ch.ethz.nlp.headline.Document;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.PTBTokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;

public abstract class CoreNLPGenerator implements Generator {

	private static PTBTokenizerAnnotator TOKENIZER_INSTANCE;
	private static WordsToSentencesAnnotator SENTENCE_SPLITTER_INSTANCE;
	private static POSTaggerAnnotator POS_TAGGER_INSTANCE;
	
	public static Annotator getTokenizer() {
		if (TOKENIZER_INSTANCE == null) {
			TOKENIZER_INSTANCE = new PTBTokenizerAnnotator(false,
					PTBTokenizerAnnotator.DEFAULT_OPTIONS);
		}
		return TOKENIZER_INSTANCE;
	}
	
	public static Annotator getSentenceSplitter() {
		if (SENTENCE_SPLITTER_INSTANCE == null) {
			SENTENCE_SPLITTER_INSTANCE = new WordsToSentencesAnnotator();
		}
		return SENTENCE_SPLITTER_INSTANCE;
	}
	
	public static Annotator getPosTagger() {
		if (POS_TAGGER_INSTANCE == null) {
			POS_TAGGER_INSTANCE = new POSTaggerAnnotator(
					DefaultPaths.DEFAULT_POS_MODEL, false);
		}
		return POS_TAGGER_INSTANCE;
	}

	/**
	 * Create an annotation with the tokens of the given document.
	 * 
	 * @param document
	 */
	public Annotation getTokenizedDocumentAnnotation(Document document)
			throws IOException {
		String content = document.load();
		Annotation annotation = new Annotation(content);
		getTokenizer().annotate(annotation);
		return annotation;
	}
	
	/**
	 * Create an annotation with tokens and sentences of the given document.
	 */
	public Annotation getTokenizedSentenceDocumentAnnotation(Document document) throws IOException {
		Annotation resultAnnotation = getTokenizedDocumentAnnotation(document);
		getSentenceSplitter().annotate(resultAnnotation);
		return resultAnnotation;
	}
}
