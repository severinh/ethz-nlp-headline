package ch.ethz.nlp.headline.compressor;

import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.stanford.nlp.pipeline.Annotation;

public class CombinedCompressor implements SentencesCompressor {

	private final List<SentencesCompressor> compressors;

	public CombinedCompressor(List<? extends SentencesCompressor> compressors) {
		this.compressors = ImmutableList.copyOf(compressors);
	}

	@Override
	public Annotation compress(Annotation annotation) {
		for (SentencesCompressor compressor : compressors) {
			annotation = compressor.compress(annotation);
		}
		return annotation;
	}

}
