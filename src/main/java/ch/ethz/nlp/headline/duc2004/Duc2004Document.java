package ch.ethz.nlp.headline.duc2004;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.DocumentId;

public class Duc2004Document implements Document {

	private static final Pattern TEXT_PATTERN = Pattern.compile(
			"<TEXT>(.*)</TEXT>", Pattern.MULTILINE | Pattern.DOTALL);

	private final Path path;
	private final DocumentId id;
	private final String content;

	public Duc2004Document(Path path) throws IOException {
		String set = path.getParent().getFileName().toString();
		String name = path.getFileName().toString();

		this.path = path;
		this.id = new DocumentId(set, name);

		String html = new String(Files.readAllBytes(getPath()));
		Matcher matcher = TEXT_PATTERN.matcher(html);
		if (matcher.find()) {
			content = matcher.group(1).trim().replace("\\n", " ");
		} else {
			throw new IOException("could not parse document");
		}
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public DocumentId getId() {
		return id;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "Document [id=" + id + "]";
	}

}
