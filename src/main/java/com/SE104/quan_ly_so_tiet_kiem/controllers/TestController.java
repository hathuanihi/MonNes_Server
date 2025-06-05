package com.SE104.quan_ly_so_tiet_kiem.controllers;

import com.SE104.quan_ly_so_tiet_kiem.service.ScheduledTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @Autowired
    private ScheduledTasksService scheduledTasksService;

    @GetMapping("/run-daily-processing")
    public String runDailyProcessing() {
        scheduledTasksService.dailyAccountProcessing();
        return "Daily processing triggered manually.";
    }
}