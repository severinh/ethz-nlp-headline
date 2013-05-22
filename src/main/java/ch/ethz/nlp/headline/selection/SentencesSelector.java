package ch.ethz.nlp.headline.selection;

import java.util.List;

import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public abstract class SentencesSelector {

	public Annotation select(Annotation annotation) {
		CoreNLPUtil.ensureSentencesAnnotation(annotation);
		List<CoreMap> selectedSentences = selectImpl(annotation);

		// The text is probably only for debugging convenience
		// The CharacterOffset*Annotation and Token*Annotation of individual
		// sentences will not be in sync with the new text, but rather refer to
		// the original text. This may not necessarily be a problem.
		Annotation result = new Annotation(sentencesToText(selectedSentences));
		result.set(SentencesAnnotation.class, selectedSentences);

		return result;
	}

	protected abstract List<CoreMap> selectImpl(Annotation annotation);

	private String sentencesToText(List<CoreMap> sentences) {
		StringBuilder builder = new StringBuilder();
		for (CoreMap sentence : sentences) {
			builder.append(sentence.get(TextAnnotation.class));
			builder.append(" ");
		}
		return builder.toString().trim();
	}

}
