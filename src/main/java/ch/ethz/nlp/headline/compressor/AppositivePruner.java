package ch.ethz.nlp.headline.compressor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.BeginIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.EndIndexAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Filter;
import edu.stanford.nlp.util.StringUtils;

public class AppositivePruner extends TreeCompressor {

	@Override
	protected Tree compress(Tree tree, CoreMap sentence) {
		SemanticGraph graph = sentence.get(BasicDependenciesAnnotation.class);

		final Set<IndexedWord> appositiveWords = new HashSet<>();

		Set<IndexedWord> words = graph.vertexSet();
		for (IndexedWord word : words) {
			Set<GrammaticalRelation> relations = graph.relns(word);
			for (GrammaticalRelation relation : relations) {
				if (Objects.equals(relation.getShortName(), "appos")) {
					Stack<IndexedWord> stack = new Stack<>();
					stack.add(word);
					while (!stack.isEmpty()) {
						IndexedWord appositiveWord = stack.pop();
						appositiveWords.add(appositiveWord);
						for (IndexedWord child : graph
								.getChildren(appositiveWord)) {
							if (!appositiveWords.contains(child)) {
								stack.add(child);
							}
						}
					}
				}
			}
		}

		final List<String> prunedWords = new ArrayList<>();

		tree = tree.prune(new Filter<Tree>() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean accept(Tree tree) {
				CoreLabel label = (CoreLabel) tree.label();
				if (label != null && label.word() != null) {
					for (IndexedWord appositiveLabel : appositiveWords) {
						if (Objects.equals(
								label.get(BeginIndexAnnotation.class),
								appositiveLabel.get(BeginIndexAnnotation.class))
								&& Objects.equals(label
										.get(EndIndexAnnotation.class),
										appositiveLabel
												.get(EndIndexAnnotation.class))) {
							prunedWords.add(label.word());
							return false;
						}
					}

				}
				return true;
			}

		});

		String trimmedText = StringUtils.join(prunedWords, " ");
		logTrimming(trimmedText, "Appositive");

		return tree;
	}
}
