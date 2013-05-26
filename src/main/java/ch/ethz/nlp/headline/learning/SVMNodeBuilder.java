package ch.ethz.nlp.headline.learning;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.util.PriorityQueue;
import libsvm.svm_node;

public class SVMNodeBuilder {

	private final List<Feature> features;

	public SVMNodeBuilder(List<Feature> features) {
		super();
		this.features = features;
	}

	public static SVMNodeBuilder makeDefault(SemanticGraph graph,
			PriorityQueue<String> tfIdfMap) {
		List<Feature> features = new ArrayList<>();
		features.add(new NamedEntityFeature("DATE"));
		features.add(new NamedEntityFeature("PERSON"));
		features.add(new NamedEntityFeature("LOCATION"));
		features.add(new TfIdfFeature(tfIdfMap));

		GrammaticalRelationProvider provider = GrammaticalRelationProvider
				.ofLabels(graph);
		for (GrammaticalRelation relation : EnglishGrammaticalRelations
				.values()) {
			features.add(new GrammaticalRelationFeature(provider, relation));
		}

		SVMNodeBuilder builder = new SVMNodeBuilder(features);
		return builder;
	}

	public svm_node[] build(IndexedWord label) {
		List<svm_node> nodes = new ArrayList<>();
		for (int i = 0; i < features.size(); i++) {
			double value = features.get(i).computeValue(label);
			if (value != 0.0) {
				svm_node node = new svm_node();
				node.index = i;
				node.value = value;
				nodes.add(node);
			}
		}
		return nodes.toArray(new svm_node[nodes.size()]);
	}

}
