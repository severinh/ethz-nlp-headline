package ch.ethz.nlp.headline.learning;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;

public class GrammaticalRelationProvider {

	private final SemanticGraph graph;
	private final Map<IndexedWord, Set<GrammaticalRelation>> map;

	public GrammaticalRelationProvider(SemanticGraph graph) {
		this.graph = graph;
		this.map = new HashMap<>();
	}

	public Set<GrammaticalRelation> getRelations(IndexedWord label) {
		Set<GrammaticalRelation> result = map.get(label);
		if (result == null) {
			result = graph.relns(label);
			map.put(label, result);
		}
		return result;
	}

}
