package ch.ethz.nlp.headline.compressor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

public abstract class TreeCompressor implements SentencesCompressor {

	private static final Logger LOG = LoggerFactory
			.getLogger(TreeCompressor.class);

	@Override
	public Annotation compress(Annotation annotation) {
		CoreNLPUtil.ensureTreeAnnotation(annotation);

		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			Tree oldTree = sentence.get(TreeAnnotation.class);
			Tree newTree = compress(oldTree, sentence);
			if (newTree == null) {
				continue;
			}
			// Rebuild the list of labels from the modified tree
			List<CoreLabel> coreLabels = new ArrayList<>();
			for (Label label : newTree.yield()) {
				coreLabels.add((CoreLabel) label);
			}
			sentence.set(TokensAnnotation.class, coreLabels);
			sentence.set(TreeAnnotation.class, newTree);
		}

		return annotation;
	}

	/**
	 * The second argument is just there in case the subclass needs more than
	 * just the tree.
	 */
	protected abstract Tree compress(Tree tree, CoreMap sentence);

	protected void logTrimming(BlacklistTreeFilter filter, String rule) {
		for (Tree prunedTree : filter.getPrunedTrees()) {
			logTrimming(prunedTree, rule);
		}
	}

	protected void logTrimming(Tree trimmedTree, String rule) {
		logTrimming(trimmedTree.yieldWords(), rule);
	}

	protected void logTrimming(List<Word> trimmedWords, String rule) {
		String trimmedText = StringUtils.join(trimmedWords, " ");
		LOG.debug("Trimming '" + trimmedText + "' [" + rule + "]");
	}

}
