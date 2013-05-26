package ch.ethz.nlp.headline.learning;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

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
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PriorityQueue;

public class RougeScoreRegression {

	private static final Logger LOG = LoggerFactory
			.getLogger(RougeScoreRegression.class);

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

			SVMNodeBuilder nodeBuilder = SVMNodeBuilder.makeDefault(graph,
					tfIdfMap);

			for (IndexedWord word : graph.vertexSet()) {
				double label = (rouge.contains(word)) ? 1.0 : 0.0;
				svm_node[] nodes = nodeBuilder.build(word);
				problemBuilder.addExample(nodes, label);
			}
		}

		return problemBuilder.build();
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
