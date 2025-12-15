# 🎓 Java Quiz Application

A complete **end‑to‑end Quiz Platform** built first as a **Monolithic application** and then refactored into a **production‑grade Microservices architecture** using Spring Boot, Spring Cloud, OAuth2, JWT, API Gateway, Eureka, Feign, and centralized Swagger.

---

## 📌 Project Goals

* Understand **Monolithic vs Microservices** architecture
* Implement **secure authentication & authorization**
* Learn **Spring Security, OAuth2, JWT**
* Design **API Gateway + Service Registry**
* Use **Feign Clients** for inter‑service communication
* Centralize **Swagger documentation**
* Build an **industry‑style backend system**

---

<img width="1919" height="902" alt="image" src="https://github.com/user-attachments/assets/6b70c2f6-5e36-4cb6-ad64-fd2991082b1d" />


# 🧱 PART 1 — MONOLITHIC ARCHITECTURE

## 🏗 Architecture Overview

```
Client
  ↓
Spring Boot Monolith
  ├── Auth Module
  ├── User Module
  ├── Quiz Module
  ├── Question Module
  ├── Result Module
  └── Database (Single)
```

---

## 📦 Monolith Modules

### 🔐 Authentication Module

* Email/Password Login
* Google OAuth2 Login
* JWT Token Generation
* Role‑based access (ADMIN / STUDENT)

### 👤 User Module

* Register users
* Fetch user profile
* Admin: list all users

### 🧠 Quiz Module

* Create quizzes (ADMIN)
* Publish quizzes
* Fetch quizzes

### ❓ Question Module

* Add questions to quizzes
* Add options (correct / incorrect)
* Fetch questions by quiz

### 📊 Result Module

* Submit quiz attempts
* Calculate score
* Store quiz history

---

## ⚠ Limitations of Monolith

❌ Tight coupling
❌ Hard to scale specific modules
❌ Single deployment unit
❌ Slower builds & releases

➡️ **Solution: Microservices**

---

# 🌐 PART 2 — MICROSERVICES ARCHITECTURE

## 🏗 High‑Level Architecture

```
Client
  ↓
API Gateway (8080)
  ↓
--------------------------------------------------
|  Auth | Quiz | Question | Result | User       |
--------------------------------------------------
        ↓
     Eureka Server (8761)
```

---

## 🔩 Microservices Breakdown

### 1️⃣ Auth Service (8081)

**Responsibilities:**

* Login / Register
* Google OAuth2
* JWT generation
* User management

**Tech:**

* Spring Security
* OAuth2 Client
* JWT
* PostgreSQL

---

### 2️⃣ Quiz Service (8082)

**Responsibilities:**

* Create quizzes
* Publish quizzes
* Fetch quizzes

**Security:**

* ADMIN only (via Gateway)

---

### 3️⃣ Question Service (8083)

**Responsibilities:**

* Create questions
* Manage options
* Validate answers

---

### 4️⃣ Result Service (8084)

**Responsibilities:**

* Submit quiz
* Calculate score
* Persist results

**Uses Feign Client:**

* Calls Question Service to validate answers

---

### 5️⃣ API Gateway (8080)

**Responsibilities:**

* Single entry point
* JWT validation
* Role‑based routing
* Forward headers (X‑User‑Email, X‑User‑Role)

---

### 6️⃣ Eureka Server (8761)

**Responsibilities:**

* Service discovery
* Dynamic routing
* Load balancing

---

## 🔐 Security Flow (JWT + OAuth2)

```
Login → Auth Service → JWT
JWT → API Gateway → Validation
Gateway → Microservices (Headers)
```

* JWT validated **only at Gateway**
* Services trust Gateway headers
* No duplicated security logic

---

## 🔁 Inter‑Service Communication

### ✅ Feign Clients

Used in:

* Result → Question Service
* Quiz → Question Service (optional)

Benefits:

* Clean REST calls
* Load‑balanced
* Eureka‑aware

---

## 📘 Centralized Swagger

### Access Swagger

```
http://localhost:8080/swagger-ui.html
```

### Features

* All services in one UI
* Dropdown selection
* Gateway‑routed `/v3/api-docs`

---

## 🧪 Common Errors Faced & Fixes

| Error                  | Fix                       |
| ---------------------- | ------------------------- |
| 401 Unauthorized       | JWT validation at Gateway |
| OAuth redirect error   | Permit OAuth endpoints    |
| Swagger 404            | Enable swagger in Gateway |
| FeignClient error      | Use interface only        |
| Eureka not registering | Fix Spring Cloud version  |

---

## ⚙ Technology Stack

* Java 21
* Spring Boot 3.x
* Spring Cloud 2023.x
* Spring Security
* OAuth2 (Google)
* JWT
* PostgreSQL
* OpenFeign
* Eureka
* Spring Cloud Gateway
* Springdoc OpenAPI

---

## 📈 What This Project Demonstrates

✔ Backend system design
✔ Security best practices
✔ Microservices decomposition
✔ API Gateway patterns
✔ Real‑world debugging experience
✔ Enterprise‑level architecture

---

## 🚀 Future Enhancements

* Docker & Docker Compose
* Kubernetes
* Rate limiting
* Circuit breakers (Resilience4j)
* Distributed tracing (Zipkin)
* CI/CD pipeline

---

## 🏁 Conclusion

This project evolved from a **simple monolith** into a **fully secure, scalable, enterprise‑grade microservices system** — mirroring real industry backend architectures.

> *"If you can build this, you are no longer a beginner."*

🔥 **Well done.**
