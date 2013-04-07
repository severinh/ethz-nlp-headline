package org.ethz.nlp.headline.generators;


/**
 * Generator that simply extracts the first sentence from the given text.
 */
public class BaselineGenerator implements Generator {

	@Override
	public String getId() {
		return "BASE";
	}

	@Override
	public String generate(String text) {
		int sentenceEndIndex = text.indexOf('.');
		if (sentenceEndIndex == -1) {
			sentenceEndIndex = text.length();
		}
		int endIndex = Math.min(sentenceEndIndex, text.length());
		String headline = text.substring(0, endIndex);
		return headline;
	}

}
