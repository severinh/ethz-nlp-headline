package ch.ethz.nlp.headline.compressor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Filter;

public class PersonNameCompressor extends TreeCompressor {

	@Override
	public Annotation compress(Annotation annotation) {
		CoreNLPUtil.ensureNamedEntityTagAnnotation(annotation);

		return super.compress(annotation);
	}

	private boolean isPerson(CoreLabel label) {
		return label.ner() != null && label.ner().equals("PERSON");
	}

	@Override
	protected Tree compress(Tree tree) {
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

}
