# Pharmacy-Management-System
A Spring Boot REST API developed as part of the Hontec Backend Assessment, featuring JWT authentication, role-based authorization, medicine management, PostgreSQL integration, Flyway migrations, Docker support, and Render deployment.
# Hontec Backend Assessment

A production-ready backend REST API developed as part of the **Hontec Backend Assessment** using **Java Spring Boot**.  
The application demonstrates secure authentication, role-based authorization, database management, RESTful API development, and cloud-ready deployment practices.

## 🚀 Live Deployment

Backend API is deployed on Render:

🔗 **Render URL:**  
https://hontec-assesment.onrender.com/swagger-ui/index.html#

---

## ✨ Features

- 🔐 JWT Authentication & Authorization
- 👥 Role-Based Access Control
  - ADMIN
  - PHARMACIST
  - CUSTOMER
- 🛡️ Spring Security Integration
- 🌐 RESTful API Design
- 🗄️ PostgreSQL Database Integration
- 🔄 Spring Data JPA & Hibernate ORM
- 🛠️ Flyway Database Migration
- ⚠️ Global Exception Handling
- ✅ Request Validation
- 🐳 Docker Support
- ☁️ Render Deployment Ready
- 📦 Maven Build Configuration
- 🏗️ Clean Layered Architecture

---

## 🛠️ Tech Stack

| Technology | Used For |
|------------|----------|
| Java 25 | Backend Development |
| Spring Boot 4 | REST API Framework |
| Spring Security | Authentication & Authorization |
| JWT | Secure Token Authentication |
| Spring Data JPA | Database Operations |
| Hibernate | ORM Framework |
| PostgreSQL | Database |
| Flyway | Database Migration |
| Maven | Dependency Management |
| Docker | Containerization |
| Render | Cloud Deployment |
| Git & GitHub | Version Control |

---

## 📂 Project Structure
src/main/java
│
├── config # Application configuration
├── controller # REST API Controllers
├── dto # Request & Response DTOs
├── exception # Custom exceptions & handlers
├── model # Entity classes
├── repository # Database repositories
├── security # JWT & Spring Security configuration
├── service # Business logic layer
└── resources
├── application.properties
└── db/migration # Flyway scripts

---

## ⚙️ Getting Started

### 1. Clone Repository

```bash
git clone <repository-url>
cd Hontec-Backend-Assessment
2. Build Application
./mvnw clean package
3. Run Application
java -jar target/*.jar
🔐 Environment Variables
JDBC_DATABASE_URL=your_database_url
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password
JWT_SECRET=your_jwt_secret
🐳 Docker Support
docker build -t hontec-backend .
docker run -p 8080:8080 hontec-backend
☁️ Deployment

The application is configured for cloud deployment using:

Render for backend hosting
Neon PostgreSQL for managed database
Docker for container deployment

Production database configuration is handled using environment variables.
📌 API Documentation

API endpoints can be tested using:

Postman
Swagger/OpenAPI (if enabled)
👨‍💻 Developer

Vijay Khetre

Java Spring Boot Developer | Backend Developer

GitHub: https://github.com/vkfullstack
