package ch.ethz.nlp.headline.generators;

import ch.ethz.nlp.headline.compressor.AppositivePruner;
import ch.ethz.nlp.headline.compressor.HedgeTrimmer;
import ch.ethz.nlp.headline.compressor.PersonNameCompressor;
import ch.ethz.nlp.headline.preprocessing.CombinedPreprocessor;
import ch.ethz.nlp.headline.selection.SentencesSelector;
import ch.ethz.nlp.headline.selection.TfIdfProvider;
import ch.ethz.nlp.headline.selection.TfIdfSentencesSelector;
import ch.ethz.nlp.headline.util.GentleAnnotationStringBuilder;
import edu.stanford.nlp.pipeline.Annotation;

public class HedgeTrimmerGenerator extends CoreNLPGenerator {

	private final SentencesSelector sentencesSelector;
	private final PersonNameCompressor nameSimplifier;
	private final AppositivePruner appositivePruner;
	private final HedgeTrimmer hedgeTrimmer;

	public HedgeTrimmerGenerator(TfIdfProvider tfIdfProvider) {
		super(CombinedPreprocessor.all(),
				GentleAnnotationStringBuilder.INSTANCE);

		this.sentencesSelector = new TfIdfSentencesSelector(tfIdfProvider);
		this.nameSimplifier = new PersonNameCompressor();
		this.appositivePruner = new AppositivePruner();
		this.hedgeTrimmer = new HedgeTrimmer();
	}

	@Override
	public String getId() {
		return "HEDGE";
	}

	@Override
	protected Annotation generate(Annotation annotation) {
		annotation = sentencesSelector.select(annotation);
		annotation = nameSimplifier.compress(annotation);
		annotation = appositivePruner.compress(annotation);
		annotation = hedgeTrimmer.compress(annotation);

		return annotation;
	}

}
