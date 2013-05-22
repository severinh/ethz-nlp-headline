package ch.ethz.nlp.headline.compressor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.BeginIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.EndIndexAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Filter;
import edu.stanford.nlp.util.Pair;

public class BlacklistTreeFilter implements Filter<Tree> {

	private static final long serialVersionUID = 1L;

	private final Set<LabelSpan> blacklist;
	private final List<Tree> prunedTrees;

	public BlacklistTreeFilter(List<? extends CoreLabel> blacklist) {
		super();

		// Referential equality of labels in the blacklist and the tree is not
		// guaranteed, because labels are copied around a lot. Also, the may not
		// even have the same keys. Thus, only compare the begin and end index
		// annotation, which do not change over time. Also, this makes it
		// possible to very efficiently determine whether a label is in the
		// blacklist.
		this.blacklist = new HashSet<>();
		for (CoreLabel label : blacklist) {
			this.blacklist.add(LabelSpan.of(label));
		}

		this.prunedTrees = new ArrayList<>();
	}

	public BlacklistTreeFilter(CoreLabel blacklistedLabel) {
		this(ImmutableList.of(blacklistedLabel));
	}

	public List<Tree> getPrunedTrees() {
		return Collections.unmodifiableList(prunedTrees);
	}

	@Override
	public boolean accept(Tree tree) {
		CoreLabel label = (CoreLabel) tree.label();
		if (label == null) {
			return true;
		}

		LabelSpan span = LabelSpan.of(label);
		if (blacklist.contains(span)) {
			prunedTrees.add(tree);
			return false;
		}
		return true;
	}

	private static class LabelSpan extends Pair<Integer, Integer> {

		private static final long serialVersionUID = 1L;

		public LabelSpan(Integer beginIndex, Integer endIndex) {
			super(beginIndex, endIndex);
		}

		public static LabelSpan of(CoreLabel label) {
			Integer beginIndex = label.get(BeginIndexAnnotation.class);
			Integer endIndex = label.get(EndIndexAnnotation.class);
			LabelSpan span = new LabelSpan(beginIndex, endIndex);
			return span;
		}

	}

}
