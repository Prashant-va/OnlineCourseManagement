package com.course.management.service;

import com.course.management.entity.Course;
import com.course.management.entity.Enrollment;
import com.course.management.entity.User;
import com.course.management.entity.TransactionRecord;
import com.course.management.exception.ResourceNotFoundException;
import com.course.management.repository.CourseRepository;
import com.course.management.repository.EnrollmentRepository;
import com.course.management.repository.TransactionRecordRepository;
import com.course.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    /**
     * Enroll student (prevents re-enrollment) and records a simulated transaction.
     */
    @Transactional
    public Enrollment enrollStudent(Long studentId, Long courseId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        boolean alreadyEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
        if (alreadyEnrolled) {
            throw new IllegalStateException("You are already enrolled in this course!");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus("Not Started");
        enrollment.setProgress(0);

        // Simulate payment transaction
        TransactionRecord transaction = new TransactionRecord();
        transaction.setOrderId(UUID.randomUUID().toString());
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setStatus("SUCCESS");
        transaction.setAmount(course.getFee());
        transaction.setStudent(student);

        transactionRecordRepository.save(transaction);

        return enrollmentRepository.save(enrollment);
    }

    /**
     * Fetch active enrollments for a student (eager-loads course).
     */
    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdWithCourse(studentId);
        // Keep completed entries unless you intentionally want them hidden; 
        // if you want to hide completed, uncomment the removal line:
        // enrollments.removeIf(e -> e.getProgress() >= 100);
        return enrollments;
    }

    /**
     * Update course progress; mark as Completed when progress >= 100 and save the entity.
     */
    @Transactional
    public Enrollment updateProgress(Long enrollmentId, double progress) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));

        int normalized = (int) Math.max(0, Math.min(100, Math.round(progress)));
        enrollment.setProgress(normalized);

        if (normalized >= 100) {
            enrollment.setStatus("Completed");
        } else if (normalized > 0) {
            enrollment.setStatus("In Progress");
        } else {
            enrollment.setStatus("Not Started");
        }

        return enrollmentRepository.save(enrollment);
    }
}