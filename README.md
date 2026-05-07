
# AI-Powered Document & Multimedia Q&A System

An AI-powered full-stack application that allows users to upload documents and interact with them through a chatbot interface using Retrieval-Augmented Generation (RAG).

The system extracts content from uploaded files, processes and stores document chunks, and generates context-aware responses using LLM-based semantic retrieval.

---

# Features

- PDF document upload and processing
- AI-powered conversational chatbot
- Context-aware question answering using RAG
- Automatic document chunking and storage
- Document summarization
- PostgreSQL-based persistence layer
- React-based responsive frontend
- Spring Boot REST API backend
- Modular and scalable backend architecture

---

# Tech Stack

## Backend
- Java 17
- Spring Boot 3.5
- Spring Data JPA
- PostgreSQL
- Apache PDFBox
- WebFlux

## Frontend
- React
- Axios
- CSS

## AI & Processing
- RAG-based retrieval pipeline
- Embedding-based semantic context flow
- LLM integration for conversational responses

---

# System Architecture

```text
Upload File
    ↓
Text Extraction
    ↓
Chunking
    ↓
Store Chunks in PostgreSQL
    ↓
User Question
    ↓
Retrieve Relevant Chunks
    ↓
Generate AI Response
    ↓
Return Context-Aware Answer
````

---

# Project Structure

```text
aidocqa-system/
│
├── backend/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   ├── dto/
│   ├── ai/
│   ├── utils/
│   └── config/
│
├── frontend/
│   ├── components/
│   ├── services/
│   └── src/
│
└── README.md
```

---

# API Endpoints

## Upload Document

```http
POST /api/documents/upload
```

Uploads and processes a document.

---

## Chat with Document

```http
POST /api/chat
```

Accepts user questions and returns AI-generated responses based on uploaded document content.

---

# Setup Instructions

## Backend

```bash
mvn clean install
mvn spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

---

## Frontend

```bash
npm install
npm start
```

Frontend runs on:

```text
http://localhost:3000
```

---

# Database Configuration

Update:

```text
application.properties
```

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/aidocqa
spring.datasource.username=postgres
spring.datasource.password=your_password
```

Create database:

```sql
CREATE DATABASE aidocqa;
```

---

# Key Highlights

* Built using clean layered architecture
* Uses semantic retrieval workflow for contextual answers
* Backend designed with modular service separation
* Frontend provides real-time chatbot interaction
* Supports scalable document ingestion flow

---

# Future Improvements

* Audio and video transcription support
* Timestamp-based media navigation
* Redis caching
* Vector database integration
* JWT authentication
* Streaming AI responses

---



# Repository

GitHub Repository:

[https://github.com/simhadriuttareni/aidocqa-system](https://github.com/simhadriuttareni/aidocqa-system)

---

# Author

Simhadri Uttareni

