package ch.ethz.nlp.headline.generators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public abstract class CoreNLPGenerator implements Generator {

	// Temporary redirect for generators that do not need the document anymore
	public String generate(Document document) {
		return generate(document.getContent());
	}

	public String generate(String content) {
		return content;
	}

	private GeneratorStatistics statistics = new GeneratorStatistics();

	public GeneratorStatistics getStatistics() {
		return statistics;
	}

	/**
	 * Create an annotation with the tokens of the given document.
	 * 
	 * @param document
	 */
	public Annotation getTokenizedDocumentAnnotation(Document document) {
		Annotation annotation = new Annotation(document.getContent());
		CoreNLPUtil.getTokenizer().annotate(annotation);
		return annotation;
	}

	/**
	 * Create an annotation with tokens and sentences of the given document.
	 */
	public Annotation getTokenizedSentenceDocumentAnnotation(Document document) {
		Annotation resultAnnotation = getTokenizedDocumentAnnotation(document);
		CoreNLPUtil.getSentenceSplitter().annotate(resultAnnotation);
		return resultAnnotation;
	}

	public Annotation makeAnnotationFromSentence(CoreMap sentence) {
		Annotation result = new Annotation(sentence.get(TextAnnotation.class));
		List<CoreMap> sentences = Collections.singletonList(sentence);
		result.set(SentencesAnnotation.class, sentences);
		return result;
	}

	protected String truncate(String headline) {
		if (headline.length() > MAX_LENGTH) {
			headline = headline.substring(0, MAX_LENGTH);
		}
		return headline;
	}

	public class GeneratorStatistics {
		private List<Integer> summaryLengths = new ArrayList<>();

		public void addSummaryResult(String summary) {
			summaryLengths.add(summary.length());
		}

		@Override
		public String toString() {
			int summaries = summaryLengths.size();
			int tooLong = 0;
			double totalLength = 0;
			for (Integer i : summaryLengths) {
				if (i > MAX_LENGTH) {
					tooLong++;
				}
				totalLength += i;
			}
			double avgLength = totalLength / summaries;

			StringBuilder sb = new StringBuilder();
			sb.append(getId() + " statistics:\n");
			sb.append(String.format("Too long: %d/%d\n", tooLong, summaries));
			sb.append(String.format("Average length: %.2f\n", avgLength));
			return sb.toString();
		}
	}

}
