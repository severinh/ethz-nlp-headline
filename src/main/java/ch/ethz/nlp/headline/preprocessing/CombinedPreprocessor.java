package ch.ethz.nlp.headline.preprocessing;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class CombinedPreprocessor implements ContentPreprocessor {

	private final List<ContentPreprocessor> preprocessors;

	public CombinedPreprocessor(List<ContentPreprocessor> preprocessors) {
		this.preprocessors = ImmutableList.copyOf(preprocessors);
	}

	@Override
	public String preprocess(String content) {
		for (ContentPreprocessor preprocessor : preprocessors) {
			content = preprocessor.preprocess(content);
		}
		return content;
	}

	public static ContentPreprocessor all() {
		ContentPreprocessor result = new CombinedPreprocessor(ImmutableList.of(
				new AcronymCreator(), new ContentSanitizer()));
		return result;
	}

}
