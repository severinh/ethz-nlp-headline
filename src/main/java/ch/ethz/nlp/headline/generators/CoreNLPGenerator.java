package ch.ethz.nlp.headline.generators;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ch.ethz.nlp.headline.Document;
import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.ie.regexp.NumberSequenceClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.MorphaAnnotator;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.PTBTokenizerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.util.CoreMap;

public abstract class CoreNLPGenerator implements Generator {

	private static PTBTokenizerAnnotator TOKENIZER_INSTANCE;
	private static WordsToSentencesAnnotator SENTENCE_SPLITTER_INSTANCE;
	private static POSTaggerAnnotator POS_TAGGER_INSTANCE;
	private static MorphaAnnotator LEMMATIZER_INSTANCE;
	private static ParserAnnotator PARSER_INSTANCE;
	private static NERCombinerAnnotator NER_INSTANCE;

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

	public static Annotator getLemmatizer() {
		if (LEMMATIZER_INSTANCE == null) {
			LEMMATIZER_INSTANCE = new MorphaAnnotator(false);
		}
		return LEMMATIZER_INSTANCE;
	}

	public static Annotator getParser() {
		if (PARSER_INSTANCE == null) {
			PARSER_INSTANCE = new ParserAnnotator(false, Integer.MAX_VALUE);
		}
		return PARSER_INSTANCE;
	}

	public static Annotator getNER() {
		if (NER_INSTANCE == null) {
			String[] models = new String[] {
					DefaultPaths.DEFAULT_NER_THREECLASS_MODEL,
					DefaultPaths.DEFAULT_NER_MUC_MODEL,
					DefaultPaths.DEFAULT_NER_CONLL_MODEL };
			NERClassifierCombiner nerCombiner = null;
			boolean applyNumericClassifiers = NERClassifierCombiner.APPLY_NUMERIC_CLASSIFIERS_DEFAULT;
			boolean useSUTime = NumberSequenceClassifier.USE_SUTIME_DEFAULT;
			try {
				nerCombiner = new NERClassifierCombiner(
						applyNumericClassifiers, useSUTime, models);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			NER_INSTANCE = new NERCombinerAnnotator(nerCombiner, false);
		}
		return NER_INSTANCE;
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
	public Annotation getTokenizedSentenceDocumentAnnotation(Document document)
			throws IOException {
		Annotation resultAnnotation = getTokenizedDocumentAnnotation(document);
		getSentenceSplitter().annotate(resultAnnotation);
		return resultAnnotation;
	}

	public Annotation makeAnnotationFromSentence(CoreMap sentence) {
		Annotation result = new Annotation(sentence.get(TextAnnotation.class));
		List<CoreMap> sentences = Collections.singletonList(sentence);
		result.set(SentencesAnnotation.class, sentences);
		return result;
	}

}
