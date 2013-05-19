package ch.ethz.nlp.headline.generators;

import java.io.IOException;

import ch.ethz.nlp.headline.Document;

public interface Generator {

	public String getId();

	public String generate(Document document) throws IOException;

}
