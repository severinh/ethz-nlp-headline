package ch.ethz.nlp.headline.preprocessing;

public class AcronymCreator implements ContentPreprocessor {

	@Override
	public String preprocess(String content) {
		content = content.replaceAll("United States", "U.S.");
		content = content.replaceAll("United Nations", "UN");
		content = content.replaceAll("America Online", "AOL");
		content = content.replaceAll("Corp.", "");
		content = content.replaceAll("said that", "says");

		return content;
	}

}
