# 🏥 Smart Appointment Booking System

A comprehensive, production-ready microservice-based healthcare appointment management system built with Spring Boot 3.2, featuring JWT authentication, asynchronous messaging, distributed caching, and API rate limiting.

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=flat&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat&logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red?style=flat&logo=redis)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-orange?style=flat&logo=rabbitmq)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=flat&logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat)](LICENSE)

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Running the Application](#-running-the-application)
- [API Documentation](#-api-documentation)
- [Default Credentials](#-default-credentials)
- [Testing](#-testing)
- [Docker Deployment](#-docker-deployment)
- [Project Structure](#-project-structure)
- [Key Functionalities](#-key-functionalities)
- [Security](#-security)
- [Monitoring](#-monitoring)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)
- [Contact](#-contact)

---

## ✨ Features

### Core Features
- ✅ **User Management** - Complete CRUD operations with role-based access
- ✅ **Appointment Scheduling** - Book, update, cancel appointments with conflict detection
- ✅ **Doctor Management** - Manage doctor profiles, specializations, and availability
- ✅ **Patient Records** - Comprehensive patient information and medical history
- ✅ **Service Catalog** - Medical services with duration and pricing

### Security & Authentication
- 🔐 **JWT Authentication** - Secure token-based authentication with refresh tokens
- 🛡️ **Role-Based Access Control (RBAC)** - Three user roles: Admin, Doctor, Patient
- 🚦 **API Rate Limiting** - Distributed rate limiting using Bucket4j and Redis
- 🔒 **Password Encryption** - BCrypt password hashing

### Messaging & Notifications
- 📧 **Email Queue System** - Asynchronous email notifications via RabbitMQ
- 🔔 **Appointment Confirmations** - Automated confirmation emails
- ⏰ **Reminder System** - Scheduled appointment reminders
- 📨 **Cancellation Notifications** - Instant cancellation alerts

### Performance & Scalability
- ⚡ **Redis Caching** - Distributed caching for improved performance
- 🔄 **Asynchronous Processing** - Non-blocking email and notification handling
- 📊 **Database Pagination** - Efficient data retrieval with pagination and sorting
- 🗄️ **Connection Pooling** - HikariCP for optimized database connections

### Developer Experience
- 📚 **OpenAPI/Swagger** - Interactive API documentation
- 🐳 **Docker Support** - Complete containerization with docker-compose
- 🔧 **Flyway Migrations** - Version-controlled database schema management
- 📝 **Comprehensive Logging** - Structured logging with SLF4J and Logback
- 🎯 **Global Exception Handling** - Centralized error handling and validation

---

## 🏗️ Architecture

