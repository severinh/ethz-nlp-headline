package ch.ethz.nlp.headline.duc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import ch.ethz.nlp.headline.Peer;

public class DucPeer implements Peer {

	public final Path path;
	public final String generatorId;

	public String headline;

	public DucPeer(Path path, String generatorId) {
		super();

		this.path = path;
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
