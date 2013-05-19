package ch.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.DocumentId;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.StringUtils;

public abstract class CoreNLPGenerator implements Generator {

	private final StanfordCoreNLP pipeline;

	/**
	 * Map the ID of each document to the document's annotation.
	 */
	protected final Map<DocumentId, Annotation> annotations;

	public CoreNLPGenerator(Dataset dataset, String... annotators)
			throws IOException {
		List<String> allAnnotators = new ArrayList<>();
		allAnnotators.add("tokenize");
		allAnnotators.addAll(Arrays.asList(annotators));

		Properties props = new Properties();
		props.put("annotators", StringUtils.join(allAnnotators, ","));
		pipeline = new StanfordCoreNLP(props);

		annotations = new HashMap<>();

		addDocuments(dataset.getDocuments());
	}

	private void addDocument(Document document) throws IOException {
		String content = document.load();
		Annotation annotation = new Annotation(content);
		pipeline.annotate(annotation);
		annotations.put(document.getId(), annotation);
	}

	private void addDocuments(List<Document> documents) throws IOException {
		for (Document document : documents) {
			addDocument(document);
		}
	}

}
