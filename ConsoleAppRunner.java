/*
 * package com.course.management.cli;
 * 
 * import com.course.management.entity.Course; import
 * com.course.management.entity.Enrollment; import
 * com.course.management.entity.Role; import com.course.management.entity.User;
 * import com.course.management.service.CourseService; import
 * com.course.management.service.EnrollmentService; import
 * com.course.management.service.ReportService; import
 * com.course.management.service.UserService; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.boot.CommandLineRunner; import
 * org.springframework.stereotype.Component;
 * 
 * import java.util.List; import java.util.Map; import java.util.Scanner; import
 * java.util.concurrent.CompletableFuture;
 * 
 * @Component public class ConsoleAppRunner implements CommandLineRunner {
 * 
 * @Autowired private UserService userService;
 * 
 * @Autowired private CourseService courseService;
 * 
 * @Autowired private EnrollmentService enrollmentService;
 * 
 * @Autowired private ReportService reportService;
 * 
 * private final Scanner sc = new Scanner(System.in);
 * 
 * @Override public void run(String... args) {
 * System.out.println("=== Welcome to Online Course Management System ===");
 * 
 * while (true) { System.out.println("\n1) Register");
 * System.out.println("2) Login"); System.out.println("0) Exit");
 * System.out.print("Enter choice: "); int choice = sc.nextInt(); sc.nextLine();
 * 
 * switch (choice) { case 1 -> register(); case 2 -> login(); case 0 -> {
 * System.out.println("Exiting..."); return; } default ->
 * System.out.println("Invalid choice!"); } } }
 * 
 * private void register() { System.out.print("Enter name: "); String name =
 * sc.nextLine(); System.out.print("Enter email: "); String email =
 * sc.nextLine(); System.out.print("Enter password: "); String password =
 * sc.nextLine();
 * 
 * System.out.println("Select role: 1) Student 2) Instructor 3) Admin"); int
 * roleChoice = sc.nextInt(); sc.nextLine();
 * 
 * Role role = switch (roleChoice) { case 1 -> Role.STUDENT; case 2 ->
 * Role.INSTRUCTOR; case 3 -> Role.ADMIN; default -> Role.STUDENT; };
 * 
 * User user = new User(); user.setName(name); user.setEmail(email);
 * user.setPassword(password); user.setRole(role);
 * 
 * userService.registerUser(user);
 * System.out.println("✅ Registered successfully as " + role); }
 * 
 * private void login() { System.out.print("Enter email: "); String email =
 * sc.nextLine(); System.out.print("Enter password: "); String password =
 * sc.nextLine();
 * 
 * try { User user = userService.login(email, password);
 * System.out.println("✅ Login successful! Welcome, " + user.getName());
 * 
 * switch (user.getRole()) { case ADMIN -> adminMenu(user); case INSTRUCTOR ->
 * instructorMenu(user); case STUDENT -> studentMenu(user); }
 * 
 * } catch (Exception e) { System.out.println("❌ " + e.getMessage()); } }
 * 
 * // -------------------- Admin Menu -------------------- private void
 * adminMenu(User admin) { while (true) {
 * System.out.println("\n--- Admin Menu ---");
 * System.out.println("1) Approve/Reject courses");
 * System.out.println("2) Generate reports"); System.out.println("0) Logout");
 * System.out.print("Enter choice: "); int choice = sc.nextInt(); sc.nextLine();
 * 
 * if (choice == 0) break;
 * 
 * switch (choice) { case 1 -> approveCourses(); case 2 -> generateReports();
 * default -> System.out.println("Invalid choice."); } } }
 * 
 * private void approveCourses() { List<Course> courses =
 * courseService.getAllUnapprovedCourses(); // Only unapproved courses if
 * (courses.isEmpty()) { System.out.println("No courses to approve yet.");
 * return; } courses.forEach(c -> System.out.println(c.getId() + ": " +
 * c.getTitle() + " (Approved=" + c.isApproved() + ")"));
 * System.out.print("Enter course ID to approve/reject: "); Long courseId =
 * sc.nextLong(); sc.nextLine(); System.out.print("Approve? (y/n): "); String
 * ans = sc.nextLine(); boolean approve = ans.equalsIgnoreCase("y");
 * courseService.approveCourse(courseId, approve);
 * System.out.println("✅ Course " + (approve ? "approved" : "rejected") +
 * " successfully!"); }
 * 
 * private void generateReports() {
 * System.out.println("Generating reports in parallel (simulated delay)...");
 * try { CompletableFuture<Map<String, Long>> studentCount =
 * reportService.getStudentCountPerCourse(); CompletableFuture<Map<String,
 * Double>> revenue = reportService.getRevenuePerCourse();
 * CompletableFuture<Map<String, Long>> statusReport =
 * reportService.getEnrollmentStatusReport();
 * 
 * CompletableFuture.allOf(studentCount, revenue, statusReport).join();
 * 
 * System.out.println("\n--- Students Enrolled per Course ---");
 * studentCount.get().forEach((course, count) -> System.out.println(course +
 * ": " + count));
 * 
 * System.out.println("\n--- Revenue Collected per Course ---");
 * revenue.get().forEach((course, amount) -> System.out.println(course + ": ₹" +
 * amount));
 * 
 * System.out.println("\n--- Enrollment Status ---");
 * statusReport.get().forEach((status, count) -> System.out.println(status +
 * ": " + count));
 * 
 * } catch (Exception e) { System.out.println("Error generating reports: " +
 * e.getMessage()); } }
 * 
 * // -------------------- Instructor Menu -------------------- private void
 * instructorMenu(User instructor) { while (true) {
 * System.out.println("\n--- Instructor Menu ---");
 * System.out.println("1) Create course");
 * System.out.println("2) Update course");
 * System.out.println("3) Delete course"); System.out.println("0) Logout");
 * System.out.print("Enter choice: "); int choice = sc.nextInt(); sc.nextLine();
 * 
 * if (choice == 0) break;
 * 
 * switch (choice) { case 1 -> createCourse(instructor); case 2 ->
 * updateCourse(instructor); case 3 -> deleteCourse(instructor); default ->
 * System.out.println("Invalid choice."); } } }
 * 
 * private void createCourse(User instructor) { Course course = new Course();
 * course.setInstructor(instructor);
 * 
 * System.out.print("Title: "); course.setTitle(sc.nextLine());
 * System.out.print("Description: "); course.setDescription(sc.nextLine());
 * System.out.print("Duration (in months): "); String durationInput =
 * sc.nextLine();
 * course.setDuration(Integer.parseInt(durationInput.replaceAll("\\D", "")));
 * System.out.print("Fee: "); course.setFee(sc.nextDouble()); sc.nextLine();
 * System.out.print("Category: "); course.setCategory(sc.nextLine());
 * 
 * courseService.createCourse(course);
 * System.out.println("✅ Course created and sent for approval!"); }
 * 
 * private void updateCourse(User instructor) { List<Course> courses =
 * courseService.getAllApprovedCourses(); courses.removeIf(c ->
 * !c.getInstructor().getId().equals(instructor.getId())); if
 * (courses.isEmpty()) { System.out.println("You have no courses to update.");
 * return; } courses.forEach(c -> System.out.println(c.getId() + ": " +
 * c.getTitle())); System.out.print("Enter course ID to update: "); Long id =
 * sc.nextLong(); sc.nextLine();
 * 
 * System.out.print("New title: "); String title = sc.nextLine();
 * System.out.print("New description: "); String desc = sc.nextLine();
 * System.out.print("New duration: "); String duration = sc.nextLine();
 * System.out.print("New fee: "); double fee = sc.nextDouble(); sc.nextLine();
 * System.out.print("New category: "); String cat = sc.nextLine();
 * 
 * Course updated = new Course(); updated.setTitle(title);
 * updated.setDescription(desc);
 * updated.setDuration(Integer.parseInt(duration)); updated.setFee(fee);
 * updated.setCategory(cat);
 * 
 * courseService.updateCourse(id, updated);
 * System.out.println("✅ Course updated successfully!"); }
 * 
 * private void deleteCourse(User instructor) { List<Course> courses =
 * courseService.getAllApprovedCourses(); courses.removeIf(c ->
 * !c.getInstructor().getId().equals(instructor.getId())); if
 * (courses.isEmpty()) { System.out.println("No courses to delete."); return; }
 * courses.forEach(c -> System.out.println(c.getId() + ": " + c.getTitle()));
 * System.out.print("Enter course ID to delete: "); Long id = sc.nextLong();
 * sc.nextLine();
 * 
 * courseService.deleteCourse(id);
 * System.out.println("✅ Course deleted successfully!"); }
 * 
 * // -------------------- Student Menu -------------------- private void
 * studentMenu(User student) { while (true) {
 * System.out.println("\n--- Student Menu ---");
 * System.out.println("1) View available courses");
 * System.out.println("2) Enroll in course");
 * System.out.println("3) Update course progress");
 * System.out.println("0) Logout"); System.out.print("Enter choice: "); int
 * choice = sc.nextInt(); sc.nextLine();
 * 
 * if (choice == 0) break;
 * 
 * switch (choice) { case 1 -> viewCourses(); case 2 -> enrollCourse(student);
 * case 3 -> updateProgress(student); default ->
 * System.out.println("Invalid choice."); } } }
 * 
 * private void viewCourses() { List<Course> courses =
 * courseService.getAllApprovedCourses(); if (courses.isEmpty()) {
 * System.out.println("No approved courses available."); return; }
 * courses.forEach(c -> System.out.println(c.getId() + ": " + c.getTitle() +
 * " - ₹" + c.getFee())); }
 * 
 * private void enrollCourse(User student) { viewCourses();
 * System.out.print("Enter course ID to enroll: "); Long courseId =
 * sc.nextLong(); sc.nextLine();
 * 
 * try { Enrollment enrollment =
 * enrollmentService.enrollStudent(student.getId(), courseId);
 * System.out.println("✅ Enrolled successfully! Enrollment ID: " +
 * enrollment.getId()); } catch (IllegalStateException e) { // This handles the
 * "You are already enrolled in this course!" error. System.out.println("❌ " +
 * e.getMessage()); } catch (Exception e) { // This handles other potential
 * errors like ResourceNotFoundException
 * System.out.println("❌ Enrollment failed: " + e.getMessage()); } }
 * 
 * private void updateProgress(User student) { List<Enrollment> enrollments =
 * enrollmentService.getEnrollmentsByStudent(student.getId()); if
 * (enrollments.isEmpty()) {
 * System.out.println("You are not enrolled in any courses."); return; }
 * enrollments.forEach(e -> System.out.println(e.getId() + ": " +
 * e.getCourse().getTitle() + " - " + e.getProgress() + "% completed"));
 * System.out.print("Enter enrollment ID to update progress: "); Long id =
 * sc.nextLong(); sc.nextLine(); System.out.print("Enter new progress (%): ");
 * int progress = sc.nextInt(); sc.nextLine();
 * enrollmentService.updateProgress(id, progress);
 * System.out.println("✅ Progress updated successfully!"); } }
 */