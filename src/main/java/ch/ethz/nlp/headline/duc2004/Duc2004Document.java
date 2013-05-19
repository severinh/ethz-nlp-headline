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

	public Duc2004Document(Path path) {
		String set = path.getParent().getFileName().toString();
		String name = path.getFileName().toString();

		this.path = path;
		this.id = new DocumentId(set, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ethz.nlp.headline.Document#getPath()
	 */
	@Override
	public Path getPath() {
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ethz.nlp.headline.Document#getId()
	 */
	public DocumentId getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ethz.nlp.headline.Document#load()
	 */
	@Override
	public String load() throws IOException {
		String html = new String(Files.readAllBytes(getPath()));
		Matcher matcher = TEXT_PATTERN.matcher(html);
		if (matcher.find()) {
			String text = matcher.group(1).trim();
			return text;
		} else {
			throw new IOException("could not parse document");
		}
	}

	@Override
	public String toString() {
		return "Document [id=" + id + "]";
	}

}
