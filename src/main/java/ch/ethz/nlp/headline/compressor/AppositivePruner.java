package ch.ethz.nlp.headline.compressor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

		for (IndexedWord root : getAppositiveRoots(graph)) {
			Set<IndexedWord> blacklist = getChildrenRecursively(graph, root);
			BlacklistTreeFilter treeFilter = new BlacklistTreeFilter(blacklist);

			tree = tree.prune(treeFilter);
			logTrimming(treeFilter.getPrunedWords(), "Appositive");
		}

		return tree;
	}

	private List<IndexedWord> getAppositiveRoots(SemanticGraph graph) {
		List<IndexedWord> appositiveRoots = new ArrayList<>();
		for (IndexedWord word : graph.vertexSet()) {
			Set<GrammaticalRelation> relations = graph.relns(word);
			for (GrammaticalRelation relation : relations) {
				if (Objects.equals(relation.getShortName(), "appos")) {
					appositiveRoots.add(word);
				}
			}
		}
		return appositiveRoots;
	}

	private Set<IndexedWord> getChildrenRecursively(SemanticGraph graph,
			IndexedWord root) {
		Set<IndexedWord> result = new HashSet<>();

		Stack<IndexedWord> stack = new Stack<>();
		stack.add(root);
		while (!stack.isEmpty()) {
			IndexedWord word = stack.pop();
			result.add(word);
			for (IndexedWord child : graph.getChildren(word)) {
				if (!result.contains(child)) {
					stack.add(child);
				}
			}
		}

		return result;
	}
}
