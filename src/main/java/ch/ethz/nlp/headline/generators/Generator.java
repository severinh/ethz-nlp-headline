package ch.ethz.nlp.headline.generators;

import java.io.IOException;

import ch.ethz.nlp.headline.Document;

public interface Generator {
	
	/**
	 * The maximum number of characters in the generated headline.
	 */
	public static final int MAX_LENGTH = 75;

	public String getId();

	public String generate(Document document) throws IOException;

}
