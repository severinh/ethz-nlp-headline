package org.ethz.nlp.headline;

import java.nio.file.Path;
import java.util.List;

public interface Dataset {

	public Path getDocumentRoot();

	public Path getModelRoot();

	public Path getPeerRoot();

	public List<Task> getTasks();

	public Peer makePeer(Task task, String generatorId);

}