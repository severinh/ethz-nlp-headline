package org.ethz.nlp.headline.generators;

public interface Generator {

	public String getId();

	public String generate(String text);

}
