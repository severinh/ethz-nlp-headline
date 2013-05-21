package ch.ethz.nlp.headline.generators;

import java.io.IOException;

import ch.ethz.nlp.headline.Document;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class SanitizingGenerator extends CoreNLPGenerator {

	@Override
	public String getId() {
		return "SANITIZING";
	}

	@Override
	public String generate(Document document) throws IOException {
		Annotation annotation = getTokenizedSentenceDocumentAnnotation(document);
		CoreMap sentenceMap = annotation.get(SentencesAnnotation.class).get(0);
		String result = sentenceMap.toString();

		// Drop prefixes such as: BRUSSELS, Belgium (AP) -
		result = result.replaceAll("\\w+, \\w+ \\(AP\\) - ", "");

		if (result.length() > MAX_LENGTH) {
			result = result.substring(0, MAX_LENGTH);
		}
		return result;
	}

}
