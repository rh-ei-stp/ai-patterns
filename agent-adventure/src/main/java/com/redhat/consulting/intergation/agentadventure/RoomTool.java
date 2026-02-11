package com.redhat.consulting.intergation.agentadventure;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import io.quarkus.logging.Log;

public class RoomTool {

    private final ContentRetriever contentRetriever;

    RoomTool(ContentRetriever contentRetriever) {
        this.contentRetriever = contentRetriever;
    }

    @Tool("Lookup the description of a room by its name.")
    public String lookup(String roomName) {

        Content description = contentRetriever.retrieve(new Query(roomName))
                .stream().findFirst()
                .orElse(Content.from("Could not find " + roomName));

        Log.info("description="+description);

        return description.textSegment().text();
    }

}
