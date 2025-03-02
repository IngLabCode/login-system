package com.ingilab.loginsystem.logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

@Service
public class LogCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(LogCleanupService.class);
    private static final String LOG_FILE = "logs/app.log";

    // Hər 3 aydan bir işləsin
    @Scheduled(cron = "0 0 0 1 */3 ?")
    public void clearLogFile() {
        File logFile = new File(LOG_FILE);
        if (logFile.exists()) {
            try (FileWriter writer = new FileWriter(logFile)) {
                writer.write(""); // Faylın məzmununu təmizlə
                logger.info("Log file cleared on: " + LocalDate.now());
            } catch (IOException e) {
                logger.error("Failed to clear log file: " + e.getMessage());
            }
        } else {
            logger.warn("Log file does not exist: " + LOG_FILE);
        }
    }
}