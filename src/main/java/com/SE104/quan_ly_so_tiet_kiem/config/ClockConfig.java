package com.SE104.quan_ly_so_tiet_kiem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils; 

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime; 
import java.time.format.DateTimeFormatter; 

@Configuration
public class ClockConfig {
    private static final Logger logger = LoggerFactory.getLogger(ClockConfig.class);

    private static Clock mutableClock = Clock.systemDefaultZone();
    private static String currentClockDescription = "System Clock (Default)";

    @Value("${app.fixed.clock.instant:}") 
    private String fixedInstantStringFromProperties;

    @Value("${app.fixed.clock.zone:Asia/Ho_Chi_Minh}")
    private String fixedZoneStringFromProperties;

    @Bean
    @Primary
    public Clock applicationClock() {

        if (StringUtils.hasText(fixedInstantStringFromProperties)) {
            try {
                Instant fixedInstant = Instant.parse(fixedInstantStringFromProperties.trim());
                ZoneId zoneId = ZoneId.of(fixedZoneStringFromProperties.trim());
                String description = "Fixed (from properties): " + ZonedDateTime.ofInstant(fixedInstant, zoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                setClockInternal(Clock.fixed(fixedInstant, zoneId), description);
                logger.info("!!! Clock initialized from properties: {} !!!", currentClockDescription);
            } catch (Exception e) {
                logger.error("!!! ERROR: Could not parse fixed clock instant from property '{}'. Defaulting to system clock. Error: {} !!!", fixedInstantStringFromProperties, e.getMessage());
                setClockInternal(Clock.systemDefaultZone(), "System Clock (Default after parse error)");
            }
        } else {
            logger.info("Clock initialized to: {}", currentClockDescription);
        }
        return mutableClock;
    }

    private static void setClockInternal(Clock newClock, String description) {
        mutableClock = newClock;
        currentClockDescription = description;
    }

    public static void setClock(Clock newClock, String description) {
        logger.warn("!!! DANGER - CLOCK OVERRIDE: Application clock is being changed via debug endpoint to: {} !!!", description);
        setClockInternal(newClock, description);
    }


    public static void resetToSystemClock() {
        logger.info("Application clock is being reset to system default via debug endpoint.");
        setClockInternal(Clock.systemDefaultZone(), "System Clock (Reset via debug)");
    }

    public static String getCurrentClockDescription() {
        ZonedDateTime currentDateTime = ZonedDateTime.ofInstant(mutableClock.instant(), mutableClock.getZone());
        return currentClockDescription + " | Current Effective Time: " + currentDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}