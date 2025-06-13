package com.SE104.quan_ly_so_tiet_kiem.controllers;

import com.SE104.quan_ly_so_tiet_kiem.config.ClockConfig;
import com.SE104.quan_ly_so_tiet_kiem.service.ScheduledTasksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/debug/clock")
@Profile({"dev", "test"}) 
public class DebugClockController {

    private static final Logger logger = LoggerFactory.getLogger(DebugClockController.class);

    @Autowired
    private ScheduledTasksService scheduledTasksService;

    @PostMapping("/set-fixed")
    public ResponseEntity<String> setFixedClock(
            @RequestParam String instantString,
            @RequestParam(required = false, defaultValue = "Asia/Ho_Chi_Minh") String zoneIdString) {
        try {
            Instant fixedInstant = Instant.parse(instantString.trim());
            ZoneId zoneId = ZoneId.of(zoneIdString.trim());
            Clock fixedClock = Clock.fixed(fixedInstant, zoneId);
            String description = "Fixed via API: " + ZonedDateTime.ofInstant(fixedInstant, zoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            ClockConfig.setClock(fixedClock, description);
            logger.info("Debug endpoint called: Clock fixed to {}", description);
            return ResponseEntity.ok("Application clock has been fixed to: " + description + ". Current effective time: " + ClockConfig.getCurrentClockDescription());
        } catch (DateTimeParseException e) {
            logger.error("Error parsing instant string '{}' or zoneId '{}'", instantString, zoneIdString, e);
            return ResponseEntity.badRequest().body("Invalid instant or zoneId format. Instant example: '2025-12-25T10:00:00Z'. ZoneId example: 'Asia/Ho_Chi_Minh'. Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error setting fixed clock", e);
            return ResponseEntity.internalServerError().body("Error setting fixed clock: " + e.getMessage());
        }
    }

    @PostMapping("/set-system")
    public ResponseEntity<String> setSystemDefaultClock() {
        ClockConfig.resetToSystemClock();
        logger.info("Debug endpoint called: Clock reset to system default.");
        return ResponseEntity.ok("Application clock has been reset to system default. Current: " + ClockConfig.getCurrentClockDescription());
    }

    @GetMapping("/current")
    public ResponseEntity<String> getCurrentClockInfo() {
        String clockInfo = ClockConfig.getCurrentClockDescription();
        logger.info("Debug endpoint called: Retrieving current clock info: {}", clockInfo);
        return ResponseEntity.ok(clockInfo);
    }

    @PostMapping("/trigger-daily-processing")
    public ResponseEntity<String> triggerDailyProcessing() {
        try {
            logger.info("Debug endpoint called: Manually triggering dailyAccountProcessing");
            scheduledTasksService.dailyAccountProcessing();
            return ResponseEntity.ok("Daily account processing completed successfully. Check logs for details.");
        } catch (Exception e) {
            logger.error("Error during manual daily processing trigger", e);
            return ResponseEntity.internalServerError().body("Error during daily processing: " + e.getMessage());
        }
    }
}