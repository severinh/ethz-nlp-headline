package ch.ethz.nlp.headline.selection;

import java.util.List;

import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public abstract class SentencesSelector {

	public Annotation select(Annotation annotation) {
		CoreNLPUtil.ensureSentencesAnnotation(annotation);
		CoreNLPUtil.ensurePartOfSpeechAnnotation(annotation);

		List<CoreMap> selectedSentences = selectImpl(annotation);
		return CoreNLPUtil.sentencesToAnnotation(selectedSentences);
	}

	protected abstract List<CoreMap> selectImpl(Annotation annotation);

}
