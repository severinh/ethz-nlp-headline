package ch.ethz.nlp.headline.compressor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Filter;
import edu.stanford.nlp.util.StringUtils;

public class PersonNameSimplifier implements SentencesCompressor {

	private static final Logger LOG = LoggerFactory
			.getLogger(PersonNameSimplifier.class);

	@Override
	public Annotation compress(Annotation annotation) {
		CoreNLPUtil.ensureNamedEntityTagAnnotation(annotation);

		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			Tree tree = sentence.get(TreeAnnotation.class);
			sentence.set(TreeAnnotation.class, compress(tree));
		}

		return annotation;
	}

	private boolean isPerson(CoreLabel label) {
		return label.ner() != null && label.ner().equals("PERSON");
	}

	private Tree compress(Tree tree) {
		final Set<String> namesToRemove = new HashSet<>();
		List<Label> labels = tree.yield();
		for (int i = 0; i < labels.size() - 1; i++) {
			CoreLabel thisLabel = (CoreLabel) labels.get(i);
			CoreLabel nextLabel = (CoreLabel) labels.get(i + 1);
			if (isPerson(thisLabel) && isPerson(nextLabel)) {
				namesToRemove.add(thisLabel.originalText());
			}
		}

		tree = tree.prune(new Filter<Tree>() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean accept(Tree tree) {
				CoreLabel label = (CoreLabel) tree.label();
				String text = label.originalText();
				if (namesToRemove.contains(text)) {
					logTrimming(tree, "First Name");
					return false;
				}
				return true;
			}

		});

		return tree;
	}

	private void logTrimming(Tree trimmedTree, String rule) {
		String trimmedText = StringUtils.join(trimmedTree.yieldWords(), " ");
		LOG.debug("Trimming '" + trimmedText + "' [" + rule + "]");
	}

}
