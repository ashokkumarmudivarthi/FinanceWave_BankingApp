package com.financewave.auth.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS = 5;      // limit
    private static final int BLOCK_MINUTES = 5;     // block duration

    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> blockTime = new ConcurrentHashMap<>();

    public void validate(String ip) {

        // 🔒 Check if blocked
        if (blockTime.containsKey(ip)) {

            LocalDateTime blockedAt = blockTime.get(ip);

            if (blockedAt.plusMinutes(BLOCK_MINUTES).isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Too many requests. Try again later.");
            } else {
                // unblock after time
                blockTime.remove(ip);
                requestCounts.remove(ip);
            }
        }

        // 🔢 Count requests
        int count = requestCounts.getOrDefault(ip, 0) + 1;
        requestCounts.put(ip, count);

        // 🚫 Block if exceeded
        if (count > MAX_REQUESTS) {
            blockTime.put(ip, LocalDateTime.now());
            throw new RuntimeException("Too many requests. IP blocked for 5 minutes.");
        }
    }
}