package ch.ethz.nlp.headline.duc2004;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import ch.ethz.nlp.headline.DocumentId;
import ch.ethz.nlp.headline.Peer;

public class Duc2004Peer implements Peer {

	public final Path path;
	public final String generatorId;

	public String headline;

	public Duc2004Peer(Path peerRoot, DocumentId documentId, String generatorId) {
		super();

		String filename = documentId.getSet().toUpperCase()
				.substring(0, documentId.getSet().length() - 1)
				+ ".P.10.T." + generatorId + "." + documentId.getName();

		this.path = peerRoot.resolve(filename);
		this.generatorId = generatorId;
	}

	@Override
	public String getGeneratorId() {
		return generatorId;
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public void store(String headline) throws IOException {
		Files.write(getPath(), headline.getBytes(), StandardOpenOption.CREATE);
		this.headline = headline;
	}

	@Override
	public String load() {
		return headline;
	}

	@Override
	public String toString() {
		return "Peer [path=" + path + ", generatorId=" + generatorId + "]";
	}

}
