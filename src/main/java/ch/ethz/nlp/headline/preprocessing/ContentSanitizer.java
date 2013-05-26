package ch.ethz.nlp.headline.preprocessing;

import java.util.regex.Pattern;

public class ContentSanitizer implements ContentPreprocessor {

	// Drop prefixes such as: BRUSSELS, Belgium (AP) -
	private static final Pattern PREFIX_PATTERN_1 = Pattern
			.compile(".+ \\((AP|Xinhua)\\) (--|_|-) ");

	// Drop prefixes such as WASHINGTON _
	private static final Pattern PREFIX_PATTERN_2 = Pattern
			.compile("([A-Z]| )+(, \\w+)? _ ");

	@Override
	public String preprocess(String content) {
		content = PREFIX_PATTERN_1.matcher(content).replaceFirst("");
		content = PREFIX_PATTERN_2.matcher(content).replaceFirst("");

		// Normalize Q & A article with formatting
		content = content.replace("Q. (italics)", "");
		content = content.replace("(end italics) A.", "");

		return content;
	}

}
