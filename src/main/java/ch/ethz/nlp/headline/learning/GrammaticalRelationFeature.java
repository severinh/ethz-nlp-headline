package ch.ethz.nlp.headline.learning;

import java.util.Collection;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.GrammaticalRelation;

public class GrammaticalRelationFeature implements Feature {

	private final GrammaticalRelationProvider provider;
	private final GrammaticalRelation relation;

	public GrammaticalRelationFeature(GrammaticalRelationProvider provider,
			GrammaticalRelation relation) {
		super();
		this.provider = provider;
		this.relation = relation;
	}

	@Override
	public double computeValue(IndexedWord label) {
		Collection<GrammaticalRelation> relations = provider
				.getRelations(label);
		return (relations.contains(relation)) ? 1.0 : 0.0;
	}

}
