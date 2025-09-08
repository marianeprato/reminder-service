package org.reminderservice;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationIT {

    @LocalServerPort
    int port;

    @Test
    void contextLoads() {
    }

    @Test
    void testHealthCheck() {
        RestAssured.given()
                .baseUri("http://localhost")
                .port(port)
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void testGetAllRemindersReturnsEmptyList() {
        RestAssured.given()
                .baseUri("http://localhost")
                .port(port)
                .when()
                .get("/reminders")
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }
}
