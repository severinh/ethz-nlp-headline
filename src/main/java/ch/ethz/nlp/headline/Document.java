package ch.ethz.nlp.headline;

import java.nio.file.Path;

public interface Document {

	public Path getPath();

	public DocumentId getId();

	public String getContent();

}