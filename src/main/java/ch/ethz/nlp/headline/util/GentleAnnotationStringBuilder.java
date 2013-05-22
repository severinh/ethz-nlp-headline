package ch.ethz.nlp.headline.util;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.parser.tools.PunctEquivalenceClasser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class GentleAnnotationStringBuilder implements AnnotationStringBuilder {

	public static final AnnotationStringBuilder INSTANCE = new GentleAnnotationStringBuilder();

	@Override
	public String build(Annotation annotation, int maxLength) {
		StringBuilder builder = new StringBuilder();

		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			for (CoreLabel label : sentence.get(TokensAnnotation.class)) {
				String word = label.word();
				if (!PunctEquivalenceClasser.getPunctClass(word).isEmpty()) {
					continue;
				}
				if (builder.length() + word.length() > maxLength) {
					break;
				}
				if (!word.equals("'s")) {
					builder.append(" ");
				}
				builder.append(word);
			}
		}

		String result = builder.toString().trim();
		return result;
	}

}
