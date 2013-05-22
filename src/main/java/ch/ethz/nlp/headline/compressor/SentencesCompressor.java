package ch.ethz.nlp.headline.compressor;

import edu.stanford.nlp.pipeline.Annotation;

public interface SentencesCompressor {

	public Annotation compress(Annotation annotation);

}
