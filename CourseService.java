package com.course.management.service;

import com.course.management.entity.Course;
import com.course.management.exception.ResourceNotFoundException;
import com.course.management.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    /**
     * Instructor creates a new course. It is initially set to unapproved.
     */
    public Course createCourse(Course course) {
        course.setApproved(false);
        return courseRepository.save(course);
    }

    /**
     * Admin approves or rejects a course.
     */
    public Course approveCourse(Long courseId, boolean approve) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        course.setApproved(approve);
        return courseRepository.save(course);
    }

    /**
     * Instructor updates their course, only updating fields if new values are provided.
     */
    public Course updateCourse(Long id, Course updatedCourse) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));

        // Update fields only if new, non-empty/non-zero values are provided
        if (updatedCourse.getTitle() != null && !updatedCourse.getTitle().isEmpty()) {
            course.setTitle(updatedCourse.getTitle());
        }
        if (updatedCourse.getDescription() != null && !updatedCourse.getDescription().isEmpty()) {
            course.setDescription(updatedCourse.getDescription());
        }
        if (updatedCourse.getDuration() > 0) {
            course.setDuration(updatedCourse.getDuration());
        }
        if (updatedCourse.getFee() > 0) {
            course.setFee(updatedCourse.getFee());
        }
        if (updatedCourse.getCategory() != null && !updatedCourse.getCategory().isEmpty()) {
            course.setCategory(updatedCourse.getCategory());
        }

        // When a course is updated, you might want to reset the approval status, but 
        // for now, we leave it as is to keep changes minimal.

        return courseRepository.save(course);
    }

    /**
     * Deletes a course by ID.
     */
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with ID: " + id);
        }
        courseRepository.deleteById(id);
    }

    // Students and API can view only approved courses
    public List<Course> getAllApprovedCourses() {
        return courseRepository.findByApprovedTrue();
    }

    // Admin can view only unapproved courses
    public List<Course> getAllUnapprovedCourses() {
        return courseRepository.findByApprovedFalse();
    }
    
    // For API controller
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    /**
     * INSTRUCTOR: Retrieves all courses created by a specific instructor ID.
     * NOTE: This assumes CourseRepository has a method like List<Course> findByInstructorId(Long instructorId);
     */
    public List<Course> getCoursesByInstructorId(Long instructorId) {
        // You must have this method defined in your CourseRepository interface:
        // List<Course> findByInstructorId(Long instructorId);
        return courseRepository.findByInstructorId(instructorId);
    }
}
