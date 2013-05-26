package ch.ethz.nlp.headline.selection;

import java.util.List;

import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.ling.CoreAnnotations.SentencePositionAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public abstract class SentencesSelector {

	/**
	 * Produces a new annotation containing only a subset of the sentences in
	 * the given annotation.
	 * 
	 * @param annotation
	 *            the original annotation with sentences to choose from
	 * @return
	 */
	public Annotation select(Annotation annotation) {
		CoreNLPUtil.ensureSentencesAnnotation(annotation);
		CoreNLPUtil.ensurePartOfSpeechAnnotation(annotation);

		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		List<CoreMap> selectedSentences = selectImpl(annotation);
		for (CoreMap selectedSentence : selectedSentences) {
			int pos = sentences.indexOf(selectedSentence);
			String stringPos = String.valueOf(pos);
			selectedSentence.set(SentencePositionAnnotation.class, stringPos);
		}
		return CoreNLPUtil.sentencesToAnnotation(selectedSentences);
	}

	protected abstract List<CoreMap> selectImpl(Annotation annotation);

}
