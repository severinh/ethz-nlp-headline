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

	public Duc2004Peer(Path peerRoot, DocumentId documentId, String generatorId) {
		super();

		String filename = documentId.getSet().toUpperCase()
				.substring(0, documentId.getSet().length() - 1)
				+ ".P.10.T." + generatorId + "." + documentId.getName();

		this.path = peerRoot.resolve(filename);
		this.generatorId = generatorId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ethz.nlp.headline.IPeer#getGeneratorId()
	 */
	@Override
	public String getGeneratorId() {
		return generatorId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ethz.nlp.headline.IPeer#getRelativePath()
	 */
	@Override
	public Path getPath() {
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ethz.nlp.headline.Peer#store(java.lang.String)
	 */
	@Override
	public void store(String headline) throws IOException {
		Files.write(getPath(), headline.getBytes(), StandardOpenOption.CREATE);
	}

	@Override
	public String toString() {
		return "Peer [path=" + path + ", generatorId=" + generatorId + "]";
	}

}
