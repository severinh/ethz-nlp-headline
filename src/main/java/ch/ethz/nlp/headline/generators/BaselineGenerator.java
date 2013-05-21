package ch.ethz.nlp.headline.generators;

import java.io.IOException;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Generator that simply extracts the first sentence from the given text.
 */
public class BaselineGenerator extends CoreNLPGenerator {

	public BaselineGenerator(Dataset dataset) {
	}

	@Override
	public String getId() {
		return "BASE";
	}

	@Override
	public String generate(Document document) throws IOException {
		Annotation annotation = getTokenizedSentenceDocumentAnnotation(document);
		CoreMap sentenceMap = annotation.get(SentencesAnnotation.class).get(0);
		String result = sentenceMap.toString();
		getStatistics().addSummaryResult(result);
		return truncate(result);
	}

}
