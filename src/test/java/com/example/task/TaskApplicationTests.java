package com.example.task;


import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskApplicationTests {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testProfileController() {
        long currentTime;
        for (int i = 0; i < 10; i++) {

            ResponseEntity<String> response = restTemplate.getForEntity(
                    "http://localhost:" + port + "/api/profile", String.class);
            assertThat(response.getBody()).isEqualTo("http://localhost:" + port + "/api/profile");
        }

        currentTime = System.currentTimeMillis();

        // The next request should exceed the throttled
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/profile", String.class);
        long currentTime1 = System.currentTimeMillis();
        System.out.println(currentTime + "   " + currentTime1);
        assertThat(currentTime1 - currentTime).isGreaterThan(3); // due to the delay
    }

    @Test
    public void testHistoryController() {
        // Note : for this test to pass increase the time interval to be 2 min or more because the execution may take more than 1 min due to the delay
        // so the throttling queue will be less than 30 and service unable msg will not appear and the test will fail
        long currentTime;
        for (int i = 0; i < 30; i++) {

            ResponseEntity<String> response = restTemplate.getForEntity(
                    "http://localhost:" + port + "/api/history", String.class);
            assertThat(response.getBody()).isEqualTo("http://localhost:" + port + "/api/history");
        }

        currentTime = System.currentTimeMillis();
        // The next request should exceed the throttled
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/history", String.class);
        long currentTime1 = System.currentTimeMillis();
        System.out.println(currentTime + "   " + currentTime1);
        assertThat(currentTime1 - currentTime).isGreaterThan(3); // due to the delay
        for (int i = 1; i < 30; i++) {

            response = restTemplate.getForEntity(
                    "http://localhost:" + port + "/api/history", String.class);
            assertThat(response.getBody()).isEqualTo("http://localhost:" + port + "/api/history");
        }
        response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/history", String.class);
        assertThat(response.getBody()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.toString());
    }
}