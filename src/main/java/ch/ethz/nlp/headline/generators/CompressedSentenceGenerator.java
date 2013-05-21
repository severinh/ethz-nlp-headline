package ch.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import edu.stanford.nlp.dcoref.CoNLL2011DocumentReader.NamedEntityAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class CompressedSentenceGenerator extends CoreNLPGenerator {

	private static final Logger LOG = LoggerFactory
			.getLogger(CompressedSentenceGenerator.class);

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

		StringBuilder builder = new StringBuilder();
		for (CoreLabel label : firstSentence.get(TokensAnnotation.class)) {
			if (!label.ner().equals("DATE")) {
				builder.append(label.word());
				builder.append(" ");
			} else {
				LOG.info("Trimming " + label.word());
			}
		}

		String result = builder.toString();
		if (result.length() > MAX_LENGTH) {
			result = result.substring(0, MAX_LENGTH);
		}
		return result;
	}
}
