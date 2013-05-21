package ch.ethz.nlp.headline.duc2004;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.DocumentId;
import ch.ethz.nlp.headline.Model;
import ch.ethz.nlp.headline.Peer;
import ch.ethz.nlp.headline.Task;

public class Duc2004Dataset extends Dataset {

	private static final String DUC_2004_ROOT = "duc2004";

	private final Path documentRoot;
	private final Path modelRoot;
	private final Path peerRoot;

	private List<Document> documents;
	private List<Model> models;

	public Duc2004Dataset(Path root) {
		documentRoot = root.resolve("docs");
		modelRoot = root.resolve("eval").resolve("models").resolve("1");
		peerRoot = root.resolve("eval").resolve("peers").resolve("1");
	}

	public static Duc2004Dataset ofDefaultRoot() {
		Path datasetRoot = FileSystems.getDefault().getPath(DUC_2004_ROOT);
		Duc2004Dataset dataset = new Duc2004Dataset(datasetRoot);
		return dataset;
	}

	@Override
	public Path getDocumentRoot() {
		return documentRoot;
	}

	@Override
	public Path getModelRoot() {
		return modelRoot;
	}

	@Override
	public Path getPeerRoot() {
		return peerRoot;
	}

	@Override
	public List<Document> getDocuments() {
		if (documents == null) {
			documents = getDocumentsImpl();
		}
		return documents;
	}

	private List<Document> getDocumentsImpl() {
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
			e.printStackTrace();
			System.exit(-1);
		}
		return result;
	}

	@Override
	public List<Model> getModels() {
		if (models == null) {
			models = getModelsImpl();
		}
		return models;
	}

	private List<Model> getModelsImpl() {
		List<Model> result = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files
				.newDirectoryStream(getModelRoot())) {
			for (Path modelPath : stream) {
				result.add(new Duc2004Model(modelPath));
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return result;
	}

	@Override
	public Peer makePeer(Task task, String generatorId) {
		DocumentId documentId = task.getDocument().getId();
		Peer peer = new Duc2004Peer(getPeerRoot(), documentId, generatorId);
		return peer;
	}

}
