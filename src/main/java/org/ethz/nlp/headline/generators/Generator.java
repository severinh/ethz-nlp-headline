package org.ethz.nlp.headline.generators;

import java.io.IOException;

import org.ethz.nlp.headline.Document;

public interface Generator {

	public String getId();

	public String generate(Document document) throws IOException;

}
