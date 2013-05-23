package ch.ethz.nlp.headline.generators;

import com.google.common.collect.ImmutableList;

import ch.ethz.nlp.headline.compressor.AppositivePruner;
import ch.ethz.nlp.headline.compressor.CombinedCompressor;
import ch.ethz.nlp.headline.compressor.HedgeTrimmer;
import ch.ethz.nlp.headline.compressor.PersonNameCompressor;
import ch.ethz.nlp.headline.compressor.SentencesCompressor;
import ch.ethz.nlp.headline.preprocessing.CombinedPreprocessor;
import ch.ethz.nlp.headline.selection.SentencesSelector;
import ch.ethz.nlp.headline.selection.TfIdfProvider;
import ch.ethz.nlp.headline.selection.TfIdfSentencesSelector;
import ch.ethz.nlp.headline.util.AnnotationCache;
import ch.ethz.nlp.headline.util.GentleAnnotationStringBuilder;
import edu.stanford.nlp.pipeline.Annotation;

public class HedgeTrimmerGenerator extends CoreNLPGenerator {

	private final SentencesSelector sentencesSelector;
	private final SentencesCompressor sentencesCompressor;

	public HedgeTrimmerGenerator(AnnotationCache cache,
			TfIdfProvider tfIdfProvider) {
		super(cache, CombinedPreprocessor.all(),
				GentleAnnotationStringBuilder.INSTANCE);

		this.sentencesSelector = new TfIdfSentencesSelector(tfIdfProvider);
		this.sentencesCompressor = new CombinedCompressor(ImmutableList.of(
				new PersonNameCompressor(), new AppositivePruner(),
				new HedgeTrimmer()));
	}

	@Override
	public String getId() {
		return "HEDGE";
	}

	@Override
	protected Annotation generate(Annotation annotation) {
		annotation = sentencesSelector.select(annotation);
		annotation = sentencesCompressor.compress(annotation);

		return annotation;
	}

}
