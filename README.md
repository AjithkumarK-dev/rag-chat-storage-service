# 🧠 RAG Chat Storage Service — Backend Microservice

**RAG Chat Storage** microservice that securely stores and manages chat sessions and messages for a **Retrieval-Augmented Generation (RAG)** chatbot system.  
Includes optional **AI response integration**, along with **caching**, **resilience**, **rate limiting**, and **fault-tolerant design** — fully containerized using **Docker Compose**.

---

## 📚 Table of Contents
1. [⚙️ Tech Stack](#️-tech-stack)
2. [🌐 Service Access URLs](#-service-access-urls)
3. [🧩 Architecture Overview](#-architecture-overview)
4. [🔐 Security & Authorization](#-security--authorization)
5. [🚀 Quick Start (Docker)](#-quick-start-docker)
6. [.env.example (Commit-safe)](#envexample-commit-safe)
7. [🖥️ Manual Local Run](#️-manual-local-run)
8. [🧠 Resilience & Fallback](#-resilience--fallback)
9. [💾 Caching](#-caching)
10. [🤖 OpenAI Integration](#-openai-integration)
11. [📘 API Endpoints](#-api-endpoints)
12. [🧩 Sample API Requests & Responses](#-sample-api-requests--responses)
13. [🧩 Monitoring](#-monitoring)
14. [✅ Client Assignment Compliance](#-client-assignment-compliance)
15. [🧾 Deployment Checklist](#-deployment-checklist)
16. [👤 Author](#-author)

---

## ⚙️ Tech Stack

| Layer | Technology |
|--------|-------------|
| Language | ☕ Java 17 |
| Framework | 🚀 Spring Boot 3.3 |
| Database | 🐘 PostgreSQL 15 |
| ORM | 🧩 Spring Data JPA + Hibernate |
| API Docs | 📘 Swagger / Springdoc OpenAPI |
| Auth | 🔐 API Key (Swagger Authorize) |
| HTTP Client | 🌐 WebClient |
| Mapper | 🧭 ModelMapper |
| Cache | ⚡ Spring Cache + Caffeine |
| Resilience | 🧠 Resilience4j (Retry + Circuit Breaker) |
| Rate Limiting | 🚦 Bucket4j |
| Monitoring | 📊 Spring Boot Actuator |
| Containerization | 🐳 Dockerfile + docker-compose |

---

## 🌐 Service Access URLs

| Service | URL |
|----------|-----|
| 🧩 Application Base | [http://localhost:8080](http://localhost:8080) |
| 📘 Swagger UI | [http://localhost:8080/swagger-ui/swagger-ui/index.html](http://localhost:8080/swagger-ui/swagger-ui/index.html) |
| 📄 OpenAPI Docs (JSON) | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |
| ❤️ Health Check | [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) |
| 💾 Cache Stats | [http://localhost:8080/actuator/caches](http://localhost:8080/actuator/caches) |
| 📈 Metrics | [http://localhost:8080/actuator/metrics](http://localhost:8080/actuator/metrics) |
| 🗃️ pgAdmin | [http://localhost:5050](http://localhost:5050) |

---

## 🧩 Architecture Overview
```
Swagger → Controller → Service → Repository → PostgreSQL
                   ↘︎
                    ↳ AIResponseService → OpenAI API
                       (Retry + CircuitBreaker + Fallback)
```

---

## 🔐 Security & Authorization
- APIs protected using **API Key** from `.env` file.
- In **Swagger UI**, click **Authorize**, paste one of the keys (e.g., `testkey1`), and proceed.
- Swagger automatically attaches the key in all subsequent requests.

---

## 🚀 Quick Start (Docker)

Clone the repository:
```bash
git clone https://github.com/AjithkumarK-dev/rag-chat-storage-service.git
cd rag-chat-storage-service
```

Create a `.env` file (or copy from `.env.example`):
```bash
DB_USER=postgres
DB_PASS=password
DB_NAME=chatdb
DB_HOST=chatdb
DB_PORT=5432
OPENAI_API_KEY=dummy_key
API_KEYS=testkey1,testkey2,testkey3
SERVER_PORT=8080
```

> ⚠️ **Note:** Make sure **Docker Desktop** (or Docker Engine) is installed and running before executing the commands below.

Run everything with one command:
```bash
docker compose up --build
```

✅ What happens:
- Starts PostgreSQL + pgAdmin containers.
- Builds and launches Spring Boot after DB is healthy.
- Exposes ports:
  - App → 8080
  - pgAdmin → 5050

To verify:
```bash
docker ps   # should show chatdb, pgadmin, rag-chat-app
docker logs -f rag-chat-app   # watch for Tomcat started on port 8080
```

---

## 🖥️ Manual Local Run
If you prefer not to use Docker:
```bash
# Ensure local Postgres is running and credentials match
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/chatdb
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=password

mvn clean install
mvn spring-boot:run
```
📝 Note: For local runs, change `DB_HOST` to `localhost`.

---

## 🧠 Resilience & Fallback
OpenAI calls are wrapped with **Resilience4j** annotations:
```java
@Retry(name = "openaiRetry", fallbackMethod = "fallbackResponse")
@CircuitBreaker(name = "openaiCB", fallbackMethod = "fallbackResponse")
```
Gracefully handles rate limits or downtime with fallback responses.

---

## 💾 Caching
- Uses **Spring Cache + Caffeine**.
- Reduces DB hits for frequently accessed sessions.
- Inspect via:  
  🔗 [http://localhost:8080/actuator/caches](http://localhost:8080/actuator/caches)

---


### 🆕 Clear All Caches
**POST** `/api/chat/admin/clear-caches`  
Clears all in-memory caches such as `chatSessions`, `chatMessages`, etc.

**Response:**
```json
{
  "code": 200,
  "message": "All caches cleared successfully",
  "data": null
}
```
---
## 🤖 OpenAI Integration
- Non-blocking **WebClient** with API key authentication.
- Supports multiple keys from `.env`.
- Integrated with Retry + CircuitBreaker for fault tolerance.

---

## 📘 API Endpoints
| Type | Method | Endpoint | Description |
|------|---------|-----------|--------------|
| Session | POST | `/api/chat/session` | Create a new chat session |
| Session | GET | `/api/chat/session/{id}` | Get session details |
| Session | PUT | `/api/chat/session/{id}` | Update session |
| Session | PATCH | `/api/chat/session/{id}/favorite` | Toggle favorite |
| Session | DELETE | `/api/chat/session/{id}` | Delete session |
| Message | POST | `/api/chat/session/{id}/message` | Add message |
| Message | GET | `/api/chat/session/{id}/messages?page=&size=` | Retrieve paginated messages |
| OpenAI | POST | `/api/chat/session/{id}/chat` | Chat with AI (needs valid key) |

---

## 🧩 Sample API Requests & Responses
All requests assume Swagger authorization is done.

### 🟢 1) Create Session
**POST** `/api/chat/session`
```json
{
  "userId": "123451",
  "name": "Support Chat",
  "favorite": false
}
```
**Response:**
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

### 🟢 2) Update Session
**PUT** `/api/chat/session/{sessionId}`
```json
{
  "name": "Support Chat",
  "favorite": false
}
```
**Response:**
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

### 🟢 3) Get Session
**GET** `/api/chat/session/{sessionId}`
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

### 🟢 4) Delete Session
**DELETE** `/api/chat/session/{sessionId}`
```json
{
  "code": 200,
  "message": "Session deleted successfully",
  "data": null
}
```

### 🟢 5) Add Message
**POST** `/api/chat/session/{sessionId}/message`
```json
{
  "sessionId": "ff084f28-36ef-492c-92af-cb63b7dfbd47",
  "sender": "user",
  "message": "Hello, how can I check my account balance?"
}
```
**Response:**
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

### 🟢 6) Toggle Favorite
**PATCH** `/api/chat/session/{sessionId}/favorite`
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

### 🟢 7) Retrieve Messages (Paginated)
**GET** `/api/chat/session/{sessionId}/messages?page=0&size=5`
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
    }
  ]
}
```

### 🟢 8) Chat with AI
**POST** `/api/chat/session/{sessionId}/chat`
```json
{
  "sessionId": "2a9c1bde-6d1f-4e8a-9a34-72e3f1a7a233",
  "prompt": "What is RAG architecture?"
}
```
**Response:**
```json
{
  "code": 200,
  "message": "AI response generated successfully",
  "data": {
    "sessionId": "2a9c1bde-6d1f-4e8a-9a34-72e3f1a7a233",
    "response": "RAG (Retrieval-Augmented Generation) combines LLMs with retrieval from a knowledge source to improve factuality and context."
  }
}
```

---

## 🧩 Monitoring
| Endpoint | Description |
|-----------|-------------|
| `/actuator/health` | Health Check |
| `/actuator/caches` | Cache Stats |
| `/actuator/metrics` | Performance Metrics |

---

## ✅ Client Assignment Compliance
| Requirement | Implementation | Status |
|--------------|----------------|---------|
| Multiple API Keys | ApiKeyFilter + .env | ✅ |
| Standard Error Codes | GlobalExceptionHandler + ApiResponseDTO | ✅ |
| AutoMapper | ModelMapper | ✅ |
| Retry & CircuitBreaker | Resilience4j | ✅ |
| Rate Limiting | Bucket4j | ✅ |
| Swagger Docs | Springdoc OpenAPI | ✅ |
| Health Monitoring | Actuator | ✅ |
| Dockerized | Dockerfile + docker-compose | ✅ |
| Caching | Caffeine | ✅ |
| Pagination | Implemented | ✅ |

---

## 🧾 Deployment Checklist
| Step | Description | Status |
|------|--------------|--------|
| `.env` file created | DB + API Keys configured | ✅ |
| Docker running | `docker compose up --build` executed | ✅ |
| PostgreSQL ready | Accessible via pgAdmin | ✅ |
| Swagger reachable | [http://localhost:8080/swagger-ui/swagger-ui/index.html](http://localhost:8080/swagger-ui/swagger-ui/index.html) | ✅ |
| Caching verified | `/actuator/caches` endpoint shows entries | ✅ |
| API Keys authorized | Swagger Authorize → key entered | ✅ |

---

## 👤 Author
**Ajith Kumar K**  
Senior Java Developer — Payments Domain  
🌐 GitHub: [AjithkumarK-dev](https://github.com/AjithkumarK-dev)

---

> 🏁 **Ready to Run:** Clone → Create `.env` → `docker compose up --build` → Open Swagger → Authorize → Test APIs ✅

