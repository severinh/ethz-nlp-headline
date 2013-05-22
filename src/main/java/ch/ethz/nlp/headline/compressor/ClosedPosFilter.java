package ch.ethz.nlp.headline.compressor;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class ClosedPosFilter implements SentencesCompressor {

	private Set<String> openTags;

	private Set<String> getOpenTags() {
		if (openTags == null) {
			openTags = CoreNLPUtil.getTagger().getTags().getOpenTags();
		}
		return openTags;
	}

	@Override
	public Annotation compress(Annotation annotation) {
		CoreNLPUtil.ensurePartOfSpeechAnnotation(annotation);

		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
			Iterator<CoreLabel> it = labels.iterator();

			while (it.hasNext()) {
				CoreLabel label = it.next();
				if (!getOpenTags().contains(label.tag())) {
					it.remove();
				}
			}
		}

		return annotation;
	}

}
