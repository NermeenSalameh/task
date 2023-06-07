package com.example.task;

import java.util.Queue;


public class ThrottlingService {
    private final long THROTTLING_DELAY; // 3 seconds

    private long currentTime;
    private Queue<Long> requestTimes;

    public ThrottlingService(long currentTime, Queue<Long> requestTimes, long throttlingDelay) {
        this.currentTime = currentTime;
        this.requestTimes = requestTimes;
        this.THROTTLING_DELAY = throttlingDelay;
    }

    public void throttle() throws InterruptedException {
        Thread throttlingThread = new ThrottlingThread(currentTime, requestTimes);
        throttlingThread.start();
        throttlingThread.join();
    }

    private class ThrottlingThread extends Thread {
        private long currentTime;
        private Queue<Long> requestTimes;

        public ThrottlingThread(long currentTime, Queue<Long> requestTimes) {
            this.currentTime = currentTime;
            this.requestTimes = requestTimes;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(THROTTLING_DELAY);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            requestTimes.add(currentTime);
        }
    }
}

