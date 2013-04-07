package org.ethz.nlp.headline;

import java.io.IOException;
import java.nio.file.Path;

public interface Peer {

	public String getGeneratorId();

	public Path getPath();

	public void store(String headline) throws IOException;

}