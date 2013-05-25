package ch.ethz.nlp.headline.learning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.Model;
import ch.ethz.nlp.headline.Task;
import ch.ethz.nlp.headline.duc2004.Duc2004Dataset;
import ch.ethz.nlp.headline.util.CoreNLPUtil;
import ch.ethz.nlp.headline.util.RougeN;
import ch.ethz.nlp.headline.util.RougeNFactory;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Computes for each document the position of the sentence that achieves the
 * highest ROUGE-N score and prints the distribution of these best positions
 * over all documents.
 */
public class BestSentencePosDistribution {

	private final RougeNFactory rougeFactory;

	public BestSentencePosDistribution(RougeNFactory rougeFactory) {
		super();
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
		String documentContent = document.getContent();

		List<Annotation> models = new ArrayList<>();
		for (Model model : task.getModels()) {
			String modelContent = model.getContent();
			models.add(getAnnotation(modelContent));
		}

		RougeN rouge = rougeFactory.make(models);

		Annotation documentAnnotation = getAnnotation(documentContent);
		List<CoreMap> sentences = documentAnnotation
				.get(SentencesAnnotation.class);

		int bestPos = 0;
		double bestRecall = 0.0;

		for (int pos = 0; pos < sentences.size(); pos++) {
			CoreMap sentence = sentences.get(pos);
			Annotation sentenceAnnotation = CoreNLPUtil
					.sentencesToAnnotation(ImmutableList.of(sentence));

			double recall = rouge.compute(sentenceAnnotation);

			if (recall > bestRecall) {
				bestPos = pos;
				bestRecall = recall;
			}
		}

		return bestPos;
	}

	private Annotation getAnnotation(String content) {
		Annotation annotation = new Annotation(content);
		CoreNLPUtil.ensureLemmaAnnotation(annotation);
		return annotation;
	}

	public static void main(String[] args) throws IOException {
		Dataset dataset = Duc2004Dataset.ofDefaultRoot();
		List<Task> tasks = dataset.getTasks();
		RougeNFactory rougeFactory = new RougeNFactory(1);
		BestSentencePosDistribution distribution = new BestSentencePosDistribution(
				rougeFactory);
		distribution.compute(tasks);
	}

}
