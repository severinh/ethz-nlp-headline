package ch.ethz.nlp.headline;

import java.nio.file.Path;

public interface Model {

	public Path getPath();

	public String getId();

	public DocumentId getDocumentId();

	public String getContent();

}