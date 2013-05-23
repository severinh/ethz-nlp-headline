package ch.ethz.nlp.headline.compressor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Stack;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Filter;

public class DatePruner extends TreeCompressor {

	@Override
	protected Tree compress(Tree tree, CoreMap sentence) {
		tree = tree.prune(new Filter<Tree>() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean accept(Tree tree) {
				// Remove [PP … [NNP [X] …] …] where X is a tagged as part of a
				// time expression
				if (tree.value().equals("PP")) {
					// Only allow at most 3 leaves to be removed. Otherwise,
					// large parts of the sentence might be trimmed by accident.
					if (tree.getLeaves().size() <= 3) {
						Stack<Tree> treeStack = new Stack<>();
						treeStack.addAll(Arrays.asList(tree.children()));
						while (!treeStack.isEmpty()) {
							Tree child = treeStack.pop();
							if (!child.value().equals("PP")) {
								CoreLabel childLabel = (CoreLabel) child
										.label();
								String ner = childLabel.ner();
								if (Objects.equals(ner, "DATE")) {
									logTrimming(tree, "Date");
									return false;
								} else {
									treeStack.addAll(Arrays.asList(child
											.children()));
								}
							}
						}
					}
				}
				// Remove [NNP [X]] where X is a tagged as part of a
				// time expression
				if (tree.value().equals("NNP")) {
					CoreLabel childLabel = (CoreLabel) tree.firstChild()
							.label();
					String ner = childLabel.ner();
					if (Objects.equals(ner, "DATE")) {
						logTrimming(tree, "Date");
						return false;
					}
				}

				return true;
			}

		});

		return tree;
	}

}
