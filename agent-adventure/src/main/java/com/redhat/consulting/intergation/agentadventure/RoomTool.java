package com.redhat.consulting.intergation.agentadventure;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RoomTool {

    @Inject RoomRetriever roomRetriever;

    @Tool("Lookup the description of a room by its name.")
    public String lookup(String roomName) {

        Content description = roomRetriever.getContentRetriever().retrieve(new Query(roomName))
                .stream().findFirst()
                .orElse(Content.from("Could not find " + roomName));

        Log.info("description="+description);

        return description.textSegment().text();
    }

}
