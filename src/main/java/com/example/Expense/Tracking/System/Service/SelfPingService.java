package com.example.Expense.Tracking.System.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SelfPingService {

    private static final Logger logger = LoggerFactory.getLogger(SelfPingService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.url}")
    private String appUrl; // Your Render app URL, e.g., https://myapp.onrender.com

    // Ping every 5 minutes (300,000 milliseconds)
    @Scheduled(fixedRate = 300000)
    public void pingSelf() {
        try {
            String response = restTemplate.getForObject(appUrl + "/", String.class);
            logger.info("Self-ping successful: {}", response);
        } catch (Exception e) {
            logger.error("Self-ping failed", e);
        }
    }
}
