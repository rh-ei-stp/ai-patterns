package com.redhat.consulting.intergation.agentadventure;

import java.util.List;

import org.apache.camel.BindToRegistry;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import org.apache.camel.component.langchain4j.agent.api.Headers;
import org.eclipse.microprofile.config.ConfigProvider;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.model.chat.ChatModel;
// import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AdventureRoute extends RouteBuilder {

    @Inject
    RoomTool roomTool;

    @BindToRegistry("agent")
    public Agent configureAgent() {
        // String ollamaEndpoint = ConfigProvider.getConfig().getValue("langchain4j-ollama-dev-service.ollama.endpoint",
        //         String.class);
        // String ollamaEndpoint = "http://localhost:11434";

        // TODO: externalize this configuration
        // ChatModel ollamaModel = OllamaChatModel.builder()
        //         .baseUrl(ollamaEndpoint)
        //         .temperature(0.1)
        //         .logRequests(false)
        //         .logResponses(false)
        //         .modelName("granite4:1b")
        //         // .modelName("qwen3:1.7b")
        //         .build();

        ChatModel openAiModel = OpenAiChatModel.builder()
                .apiKey("demo")
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .modelName("gpt-4o-mini").temperature(0.3)
                // .timeout(ofSeconds(3000))
                .build();

        // TODO: externalize this config
        McpTransport diceRollerTransport = new StreamableHttpMcpTransport.Builder()
                .url("http://localhost:8081/mcp")
                .logRequests(true)
                .logRequests(true)
                .build();

        McpClient diceRollerClient = new DefaultMcpClient.Builder()
                .clientName("dice_roller")
                .transport(diceRollerTransport)
                .build();

        // Create agent configuration
        AgentConfiguration configuration = new AgentConfiguration()
                // .withChatModel(ollamaModel)
                .withChatModel(openAiModel)
                .withCustomTools(List.of(roomTool))
                .withMcpClient(diceRollerClient);

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
                Look up the user's location by name and vividly describe it in the style of a fantasy writer.
                Use the MCP client dice_roller to rollSimple to get a number between 1 and 100. If the number is 1, then include a skeleton in the room description.
                Limit descriptions to less than 50 words. 
                """)
            // .setHeader(Headers.SYSTEM_MESSAGE).simple("""
            //     You are a text-based adventure set in a ruined castle.
            //     Use the MCP client dice_roller to call rollSimple to get a number between 1 and 2. If the number is 2, then include a skeleton in the room description.
            //     Roll dice with lower bound at 1 and upper bound at 2. If it returns 2, include a skeleton in the room description.
            //     Look up the user's location by name and vividly describe it in the style of a fantasy writer.
            //     Limit descriptions to less than 50 words. 
            //     """)
            // .setHeader(Headers.SYSTEM_MESSAGE).simple("""
            //     You are a text-based adventure set in a ruined castle.
            //     Roll dice 1d1+0 and if the result equals 1 then include a skeleton in the room description.
            //     Look up the user's location by name and vividly describe it in the style of a fantasy writer.
            //     Limit descriptions to less than 50 words. 
            //     """)
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
