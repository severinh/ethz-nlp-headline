package ch.ethz.nlp.headline.learning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.Task;
import ch.ethz.nlp.headline.cache.AnnotationCache;
import ch.ethz.nlp.headline.cache.AnnotationProvider;
import ch.ethz.nlp.headline.cache.SlimAnnotationProvider;
import ch.ethz.nlp.headline.duc.Duc2004Dataset;
import ch.ethz.nlp.headline.selection.BestSentenceSelector;
import ch.ethz.nlp.headline.util.RougeN;
import ch.ethz.nlp.headline.util.RougeNFactory;
import edu.stanford.nlp.ling.CoreAnnotations.SentencePositionAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Computes for each document the position of the sentence that achieves the
 * highest ROUGE-N score and prints the distribution of these best positions
 * over all documents.
 */
public class BestSentencePosDistribution {

	private final AnnotationProvider annotationProvider;
	private final RougeNFactory rougeFactory;

	public BestSentencePosDistribution(AnnotationProvider annotationProvider,
			RougeNFactory rougeFactory) {
		super();
		this.annotationProvider = annotationProvider;
		this.rougeFactory = rougeFactory;
	}

	public void compute(List<Task> tasks) throws IOException {
		Map<Integer, Integer> posFreqs = new HashMap<>();

		for (Task task : tasks) {
			int bestPos = getBestSentencePos(task);
			Integer oldCount = posFreqs.get(bestPos);
			if (oldCount == null) {
				posFreqs.put(bestPos, 1);
			} else {
				posFreqs.put(bestPos, oldCount + 1);
			}
		}

		List<Integer> positions = new ArrayList<>(posFreqs.keySet());
		Collections.sort(positions);
		System.out.println();
		for (Integer position : positions) {
			int documentCount = posFreqs.get(position);
			System.out.println(position + "\t" + documentCount);
		}
	}

	/**
	 * Finds the position of the sentence in the given document that achieves
	 * the best ROUGE-N scores w.r.t. to the reference summaries.
	 * 
	 * @param task
	 *            the document and the corresponding models
	 * @return the position of the best sentence in the document
	 * @throws IOException
	 */
	public int getBestSentencePos(Task task) {
		Document document = task.getDocument();
		Annotation documentAnnotation = annotationProvider
				.getAnnotation(document.getContent());

		RougeN rouge = rougeFactory.make(task.getModels(), annotationProvider);
		BestSentenceSelector sentenceSelector = new BestSentenceSelector(rouge);
		Annotation bestAnnotation = sentenceSelector.select(documentAnnotation);
		CoreMap sentence = bestAnnotation.get(SentencesAnnotation.class).get(0);
		String bestPos = sentence.get(SentencePositionAnnotation.class);

		return Integer.valueOf(bestPos);
	}

	public static void main(String[] args) throws IOException {
		Dataset dataset = Duc2004Dataset.ofDefaultRoot();
		List<Task> tasks = dataset.getTasks();
		AnnotationProvider provider = new AnnotationCache(
				new SlimAnnotationProvider());
		RougeNFactory rougeFactory = new RougeNFactory(1);
		BestSentencePosDistribution distribution = new BestSentencePosDistribution(
				provider, rougeFactory);
		distribution.compute(tasks);
	}

}
