package ch.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

public class PosFilteredGenerator extends CoreNLPGenerator {

	private final Set<String> openTags;

	public PosFilteredGenerator(Dataset dataset) throws IOException,
			ClassNotFoundException {
		super(dataset, "ssplit", "pos");

		// Temporarily create a tagger to gain access to the list of open tags
		MaxentTagger tagger = new MaxentTagger(MaxentTagger.DEFAULT_JAR_PATH);
		openTags = tagger.getTags().getOpenTags();
	}

	@Override
	public String getId() {
		return "POS-F";
	}

	@Override
	public String generate(Document document) throws IOException {
		Annotation annotation = getDocumentAnnotation(document);

		CoreMap sentenceMap = annotation.get(SentencesAnnotation.class).get(0);
		List<CoreLabel> labels = sentenceMap.get(TokensAnnotation.class);
		List<String> wordsWithOpenTag = new ArrayList<>();

		for (CoreLabel label : labels) {
			String tag = label.get(PartOfSpeechAnnotation.class);
			if (openTags.contains(tag)) {
				wordsWithOpenTag.add(label.word());
			}
		}

		return StringUtils.join(wordsWithOpenTag);
	}

}
