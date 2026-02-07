package com.redhat.consulting.intergation.agentadventure;

import org.apache.camel.builder.RouteBuilder;

public class PlayerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        rest("/camel/player/{playerId}")
            .post()
                .routeId("playerHttp")
                .to("direct:playerInput");

        // TODO: externalize the amqp config
        from("direct:playerInput")
            .routeId("handlePlayerInput")
            .to("log:handlePlayerInput?showHeaders=true")
            .to("amqp:queue:players/input?requestTimeout=60000&disableReplyTo=true")
            .pollEnrich().simple("amqp:queue:players/${header.playerId}/response?requestTimeout=60000");

    }

}
