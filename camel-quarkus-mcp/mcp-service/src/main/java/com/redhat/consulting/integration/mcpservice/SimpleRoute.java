package com.redhat.consulting.integration.mcpservice;

import java.util.List;

import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SimpleRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        rest("/camel/countEs")
            .post()
                .to("direct:countEs");

        from("direct:countEs")
            .log("word=${body}")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                String result = Long.toString(List.of(body.split("")).stream().filter(s -> s.equalsIgnoreCase("e")).count());
                exchange.getIn().setBody(result);
            })
            .log("count=${body}");
    }
}
