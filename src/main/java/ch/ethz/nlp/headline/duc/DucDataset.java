package ch.ethz.nlp.headline.duc;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.DocumentId;
import ch.ethz.nlp.headline.Model;
import ch.ethz.nlp.headline.Peer;
import ch.ethz.nlp.headline.Task;

public abstract class DucDataset extends Dataset {

	private static final Pattern TEXT_PATTERN = Pattern.compile(
			"<TEXT>(.*)</TEXT>", Pattern.MULTILINE | Pattern.DOTALL);

	private final Path documentRoot;
	private final Path modelRoot;
	private final Path peerRoot;

	private List<Document> documents;
	private List<Model> models;

	protected DucDataset(Path documentRoot, Path modelRoot, Path peerRoot) {
		super();
		this.documentRoot = documentRoot;
		this.modelRoot = modelRoot;
		this.peerRoot = peerRoot;
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
						Optional<Document> document = makeDocument(documentPath);
						if (document.isPresent()) {
							result.add(document.get());
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return result;
	}

	protected Optional<Document> makeDocument(Path documentPath)
			throws IOException {
		DocumentId id = getDocumentId(documentPath);
		String content = getDocumentContent(documentPath);

		return Optional
				.<Document> of(new DucDocument(documentPath, id, content));
	}

	protected DocumentId getDocumentId(Path documentPath) {
		String set = documentPath.getParent().getFileName().toString();
		String name = documentPath.getFileName().toString();

		return new DocumentId(set, name);
	}

	protected String getDocumentContent(Path documentPath) throws IOException {
		String html = new String(Files.readAllBytes(documentPath));
		Matcher matcher = TEXT_PATTERN.matcher(html);
		String content = null;
		if (matcher.find()) {
			content = matcher.group(1);
		} else {
			throw new IOException("could not parse document");
		}

		content = content.replaceAll("</?P>", "");
		return content;
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
				Optional<Model> model = makeModel(modelPath);
				if (model.isPresent()) {
					result.add(model.get());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return result;
	}

	protected Optional<Model> makeModel(Path path) throws IOException {
		String[] parts = path.getFileName().toString().split("\\.");
		if (parts.length <= 6) {
			return Optional.absent();
		}

		String documentSet = (parts[0] + parts[3]).toLowerCase();
		String documentName = (parts[5] + "." + parts[6]);

		DocumentId documentId = new DocumentId(documentSet, documentName);
		String id = parts[4];
		String content = new String(Files.readAllBytes(path)).trim();

		return Optional.<Model> of(new DucModel(path, documentId, id, content));
	}

	public Peer makePeer(Task task, String generatorId) {
		DocumentId documentId = task.getDocument().getId();
		String filename = documentId.getSet().toUpperCase()
				.substring(0, documentId.getSet().length() - 1)
				+ ".P.10.T." + generatorId + "." + documentId.getName();
		Path peerPath = getPeerRoot().resolve(filename);

		return new DucPeer(peerPath, generatorId);
	}

}
