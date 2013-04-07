package org.ethz.nlp.headline.generators;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordToSentenceProcessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosFilteredGenerator implements Generator {

	private final MaxentTagger tagger;
	private final POSTaggerAnnotator annotator;
	private final Set<String> openTags;

	public PosFilteredGenerator() throws ClassNotFoundException, IOException {
		tagger = new MaxentTagger(MaxentTagger.DEFAULT_JAR_PATH);
		annotator = new POSTaggerAnnotator(tagger);
		openTags = tagger.getTags().getOpenTags();
	}

	@Override
	public String getId() {
		return "POS-F";
	}

	@Override
	public String generate(String text) {
		List<CoreLabel> firstSentence = getFirstSentence(text);
		List<CoreLabel> labelsWithOpenTags = new ArrayList<>();
		List<? extends CoreLabel> labels = annotator.processText(firstSentence);

		for (CoreLabel label : labels) {
			String tag = label.get(PartOfSpeechAnnotation.class);
			if (openTags.contains(tag)) {
				labelsWithOpenTags.add(label);
			}
		}

		StringBuilder builder = new StringBuilder();
		for (CoreLabel label : labelsWithOpenTags) {
			builder.append(label.word());
			builder.append(" ");
		}

		return builder.toString();
	}

	private List<CoreLabel> getFirstSentence(String text) {
		StringReader textReader = new StringReader(text);
		PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<>(textReader,
				new CoreLabelTokenFactory(), "");
		List<CoreLabel> allTokens = new ArrayList<>();
		while (tokenizer.hasNext()) {
			allTokens.add(tokenizer.next());
		}

		WordToSentenceProcessor<CoreLabel> wordToSentenceProcessor = new WordToSentenceProcessor<>();
		List<List<CoreLabel>> sentences = wordToSentenceProcessor
				.process(allTokens);
		List<CoreLabel> firstSentence = sentences.get(0);
		return firstSentence;
	}

}
