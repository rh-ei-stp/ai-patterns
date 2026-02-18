package com.redhat.consulting.intergation.agentadventure;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.microprofile.config.ConfigProvider;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class RoomRetriever {

    static final String ROOMS_FILENAME = "rooms.txt";

    private ContentRetriever contentRetriever;

    public ContentRetriever getContentRetriever() {
        return contentRetriever;
    }

    void ingest(@Observes StartupEvent event) {
        Log.info("Ingesting rooms for RAG from " + ROOMS_FILENAME);
        Document roomsDoc = loadDocument(ROOMS_FILENAME);

        DocumentSplitter splitter = new DocumentByParagraphSplitter(300, 0);
        List<TextSegment> segments = splitter.split(roomsDoc);
        Log.info("segments=" + segments);

        EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl(getBaseUrl())
                .modelName("granite-embedding:30m")
                .build();

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        InMemoryEmbeddingStore<TextSegment> roomStore = new InMemoryEmbeddingStore<>();
        roomStore.addAll(embeddings, segments);
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(roomStore)
                .embeddingModel(embeddingModel)
                .maxResults(1)
                .minScore(0.8)
                .build();

        this.contentRetriever = contentRetriever;
    }

    // TODO: Abstract this so it can be deployed without Ollama.
    // TODO: Currently this is a workaround for not being able to set the Ollama Dev Services container to a fixed port.
    // TODO: move to a shared utility/config class
    private static String getBaseUrl() {
        String ollamaEndpoint = ConfigProvider.getConfig().getValue("langchain4j-ollama-dev-service.ollama.endpoint",
                String.class);
        Log.info("ollamaEndpoint=" + ollamaEndpoint);
        return ollamaEndpoint;
    }

    private static Document loadDocument(String filename) {
        try {
            var filePath = Path.of(RoomRetriever.class.getClassLoader().getResource(filename).toURI());
            return FileSystemDocumentLoader.loadDocument(filePath);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
