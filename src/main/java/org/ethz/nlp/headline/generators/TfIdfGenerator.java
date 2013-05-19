package org.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ethz.nlp.headline.Dataset;
import org.ethz.nlp.headline.Document;
import org.ethz.nlp.headline.DocumentId;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.BinaryHeapPriorityQueue;
import edu.stanford.nlp.util.PriorityQueue;
import edu.stanford.nlp.util.StringUtils;

public class TfIdfGenerator implements Generator {

	/**
	 * The maximum number of characters in the generated headline. The generator
	 * will greedily build the headline from the terms with the highest TF-IDF
	 * score until the limit is reached.
	 */
	private static final int MAX_LENGTH = 100;

	private final StanfordCoreNLP pipeline;

	/**
	 * For each term, stores the collection of the IDs of all documents in which
	 * it occurs.
	 */
	private final Multimap<String, DocumentId> docFreqs;

	/**
	 * Map the ID of each document to the document's annotation.
	 */
	private final Map<DocumentId, Annotation> annotations;

	public TfIdfGenerator(Dataset dataset, String... annotators)
			throws IOException {
		List<String> allAnnotators = new ArrayList<>();
		allAnnotators.add("tokenize");
		allAnnotators.addAll(Arrays.asList(annotators));

		Properties props = new Properties();
		props.put("annotators", StringUtils.join(allAnnotators, ","));
		pipeline = new StanfordCoreNLP(props);

		docFreqs = HashMultimap.create();
		annotations = new HashMap<>();

		addDocuments(dataset.getDocuments());
	}

	private void addDocument(Document document) throws IOException {
		String content = document.load();
		Annotation annotation = new Annotation(content);
		pipeline.annotate(annotation);

		annotations.put(document.getId(), annotation);
		for (CoreLabel label : annotation.get(TokensAnnotation.class)) {
			docFreqs.put(label.word(), document.getId());
		}
	}

	private void addDocuments(List<Document> documents) throws IOException {
		for (Document document : documents) {
			addDocument(document);
		}
	}

	@Override
	public String getId() {
		return "TF-IDF";
	}

	protected Annotation getAnnotation(DocumentId documentId) {
		return annotations.get(documentId);
	}

	/**
	 * Compute the frequency of each term in the given document.
	 * 
	 * @param documentId
	 * @return
	 */
	protected Multiset<String> getTermFreqs(DocumentId documentId) {
		Multiset<String> termFreqs = HashMultiset.create();
		Annotation annotation = annotations.get(documentId);

		for (CoreLabel label : annotation.get(TokensAnnotation.class)) {
			String term = label.word();
			termFreqs.add(term);
		}

		return termFreqs;
	}

	/**
	 * Compute the tf-idf score for each term in the given document.
	 * 
	 * @param documentId
	 * @return
	 */
	protected PriorityQueue<String> getTfIdfMap(DocumentId documentId) {
		Multiset<String> termFreqs = getTermFreqs(documentId);
		PriorityQueue<String> tfIdfMap = new BinaryHeapPriorityQueue<>();
		double numDocs = annotations.size();

		for (String term : termFreqs.elementSet()) {
			double termFreq = termFreqs.count(term);
			double docFreq = docFreqs.get(term).size();
			double inverseDocFreq = Math.log(numDocs / docFreq);
			double tfIdf = termFreq * inverseDocFreq;
			tfIdfMap.add(term, tfIdf);
		}

		return tfIdfMap;
	}

	@Override
	public String generate(Document document) throws IOException {
		PriorityQueue<String> tfIdfMap = getTfIdfMap(document.getId());

		// Build the headline from the words with the highest tf-idf score
		List<String> sortedTerms = tfIdfMap.toSortedList();
		StringBuilder builder = new StringBuilder();
		for (String sortedTerm : sortedTerms) {
			if (builder.length() + sortedTerm.length() > MAX_LENGTH) {
				break;
			}
			builder.append(" " + sortedTerm);
		}

		String result = builder.toString().trim();
		return result;
	}

}
