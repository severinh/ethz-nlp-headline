package ch.ethz.nlp.headline.preprocessing;

public class ContentSanitizer implements ContentPreprocessor {

	@Override
	public String preprocess(String content) {
		// Drop prefixes such as: BRUSSELS, Belgium (AP) -
		content = content.replaceAll("\\w+, \\w+ \\(AP\\) . ", "");

		// Replace newlines
		content = content.replaceAll("\\n", " ");

		return content;
	}

}
