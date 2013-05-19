package org.ethz.nlp.headline.generators;

import java.io.IOException;
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

	public TfIdfGenerator() {
		Properties props = new Properties();
		props.put("annotators", "tokenize");
		pipeline = new StanfordCoreNLP(props);

		docFreqs = HashMultimap.create();
		annotations = new HashMap<>();
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

	/**
	 * Constructs a new generator from the given data set.
	 * 
	 * @param dataset
	 * @return the newly constructed generator
	 * @throws IOException
	 */
	public static TfIdfGenerator of(Dataset dataset) throws IOException {
		TfIdfGenerator generator = new TfIdfGenerator();
		for (Document document : dataset.getDocuments()) {
			generator.addDocument(document);
		}
		return generator;
	}

	@Override
	public String getId() {
		return "TF-IDF";
	}

	@Override
	public String generate(Document document) throws IOException {
		// Compute the frequency of each term in the document
		Multiset<String> termFreqs = HashMultiset.create();
		Annotation annotation = annotations.get(document.getId());

		for (CoreLabel label : annotation.get(TokensAnnotation.class)) {
			String term = label.word();
			termFreqs.add(term);
		}

		// Compute the tf-idf score for each term
		PriorityQueue<String> tfIdfMap = new BinaryHeapPriorityQueue<>();
		double numDocs = annotations.size();

		for (String term : termFreqs.elementSet()) {
			double termFreq = termFreqs.count(term);
			double docFreq = docFreqs.get(term).size();
			double inverseDocFreq = Math.log(numDocs / docFreq);
			double tfIdf = termFreq * inverseDocFreq;
			tfIdfMap.add(term, tfIdf);
		}

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
