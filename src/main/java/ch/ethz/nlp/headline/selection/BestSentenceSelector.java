package ch.ethz.nlp.headline.selection;

import java.util.List;

import ch.ethz.nlp.headline.util.CoreNLPUtil;
import ch.ethz.nlp.headline.util.RougeN;

import com.google.common.collect.ImmutableList;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Selects the sentence from a document whose ROUGE score is the highest.
 * 
 * Of course, this selector may not be used in production.
 */
public class BestSentenceSelector extends SentencesSelector {

	private final RougeN rouge;

	public BestSentenceSelector(RougeN rouge) {
		super();
		this.rouge = rouge;
	}

	@Override
	protected List<CoreMap> selectImpl(Annotation annotation) {
		CoreMap bestSentence = null;
		double bestRecall = 0;

		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			Annotation sentenceAnnotation = CoreNLPUtil
					.sentencesToAnnotation(ImmutableList.of(sentence));
			double recall = rouge.compute(sentenceAnnotation);

			if (recall > bestRecall) {
				bestRecall = recall;
				bestSentence = sentence;
			}
		}

		return ImmutableList.of(bestSentence);
	}

}
