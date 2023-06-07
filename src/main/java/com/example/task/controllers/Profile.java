package com.example.task.controllers;

import com.example.task.ThrottlingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
public class Profile {
    private static final int THRESHOLD = 10;
    private static final long TIMEINTERVAL = 60000L; // 1 minute
    private static final long THROTTLING_DELAY = 3000L; // 3 seconds

    private Map<String, Queue<Long>> requestTimesMap = new ConcurrentHashMap<>();


    @GetMapping(path = "/api/profile")
    public String getHostName(HttpServletRequest request) throws InterruptedException {
        String ipAddress = request.getRemoteAddr();
        long currentTime = System.currentTimeMillis();
        //check if there is already a queue of request times for this ip address
        Queue<Long> requestTimes = requestTimesMap.get(ipAddress);
        if (requestTimes == null) {
            requestTimes = new ConcurrentLinkedQueue<>();
            Queue<Long> existingQueue = requestTimesMap.put(ipAddress, requestTimes);
            if (existingQueue != null) {
                requestTimes = existingQueue;
            }
        }
        //ensuring that only one thread can enter and execute the code at a time (shared variable)
        synchronized (requestTimes) {
            // Remove expired requests based on their request time
            while (!requestTimes.isEmpty() && requestTimes.peek() < currentTime - TIMEINTERVAL) {
                requestTimes.poll();
            }
        }

        if (requestTimes.size() < THRESHOLD) {
            requestTimes.add(currentTime);
        } else {
            // Throttle the request if the queue for throttling > 10
            ThrottlingService throttlingService = new ThrottlingService(currentTime, requestTimes, THROTTLING_DELAY);
            throttlingService.throttle();
        }
        return getBaseUrl(request) + "/api/profile";
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getHeader("Host");
        return scheme + "://" + host;
    }
}
