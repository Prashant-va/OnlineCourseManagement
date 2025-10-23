package com.course.management.controller;

import com.course.management.entity.Course;
import com.course.management.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // GET /api/courses -> returns approved courses (always an array)
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        try {
            List<Course> courses = courseService.getAllApprovedCourses();
            return ResponseEntity.ok(courses != null ? courses : Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // ADMIN: view unapproved courses
    @GetMapping("/unapproved")
    public ResponseEntity<List<Course>> getUnapproved() {
        try {
            List<Course> courses = courseService.getAllUnapprovedCourses();
            return ResponseEntity.ok(courses != null ? courses : Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // POST /api/courses -> create course (instructor)
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        try {
            Course saved = courseService.createCourse(course);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PUT /api/courses/{id} -> update course
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course updated) {
        try {
            Course saved = courseService.updateCourse(id, updated);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // DELETE /api/courses/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.ok("Course deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }
    }

    // Approve/reject (admin)
    @PostMapping("/{id}/approve")
    public ResponseEntity<Course> approveCourse(@PathVariable Long id, @RequestParam boolean approve) {
        try {
            Course saved = courseService.approveCourse(id, approve);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // returns all courses (for admin dashboard, and instructor frontend logic)
    @GetMapping("/all")
    public ResponseEntity<List<Course>> findAll() {
        try {
            List<Course> courses = courseService.findAll();
            return ResponseEntity.ok(courses != null ? courses : Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}