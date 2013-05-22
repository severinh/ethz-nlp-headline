package ch.ethz.nlp.headline.generators;

import ch.ethz.nlp.headline.compressor.HedgeTrimmer;
import ch.ethz.nlp.headline.compressor.PersonNameSimplifier;
import ch.ethz.nlp.headline.preprocessing.CombinedPreprocessor;
import ch.ethz.nlp.headline.preprocessing.ContentPreprocessor;
import ch.ethz.nlp.headline.selection.FirstSentenceSelector;
import ch.ethz.nlp.headline.selection.SentencesSelector;
import ch.ethz.nlp.headline.util.CoreNLPUtil;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class HedgeTrimmerGenerator extends CoreNLPGenerator {

	private final ContentPreprocessor preprocessor = CombinedPreprocessor.all();
	private final SentencesSelector sentencesSelector = new FirstSentenceSelector();
	private final PersonNameSimplifier nameSimplifier = new PersonNameSimplifier();
	private final HedgeTrimmer hedgeTrimmer = new HedgeTrimmer();

	@Override
	public String getId() {
		return "HEDGE";
	}

	@Override
	public String generate(String content) {
		content = preprocessor.preprocess(content);

		Annotation annotation = new Annotation(content);
		annotation = sentencesSelector.select(annotation);
		annotation = nameSimplifier.compress(annotation);
		annotation = hedgeTrimmer.compress(annotation);

		CoreMap sentence = annotation.get(SentencesAnnotation.class).get(0);
		Tree tree = sentence.get(TreeAnnotation.class);

		String result = CoreNLPUtil.treeToString(tree, Integer.MAX_VALUE);
		String trimmedResult = CoreNLPUtil.treeToString(tree, MAX_LENGTH);

		getStatistics().addSummaryResult(result);
		return trimmedResult;
	}

}
