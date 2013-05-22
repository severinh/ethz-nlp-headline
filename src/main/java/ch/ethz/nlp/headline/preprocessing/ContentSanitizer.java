package ch.ethz.nlp.headline.preprocessing;

public class ContentSanitizer implements ContentPreprocessor {

	@Override
	public String preprocess(String content) {
		// Drop prefixes such as: BRUSSELS, Belgium (AP) -
		content = content.replaceAll("\\w+, \\w+ \\(AP\\) (--|_|-) ", "");

		// Normalize Q & A article with formatting
		content = content.replaceAll("Q. \\(italics\\)", "");
		content = content.replaceAll("\\(end italics\\) A.", "");

		return content;
	}
}
