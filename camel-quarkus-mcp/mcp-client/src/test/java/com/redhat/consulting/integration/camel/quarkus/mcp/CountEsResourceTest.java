package com.redhat.consulting.integration.camel.quarkus.mcp;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class CountEsResourceTest {

    // TODO: mock the AiLetterCounterService

    @Test
    void testCountEndpoint() {
        given()
          .when().get("/countEs/splendiferous")
          .then()
             .statusCode(200)
             .body(containsString("2"));
    }

}