package ch.ethz.nlp.headline.compressor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class PersonNameCompressor extends TreeCompressor {

	@Override
	public Annotation compress(Annotation annotation) {
		CoreNLPUtil.ensureNamedEntityTagAnnotation(annotation);

		return super.compress(annotation);
	}

	private boolean isPerson(CoreLabel label) {
		return Objects.equals(label.ner(), "PERSON");
	}

	@Override
	protected Tree compress(Tree tree, CoreMap sentence) {
		List<CoreLabel> namesToRemove = new ArrayList<>();
		List<Label> labels = tree.yield();
		for (int i = 0; i < labels.size() - 1; i++) {
			CoreLabel thisLabel = (CoreLabel) labels.get(i);
			CoreLabel nextLabel = (CoreLabel) labels.get(i + 1);
			if (isPerson(thisLabel) && isPerson(nextLabel)) {
				namesToRemove.add(thisLabel);
			}
		}

		BlacklistTreeFilter treeFilter = new BlacklistTreeFilter(namesToRemove);
		tree = tree.prune(treeFilter);
		logTrimming(treeFilter, "First Name");

		return tree;
	}

}
