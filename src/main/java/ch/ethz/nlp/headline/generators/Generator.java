package ch.ethz.nlp.headline.generators;

public interface Generator {

	/**
	 * The maximum number of characters in the generated headline.
	 */
	public static final int MAX_LENGTH = 75;

	public String getId();

	public String generate(String content);

}
