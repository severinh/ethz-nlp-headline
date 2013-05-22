package ch.ethz.nlp.headline.preprocessing;

public final class NopPreprocessor implements ContentPreprocessor {

	public static final NopPreprocessor INSTANCE = new NopPreprocessor();

	private NopPreprocessor() {
		// nop
	}

	@Override
	public String preprocess(String content) {
		return content;
	}

}
