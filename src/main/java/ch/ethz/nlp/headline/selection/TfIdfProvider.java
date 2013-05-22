package ch.ethz.nlp.headline.selection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.util.CoreNLPUtil;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.BinaryHeapPriorityQueue;
import edu.stanford.nlp.util.PriorityQueue;

public class TfIdfProvider {

	private final int numDocs;

	/**
	 * For each lemma, stores the number of documents in which it occurs.
	 */
	private final Map<String, Integer> docFreqs;

	private TfIdfProvider(int numDocs) {
		this.docFreqs = new HashMap<>();
		this.numDocs = numDocs;
	}

	public static TfIdfProvider of(Dataset dataset) {
		List<Document> documents = dataset.getDocuments();
		TfIdfProvider result = new TfIdfProvider(documents.size());

		for (Document document : documents) {
			Annotation annotation = new Annotation(document.getContent());
			CoreNLPUtil.ensureLemmaAnnotation(annotation);

			for (CoreLabel label : annotation.get(TokensAnnotation.class)) {
				String lemma = label.lemma();

				Integer currentCount = result.docFreqs.get(lemma);
				currentCount = (currentCount == null) ? 1 : currentCount + 1;
				result.docFreqs.put(lemma, currentCount);
			}
		}

		return result;
	}

	/**
	 * Compute the frequency of each term in the given annotation.
	 */
	protected Multiset<String> getLemmaFreqs(Annotation annotation) {
		CoreNLPUtil.ensureLemmaAnnotation(annotation);
		Multiset<String> termFreqs = HashMultiset.create();

		for (CoreLabel label : annotation.get(TokensAnnotation.class)) {
			String term = label.word();
			termFreqs.add(term);
		}

		return termFreqs;
	}

	/**
	 * Compute the tf-idf score for each lemma in the given annotation.
	 */
	protected PriorityQueue<String> getTfIdfMap(Annotation annotation) {
		Multiset<String> termFreqs = getLemmaFreqs(annotation);
		PriorityQueue<String> tfIdfMap = new BinaryHeapPriorityQueue<>();

		for (String term : termFreqs.elementSet()) {
			double termFreq = termFreqs.count(term);
			double docFreq = docFreqs.get(term);
			double inverseDocFreq = Math.log(numDocs / docFreq);
			double tfIdf = termFreq * inverseDocFreq;
			tfIdfMap.add(term, tfIdf);
		}

		return tfIdfMap;
	}

}
