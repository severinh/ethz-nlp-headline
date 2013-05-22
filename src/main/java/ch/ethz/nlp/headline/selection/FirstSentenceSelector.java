package ch.ethz.nlp.headline.selection;

import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class FirstSentenceSelector extends SentencesSelector {

	@Override
	protected List<CoreMap> selectImpl(Annotation annotation) {
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		return ImmutableList.of(sentences.get(0));
	}

}
