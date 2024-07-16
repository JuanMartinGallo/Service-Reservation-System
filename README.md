# Service Reservation System

The **Service Reservation System** is a web application developed with Spring Boot, utilizing a range of modern
technologies and dependencies to create a secure and efficient platform for users to register, log in, and book various
services. The system uses JSON Web Tokens (JWT) for user authentication and authorization, ensuring data security and a
seamless user experience.

## Key Features

- **User Registration:** Allows users to create accounts with personal information and login credentials.
- **Secure Login:** Users can log in securely using their credentials and receive a JWT token used to access the
  platform safely.
- **Service Bookings:** Users have the option to browse and select from a variety of available services to make
  bookings.
- **Booking Management:** The system allows users to view, modify, and cancel their existing bookings.
- **JWT Security:** The application relies on JWT tokens to ensure secure user authentication and authorization,
  protecting user data and booking details.
- **Reactive Programming:** Utilizes Project Reactor and Spring WebFlux to provide a highly scalable and responsive
  application.
- **Database Integration:** Supports both SQL (PostgreSQL) and R2DBC for reactive database access.
- **API Documentation:** Integrated with Springdoc OpenAPI for interactive API documentation and exploration.
- **Thymeleaf for Templates:** Uses Thymeleaf, a modern server-side Java template engine, to render dynamic content on
  the frontend.

## Project Usage

To set up the project on your local environment, follow these steps:

1. **Clone the Repository:** Clone this repository to your local machine.
2. **Database Configuration:** Configure the database of your choice, such as PostgreSQL or H2, as specified in the
   configuration file.
3. **Run the Application:** Use Spring Boot to run the application.
4. **Register and Log In:** Register as a user and then log in to start making service bookings.

## Technologies and Dependencies

### Backend

- **Spring Boot:** Simplifies the creation of Spring-based Java applications.
- **Spring WebFlux:** Supports reactive programming models with non-blocking I/O.
- **Project Reactor:** The foundation for reactive programming in Java.
- **JWT (JSON Web Tokens):** Secure user authentication and token generation.
- **R2DBC (Reactive Relational Database Connectivity):** Reactive database access for PostgreSQL.
- **Spring Security:** Manages authentication and authorization.
- **Springdoc OpenAPI:** Documentation and interactive API exploration.
- **Lombok:** Reduces boilerplate code with useful annotations.
- **Flyway:** Database migrations and version control.

### Data

- **PostgreSQL:** Primary database for storing user and booking data.
- **H2:** In-memory database option for testing and development.
- **R2DBC PostgreSQL:** Reactive database driver for PostgreSQL.

### Frontend

- **Thymeleaf:** Server-side Java template engine for rendering HTML.
- **Bootstrap:** CSS framework for responsive and mobile-first frontend development.
- **Popper.js:** Manages the positioning of pop-ups on the frontend.
- **WebJars:** Manages frontend dependencies in Java.

### Testing and Development

- **Testcontainers:** Provides disposable instances of common databases, Selenium web browsers, or anything else that
  can run in a Docker container.
- **JUnit and TestNG:** Testing frameworks for writing and running tests.

### Resilience and Monitoring

- **AspectJ:** Aspect-oriented programming (AOP) framework for Java.
- **Spring Retry:** Adds retry functionality to Spring applications.
- **Docker Java:** Docker API for Java.

### Plugins

- **Ben Manes Versions:** Gradle plugin for discovering dependency updates.
- **OWASP Dependency Check:** Identifies project dependencies and checks for publicly disclosed vulnerabilities.
- **SonarQube:** Continuous code quality inspection.
- **Spring Dependency Management:** Manages dependencies and versions in Spring Boot projects.

## Contributions and Issues

Contributions to this project are welcome, and we encourage reporting any issues you encounter. Please review the
contribution guidelines and create issues to address bugs or propose enhancements.

Thank you for choosing the **Service Reservation System**! Our goal is to provide a secure and efficient experience for
service bookings.
