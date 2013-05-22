package ch.ethz.nlp.headline.preprocessing;

public class AcronymCreator implements ContentPreprocessor {

	@Override
	public String preprocess(String content) {
		content = content.replaceAll("United States", "U.S.");
		content = content.replaceAll("America Online", "AOL");
		content = content.replaceAll("Corp.", "");

		return content;
	}

}
