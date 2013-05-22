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
			Tree newTree = compress(oldTree);

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

	protected abstract Tree compress(Tree tree);

	protected void logTrimming(Tree trimmedTree, String rule) {
		String trimmedText = StringUtils.join(trimmedTree.yieldWords(), " ");
		LOG.debug("Trimming '" + trimmedText + "' [" + rule + "]");
	}

}
