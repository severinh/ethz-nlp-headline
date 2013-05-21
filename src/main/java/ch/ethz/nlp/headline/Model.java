package ch.ethz.nlp.headline;

import java.io.IOException;
import java.nio.file.Path;

public interface Model {

	public Path getPath();

	public String getId();

	public DocumentId getDocumentId();

	public String load() throws IOException;

}