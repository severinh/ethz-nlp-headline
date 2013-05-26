package ch.ethz.nlp.headline.generators;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.nlp.headline.cache.AnnotationProvider;
import ch.ethz.nlp.headline.compressor.AppositivePruner;
import ch.ethz.nlp.headline.compressor.CombinedCompressor;
import ch.ethz.nlp.headline.compressor.DatePruner;
import ch.ethz.nlp.headline.compressor.HedgeTrimmer;
import ch.ethz.nlp.headline.compressor.LowContentLemmaPruner;
import ch.ethz.nlp.headline.compressor.PersonNameCompressor;
import ch.ethz.nlp.headline.compressor.SentencesCompressor;
import ch.ethz.nlp.headline.preprocessing.CombinedPreprocessor;
import ch.ethz.nlp.headline.selection.SentencesSelector;
import ch.ethz.nlp.headline.selection.TfIdfProvider;
import ch.ethz.nlp.headline.selection.ScoredSentencesSelector;
import ch.ethz.nlp.headline.util.GentleAnnotationStringBuilder;
import edu.stanford.nlp.pipeline.Annotation;

public class HedgeTrimmerGenerator extends CoreNLPGenerator {

	private final SentencesSelector sentencesSelector;
	private final SentencesCompressor sentencesCompressor;

	public HedgeTrimmerGenerator(AnnotationProvider annotationProvider,
			TfIdfProvider tfIdfProvider) {
		super(annotationProvider, CombinedPreprocessor.all(),
				GentleAnnotationStringBuilder.INSTANCE);

		List<SentencesCompressor> compressors = new ArrayList<>();
		compressors.add(new PersonNameCompressor());
		compressors.add(new AppositivePruner());
		compressors.add(new LowContentLemmaPruner());
		compressors.add(new DatePruner());
		compressors.add(new HedgeTrimmer());

		this.sentencesSelector = new ScoredSentencesSelector(tfIdfProvider);
		this.sentencesCompressor = new CombinedCompressor(compressors);
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
