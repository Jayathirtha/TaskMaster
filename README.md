Secure Collaboration API

This Spring Boot API provides secure User Authentication, Authorization, and a modular Team/Project Collaboration system. It utilizes JWT for stateless session management and adheres to SOLID design principles.

🚀 Features

Authentication & Security

Secure Authentication: Uses BCrypt for password hashing.

JWT Session Management: Implements stateless sessions using JWT tokens.

Immediate Logout: Achieved via a server-side Token Blacklist for token invalidation.

Role-Based Access (Basic): Foundation for user roles.

Collaboration & Management

User Profiles: Secure endpoints for viewing and updating user details.

Team & Projects: Users create teams and define projects within them.

Team Membership: Functionality to add members (requires existing membership).

Task Management: Create, assign, and update task status within a project.

Task Details: Tasks support comments and attachment metadata logging.

Authorization Checks: Strict access control ensures users only interact with teams/projects they are members of.

🛠️ Tech Stack

Language: Java 17+

Framework: Spring Boot 3.2.0 (Web, JPA, Security)

Database: MySQL

Session Management: JJWT (JSON Web Token)

Security: Spring Security & BCrypt

⚙️ Setup and Configuration

Prerequisites

Java Development Kit (JDK) 17 or higher.

Maven.

A running MySQL instance (default port 3306).

Database Configuration

The API requires a MySQL database named authdb.

Database & Credentials: Ensure authdb exists and update src/main/resources/application.properties with your MySQL connection details.

spring.datasource.url=jdbc:mysql://localhost:3306/authdb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=<YOUR_MYSQL_USERNAME>
spring.datasource.password=<YOUR_MYSQL_PASSWORD>



JWT Secret: Use a unique, strong, Base64-encoded key (min. 256 bits) for production.

jwt.secret=413F4428472B4B6250655368566D597133743677397A24432646294A404E6352



Running the Application

Run the application using the Spring Boot Maven plugin:

./mvnw spring-boot:run



The API will be accessible at http://localhost:8080.

📌 Key API Endpoints

All secured endpoints require the Authorization: Bearer <TOKEN> header.

Authentication & Profile (/api/auth, /api/user)

Method

Path

Description

Access

POST

/api/auth/register

Create a new user account.

Public

POST

/api/auth/login

Authenticate user and receive a JWT.

Public

POST

/api/auth/logout

Invalidate the current JWT by blacklisting it.

Secured

GET

/api/user/profile

Retrieve the authenticated user's profile.

Secured

PUT

/api/user/profile

Update the authenticated user's profile details.

Secured

Teams & Projects (/api/teams)

Method

Path

Description

Access

POST

/api/teams

Create a new team (current user becomes a member).

Secured

GET

/api/teams/my

Get all teams the current user is a member of.

Secured

POST

/api/teams/{teamId}/members?username={name}

Add a user to an existing team.

Secured (Team Member)

POST

/api/teams/{teamId}/projects

Create a new project under a specific team.

Secured (Team Member)

Task Management (/api/tasks)

Method

Path

Description

Access

POST

/api/tasks

Create a new task for a specified project.

Secured (Team Member)

GET

/api/tasks/my

Get all tasks assigned to the current user.

Secured

GET

/api/tasks/project/{projectId}

Get all tasks for a specific project.

Secured (Team Member)

PATCH

/api/tasks/{taskId}/assign?username={name}

Assign a task to another team member.

Secured (Team Member)

PATCH

/api/tasks/{taskId}/status?status={STATUS}

Update task status (e.g., OPEN, COMPLETE).

Secured (Team Member)

POST

/api/tasks/{taskId}/comments

Add a comment and attachment metadata to a task.

Secured (Team Member)

GET

/api/tasks/{taskId}/comments

Retrieve all comments for a task.

Secured (Team Member)
