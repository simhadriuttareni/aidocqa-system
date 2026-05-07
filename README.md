# 🤖 AI-Powered Document & Multimedia Q&A System

An AI-powered full-stack application that allows users to upload documents and interact with them through a real-time conversational chatbot interface using Retrieval-Augmented Generation (RAG).

The system processes uploaded documents, retrieves relevant contextual information, and generates intelligent conversational AI responses through a chatbot-style interface similar to modern AI assistants.

---

# ✨ Features

- 📄 PDF document upload and processing
- 💬 Real-time chatbot-style conversation interface
- 🧠 Context-aware AI responses using RAG
- 📚 Intelligent document chunking and retrieval
- 📝 AI-generated document summaries
- ⚡ Interactive React frontend experience
- 🗂️ PostgreSQL-based persistence layer
- 🔗 Spring Boot REST API backend
- 🏗️ Clean layered architecture
- 🐳 Docker-ready setup

---

# 🖥️ Chatbot Interface Preview

The application provides a real-time conversational chatbot experience similar to modern AI assistants.

Users can:
- Upload documents
- Select previously uploaded files
- Ask natural language questions
- Receive intelligent AI-generated responses
- Interact through a clean chat-style interface

The frontend is designed to mimic modern conversational AI systems with contextual responses and seamless document interaction.

---

# 🏗️ System Workflow

```text
Upload Document
      ↓
Extract Text Content
      ↓
Chunk & Store Data
      ↓
User Asks Question
      ↓
Retrieve Relevant Context
      ↓
Generate AI Response
      ↓
Display Conversational Reply in Chat UI
````

---

# 🛠️ Tech Stack

## Backend

* Java 17
* Spring Boot 3.5
* Spring Data JPA
* PostgreSQL
* Apache PDFBox
* Spring WebFlux

## Frontend

* React
* Axios
* CSS

## AI & Processing

* Retrieval-Augmented Generation (RAG)
* Embedding-based contextual retrieval
* LLM integration for conversational responses

---

# 📂 Project Structure

```text
aidocqa-system/
│
├── aidocqa/                     # Spring Boot Backend
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
│   └── aidocqa-frontend/
│       ├── src/
│       ├── public/
│       └── package.json
│
└── README.md
```

---

# 🚀 Getting Started

## Prerequisites

Make sure you have:

* Java 17+
* Node.js
* PostgreSQL
* Maven
* Groq/OpenAI API Key

---

# ⚙️ Backend Setup

## 1. Clone Repository

```bash
git clone https://github.com/simhadriuttareni/aidocqa-system.git
cd aidocqa-system
```

---

## 2. Create Database

```sql
CREATE DATABASE aidocqa;
```

---

## 3. Configure Database

Update:

```text
src/main/resources/application.properties
```

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/aidocqa
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

## 4. Configure API Key

### Windows PowerShell

```powershell
$env:GROQ_API_KEY="your_api_key"
```

---

## 5. Run Backend

```bash
cd aidocqa
mvn clean install
mvn spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

---

# 🎨 Frontend Setup

Open a new terminal:

```bash
cd frontend/aidocqa-frontend
npm install
npm start
```

Frontend runs on:

```text
http://localhost:3000
```

---

# 💬 Chatbot Experience

The application provides a conversational chatbot interface where users can interact naturally with uploaded documents.

Example questions:

* “What is this document about?”
* “Summarize the key points”
* “Explain the main concepts”
* “What technologies are mentioned?”

The AI retrieves relevant document context and generates human-like conversational responses directly in the frontend chat interface.

---

# 📡 API Endpoints

## Upload Document

```http
POST /api/documents/upload
```

Uploads and processes documents for AI interaction.

---

## Chat with AI

```http
POST /api/chat/ask
```

Accepts natural language questions from the frontend chat interface and returns intelligent conversational AI responses based on uploaded document content.

---

## Get Uploaded Documents

```http
GET /api/documents
```

Returns all uploaded documents.

---

# 🧪 Running Tests

## Backend

```bash
mvn test
```

## Frontend

```bash
npm test
```

---

# 🐳 Docker Support

Run the entire application using Docker Compose:

```bash
docker-compose up --build
```

---

# 🔥 Key Highlights

* Implements Retrieval-Augmented Generation (RAG)
* Real-time chatbot interaction experience
* Conversational AI workflow similar to modern AI assistants
* Full-stack integration with React and Spring Boot
* Modular and scalable backend architecture
* Intelligent semantic retrieval pipeline

---

# 🚀 Future Improvements

* Audio & video transcription support
* Timestamp-based media navigation
* Vector database integration
* JWT authentication
* Streaming AI responses
* Redis caching

---



# 🔗 GitHub Repository

[https://github.com/simhadriuttareni/aidocqa-system](https://github.com/simhadriuttareni/aidocqa-system)

---

# 👨‍💻 Author

Simhadri Uttareni

---

# 📌 Final Note

This project was built as part of the SDE-1 Assignment for PanScience Innovations and focuses on building a real-world AI-powered conversational document interaction system using modern backend, frontend, and AI engineering concepts.

