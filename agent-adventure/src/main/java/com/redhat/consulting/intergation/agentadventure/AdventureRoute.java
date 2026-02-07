package com.redhat.consulting.intergation.agentadventure;

import org.apache.camel.BindToRegistry;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import org.apache.camel.component.langchain4j.agent.api.Headers;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class AdventureRoute extends RouteBuilder {

    @BindToRegistry("agent")
    public Agent configureAgent() {

        // TODO: externalize this configuration
        ChatModel ollamaModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .temperature(0.0)
                .logRequests(false)
                .logResponses(false)
                // .modelName("granite4:1b")
                .modelName("granite4:1b")
                .build();

        // Create agent configuration
        AgentConfiguration configuration = new AgentConfiguration()
                .withChatModel(ollamaModel);

        // Create the agent
        Agent agent = new AgentWithoutMemory(configuration);
        return agent;
    }

    @Override
    public void configure() throws Exception {

        // @formatter:off
        from("amqp:queue:players/input")
            .routeId("adventure-listener")
            .to("log:adventure-listener?showHeaders=true")
            .to("direct:adventure-agent")
            .to("log:adventure-listener?showHeaders=true")
            .toD("amqp:queue:players/${header.playerId}/response");

        from("direct:adventure-agent")
            .routeId("adventure-agent")
            // TODO: externalize all prompt messages
            .setHeader(Headers.SYSTEM_MESSAGE).simple("""
                You are a text-based adventure set in a ruined castle.
                Vividly describe the user's location and what is happening in the style of a fantasy writer.
                Limit descriptions to less than 50 words. 
                """)
            .convertBodyTo(String.class)
            .to("log:adventure-agent?showHeaders=true")
            .to("langchain4j-agent:adventure?agent=#agent");

        // Runs once at startup to describe the entrance
        // from("timer:runOnce?repeatCount=1")
        //     .routeId("enterTheCastle")
        //     .setHeader("playerId", constant("player001"))
        //     .setBody().constant("I enter the castle.")
        //     .to("direct:adventure-agent")
        //     .to("stream:out");
        // @formatter:on
    }
}
