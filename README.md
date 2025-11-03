# Smart Appointment Booking System

A comprehensive, production-ready REST API for managing medical appointments with advanced security features, real-time conflict detection, and automated notifications.

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-brightgreen) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791) ![Redis](https://img.shields.io/badge/Redis-7-DC382D) ![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-FF6600) ![License](https://img.shields.io/badge/License-MIT-blue)

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Database Schema](#database-schema)
- [Deployment](#deployment)
- [Security Features](#security-features)
- [Contributing](#contributing)
- [License](#license)

---

## 🎯 Project Overview

Smart Appointment Booking System is a monolithic REST API built with Spring Boot 3.2.0 that enables healthcare facilities to:

- Manage patient and doctor profiles
- Schedule appointments with real-time conflict detection
- Send automated email confirmations
- Protect against brute-force attacks with login throttling
- Implement rate limiting to prevent DDoS attacks
- Maintain comprehensive audit trails

**Current Version:** 1.0.0
**Status:** Production Ready ✅

---

## ✨ Features

### Core Features
✅ User Management (Patients, Doctors, Admins)
✅ Appointment Scheduling with Conflict Detection
✅ Doctor Availability Management
✅ Medical Services Catalog
✅ Appointment Status Tracking
✅ Follow-up Appointment Management
✅ Medical Notes & Prescriptions

### Security Features
✅ JWT-based Authentication
✅ Login Throttling (5 attempts, 15-minute lockout)
✅ Progressive Delay (0s, 1s, 2s, 4s, 8s exponential backoff)
✅ IP-based Rate Limiting (200 requests/minute)
✅ BCrypt Password Hashing
✅ Role-Based Access Control (ADMIN, DOCTOR, PATIENT)
✅ CORS Configuration
✅ SQL Injection Prevention

### Advanced Features
✅ Asynchronous Email Notifications (RabbitMQ)
✅ Distributed Caching (Redis)
✅ Database Migrations (Flyway)
✅ Automated API Documentation (Swagger)
✅ Health Monitoring (Spring Actuator)
✅ Comprehensive Logging

---

## 🛠️ Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Framework** | Spring Boot | 3.2.0 |
| **Language** | Java | 21 |
| **Database** | PostgreSQL | 15 |
| **Caching** | Redis | 7 |
| **Message Queue** | RabbitMQ | 3 |
| **Security** | Spring Security | 6.x |
| **Tokens** | JWT (JJWT) | 0.12.3 |
| **ORM** | Hibernate | 6.3.1 |
| **API Docs** | Swagger/OpenAPI | 2.3.0 |
| **Build Tool** | Maven | 3.11.0 |
| **Testing** | JUnit 5 + Mockito | Latest |

---

## 📦 Prerequisites

- **Java 21 JDK** - [Download](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.11+** - [Download](https://maven.apache.org/download.cgi)
- **PostgreSQL 15** - [Download](https://www.postgresql.org/download/)
- **Redis 7** - [Download](https://redis.io/download) (or Docker)
- **RabbitMQ 3** - [Download](https://www.rabbitmq.com/download.html) (or Docker)
- **Git** - [Download](https://git-scm.com/downloads)
- **Docker & Docker Compose** (optional) - [Download](https://www.docker.com/products/docker-desktop)

### Verify Installation
```bash
java -version           # Should show Java 21
mvn -version            # Should show Maven 3.11+
psql --version          # Should show PostgreSQL 15
```

---

## 🚀 Installation & Setup

### Step 1: Clone Repository
```bash
git clone https://github.com/yourusername/smart-appointment-booking-system.git
cd smart-appointment-booking-system
```

### Step 2: Option A - Using Docker (Recommended)
```bash
# Start all services (PostgreSQL, Redis, RabbitMQ)
docker-compose up -d

# Verify containers are running
docker ps
```

### Step 2: Option B - Manual Installation

**PostgreSQL Setup:**
```bash
# Create database
createdb -U postgres appointmentdb

# Create user
createuser -U postgres -P appointment_user
# Password: appointment_pass

# Grant privileges
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE appointmentdb TO appointment_user;"
```

**Redis Setup:**
```bash
# Start Redis
redis-server

# Verify connection
redis-cli ping
# Output: PONG
```

**RabbitMQ Setup:**
```bash
# Start RabbitMQ (if installed)
rabbitmq-server

# Access management UI
# URL: http://localhost:15672
# Username: guest
# Password: guest
```

### Step 3: Install Java Dependencies
```bash
mvn clean install
```

### Step 4: Run Database Migrations
```bash
mvn flyway:migrate
```

### Step 5: Start Application
```bash
mvn spring-boot:run
```

**Expected Output:**
```
Started SmartAppointmentBookingSystemApplication on http://localhost:8080
```

---

## ⚙️ Configuration

### application.yml Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: smart-appointment-booking-system
  
  # Database Configuration
  datasource:
    url: jdbc:postgresql://localhost:5432/appointmentdb
    username: appointment_user
    password: appointment_pass
    driver-class-name: org.postgresql.Driver
  
  # JPA/Hibernate Configuration
  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.PostgreSQL10Dialect
    show-sql: false
    properties:
      hibernate:
        format_sql: true
  
  # Redis Configuration
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
  
  # RabbitMQ Configuration
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
  
  # Mail Configuration
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_APP_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

# Application Configuration
app:
  security:
    jwt:
      secret: your-256-bit-secret-key-change-in-production
      expiration: 86400000      # 24 hours
      refresh-expiration: 604800000  # 7 days
    
    login:
      max-attempts: 5
      lock-duration-minutes: 15
      progressive-delay-enabled: true
  
  rate-limiting:
    requests-per-minute: 200

server:
  port: 8080
```

### Environment Variables

For production, set these as environment variables:

```bash
export DB_URL=jdbc:postgresql://prod-db:5432/appointmentdb
export DB_USER=production_user
export DB_PASSWORD=secure_password
export JWT_SECRET=your-secure-256-bit-secret
export EMAIL_USERNAME=your-email@gmail.com
export EMAIL_APP_PASSWORD=your-app-password
export REDIS_HOST=redis.prod.com
export RABBITMQ_HOST=rabbitmq.prod.com
```

---

## ▶️ Running the Application

### Development Mode
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Production Mode
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### With Debug Logging
```bash
mvn spring-boot:run -Ddebug
```

### Build JAR and Run
```bash
mvn clean package -DskipTests
java -jar target/smart-appointment-booking-system-1.0.0.jar
```

---

## 📚 API Documentation

### Swagger UI
**URL:** http://localhost:8080/swagger-ui.html

### Key Endpoints

**Authentication:**
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/refresh` - Refresh JWT token
- `POST /api/v1/auth/admin/unlock/{username}` - Admin unlock account
- `GET /api/v1/auth/admin/login-attempts/{username}` - View login statistics

**Users:**
- `GET /api/v1/users` - Get all users (Admin)
- `GET /api/v1/users/{id}` - Get user by ID
- `PUT /api/v1/users/{id}` - Update user
- `DELETE /api/v1/users/{id}` - Delete user

**Doctors:**
- `GET /api/v1/doctors` - Get all doctors
- `GET /api/v1/doctors/{id}` - Get doctor by ID
- `POST /api/v1/doctors` - Create doctor (Admin)
- `PUT /api/v1/doctors/{id}` - Update doctor
- `DELETE /api/v1/doctors/{id}` - Delete doctor
- `GET /api/v1/doctors/available` - Get available doctors

**Appointments:**
- `POST /api/v1/appointments` - Create appointment (with conflict detection)
- `GET /api/v1/appointments` - Get all appointments
- `GET /api/v1/appointments/{id}` - Get appointment by ID
- `PUT /api/v1/appointments/{id}` - Update appointment
- `DELETE /api/v1/appointments/{id}` - Cancel appointment
- `PATCH /api/v1/appointments/{id}/status` - Update status

### Example Request/Response

**Login:**
```
POST /api/v1/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "john.doe",
  "password": "SecurePassword123!"
}

Response (200 OK):
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userId": 1,
    "username": "john.doe"
  }
}
```

**Create Appointment:**
```
POST /api/v1/appointments
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "patientId": 1,
  "doctorId": 1,
  "serviceId": 1,
  "appointmentDate": "2025-11-10",
  "startTime": "14:00",
  "endTime": "14:30",
  "reasonForVisit": "General Checkup"
}

Response (201 Created):
{
  "success": true,
  "message": "Appointment created successfully",
  "data": {
    "id": 100,
    "patientId": 1,
    "doctorId": 1,
    "appointmentDate": "2025-11-10",
    "status": "PENDING"
  }
}
```

---

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=UserServiceTest
```

### Run with Coverage
```bash
mvn test jacoco:report
```

### Run Integration Tests
```bash
mvn verify
```

### Test Categories

**Unit Tests:**
- UserServiceTest.java
- DoctorServiceTest.java
- AppointmentServiceTest.java
- LoginAttemptServiceTest.java

**Integration Tests:**
- AppointmentIntegrationTest.java
- AuthControllerTest.java
- UserControllerTest.java

**Test Tools:**
- JUnit 5 - Testing framework
- Mockito - Mocking framework
- MockMvc - REST API testing
- TestContainers - Container testing

---

## 🗄️ Database Schema

### Core Tables

**Users Table:**
```sql
- id (PK)
- username (UNIQUE)
- email (UNIQUE)
- password (BCrypt hashed)
- firstName, lastName
- phoneNumber
- role (ADMIN, DOCTOR, PATIENT)
- enabled, accountLocked
- createdAt, updatedAt
```

**Doctors Table:**
```sql
- id (PK)
- user_id (FK → Users, 1:1)
- specialization
- licenseNumber (UNIQUE)
- yearsOfExperience
- consultationFee
- availableFrom, availableTo
```

**Patients Table:**
```sql
- id (PK)
- user_id (FK → Users, 1:1)
- dateOfBirth
- gender
- bloodGroup
- address
- emergencyContact
```

**Appointments Table:**
```sql
- id (PK)
- patient_id (FK → Patients)
- doctor_id (FK → Doctors)
- service_id (FK → Services)
- appointmentDate
- startTime, endTime
- status (PENDING, CONFIRMED, COMPLETED, CANCELLED)
- reasonForVisit, notes, diagnosis, prescription
- followUpRequired, followUpDate
- createdAt, updatedAt
```

### Relationships
- User ↔ Doctor (1:1)
- User ↔ Patient (1:1)
- Patient ↔ Appointment (1:N)
- Doctor ↔ Appointment (1:N)
- Service ↔ Appointment (1:N)

---

## 🐳 Deployment

### Docker Deployment

**Build Docker Image:**
```bash
docker build -t appointment-system:1.0.0 .
```

**Run with Docker Compose:**
```bash
docker-compose up -d
```

**Check Logs:**
```bash
docker logs appointment-postgres
docker logs appointment-redis
docker logs appointment-rabbitmq
docker logs appointment-app
```

### Cloud Deployment Options

**AWS Deployment:**
- Use EC2 for application
- RDS for PostgreSQL
- ElastiCache for Redis
- SQS for message queue
- Elastic Load Balancer for traffic

**Azure Deployment:**
- App Service for application
- Azure Database for PostgreSQL
- Azure Cache for Redis
- Service Bus for messaging
- Application Gateway for load balancing

**Google Cloud Deployment:**
- Cloud Run for application
- Cloud SQL for PostgreSQL
- Memorystore for Redis
- Cloud Pub/Sub for messaging
- Cloud Load Balancing

### Kubernetes Deployment

```bash
# Build image
docker build -t appointment-system:1.0.0 .

# Push to registry
docker push your-registry/appointment-system:1.0.0

# Deploy with Helm
helm install appointment-system ./helm-charts
```

---

## 🔒 Security Features

### 1. Login Throttling

**Configuration:**
- Max Attempts: 5
- Lock Duration: 15 minutes
- Progressive Delay: Exponential (0s, 1s, 2s, 4s, 8s)

**Example Flow:**
1. Wrong password attempt 1 → HTTP 401, "Remaining: 4"
2. Wrong password attempt 2 → 1s delay, HTTP 401, "Remaining: 3"
3. Wrong password attempt 3 → 2s delay, HTTP 401, "Remaining: 2"
4. Wrong password attempt 4 → 4s delay, HTTP 401, "Remaining: 1"
5. Wrong password attempt 5 → 8s delay, HTTP 423 (Locked)

### 2. Rate Limiting

**Configuration:**
- 200 requests per minute per IP
- Uses Bucket4j + Redis
- Returns HTTP 429 when exceeded

### 3. JWT Authentication

- Token expiration: 24 hours
- Refresh token: 7 days
- Algorithm: HS512
- Claims: username, role, userId

### 4. Password Security

- BCrypt hashing with salt
- Minimum requirements enforced
- Regular password reset support

### 5. CORS Configuration

```yaml
corsAllowedOrigins:
  - http://localhost:3000
  - http://localhost:8080
  - https://yourdomain.com
```

### 6. SQL Injection Prevention

- Parameterized queries (JPA)
- Input validation
- Prepared statements

---

## 📊 Monitoring & Metrics

### Spring Boot Actuator
**Endpoints:**
- `/actuator/health` - Application health
- `/actuator/metrics` - Performance metrics
- `/actuator/prometheus` - Prometheus metrics

### Monitoring Setup:
```bash
# View health
curl http://localhost:8080/actuator/health

# View metrics
curl http://localhost:8080/actuator/metrics

# View specific metric
curl http://localhost:8080/actuator/metrics/http.server.requests
```

---

## 🐛 Troubleshooting

### Issue: PostgreSQL Connection Refused
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# If not running, start it
docker-compose up -d postgres
```

### Issue: Redis Connection Timeout
```bash
# Verify Redis running
redis-cli ping

# If not running
docker-compose up -d redis
```

### Issue: RabbitMQ Queue Not Processing
```bash
# Access RabbitMQ Management
http://localhost:15672
# Default: guest / guest

# Check queue status
# Verify email-queue exists and has bindings
```

### Issue: JWT Token Expired
```bash
# Use refresh token to get new token
POST /api/v1/auth/refresh?refreshToken=<refresh-token>
```

---

## 📝 Database Migrations

### Create New Migration
```bash
# Flyway auto-discovers migration files in src/main/resources/db/migration/
# Name format: V{version}__{description}.sql
# Example: V4__Add_appointment_notes.sql
```

### Run Migrations
```bash
mvn flyway:migrate
```

### Undo Last Migration
```bash
mvn flyway:undo
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

### Code Standards:
- Follow Spring Boot conventions
- Add unit tests for new features
- Update API documentation
- Use meaningful commit messages

---

## 📄 License

This project is licensed under the MIT License - see LICENSE.md for details.

---

## 👨‍💻 Author

**Your Name**
- GitHub: [@yourusername](https://github.com/yourusername)
- Email: your.email@example.com

---

## 📞 Support

For issues and questions:
- Open an [GitHub Issue](https://github.com/yourusername/smart-appointment-booking-system/issues)
- Check [Documentation](./docs/)
- Review [API Documentation](http://localhost:8080/swagger-ui.html)

---

## 🗺️ Roadmap

- [ ] Mobile application (iOS/Android)
- [ ] Advanced analytics dashboard
- [ ] Microservices migration
- [ ] Video consultation support
- [ ] Payment integration
- [ ] Multi-language support
- [ ] Machine learning for appointment predictions

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/documentation)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [JWT Documentation](https://jwt.io/)

---

**Last Updated:** November 3, 2025
**Status:** Production Ready ✅
