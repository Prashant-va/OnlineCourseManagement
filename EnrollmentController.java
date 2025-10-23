package com.course.management.controller;

import com.course.management.entity.Enrollment;
import com.course.management.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping("/student/{id}")
    public ResponseEntity<List<Enrollment>> getByStudent(@PathVariable Long id) {
        try {
            List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(id);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> enroll(@RequestParam Long studentId, @RequestParam Long courseId) {
        try {
            Enrollment enrollment = enrollmentService.enrollStudent(studentId, courseId);
            return ResponseEntity.status(201).body(enrollment);
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(400).body(ise.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<?> updateProgress(@PathVariable Long id, @RequestParam double progress) {
        try {
            Enrollment updated = enrollmentService.updateProgress(id, progress);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}