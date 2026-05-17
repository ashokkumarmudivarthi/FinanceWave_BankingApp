package com.financewave.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    // 🔧 CONFIG FROM application.properties
    @Value("${rate.limit.maxRequests}")
    private int maxRequests;

    @Value("${rate.limit.blockMinutes}")
    private int blockMinutes;
    

    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> blockTime = new ConcurrentHashMap<>();

    public void validate(String ip) {

        // 🔒 Check if blocked
        if (blockTime.containsKey(ip)) {

            LocalDateTime blockedAt = blockTime.get(ip);

            if (blockedAt.plusMinutes(blockMinutes).isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Too many requests. IP blocked for " + blockMinutes + " minutes.");
            } else {
                // ✅ Unblock after time
                blockTime.remove(ip);
                requestCounts.remove(ip);
            }
        }

        // 🔢 Count requests
        int count = requestCounts.getOrDefault(ip, 0) + 1;
        requestCounts.put(ip, count);

        // 🚫 Block if exceeded
        if (count > maxRequests) {
            blockTime.put(ip, LocalDateTime.now());
            throw new RuntimeException("Too many requests. IP blocked for " + blockMinutes + " minutes.");
        }
    }

    // 🧪 DEV ONLY (optional helper)
    public void reset() {
        requestCounts.clear();
        blockTime.clear();
    }
    
    
}