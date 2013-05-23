package ch.ethz.nlp.headline.compressor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableSet;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Filter;

public class HedgeTrimmer extends TreeCompressor {

	private static final Set<String> TRIMMED_LEMMAS = ImmutableSet.of("a",
			"the", "have", "be", "its", "here");

	@Override
	public Tree compress(Tree tree, CoreMap sentence) {
		tree = getLowestLeftmostSWithNPVP(tree);
		tree = removeLowContentNodes(tree);
		if (tree == null) {
			return null;
		}
		// TODO: Currently not used because it makes the performance worse
		// tree = shortenIterativelyRule1(tree);
		tree = shortenIterativelyRule2(tree);

		return tree;
	}

	private Tree getLowestLeftmostSWithNPVP(Tree tree) {
		Tree newTree = getLowestLeftmostSWithNPVPImpl(tree);
		if (newTree == null) {
			// Account for cases where the first sentence is only a fragment
			newTree = tree;
		}
		return newTree;
	}

	private Tree getLowestLeftmostSWithNPVPImpl(Tree tree) {
		Tree result = null;
		if (tree.value().equals("S")) {
			boolean hasNPChild = false;
			boolean hasVPChild = false;
			for (Tree child : tree.children()) {
				if (child.value().equals("NP")) {
					hasNPChild = true;
				} else if (child.value().equals("VP")) {
					hasVPChild = true;
				}
				if (hasNPChild && hasVPChild) {
					break;
				}
			}
			if (hasNPChild && hasVPChild) {
				result = tree;
			}
		}
		// Only consider the left-most child
		Tree firstChild = tree.firstChild();
		if (firstChild != null) {
			Tree childResult = getLowestLeftmostSWithNPVPImpl(firstChild);
			if (childResult != null) {
				result = childResult;
			}
		}

		return result;
	}

	private Tree removeLowContentNodes(Tree tree) {
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

	@SuppressWarnings("unused")
	private Tree shortenIterativelyRule1(Tree fullTree) {
		List<CoreLabel> candidates = new ArrayList<>();
		Set<String> XP = ImmutableSet.of("NP", "VP", "S");

		for (Tree tree : fullTree) {
			if (XP.contains(tree.value())) {
				Tree[] children = tree.children();
				if (children[0].value().equals(tree.value())) {
					for (int i = 1; i < children.length; i++) {
						candidates.add((CoreLabel) children[i].label());
					}
				}
			}
		}

		if (!candidates.isEmpty()) {
			// Just eliminate the right-most, deepest candidate for the moment
			CoreLabel candidate = candidates.get(candidates.size() - 1);
			BlacklistTreeFilter treeFilter = new BlacklistTreeFilter(candidate);
			fullTree = fullTree.prune(treeFilter);

			logTrimming(treeFilter, "Iterative Shortening Rule 1");
		}

		return fullTree;
	}

	private Tree shortenIterativelyRule2(Tree tree) {
		if (tree.value().equals("S")) {
			while (tree.firstChild() != null
					&& !tree.firstChild().value().equals("NP")) {
				logTrimming(tree.firstChild(), "Iterative Shortening Rule 2");
				tree.removeChild(0);
			}
		}
		return tree;
	}

}
