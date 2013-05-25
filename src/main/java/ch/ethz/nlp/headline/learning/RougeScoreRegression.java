package ch.ethz.nlp.headline.learning;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.io.Files;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Model;
import ch.ethz.nlp.headline.Task;
import ch.ethz.nlp.headline.cache.AnnotationCache;
import ch.ethz.nlp.headline.cache.AnnotationProvider;
import ch.ethz.nlp.headline.cache.RichAnnotationProvider;
import ch.ethz.nlp.headline.duc.Duc2003Dataset;
import ch.ethz.nlp.headline.duc.Duc2004Dataset;
import ch.ethz.nlp.headline.selection.BestSentenceSelector;
import ch.ethz.nlp.headline.selection.SentencesSelector;
import ch.ethz.nlp.headline.util.CoreNLPUtil;
import ch.ethz.nlp.headline.util.RougeN;
import ch.ethz.nlp.headline.util.RougeNFactory;
import edu.berkeley.compbio.jlibsvm.legacyexec.svm_predict;
import edu.berkeley.compbio.jlibsvm.legacyexec.svm_train;
import edu.berkeley.compbio.jlibsvm.regression.MutableRegressionProblemImpl;
import edu.berkeley.compbio.jlibsvm.util.SparseVector;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class RougeScoreRegression {

	private static final GrammaticalRelationIndex RELATION_INDEX;
	private static final int NAMED_ENTITY_INDEX;

	static {
		RELATION_INDEX = GrammaticalRelationIndex.makeDefault();
		NAMED_ENTITY_INDEX = 1;
	}

	private static final int getRelationIndex(GrammaticalRelation relation) {
		return RELATION_INDEX.getIndex(relation) + 2;
	}

	private final AnnotationProvider annotationProvider;
	private final RougeNFactory rougeFactory;

	public RougeScoreRegression(AnnotationProvider annotationProvider) {
		super();
		this.annotationProvider = annotationProvider;
		this.rougeFactory = new RougeNFactory(1);
	}

	public Problem buildProblem(List<Task> tasks) {
		Problem problem = new Problem(10000);

		for (Task task : tasks) {
			List<Model> models = task.getModels();
			RougeN rouge = rougeFactory.make(models, annotationProvider);
			SentencesSelector sentenceSelector = new BestSentenceSelector(rouge);

			String content = task.getDocument().getContent();
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
					double recall = rouge.compute(word);
					if (recall > 0.0) {
						recall = 1.0;
					}
					boolean isNamedEntity = !Objects.equals(word.ner(), "O");

					SparseVector vector = new SparseVector(2);
					vector.indexes[0] = NAMED_ENTITY_INDEX;
					vector.values[0] = (isNamedEntity) ? 1.0f : 0.0f;

					vector.indexes[1] = getRelationIndex(relation);
					vector.values[1] = 1.0f;

					problem.addExample(vector, (float) recall);
				}
			}
		}

		return problem;
	}

	public static void main(String[] args) throws IOException {
		AnnotationProvider cache = new AnnotationCache(
				new RichAnnotationProvider());
		RougeScoreRegression regression = new RougeScoreRegression(cache);

		Dataset trainDataset = Duc2003Dataset.ofDefaultRoot();
		Problem trainProblem = regression.buildProblem(trainDataset.getTasks());
		File trainProblemFile = new File("rouge.train");
		Files.write(RegressionProblemWriter.toString(trainProblem).getBytes(),
				trainProblemFile);

		Dataset testDataset = Duc2004Dataset.ofDefaultRoot();
		Problem testProblem = regression.buildProblem(testDataset.getTasks());
		File testProblemFile = new File("rouge.test");
		Files.write(RegressionProblemWriter.toString(testProblem).getBytes(),
				testProblemFile);

		File modelFile = new File("rouge.model");
		File outputFile = new File("rouge.out");

		svm_train.main(new String[] { "-s", "3", trainProblemFile.getName(),
				modelFile.getName() });
		svm_predict.main(new String[] { testProblemFile.getName(),
				modelFile.getName(), outputFile.getName() });
	}

	/**
	 * Make the code easier to read.
	 */
	private static class Problem extends
			MutableRegressionProblemImpl<SparseVector> {

		public Problem(int numExamples) {
			super(numExamples);
		}

	}

}
