package ch.ethz.nlp.headline.learning;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import com.google.common.base.Optional;
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
import ch.ethz.nlp.headline.selection.TfIdfProvider;
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
import edu.stanford.nlp.util.PriorityQueue;

public class RougeScoreRegression {

	private static final Logger LOG = LoggerFactory
			.getLogger(RougeScoreRegression.class);

	public static final GrammaticalRelationIndex RELATION_INDEX;
	public static final int DATE_INDEX = 1;
	public static final int PERSON_INDEX = 2;
	public static final int LOCATION_INDEX = 3;
	public static final int TFIDF_INDEX = 4;

	static {
		RELATION_INDEX = GrammaticalRelationIndex.makeDefault();
	}

	public static final int getRelationIndex(GrammaticalRelation relation) {
		return RELATION_INDEX.getIndex(relation) + 5;
	}

	private final AnnotationProvider annotationProvider;
	private final RougeNFactory rougeFactory;
	private final ContentPreprocessor preprocessor = CombinedPreprocessor.all();

	public RougeScoreRegression(AnnotationProvider annotationProvider) {
		super();
		this.annotationProvider = annotationProvider;
		this.rougeFactory = new RougeNFactory(1);
	}

	public SVMProblem buildProblem(Dataset dataset) {
		TfIdfProvider tfIdfProvider = TfIdfProvider.of(annotationProvider,
				dataset);

		List<Task> tasks = dataset.getTasks();
		SVMProblemBuilder problemBuilder = new SVMProblemBuilder();

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

			PriorityQueue<String> tfIdfMap = tfIdfProvider
					.getTfIdfMap(documentAnnotation);

			CoreNLPUtil
					.ensureCollapsedCCProcessedDependenciesAnnotation(bestAnnotation);

			SemanticGraph graph = bestSentence
					.get(CollapsedCCProcessedDependenciesAnnotation.class);

			for (IndexedWord word : graph.vertexSet()) {
				Optional<svm_node[]> nodes = buildNodes(graph, word, tfIdfMap);
				if (nodes.isPresent()) {
					double label = (rouge.contains(word)) ? 1.0 : 0.0;
					problemBuilder.addExample(nodes.get(), label);
				}
			}
		}

		return problemBuilder.build();
	}

	public static Optional<svm_node[]> buildNodes(SemanticGraph graph,
			IndexedWord word, PriorityQueue<String> tfIdfMap) {
		Set<GrammaticalRelation> relations = graph.relns(word);
		if (!relations.isEmpty()) {
			GrammaticalRelation relation = relations.iterator().next();
			// For some reason, collapsed dependencies such as 'prep_in'
			// are not in the index yet
			if (!RELATION_INDEX.contains(relation)) {
				return Optional.absent();
			}
			svm_node[] nodes = new svm_node[5];

			nodes[0] = new svm_node();
			nodes[0].index = DATE_INDEX;
			nodes[0].value = (Objects.equals(word.ner(), "DATE")) ? 1.0f : 0.0f;

			nodes[1] = new svm_node();
			nodes[1].index = PERSON_INDEX;
			nodes[1].value = (Objects.equals(word.ner(), "PERSON")) ? 1.0f
					: 0.0f;

			nodes[2] = new svm_node();
			nodes[2].index = LOCATION_INDEX;
			nodes[2].value = (Objects.equals(word.ner(), "LOCATION")) ? 1.0f
					: 0.0f;

			nodes[3] = new svm_node();
			nodes[3].index = TFIDF_INDEX;
			double tfIdfScore = tfIdfMap.getPriority(word.lemma());
			nodes[3].value = Math.max(0.0, tfIdfScore);

			nodes[4] = new svm_node();
			nodes[4].index = getRelationIndex(relation);
			nodes[4].value = 1.0f;
			return Optional.of(nodes);
		}
		return Optional.absent();
	}

	public static void main(String[] args) throws IOException {
		AnnotationProvider cache = new AnnotationCache(
				new RichAnnotationProvider());
		RougeScoreRegression regression = new RougeScoreRegression(cache);

		Dataset trainDataset = Duc2003Dataset.ofDefaultRoot();
		SVMProblem trainProblem = regression.buildProblem(trainDataset);
		File trainProblemFile = new File("rouge.train");
		Files.write(trainProblem.toString().getBytes(), trainProblemFile);

		Dataset testDataset = Duc2004Dataset.ofDefaultRoot();
		SVMProblem testProblem = regression.buildProblem(testDataset);
		File testProblemFile = new File("rouge.test");
		Files.write(testProblem.toString().getBytes(), testProblemFile);

		File modelFile = new File("rouge.model");

		SVMParameter parameter = SVMParameter.makeDefault();

		svm_model model = svm.svm_train(trainProblem, parameter);
		svm.svm_save_model(modelFile.getName(), model);
	}

}
