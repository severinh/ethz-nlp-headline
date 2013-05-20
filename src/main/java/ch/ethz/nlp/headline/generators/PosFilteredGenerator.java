package ch.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
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
		Annotation annotation = getTokenizedSentenceDocumentAnnotation(document);

		// POS-tag the first sentence. Since the tagger exects an annotation with sentences,
		// we create a new annotation with only that sentence under the key SentencesAnnotation.
		CoreMap firstSentenceMap = annotation.get(SentencesAnnotation.class).get(0);
		Annotation singleSentenceAnnotation = new Annotation(firstSentenceMap.get(TextAnnotation.class));
		List<CoreMap> singletonList = new LinkedList<CoreMap>();
		singletonList.add(firstSentenceMap);
		singleSentenceAnnotation.set(SentencesAnnotation.class, singletonList);
		getPosTagger().annotate(singleSentenceAnnotation);
		
		List<CoreLabel> labels = firstSentenceMap.get(TokensAnnotation.class);
		List<String> wordsWithOpenTag = new ArrayList<>();

		for (CoreLabel label : labels) {
			String tag = label.get(PartOfSpeechAnnotation.class);
			if (openTags.contains(tag)) {
				wordsWithOpenTag.add(label.word());
			}
		}

		String result =  StringUtils.join(wordsWithOpenTag);
		if (result.length() > MAX_LENGTH) {
			result = result.substring(0, MAX_LENGTH);
		}
		return result;
	}

}
