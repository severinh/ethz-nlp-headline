package ch.ethz.nlp.headline.compressor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

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

		if (!appositiveWords.isEmpty()) {
			BlacklistTreeFilter treeFilter = new BlacklistTreeFilter(
					new ArrayList<>(appositiveWords));

			tree = tree.prune(treeFilter);
			logTrimming(treeFilter.getPrunedWords(), "Appositive");
		}

		return tree;
	}

}
