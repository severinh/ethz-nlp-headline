package org.ethz.nlp.headline.duc2004;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ethz.nlp.headline.Dataset;
import org.ethz.nlp.headline.DocumentId;
import org.ethz.nlp.headline.Model;
import org.ethz.nlp.headline.Peer;
import org.ethz.nlp.headline.Task;

public class Duc2004Dataset implements Dataset {

	private final Path documentRoot;
	private final Path modelRoot;
	private final Path peerRoot;

	public Duc2004Dataset(Path root) {
		documentRoot = root.resolve("docs");
		modelRoot = root.resolve("eval").resolve("models").resolve("1");
		peerRoot = root.resolve("eval").resolve("peers").resolve("1");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ethz.nlp.headline.duc2004.Dataset#getDocumentRoot()
	 */
	@Override
	public Path getDocumentRoot() {
		return documentRoot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ethz.nlp.headline.duc2004.Dataset#getModelRoot()
	 */
	@Override
	public Path getModelRoot() {
		return modelRoot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ethz.nlp.headline.duc2004.Dataset#getPeerRoot()
	 */
	@Override
	public Path getPeerRoot() {
		return peerRoot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ethz.nlp.headline.Dataset#getTasks()
	 */
	@Override
	public List<Task> getTasks() {
		List<Task> tasks = new ArrayList<>();
		Map<DocumentId, List<Model>> modelMap = new HashMap<>();
		for (Duc2004Model model : getModels()) {
			List<Model> models = modelMap.get(model.getDocumentId());
			if (models == null) {
				models = new ArrayList<>();
				modelMap.put(model.getDocumentId(), models);
			}
			models.add(model);
		}
		for (Duc2004Document document : getDocuments()) {
			List<Model> models = modelMap.get(document.getId());
			if (models == null) {
				models = Collections.emptyList();
			}
			Task task = new Task(document, models);
			tasks.add(task);
		}
		return tasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ethz.nlp.headline.Dataset#makePeer(org.ethz.nlp.headline.Task,
	 * java.lang.String)
	 */
	@Override
	public Peer makePeer(Task task, String generatorId) {
		DocumentId documentId = task.getDocument().getId();
		Peer peer = new Duc2004Peer(getPeerRoot(), documentId, generatorId);
		return peer;
	}

	private List<Duc2004Document> getDocuments() {
		List<Duc2004Document> result = new ArrayList<>();
		try (DirectoryStream<Path> setStream = Files
				.newDirectoryStream(getDocumentRoot())) {
			for (Path documentSetPath : setStream) {
				try (DirectoryStream<Path> docStream = Files
						.newDirectoryStream(documentSetPath)) {
					for (Path documentPath : docStream) {
						result.add(new Duc2004Document(documentPath));
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private List<Duc2004Model> getModels() {
		List<Duc2004Model> result = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files
				.newDirectoryStream(getModelRoot())) {
			for (Path modelPath : stream) {
				result.add(new Duc2004Model(modelPath));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
