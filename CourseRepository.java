package com.course.management.repository;

import com.course.management.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findByApprovedTrue();
    
    List<Course> findByApprovedFalse();

    /**
     * Finds all courses associated with a specific instructor ID.
     * Spring Data JPA creates the query automatically based on the method name, 
     * assuming 'instructor' is an entity in the Course class with an 'id' field.
     */
    List<Course> findByInstructorId(Long instructorId);
}
