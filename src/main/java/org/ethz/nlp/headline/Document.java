package org.ethz.nlp.headline;

import java.io.IOException;
import java.nio.file.Path;

public interface Document {

	public Path getPath();

	public DocumentId getId();

	public String load() throws IOException;

}