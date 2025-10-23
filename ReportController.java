package com.course.management.controller;

import com.course.management.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*") // Allow requests from the frontend
public class ReportController {

    @Autowired
    private ReportService reportService;

    // ADMIN: Generate report on students enrolled per course
    @GetMapping("/students-per-course")
    public Map<String, Long> getStudentCountPerCourse() throws ExecutionException, InterruptedException {
        // Assuming ReportService methods return CompletableFuture<Map<String, T>> and you call .get()
        return reportService.getStudentCountPerCourse().get();
    }

    // ADMIN: Generate report on revenue collected per course
    @GetMapping("/revenue-per-course")
    public Map<String, Double> getRevenuePerCourse() throws ExecutionException, InterruptedException {
        return reportService.getRevenuePerCourse().get();
    }

    // ADMIN: Generate report on enrollment status (e.g., Active, Completed)
    @GetMapping("/enrollment-status")
    public Map<String, Long> getEnrollmentStatusReport() throws ExecutionException, InterruptedException {
        return reportService.getEnrollmentStatusReport().get();
    }
}
