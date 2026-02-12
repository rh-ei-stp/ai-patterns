package com.redhat.consulting.integration.camel.quarkus.mcp;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class CountEsResourceTest {
    @Test
    void testCountEndpoint() {
        given()
          .when().get("/count/splendiferous")
          .then()
             .statusCode(200)
             .body(is("2"));
    }

}