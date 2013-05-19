package ch.ethz.nlp.headline;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public abstract class Dataset {

	public abstract Path getDocumentRoot();

	public abstract Path getModelRoot();

	public abstract Path getPeerRoot();

	public abstract List<Document> getDocuments();

	public abstract List<Model> getModels();

	public abstract Peer makePeer(Task task, String generatorId);

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

}