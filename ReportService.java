package com.course.management.service;

import com.course.management.entity.Course;
import com.course.management.repository.CourseRepository;
import com.course.management.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ReportService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    /**
     * Calculates the number of students enrolled per course asynchronously.
     */
    public CompletableFuture<Map<String, Long>> getStudentCountPerCourse() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Long> report = new HashMap<>();
            for (Course course : courseRepository.findAll()) {
                Long count = enrollmentRepository.countByCourseId(course.getId());
                report.put(course.getTitle(), count);
            }
            return report;
        });
    }

    /**
     * Calculates the revenue collected per course (Enrollment Count * Fee) asynchronously.
     */
    public CompletableFuture<Map<String, Double>> getRevenuePerCourse() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Double> report = new HashMap<>();
            for (Course course : courseRepository.findAll()) {
                Long count = enrollmentRepository.countByCourseId(course.getId());
                double revenue = count * course.getFee();
                report.put(course.getTitle(), revenue);
            }
            return report;
        });
    }

    /**
     * Provides a status count for all enrollments (Not Started, In Progress, Completed) asynchronously.
     */
    public CompletableFuture<Map<String, Long>> getEnrollmentStatusReport() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Long> report = new HashMap<>();
            report.put("Not Started", enrollmentRepository.countByStatus("Not Started"));
            report.put("In Progress", enrollmentRepository.countByStatus("In Progress"));
            report.put("Completed", enrollmentRepository.countByStatus("Completed"));
            return report;
        });
    }
}
