# Online Course Management System

## Overview

This project is an Online Course Management System built using Spring Boot and follows a microservice architecture. It enables the management of courses, students, teachers, and enrollments to create a scalable and maintainable educational platform.

## Features

* Student Registration and Authentication
* Course Creation and Management
* Teacher Management
* Enrollment Functionality for Students
* RESTful APIs for seamless integration

## Tech Stack

* **Backend:** Spring Boot, Java
* **Database:** MySQL / PostgreSQL (as per configuration)
* **Security:** Spring Security (JWT optional)
* **Build Tool:** Maven

## Project Structure

```
/online-course-management
 ├─ src/main/java/com/yourproject/
 │   ├─ controller
 │   ├─ service
 │   ├─ repository
 │   ├─ entity
 │   └─ dto
 ├─ src/main/resources
 │   ├─ application.properties
 │   └─ schema.sql / data.sql
 ├─ pom.xml
 └─ README.md
```

## Installation & Setup

### Prerequisites

* Java 17+
* Maven 3.8+
* MySQL/PostgreSQL configured

### Steps

```bash
# Clone the repository
git clone <repository-url>
cd online-course-management

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

## API Endpoints

| Method | Endpoint               | Description          |
| ------ | ---------------------- | -------------------- |
| POST   | /api/students/register | Register new student |
| POST   | /api/courses           | Create course        |
| GET    | /api/courses           | Fetch all courses    |
| POST   | /api/enroll            | Student enrollment   |

*(Add more based on your implementation)*

## Future Enhancements

* Add JWT-based security
* Swagger API Documentation
* Admin Dashboard
* Notification Service

## Contributors

* Developer: **Prashant Vaddodagi**

