package ch.ethz.nlp.headline;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Properties;
import javax.xml.transform.OutputKeys;

import com.google.common.collect.Multimap;
import com.jamesmurty.utils.XMLBuilder;

public class EvaluationConfig {

	private final Properties outputProperties;
	private final Dataset dataset;

	public EvaluationConfig(Dataset dataset) {
		this.outputProperties = new Properties();
		this.outputProperties.put(OutputKeys.INDENT, "yes");
		this.outputProperties.put("{http://xml.apache.org/xslt}indent-amount",
				"2");
		this.outputProperties.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
		this.dataset = dataset;
	}

	public void write(Path configPath, Multimap<Task, Peer> peersMap) {
		Path peerRoot = dataset.getPeerRoot();
		Path modelRoot = dataset.getModelRoot();
		try {
			XMLBuilder builder = XMLBuilder.create("ROUGE_EVAL");
			for (Task task : peersMap.keySet()) {
				Document document = task.getDocument();
				Collection<Peer> peers = peersMap.get(task);
				XMLBuilder evalBuilder = builder.elem("EVAL");
				evalBuilder.attr("ID", document.getId().toString());
				evalBuilder.elem("PEER-ROOT").text(peerRoot.toString());
				evalBuilder.elem("MODEL-ROOT").text(modelRoot.toString());
				evalBuilder.elem("INPUT-FORMAT").attr("TYPE", "SPL");
				XMLBuilder peersBuilder = evalBuilder.elem("PEERS");
				for (Peer peer : peers) {
					Path relativePath = peerRoot.relativize(peer.getPath());
					XMLBuilder peerBuilder = peersBuilder.elem("P");
					peerBuilder.attr("ID", peer.getGeneratorId());
					peerBuilder.text(relativePath.toString());
				}
				XMLBuilder modelsBuilder = evalBuilder.elem("MODELS");
				for (Model model : task.getModels()) {
					Path relativePath = modelRoot.relativize(model.getPath());
					XMLBuilder modelBuilder = modelsBuilder.elem("M");
					modelBuilder.attr("ID", model.getId());
					modelBuilder.text(relativePath.toString());
				}
			}

			StringWriter stringWriter = new StringWriter();
			builder.toWriter(stringWriter, outputProperties);
			byte[] bytes = stringWriter.toString().getBytes();
			Files.write(configPath, bytes, StandardOpenOption.CREATE);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
