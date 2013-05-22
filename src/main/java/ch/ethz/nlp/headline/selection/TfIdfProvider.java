package ch.ethz.nlp.headline.selection;

import java.util.HashMap;
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
		TfIdfProvider result = new TfIdfProvider(dataset.getDocuments().size());

		for (Document document : dataset.getDocuments()) {
			Annotation annotation = new Annotation(document.getContent());
			Multiset<String> lemmaFreqs = getLemmaFreqs(annotation);
			for (String lemma : lemmaFreqs.elementSet()) {
				Integer docFreq = result.docFreqs.get(lemma);
				docFreq = (docFreq == null) ? 1 : docFreq + 1;
				result.docFreqs.put(lemma, docFreq);
			}
		}

		return result;
	}

	/**
	 * Compute the frequency of each term in the given annotation.
	 */
	protected static Multiset<String> getLemmaFreqs(Annotation annotation) {
		CoreNLPUtil.ensureLemmaAnnotation(annotation);
		Multiset<String> termFreqs = HashMultiset.create();

		for (CoreLabel label : annotation.get(TokensAnnotation.class)) {
			termFreqs.add(label.lemma());
		}

		return termFreqs;
	}

	/**
	 * Compute the tf-idf score for each lemma in the given annotation.
	 */
	protected PriorityQueue<String> getTfIdfMap(Annotation annotation) {
		Multiset<String> lemmaFreqs = getLemmaFreqs(annotation);
		PriorityQueue<String> tfIdfMap = new BinaryHeapPriorityQueue<>();

		for (String lemma : lemmaFreqs.elementSet()) {
			double lemmaFreq = lemmaFreqs.count(lemma);
			double docFreq = docFreqs.get(lemma);
			double inverseDocFreq = Math.log(numDocs / docFreq);
			double tfIdf = lemmaFreq * inverseDocFreq;
			tfIdfMap.add(lemma, tfIdf);
		}

		return tfIdfMap;
	}

}
