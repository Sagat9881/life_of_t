package ru.lifegame.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke tests: verifies the Spring application context loads and the
 * basic HTTP endpoints respond correctly.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
        // If this test runs, the context started successfully.
    }

    @Test
    void statusEndpointReturns200() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/demo/status", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();
    }

    @Test
    void indexPageLoads() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/", String.class);
        // Spring Boot serves index.html from /static/ on GET /
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Life of T");
    }

    @Test
    void questLogEndpointReturnsList() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/demo/quest-log", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Quest-log JSON must be an array
        assertThat(response.getBody()).startsWith("[");
    }

    @Test
    void charactersEndpointContainsTanya() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/demo/characters", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("tanya");
    }
}
