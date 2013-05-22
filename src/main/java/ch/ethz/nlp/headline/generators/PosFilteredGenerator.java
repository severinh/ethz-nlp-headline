package ch.ethz.nlp.headline.generators;

import ch.ethz.nlp.headline.compressor.ClosedPosFilter;
import ch.ethz.nlp.headline.selection.FirstSentenceSelector;
import ch.ethz.nlp.headline.selection.SentencesSelector;
import edu.stanford.nlp.pipeline.Annotation;

public class PosFilteredGenerator extends CoreNLPGenerator {

	private final SentencesSelector sentencesSelector = new FirstSentenceSelector();
	private final ClosedPosFilter closedPosFilter = new ClosedPosFilter();

	@Override
	public String getId() {
		return "POS-F";
	}

	@Override
	protected Annotation generate(Annotation annotation) {
		annotation = sentencesSelector.select(annotation);
		annotation = closedPosFilter.compress(annotation);

		return annotation;
	}

}
