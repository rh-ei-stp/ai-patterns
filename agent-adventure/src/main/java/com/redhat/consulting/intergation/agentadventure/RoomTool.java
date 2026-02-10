package com.redhat.consulting.intergation.agentadventure;

import java.util.stream.Collectors;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import io.quarkus.logging.Log;

public class RoomTool {
    // final RetrievalAugmentor roomAugmentor;

    // RoomRetriever(RetrievalAugmentor roomAugmentor){
    //     this.roomAugmentor = roomAugmentor;
    // }
    private final ContentRetriever contentRetriever;

    RoomTool(ContentRetriever contentRetriever) {
        this.contentRetriever = contentRetriever;
    }

    @Tool("Lookup the description of a room by its name.")
    public String lookup(String roomName) {
        return contentRetriever.retrieve(new Query(roomName)).stream()
                .map(content -> {
                    Log.info("content="+content);
                    return content.textSegment().text();
                })
                .collect(Collectors.joining("\n\n"));
    }

}
