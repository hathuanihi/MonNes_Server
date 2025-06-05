package com.SE104.quan_ly_so_tiet_kiem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils; // Import này cần thiết

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime; // Import này
import java.time.format.DateTimeFormatter; // Import này

@Configuration
public class ClockConfig {
    private static final Logger logger = LoggerFactory.getLogger(ClockConfig.class);

    // Trường static này sẽ giữ instance Clock hiện tại của ứng dụng.
    // Nó được khởi tạo với đồng hồ hệ thống.
    private static Clock mutableClock = Clock.systemDefaultZone();
    private static String currentClockDescription = "System Clock (Default)";

    // Đọc giá trị từ application.properties
    // Sử dụng một giá trị mặc định là một chuỗi rỗng nếu property không được đặt
    @Value("${app.fixed.clock.instant:}") 
    private String fixedInstantStringFromProperties;

    @Value("${app.fixed.clock.zone:Asia/Ho_Chi_Minh}")
    private String fixedZoneStringFromProperties;

    /**
     * Bean Clock chính được inject vào các service.
     * Nó sẽ trả về instance hiện tại của mutableClock.
     * @return Clock hiện tại của ứng dụng.
     */
    @Bean
    @Primary
    public Clock applicationClock() {
        // Kiểm tra và áp dụng cấu hình từ properties khi bean này được tạo lần đầu
        // (thường là khi ứng dụng khởi động)
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

    /**
     * Phương thức nội bộ để thay đổi clock và description.
     */
    private static void setClockInternal(Clock newClock, String description) {
        mutableClock = newClock;
        currentClockDescription = description;
    }

    /**
     * Cho phép DebugClockController thay đổi Clock của ứng dụng.
     * CHỈ DÙNG CHO MỤC ĐÍCH DEBUG/TEST.
     */
    public static void setClock(Clock newClock, String description) {
        logger.warn("!!! DANGER - CLOCK OVERRIDE: Application clock is being changed via debug endpoint to: {} !!!", description);
        setClockInternal(newClock, description);
    }

    /**
     * Reset Clock về thời gian hệ thống.
     */
    public static void resetToSystemClock() {
        logger.info("Application clock is being reset to system default via debug endpoint.");
        setClockInternal(Clock.systemDefaultZone(), "System Clock (Reset via debug)");
    }

    /**
     * Lấy mô tả về Clock hiện tại đang được sử dụng.
     */
    public static String getCurrentClockDescription() {
        ZonedDateTime currentDateTime = ZonedDateTime.ofInstant(mutableClock.instant(), mutableClock.getZone());
        return currentClockDescription + " | Current Effective Time: " + currentDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}