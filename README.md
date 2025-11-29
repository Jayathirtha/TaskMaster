
# **TaskMaster assignment**
This Spring Boot API provides secure User Authentication, Authorization, and a modular Team/Project Collaboration system. It utilizes JWT for stateless session management and adheres to SOLID design principles.


**features**


#### Collaboration & Management

* User Profiles: Secure endpoints for viewing and updating user details.

* Team & Projects: Users create teams and define projects within them.

* Team Membership: Functionality to add members (requires existing membership).

* Task Management: Create, assign, and update task status within a project.

* Task Details: Tasks support comments and attachment metadata logging.

* Authorization Checks: Strict access control ensures users only interact with teams/projects they are members of.


## Tech Stack

Language: Java 17+

* Framework: Spring Boot 3.2.0 (Web, JPA, Security)

* Database: MySQL

* Session Management: JJWT (JSON Web Token)

* Security: Spring Security & BCrypt

## Running Tests

Setup and Configuration

#### Pre-requisites

- Java Development Kit (JDK) 17 or higher.

- Maven.

- A running MySQL instance (default port 3306).

#### To run tests, use the post man collection provided in the repository.
https://github.com/Jayathirtha/TaskMaster/blob/master/Task%20Master.postman_collection.json

## Run Locally

Clone the project

#### 1. Database Configuration

The API requires a MySQL database named authdb.

Database & Credentials: Ensure authdb exists and update src/main/resources/application.properties with your MySQL connection details.

```bash
spring.datasource.url=jdbc:mysql://localhost:3306/authdb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=<YOUR_MYSQL_USERNAME>
spring.datasource.password=<YOUR_MYSQL_PASSWORD>
```
#### 2. JWT Secret: Use a unique, strong, Base64-encoded key (min. 256 bits) for production.

```bash
jwt.secret=413F4428472B4B6250655368566D597133743677397A24432646294A404E6352
```
#### 3. Running the Application

Run the application using the Spring Boot Maven plugin:

```bash
./mvnw spring-boot:run
```

The API will be accessible at http://localhost:8080.

#### key API Endpoints

All secured endpoints require the Authorization: Bearer <TOKEN> header.

#### i Authentication & Profile (/api/auth, /api/user)
#### ii Teams & Projects (/api/teams)
#### iii Task Management (/api/tasks)
