package com.example.task.controllers;

import com.example.task.ThrottlingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@RestController
public class History {
    private static final int THRESHOLD = 30;
    private static final long TIMEINTERVAL = 60000L; // 1 minute
    private static final long THROTTLING_DELAY = 3000L; // 3 seconds

    private Map<String, Queue<Long>> requestTimesMap = new ConcurrentHashMap<>();

    @GetMapping(path = "/api/history")
    public String getHostName(HttpServletRequest request) throws InterruptedException {
        String ipAddress = request.getRemoteAddr();
        long currentTime = System.currentTimeMillis();
        Queue<Long> requestTimes = requestTimesMap.get(ipAddress);
        if (requestTimes == null) {
            requestTimes = new ConcurrentLinkedQueue<>();
            Queue<Long> existingQueue = requestTimesMap.put(ipAddress, requestTimes);
            if (existingQueue != null) {
                requestTimes = existingQueue;
            }
        }

        // Remove expired requests based on their request time
        synchronized (requestTimes) {
            while (!requestTimes.isEmpty() && requestTimes.peek() < currentTime - TIMEINTERVAL) {
                requestTimes.poll();
            }
        }

        if (requestTimes.size() < THRESHOLD) {
            requestTimes.add(currentTime);
            return getBaseUrl(request) + "/api/history";
        } else if (requestTimes.size() >= 60) {
            //more than 30 requests are queued for throttling (overall >= 60)
            requestTimes.add(currentTime);
            return HttpStatus.SERVICE_UNAVAILABLE.toString();
        } else {
            // Throttle the request if the queued for throttling < 30 (overall < 60)
            ThrottlingService throttlingService = new ThrottlingService(currentTime, requestTimes, THROTTLING_DELAY);
            throttlingService.throttle();
            return getBaseUrl(request) + "/api/history";
        }
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getHeader("Host");
        return scheme + "://" + host;
    }
}