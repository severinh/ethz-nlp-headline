package org.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.List;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.ethz.nlp.headline.Dataset;
import org.ethz.nlp.headline.Document;

import edu.stanford.nlp.util.BinaryHeapPriorityQueue;
import edu.stanford.nlp.util.PriorityQueue;

/**
 * Computes the TF-IDF score for each term in a document and chooses the terms
 * with the highest score for the headline.
 */
public class TfIdfGenerator implements Generator {

	private static final String ID_FIELD = "id";
	private static final String CONTENT_FIELD = "content";

	/**
	 * The maximum number of characters in the generated headline. The generator
	 * will greedily build the headline from the terms with the highest TF-IDF
	 * score until the limit is reached.
	 */
	private static final int MAX_LENGTH = 100;

	private final IndexReader indexReader;
	private final IndexSearcher indexSearcher;

	public TfIdfGenerator(Dataset dataset) throws IOException {
		Directory directory = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43,
				new StandardAnalyzer(Version.LUCENE_43));
		IndexWriter indexWriter = new IndexWriter(directory, config);

		// Store each document in the index
		for (Document document : dataset.getDocuments()) {
			String documentId = document.getId().toString();
			org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
			FieldType idFieldType = new FieldType();
			idFieldType.setStored(true);
			idFieldType.setTokenized(false);
			idFieldType.setIndexed(true);
			luceneDoc.add(new Field(ID_FIELD, documentId, idFieldType));

			String content = document.load();
			FieldType textFieldType = new FieldType();
			textFieldType.setStored(true);
			textFieldType.setTokenized(true);
			textFieldType.setIndexed(true);
			textFieldType.setStoreTermVectors(true);
			luceneDoc.add(new Field(CONTENT_FIELD, content, textFieldType));
			indexWriter.addDocument(luceneDoc);
		}

		indexWriter.commit();
		indexWriter.close();
		indexReader = DirectoryReader.open(directory);
		indexSearcher = new IndexSearcher(indexReader);
	}

	@Override
	public String getId() {
		return "TF-IDF";
	}

	@Override
	public String generate(Document document) throws IOException {
		// Look up the document in the index
		String documentId = document.getId().toString();
		Query query = new TermQuery(new Term(ID_FIELD, documentId));
		TopDocs results = indexSearcher.search(query, 1);
		int internalId = results.scoreDocs[0].doc;

		// Get the terms occurring in this document
		Terms terms = indexReader.getTermVector(internalId, CONTENT_FIELD);
		TermsEnum termsEnum = terms.iterator(null);
		BytesRef term = null;

		// Maps every term to its TF-IDF score
		PriorityQueue<String> tfIdfMap = new BinaryHeapPriorityQueue<>();

		int numDocs = indexReader.getDocCount(CONTENT_FIELD);
		DefaultSimilarity similarity = new DefaultSimilarity();

		while ((term = termsEnum.next()) != null) {
			String termString = term.utf8ToString();
			// The total number of documents that contain this term
			long docFreq = indexReader.docFreq(new Term(CONTENT_FIELD,
					termString));
			float tf = similarity.tf(termsEnum.totalTermFreq());
			float idf = similarity.idf(docFreq, numDocs);
			float tfIdf = tf * idf;
			tfIdfMap.add(termString, tfIdf);
		}

		// Greedily pick the terms with the highest score
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
