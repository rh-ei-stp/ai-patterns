# camel-quarkus-mcp

An example for instrumenting existing Camel Quarkus routes with Quarkus MCP. 

The demo route counts the number of letter 'e's in an input, a task LLMs are famously bad at.

```
❯ ollama run granite4:1b
>>> How many 'e's are in the word splendiferous?
The word "splendiferous" contains 4 'e' letters.
```

## Start `mcp-service`

```sh
cd mcp-service
quarkus dev
```

## Test `mcp-service`

### HTTP endpoint

```sh
curl -X POST localhost:8080/camel/countEs -H "Content-Type: text/plain"  -d "Splendiferous"
```

### MCP Endpoint

Visit the Dev UI page: http://localhost:8080/q/dev-ui/quarkus-mcp-server-http/tools

### Call with an agent

*not yet implemented*
