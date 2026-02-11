//DEPS org.apache.camel:camel-bom:4.17.0@pom
//DEPS org.apache.camel:camel-core

import org.apache.camel.builder.RouteBuilder;

public class simpleRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:java?repeatCount=1")
            .setBody()
                .simple("Hello Camel from ${routeId}")
            .log("${body}")
            .to("direct:uppercaseAndReverse");

        from("direct:uppercaseAndReverse")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                String transformed = new StringBuilder(body.toUpperCase()).reverse().toString();
                exchange.getIn().setBody(transformed);
            })
            .log("${body}");
    }
}
