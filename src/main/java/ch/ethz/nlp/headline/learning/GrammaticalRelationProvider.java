package ch.ethz.nlp.headline.learning;

import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphEdge;

public class GrammaticalRelationProvider {

	private final Multimap<IndexedWord, GrammaticalRelation> map;

	private GrammaticalRelationProvider() {
		this.map = HashMultimap.create();
	}

	public static GrammaticalRelationProvider ofLabels(SemanticGraph graph) {
		GrammaticalRelationProvider provider = new GrammaticalRelationProvider();

		for (SemanticGraphEdge edge : graph.edgeIterable()) {
			GrammaticalRelation relation = edge.getRelation();
			IndexedWord dependent = edge.getDependent();
			provider.map.put(dependent, relation);
		}

		return provider;
	}

	public Collection<GrammaticalRelation> getRelations(IndexedWord label) {
		return map.get(label);
	}

}
