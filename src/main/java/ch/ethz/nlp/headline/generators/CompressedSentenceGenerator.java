package ch.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.List;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class CompressedSentenceGenerator extends CoreNLPGenerator {

	public CompressedSentenceGenerator(Dataset dataset) throws IOException {

	}

	@Override
	public String getId() {
		return "COMPRESS";
	}

	@Override
	public String generate(Document document) throws IOException {
		Annotation annotation = getTokenizedSentenceDocumentAnnotation(document);
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		CoreMap firstSentence = sentences.get(0);

		Annotation sentenceAnnotation = makeAnnotationFromSentence(firstSentence);

		getPosTagger().annotate(sentenceAnnotation);
		getLemmatizer().annotate(sentenceAnnotation);
		getParser().annotate(sentenceAnnotation);
		getNER().annotate(sentenceAnnotation);

		// Tree tree = firstSentence.get(TreeAnnotation.class);
		// SemanticGraph dependencies = firstSentence
		// .get(CollapsedCCProcessedDependenciesAnnotation.class);

		String result = firstSentence.toString();

		// TODO: Actually do something

		if (result.length() > MAX_LENGTH) {
			result = result.substring(0, MAX_LENGTH);
		}
		return result;
	}

}
