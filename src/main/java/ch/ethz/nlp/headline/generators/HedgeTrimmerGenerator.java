package ch.ethz.nlp.headline.generators;

import ch.ethz.nlp.headline.compressor.HedgeTrimmer;
import ch.ethz.nlp.headline.compressor.PersonNameCompressor;
import ch.ethz.nlp.headline.preprocessing.CombinedPreprocessor;
import ch.ethz.nlp.headline.selection.FirstSentenceSelector;
import ch.ethz.nlp.headline.selection.SentencesSelector;
import ch.ethz.nlp.headline.util.GentleAnnotationStringBuilder;
import edu.stanford.nlp.pipeline.Annotation;

public class HedgeTrimmerGenerator extends CoreNLPGenerator {

	private final SentencesSelector sentencesSelector = new FirstSentenceSelector();
	private final PersonNameCompressor nameSimplifier = new PersonNameCompressor();
	private final HedgeTrimmer hedgeTrimmer = new HedgeTrimmer();

	public HedgeTrimmerGenerator() {
		super(CombinedPreprocessor.all(),
				GentleAnnotationStringBuilder.INSTANCE);
	}

	@Override
	public String getId() {
		return "HEDGE";
	}

	@Override
	protected Annotation generate(Annotation annotation) {
		annotation = sentencesSelector.select(annotation);
		annotation = nameSimplifier.compress(annotation);
		annotation = hedgeTrimmer.compress(annotation);

		return annotation;
	}

}
