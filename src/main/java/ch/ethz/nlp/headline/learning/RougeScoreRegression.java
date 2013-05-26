package ch.ethz.nlp.headline.learning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import com.google.common.io.Files;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Model;
import ch.ethz.nlp.headline.Task;
import ch.ethz.nlp.headline.cache.AnnotationCache;
import ch.ethz.nlp.headline.cache.AnnotationProvider;
import ch.ethz.nlp.headline.cache.RichAnnotationProvider;
import ch.ethz.nlp.headline.duc.Duc2003Dataset;
import ch.ethz.nlp.headline.duc.Duc2004Dataset;
import ch.ethz.nlp.headline.preprocessing.CombinedPreprocessor;
import ch.ethz.nlp.headline.preprocessing.ContentPreprocessor;
import ch.ethz.nlp.headline.selection.BestSentenceSelector;
import ch.ethz.nlp.headline.selection.SentencesSelector;
import ch.ethz.nlp.headline.util.CoreNLPUtil;
import ch.ethz.nlp.headline.util.RougeN;
import ch.ethz.nlp.headline.util.RougeNFactory;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class RougeScoreRegression {

	private static final Logger LOG = LoggerFactory
			.getLogger(RougeScoreRegression.class);

	public static final GrammaticalRelationIndex RELATION_INDEX;
	public static final int NAMED_ENTITY_INDEX;

	static {
		RELATION_INDEX = GrammaticalRelationIndex.makeDefault();
		NAMED_ENTITY_INDEX = 1;
	}

	public static final int getRelationIndex(GrammaticalRelation relation) {
		return RELATION_INDEX.getIndex(relation) + 2;
	}

	private final AnnotationProvider annotationProvider;
	private final RougeNFactory rougeFactory;
	private final ContentPreprocessor preprocessor = CombinedPreprocessor.all();

	public RougeScoreRegression(AnnotationProvider annotationProvider) {
		super();
		this.annotationProvider = annotationProvider;
		this.rougeFactory = new RougeNFactory(1);
	}

	public svm_problem buildProblem(List<Task> tasks) {
		List<Double> labels = new ArrayList<>(15000);
		List<svm_node[]> nodeMatrix = new ArrayList<>(15000);

		for (int i = 0; i < tasks.size(); i++) {
			Task task = tasks.get(i);
			LOG.info(String.format("Processing task %d of %d: %s", i + 1,
					tasks.size(), task.getDocument().getId()));

			List<Model> models = task.getModels();
			RougeN rouge = rougeFactory.make(models, annotationProvider);
			SentencesSelector sentenceSelector = new BestSentenceSelector(rouge);

			String content = task.getDocument().getContent();
			content = preprocessor.preprocess(content);
			Annotation documentAnnotation = annotationProvider
					.getAnnotation(content);
			Annotation bestAnnotation = sentenceSelector
					.select(documentAnnotation);
			CoreMap bestSentence = bestAnnotation
					.get(SentencesAnnotation.class).get(0);

			CoreNLPUtil
					.ensureCollapsedCCProcessedDependenciesAnnotation(bestAnnotation);

			SemanticGraph graph = bestSentence
					.get(CollapsedCCProcessedDependenciesAnnotation.class);

			for (IndexedWord word : graph.vertexSet()) {
				Set<GrammaticalRelation> relations = graph.relns(word);
				if (!relations.isEmpty()) {
					GrammaticalRelation relation = relations.iterator().next();
					// For some reason, collapsed dependencies such as 'prep_in'
					// are not in the index yet
					if (!RELATION_INDEX.contains(relation)) {
						continue;
					}
					double recall = rouge.compute(word);
					if (recall > 0.0) {
						recall = 1.0;
					}
					boolean isNamedEntity = !Objects.equals(word.ner(), "O");

					svm_node[] nodes = new svm_node[2];

					nodes[0] = new svm_node();
					nodes[0].index = NAMED_ENTITY_INDEX;
					nodes[0].value = (isNamedEntity) ? 1.0f : 0.0f;

					nodes[1] = new svm_node();
					nodes[1].index = getRelationIndex(relation);
					nodes[1].value = 1.0f;

					nodeMatrix.add(nodes);
					labels.add(recall);
				}
			}
		}

		svm_problem problem = new svm_problem();
		problem.l = labels.size();
		problem.y = new double[problem.l];
		problem.x = new svm_node[problem.l][];
		for (int i = 0; i < problem.l; i++) {
			problem.y[i] = labels.get(i);
			problem.x[i] = nodeMatrix.get(i);
		}

		return problem;
	}

	public static void main(String[] args) throws IOException {
		AnnotationProvider cache = new AnnotationCache(
				new RichAnnotationProvider());
		RougeScoreRegression regression = new RougeScoreRegression(cache);

		Dataset trainDataset = Duc2003Dataset.ofDefaultRoot();
		svm_problem trainProblem = regression.buildProblem(trainDataset
				.getTasks());
		File trainProblemFile = new File("rouge.train");
		Files.write(SVMProblemWriter.toString(trainProblem).getBytes(),
				trainProblemFile);

		Dataset testDataset = Duc2004Dataset.ofDefaultRoot();
		svm_problem testProblem = regression.buildProblem(testDataset
				.getTasks());
		File testProblemFile = new File("rouge.test");
		Files.write(SVMProblemWriter.toString(testProblem).getBytes(),
				testProblemFile);

		File modelFile = new File("rouge.model");

		svm_parameter parameter = new svm_parameter();

		// Default values
		parameter.svm_type = svm_parameter.EPSILON_SVR;
		parameter.kernel_type = svm_parameter.RBF;
		parameter.degree = 3;
		parameter.gamma = 0.0625;
		parameter.coef0 = 0;
		parameter.nu = 0.125;
		parameter.cache_size = 40;
		parameter.C = 8;
		parameter.eps = 1e-3;
		parameter.p = 0.1;
		parameter.shrinking = 1;
		parameter.probability = 0;
		parameter.nr_weight = 0;
		parameter.weight_label = new int[0];
		parameter.weight = new double[0];

		svm_model model = svm.svm_train(trainProblem, parameter);
		svm.svm_save_model(modelFile.getName(), model);
	}

}
