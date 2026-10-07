# BloodBankManagementSystem
The Blood Bank Management system makes the communication easy between blood donor and requested persons.
# Blood Bank Management System

A Spring Boot-based blood bank administration dashboard that helps manage donors, track blood inventory, record donations, and process blood requests efficiently.

This project provides a simple but functional admin portal for a blood bank with a secure login, donor management, inventory tracking, and blood request approval workflow.

## Overview

The application is designed for blood bank staff to:

- Manage donor records
- Record blood donations
- Monitor available blood stock by group
- Accept and review blood requests from hospitals or patients
- Approve or reject requests based on inventory availability
- Use a clean dashboard to monitor daily operations

## Features

- Secure admin login with token-based authentication
- Donor registration and search
- Donation recording with automatic inventory updates
- Blood group inventory dashboard
- Blood request submission and approval workflow
- Responsive web interface built with HTML, CSS, and JavaScript
- H2 in-memory database for easy local setup and testing
- REST API built with Spring Boot

## Tech Stack

- Java 17
- Spring Boot 3.3.4
- Spring Web
- Spring Data JPA
- Spring Security
- Hibernate
- H2 Database
- HTML / CSS / JavaScript
- Maven

## Project Structure

```text
bloodbank/
├── App.java
├── Api.java
├── Models.java
├── SecurityConfig.java
├── application.properties
├── pom.xml
├── public/
│   ├── app.js
│   ├── index.html
│   └── style.css
├── target/
└── README.md
```

## Prerequisites

Before running this project, make sure you have:

- JDK 17 or later
- Maven 3.9+ installed
- Git installed
- A browser to access the app locally

## Installation and Setup

1. Clone the repository:

```bash
git clone https://github.com/your-username/bloodbank.git
cd bloodbank
```

2. Build the project:

```bash
mvn clean install
```

3. Run the application:

```bash
mvn spring-boot:run
```

4. Open the application in your browser:

```text
http://localhost:8082
```

## Default Login Credentials

The application seeds a default admin user automatically at startup.

- Username: `admin`
- Password: `admin123`

## Dashboard and Usage

### Admin Login

When the app starts, you can log in using the default administrator account above.

### Inventory Panel

The dashboard shows blood stock grouped by blood type, including current available units and reserved blood.

### Donors

You can add donor records with:

- Full name
- Age
- Blood group
- Phone number
- Email
- Address and registration details

### Donations

Each donation can be recorded against a valid donor. When a donation is saved:

- donor information is updated
- inventory stock is increased
- donation metadata is stored

### Blood Requests

The system supports creating blood requests and then approving or rejecting them. Approval checks current stock and reduces available inventory when enough blood is available.

## API Endpoints

The backend exposes the following REST endpoints:

### Authentication

```http
POST /api/auth/login
```

### Donors

```http
GET /api/donors
POST /api/donors
GET /api/donors/{id}
PUT /api/donors/{id}
DELETE /api/donors/{id}
```

### Donations

```http
GET /api/donations
POST /api/donations
```

### Inventory

```http
GET /api/inventory
GET /api/inventory/{bloodGroup}
```

### Blood Requests

```http
GET /api/blood-requests
POST /api/blood-requests
PUT /api/blood-requests/{id}/approve
PUT /api/blood-requests/{id}/reject
```

## Database Configuration

The project uses an H2 in-memory database with the following configuration in `application.properties`:

```properties
server.port=8082
spring.datasource.url=jdbc:h2:mem:bloodbank;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.h2.console.enabled=true
```

You can access the H2 console at:

```text
http://localhost:8082/h2-console
```

Use:

- JDBC URL: `jdbc:h2:mem:bloodbank`
- Username: `sa`
- Password: blank

## Security Notes

- CSRF is disabled for API usage in this demo project.
- Requests are protected by token validation.
- The app currently contains a basic admin authentication flow intended for local or educational use.

## Future Improvements

The current implementation can be expanded with:

- Expired blood stock handling
- Patient and hospital records
- Donor eligibility checks and reminders
- PDF or report generation
- Email/SMS notifications
- Better analytics and dashboard charts
- Production-grade authentication and authorization

## License

This project is currently shared as a learning/demo project and does not include a formal license file. If you plan to publish it publicly on GitHub, you may want to add a license such as MIT before release.

## Author

Built as a blood bank management system demonstration using Java Spring Boot and a frontend admin dashboard.

## Contribution

Contributions are welcome. If you want to improve the project, feel free to fork the repository, make changes, and open a pull request.

