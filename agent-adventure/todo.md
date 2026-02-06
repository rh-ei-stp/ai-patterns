# To Do

- [ ] ~~Camel JBang routes/scripts to send prompts and retrieve responses over AMQP~~ Using REST dsl and curl instead. 

- [ ] Dev Services for the Apache Artemis broker. 
        Found the issue. Dev Services assigns a random port to the containers it starts. This is normally mapped to a Quarkus config property.
        But the Camel Components are ignorant of this random port and the property, so they fail to connect.
        Solution is probably to reference the Quarkus port property in the component URI with {{quakus.property.name}}
        Google didn't turn up any posts or examples for this. This is worth a blog post and example repo of its own.
        There are examples for Camel Quarkus Dev Services for a PostgresDB, but nothing for AMQP. 

- [ ] Dev Services for an Ollama instance running a granite model

- [ ] Externalize configuration for LangChain4J Agent bean

- [ ] Externalize system prompt(s)

- [ ] Agent method tool use

- [ ] Agent MCP tool use

- [ ] Agent guardrails

- [ ] Agent RAG
