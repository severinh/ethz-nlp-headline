package ch.ethz.nlp.headline.compressor;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Filter;

public class LowContentLemmaPruner extends TreeCompressor {

	private static final Set<String> TRIMMED_LEMMAS = ImmutableSet.of("a",
			"the", "have", "be", "its", "here");

	@Override
	protected Tree compress(Tree tree, CoreMap sentence) {
		tree = tree.prune(new Filter<Tree>() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean accept(Tree tree) {
				CoreLabel label = (CoreLabel) tree.label();
				String lemma = label.lemma();
				if (TRIMMED_LEMMAS.contains(lemma)) {
					return false;
				}
				return true;
			}

		});

		return tree;
	}

}
