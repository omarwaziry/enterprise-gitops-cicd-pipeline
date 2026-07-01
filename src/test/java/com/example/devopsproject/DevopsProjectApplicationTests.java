package com.example.devopsproject;

import com.example.devopsproject.controller.HomeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration and unit tests for the Spring Boot application.
 * Verifies context loading, dashboard html rendering, and info json responses.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DevopsProjectApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private HomeController homeController;

    /**
     * Verifies that the application context loads successfully and
     * the HomeController bean is instantiated.
     */
    @Test
    void contextLoads() {
        assertThat(homeController).isNotNull();
     }
 
    /**
     * Verifies that the root path endpoint returns the HTML dashboard UI
     * containing expected header and status indicator texts.
     */
    @Test
    void indexReturnsHtmlDashboard() {
        String response = this.restTemplate.getForObject("http://localhost:" + port + "/", String.class);
        assertThat(response).contains("GitOps Deployment Dashboard");
        assertThat(response).contains("Live & Synced");
    }
 
    /**
     * Verifies that the /api/info endpoint returns a JSON payload
     * indicating a successful health check status.
     */
    @SuppressWarnings("unchecked")
    @Test
    void infoReturnsJsonDetails() {
        ResponseEntity<Map> responseEntity = this.restTemplate.getForEntity("http://localhost:" + port + "/api/info", Map.class);
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        
        Map<String, Object> body = responseEntity.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo("UP");
        assertThat(body.get("version")).isEqualTo("1.0.0");
        assertThat(body.get("framework")).isEqualTo("Spring Boot");
    }
}
