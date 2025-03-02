package com.ingilab.loginsystem.logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingService {
    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);

    public void logMessages() {
        logger.info("This is an info message."); // Loglara yazılır
        logger.error("This is an error message."); // Loglara yazılır
    }
}