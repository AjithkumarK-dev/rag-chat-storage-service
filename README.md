# 🧠 RAG Chat Storage Service — Backend Microservice

A **Spring Boot 3** microservice that securely stores and manages chat sessions and messages for a **Retrieval-Augmented Generation (RAG)** chatbot system.  
Implements OpenAI chat integration, caching, resilience, and fault-tolerant design.

---

## 📚 Table of Contents
- [Tech Stack](#-tech-stack)
- [Service Access URLs](#-service-access-urls)
- [Architecture Overview](#-architecture-overview)
- [Security and Authorization](#-security-and-authorization)
- [Key Features Implemented](#-key-features-implemented)
- [Setup Instructions](#-setup-instructions)
- [Resilience and Fallback Logic](#-resilience-and-fallback-logic)
- [Caching Behavior](#-caching-behavior)
- [OpenAI Integration](#-openai-integration)
- [API Endpoints Summary](#-api-endpoints-summary)
- [Sample API Requests and Responses](#-sample-api-requests-and-responses)
- [Monitoring](#-monitoring)
- [Client Assignment Compliance](#-client-assignment-compliance)
- [Run Locally](#-run-locally)
- [Author](#-author)

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3 |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA + Hibernate |
| API Docs | Swagger / Springdoc OpenAPI |
| Auth | API Key (Swagger Authorize) |
| HTTP Client | WebClient |
| Mapper | ModelMapper |
| Cache | Spring Cache + Caffeine |
| Resilience | Resilience4j (Retry + Circuit Breaker) |
| Rate Limiting | Bucket4j |
| Monitoring | Spring Boot Actuator |
| Docker | docker-compose (PostgreSQL + pgAdmin) |

---

## 🌐 Service Access URLs

| Service | URL |
|---|---|
| Application Base | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI Docs (JSON) | `http://localhost:8080/v3/api-docs` |
| Health Check | `http://localhost:8080/actuator/health` |
| Cache Stats | `http://localhost:8080/actuator/caches` |
| Metrics | `http://localhost:8080/actuator/metrics` |
| pgAdmin (Database UI) | `http://localhost:5050` |

---

## 🧩 Architecture Overview

```
Swagger → Controller → Service → Repository → PostgreSQL
                   ↘︎
                    ↳ AIResponseService → OpenAI API
                       (CircuitBreaker + Retry + Fallback)
```

---

## 🔐 Security and Authorization

- All APIs are protected via API Key from `.env`
- In **Swagger UI**, click **“Authorize”** → enter the secret key once.
- Once authorized, all endpoints can be tested without headers.

---

## 🧱 Key Features Implemented

- ✅ Create, update, delete chat sessions
- ✅ Mark sessions as favorite/unfavorite
- ✅ Add & retrieve messages (paginated)
- ✅ Rate Limiting with Bucket4j
- ✅ API-Key Security via Filter
- ✅ CORS ready
- ✅ Centralized Error Handling (standard error codes)
- ✅ Resilience4j Retry + Circuit Breaker
- ✅ Spring Cache (Caffeine)
- ✅ AutoMapper (ModelMapper)
- ✅ Dockerized PostgreSQL + pgAdmin
- ✅ Actuator endpoints for monitoring

---

## 🧰 Setup Instructions

### 1️⃣ Clone Repo
```bash
git clone https://github.com/yourUsename/rag-chat-storage-service.git
cd rag-chat-storage-service
```

### 2️⃣ Create `.env`
```env
DB_USER=postgres
DB_PASS=password
DB_NAME=chatdb
OPENAI_API_KEY=sk-xxxxx
API_KEYS=key1,key2,key3
SERVER_PORT=8080
```

### 3️⃣ Start Docker Services
```bash
docker-compose up -d
```

### 4️⃣ Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

---

## 🧠 Resilience and Fallback Logic

OpenAI calls are wrapped with **Resilience4j**:
```java
@Retry(name = "openaiRetry", fallbackMethod = "fallbackResponse")
@CircuitBreaker(name = "openaiCB", fallbackMethod = "fallbackResponse")
```

If OpenAI is unreachable:
```json
{
  "message": "⚠️ OpenAI service is temporarily unavailable. Please try again later."
}
```

This demonstrates robust error recovery and fault-tolerant design.

---

## 💾 Caching Behavior
- **Spring Cache + Caffeine** minimize DB hits.
- Inspect via:
```
GET /actuator/caches
```

---

## 🧠 OpenAI Integration

- Uses `WebClient` with API key authorization.
- Retry & circuit breaker ensure fault tolerance.
- If OpenAI doesn’t respond, fallback message is generated and logged.

---

## 📘 API Endpoints Summary

| Type | Method | Endpoint | Description |
|------|--------|-----------|-------------|
| Session | POST | `/api/chat/session` | Create a new chat session |
| Session | GET | `/api/chat/session/{id}` | Get session details |
| Session | PUT | `/api/chat/session/{id}` | Update session name/favorite |
| Session | PATCH | `/api/chat/session/{id}/favorite` | Toggle favorite |
| Session | DELETE | `/api/chat/session/{id}` | Delete session |
| Message | POST | `/api/chat/session/{id}/message` | Add message |
| Message | GET | `/api/chat/session/{id}/messages` | Retrieve paginated messages |
| OpenAI | POST | `/api/chat/session/{id}/chat` | Get AI-generated response |

---

## 🧪 Sample API Requests and Responses

> **Swagger Authorization:** Click **“Authorize”** once in Swagger UI to provide the API key; requests below omit headers on purpose.

### 1) Create Session
**POST** `/api/chat/session`

**Request**
```json
{
  "userId": "123451",
  "name": "Support Chat",
  "favorite": false
}
```

**Response**
```json
{
  "code": 200,
  "message": "Session creation completed",
  "data": {
    "id": "624e02cc-c51f-4a58-91ca-91a1ebda568c",
    "userId": "123451",
    "name": "Support Chat",
    "favorite": false,
    "createdAt": "2025-10-27T20:04:10.586788",
    "updatedAt": "2025-10-27T20:04:10.586788"
  }
}
```

---

### 2) Update Session
**PUT** `/api/chat/session/{sessionId}`

**Request**
```json
{
  "name": "Support Chat",
  "favorite": false
}
```

**Response**
```json
{
  "code": 200,
  "message": "Session updated successfully",
  "data": {
    "id": "624e02cc-c51f-4a58-91ca-91a1ebda568c",
    "userId": "123451",
    "name": "Support Chat",
    "favorite": false,
    "createdAt": "2025-10-27T20:04:10.586788",
    "updatedAt": "2025-10-27T20:04:10.586788"
  }
}
```

---

### 3) Get Session
**GET** `/api/chat/session/{sessionId}`

**Response**
```json
{
  "code": 200,
  "message": "Session fetched successfully",
  "data": {
    "id": "624e02cc-c51f-4a58-91ca-91a1ebda568c",
    "userId": "123451",
    "name": "Support Chat",
    "favorite": false,
    "createdAt": "2025-10-27T20:04:10.586788",
    "updatedAt": "2025-10-27T20:04:10.586788"
  }
}
```

---

### 4) Delete Session
**DELETE** `/api/chat/session/{sessionId}`

**Response**
```json
{
  "code": 200,
  "message": "Session deleted successfully",
  "data": null
}
```

---

### 5) Add Message
**POST** `/api/chat/session/{sessionId}/message`

**Request**
```json
{
  "sessionId": "ff084f28-36ef-492c-92af-cb63b7dfbd47",
  "sender": "user",
  "message": "Hello, how can I check my account balance?"
}
```

**Response**
```json
{
  "code": 200,
  "message": "Message added successfully",
  "data": {
    "id": "ce5305d2-1130-4028-a06f-1325d409da57",
    "sessionId": "ff084f28-36ef-492c-92af-cb63b7dfbd47",
    "sender": "user",
    "message": "Hello, how can I check my account balance?"
  }
}
```

---

### 6) Toggle Favorite
**PATCH** `/api/chat/session/{sessionId}/favorite`

**Response**
```json
{
  "code": 200,
  "message": "Favorite status toggled successfully",
  "data": {
    "id": "ff084f28-36ef-492c-92af-cb63b7dfbd47",
    "userId": "767687",
    "name": "checking",
    "favorite": true,
    "createdAt": "2025-10-27T15:19:51.233239",
    "updatedAt": "2025-10-27T20:15:25.959514"
  }
}
```

---

### 7) Retrieve Messages
**GET** `/api/chat/session/{sessionId}/messages?page=0&size=5`

**Response**
```json
{
  "code": 200,
  "message": "Messages retrieved successfully",
  "data": [
    {
      "id": "6f6910d1-c222-4b5a-a653-03cfb0b077ed",
      "sessionId": "ff084f28-36ef-492c-92af-cb63b7dfbd47",
      "sender": "user",
      "message": "Hello, how can I check my account balance?"
    },
    {
      "id": "20f80fcf-3c7f-4492-89ad-7408396798bb",
      "sessionId": "ff084f28-36ef-492c-92af-cb63b7dfbd47",
      "sender": "assistant",
      "message": "No response received from OpenAI."
    },
    {
      "id": "bc62c1d3-3314-4efd-9cc8-9436221e1ab8",
      "sessionId": "ff084f28-36ef-492c-92af-cb63b7dfbd47",
      "sender": "user",
      "message": "Hello, how can I check my account balance?"
    },
    {
      "id": "ebd58d91-1f74-4fbe-8fe4-6250322d29a9",
      "sessionId": "ff084f28-36ef-492c-92af-cb63b7dfbd47",
      "sender": "assistant",
      "message": "No response received from OpenAI."
    },
    {
      "id": "ce5305d2-1130-4028-a06f-1325d409da57",
      "sessionId": "ff084f28-36ef-492c-92af-cb63b7dfbd47",
      "sender": "user",
      "message": "Hello, how can I check my account balance?"
    }
  ]
}
```



## 🧩 Monitoring

| Endpoint | Description |
|---|---|
| `/actuator/health` | Health Check |
| `/actuator/caches` | Cache Stats |
| `/actuator/metrics` | Performance Metrics |

---

## ✅ Client Assignment Compliance

| Requirement | Implementation | Status |
|---|---|---|
| Multiple API Keys | ApiKeyFilter + `.env` | ✅ |
| Error Codes | GlobalExceptionHandler + ApiResponseDTO | ✅ |
| AutoMapper | ModelMapper | ✅ |
| Retry & CircuitBreaker | Resilience4j | ✅ |
| Rate Limiting | Bucket4j | ✅ |
| Swagger Docs | Springdoc OpenAPI | ✅ |
| Health Monitoring | Actuator | ✅ |
| Docker Setup | Dockerfile + docker-compose | ✅ |
| Caching | Caffeine | ✅ |
| Pagination | Implemented | ✅ |

---

## 🏁 Run Locally

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/chatdb
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=password
export OPENAI_API_KEY=sk-xxxxx
mvn spring-boot:run
```

---

## 👤 Author

**Ajith Kumar K**  
Senior Java Developer — Payments Domain  
GitHub: https://github.com/AjithkumarK-dev
