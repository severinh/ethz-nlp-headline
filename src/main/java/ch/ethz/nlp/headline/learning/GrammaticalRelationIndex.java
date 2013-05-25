package ch.ethz.nlp.headline.learning;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;

public class GrammaticalRelationIndex {

	private final ImmutableList<GrammaticalRelation> relations;
	private final Map<GrammaticalRelation, Integer> map;

	public GrammaticalRelationIndex(List<GrammaticalRelation> relations) {
		super();

		this.relations = ImmutableList.copyOf(relations);
		this.map = new LinkedHashMap<>();
		for (int i = 0; i < relations.size(); i++) {
			map.put(relations.get(i), i);
		}
	}

	public ImmutableList<GrammaticalRelation> getRelations() {
		return relations;
	}

	public int getIndex(GrammaticalRelation relation) {
		return map.get(relation);
	}

	public static GrammaticalRelationIndex makeDefault() {
		List<GrammaticalRelation> relations = EnglishGrammaticalRelations
				.values();
		return new GrammaticalRelationIndex(relations);
	}

	public static void main(String[] args) {

	}

}
