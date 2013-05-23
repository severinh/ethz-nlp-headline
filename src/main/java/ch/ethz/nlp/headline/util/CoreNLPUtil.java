package ch.ethz.nlp.headline.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.stanford.nlp.dcoref.CoNLL2011DocumentReader.NamedEntityAnnotation;
import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.ie.regexp.NumberSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.parser.tools.PunctEquivalenceClasser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.MorphaAnnotator;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.PTBTokenizerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotatorUtils;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

public final class CoreNLPUtil {

	private static PTBTokenizerAnnotator TOKENIZER_INSTANCE;
	private static WordsToSentencesAnnotator SENTENCE_SPLITTER_INSTANCE;
	private static POSTaggerAnnotator POS_TAGGER_INSTANCE;
	private static MorphaAnnotator LEMMATIZER_INSTANCE;
	private static ParserAnnotator PARSER_INSTANCE;
	private static NERCombinerAnnotator NER_INSTANCE;
	private static MaxentTagger TAGGER_INSTANCE;

	private CoreNLPUtil() {
		// nop
	}

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
			Properties properties = new Properties();
			properties
					.setProperty("parse.buildgraphs", Boolean.toString(false));
			PARSER_INSTANCE = new ParserAnnotator("parse", properties);
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

	public static MaxentTagger getTagger() {
		if (TAGGER_INSTANCE == null) {
			try {
				String modelFile = MaxentTagger.DEFAULT_JAR_PATH;
				TAGGER_INSTANCE = new MaxentTagger(modelFile);
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		return TAGGER_INSTANCE;
	}

	public static void ensureTokensAnnotation(Annotation annotation) {
		if (!annotation.has(TokensAnnotation.class)) {
			getTokenizer().annotate(annotation);
		}
	}

	public static void ensureSentencesAnnotation(Annotation annotation) {
		ensureTokensAnnotation(annotation);

		if (!annotation.has(SentencesAnnotation.class)) {
			getSentenceSplitter().annotate(annotation);
		}
	}

	public static void ensurePartOfSpeechAnnotation(Annotation annotation) {
		ensureSentencesAnnotation(annotation);

		CoreMap sentence = annotation.get(SentencesAnnotation.class).get(0);
		CoreLabel firstLabel = sentence.get(TokensAnnotation.class).get(0);

		if (!firstLabel.has(PartOfSpeechAnnotation.class)) {
			getPosTagger().annotate(annotation);
		}
	}

	public static void ensureLemmaAnnotation(Annotation annotation) {
		ensurePartOfSpeechAnnotation(annotation);

		CoreMap sentence = annotation.get(SentencesAnnotation.class).get(0);
		CoreLabel firstLabel = sentence.get(TokensAnnotation.class).get(0);

		if (!firstLabel.has(LemmaAnnotation.class)) {
			getLemmatizer().annotate(annotation);
		}
	}

	public static void ensureTreeAnnotation(Annotation annotation) {
		ensureSentencesAnnotation(annotation);

		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			if (!sentence.has(TreeAnnotation.class)) {
				List<CoreMap> singleton = ImmutableList.of(sentence);
				Annotation sentenceAnnotation = sentencesToAnnotation(singleton);
				getParser().annotate(sentenceAnnotation);
			}
		}
	}

	public static void ensureBasicDependencyAnnotation(Annotation annotation) {
		ensureTreeAnnotation(annotation);

		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			if (!sentence.has(BasicDependenciesAnnotation.class)) {
				Tree tree = sentence.get(TreeAnnotation.class);
				SemanticGraph graph = ParserAnnotatorUtils
						.generateUncollapsedDependencies(tree);
				sentence.set(BasicDependenciesAnnotation.class, graph);
			}
		}
	}

	public static void ensureNamedEntityTagAnnotation(Annotation annotation) {
		ensureLemmaAnnotation(annotation);

		CoreMap sentence = annotation.get(SentencesAnnotation.class).get(0);
		CoreLabel firstLabel = sentence.get(TokensAnnotation.class).get(0);

		if (!firstLabel.has(NamedEntityAnnotation.class)) {
			getNER().annotate(annotation);
		}
	}

	public static Set<String> getNGrams(Annotation annotation, int n) {
		ensureTokensAnnotation(annotation);
		Set<String> result = new HashSet<>();
		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
			for (int i = 0; i < labels.size() - n + 1; i++) {
				List<CoreLabel> subLabels = labels.subList(i, i + n);
				List<String> lemmas = Lists.transform(subLabels,
						LEMMA_TO_STRING);
				String nGram = StringUtils.join(lemmas, " ");
				result.add(nGram);
			}
		}

		return result;
	}

	private static final Function<CoreLabel, String> LEMMA_TO_STRING = new Function<CoreLabel, String>() {

		@Override
		public String apply(CoreLabel input) {
			return input.lemma();
		}

	};

	public static Annotation sentencesToAnnotation(List<CoreMap> sentences) {
		// The text is probably only for debugging convenience
		// The CharacterOffset*Annotation and Token*Annotation of individual
		// sentences will not be in sync with the new text, but rather refer to
		// the original text. This may not necessarily be a problem.
		Annotation result = new Annotation(sentencesToText(sentences));
		result.set(SentencesAnnotation.class, sentences);

		return result;
	}

	private static String sentencesToText(List<CoreMap> sentences) {
		StringBuilder builder = new StringBuilder();
		for (CoreMap sentence : sentences) {
			builder.append(sentence.get(TextAnnotation.class));
			builder.append(" ");
		}
		return builder.toString().trim();
	}

	public static boolean isPunctuation(String word) {
		return !PunctEquivalenceClasser.getPunctClass(word).isEmpty();
	}

}
