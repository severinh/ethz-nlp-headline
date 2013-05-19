package ch.ethz.nlp.headline.duc2004;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.DocumentId;
import ch.ethz.nlp.headline.Model;
import ch.ethz.nlp.headline.Peer;
import ch.ethz.nlp.headline.Task;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

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
		Multimap<DocumentId, Model> modelMap = LinkedListMultimap.create();
		for (Model model : getModels()) {
			modelMap.put(model.getDocumentId(), model);
		}
		for (Document document : getDocuments()) {
			Collection<Model> models = modelMap.get(document.getId());
			Task task = new Task(document, new ArrayList<>(models));
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

	@Override
	public List<Document> getDocuments() {
		List<Document> result = new ArrayList<>();
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

	@Override
	public List<Model> getModels() {
		List<Model> result = new ArrayList<>();
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
