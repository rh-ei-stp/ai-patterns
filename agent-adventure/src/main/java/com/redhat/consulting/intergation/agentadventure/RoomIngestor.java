package com.redhat.consulting.intergation.agentadventure;

import java.util.List;

import org.eclipse.microprofile.config.ConfigProvider;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.quarkus.logging.Log;

public class RoomIngestor {

    // TODO: externalize
    final static String ROOM_KNOWLEDGE_BASE = """
            ENTRANCE
            name: entrance
            features: crumpled bridge over a moat, empty doorway flanked by statues
            exits: gatehouse, forest

            GATEHOUSE
            name: gatehouse
            features: rusted porticullis on the ground, arrow slits
            exits: great-hall, entrance

            FOREST
            name: forest
            features: dense thicket of gnarled trees, wall of thorns
            exits: entrance

            GREAT_HALL
            name: great hall
            features: rotten tapestries, splintered tables
            exits: chapel, throne-room

            CHAPEL
            name: chapel
            features: chipped stone altar, shattered stained glass windows
            exits: great hall

            THRONE_ROOM
            name: throne room
            features: toppled throne
            exits: great hall
            """;

    public static ContentRetriever ingest() {
        Log.info("Ingesting rooms for RAG.");
        Document roomsDoc = Document.from(ROOM_KNOWLEDGE_BASE);

        // DocumentSplitter splitter = new DocumentByParagraphSplitter(300, 0);
        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.split(roomsDoc);
        Log.info("segments="+segments);


        // TODO: abstract this so it can be deployed without ollama
        // String ollamaEndpoint = ConfigProvider.getConfig().getValue("langchain4j-ollama-dev-service.ollama.endpoint",
        //         String.class);
        String ollamaEndpoint = "http://localhost:11434";
        Log.info("ollamaEndpoint=" + ollamaEndpoint);
        EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl(ollamaEndpoint)
                .modelName("granite-embedding:30m")
                .build();

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        InMemoryEmbeddingStore<TextSegment> roomStore = new InMemoryEmbeddingStore<>();
        roomStore.addAll(embeddings);
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(roomStore)
                .embeddingModel(embeddingModel)
                .maxResults(1)
                .minScore(0.6)
                .build(); 
        // RetrievalAugmentor augmentor = DefaultRetrievalAugmentor.builder()
        //         .contentRetriever(contentRetriever)
        //         .build();
        // return augmentor;


        return contentRetriever;
    }
}
