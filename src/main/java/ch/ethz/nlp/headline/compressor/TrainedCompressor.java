package ch.ethz.nlp.headline.compressor;

import java.util.ArrayList;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import ch.ethz.nlp.headline.generators.Generator;
import ch.ethz.nlp.headline.learning.RougeScoreRegression;
import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.BinaryHeapPriorityQueue;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PriorityQueue;

public class TrainedCompressor extends TreeCompressor {

	private final svm_model model;
	private PriorityQueue<String> tfIdfMap;

	public TrainedCompressor(svm_model model) {
		this.model = model;
	}

	public Annotation compress(Annotation annotation,
			PriorityQueue<String> tfIdfMap) {
		this.tfIdfMap = tfIdfMap;

		return super.compress(annotation);
	}

	@Override
	protected Tree compress(Tree tree, CoreMap sentence) {
		Annotation annotation = CoreNLPUtil.sentencesToAnnotation(ImmutableList
				.of(sentence));
		CoreNLPUtil
				.ensureCollapsedCCProcessedDependenciesAnnotation(annotation);

		SemanticGraph graph = sentence
				.get(CollapsedCCProcessedDependenciesAnnotation.class);

		PriorityQueue<IndexedWord> queue = new BinaryHeapPriorityQueue<>();
		int estimatedLength = 0;
		for (IndexedWord label : graph.vertexSet()) {
			Optional<svm_node[]> nodes = RougeScoreRegression.buildNodes(graph,
					label, tfIdfMap);
			if (nodes.isPresent()) {
				double prediction = svm.svm_predict(model, nodes.get());
				queue.add(label, -prediction);
			}
			estimatedLength += label.word().length() + 1;
		}

		List<CoreLabel> labelsToRemove = new ArrayList<>();
		while (estimatedLength > Generator.MAX_LENGTH && !queue.isEmpty()) {
			CoreLabel label = queue.removeFirst();
			labelsToRemove.add(label);
			estimatedLength -= label.word().length() + 1;
		}

		BlacklistTreeFilter treeFilter = new BlacklistTreeFilter(labelsToRemove);
		tree = tree.prune(treeFilter);

		return tree;
	}
}
