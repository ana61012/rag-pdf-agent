
# Ask my PDF

A small Retrieval-Augmented Generation (RAG) app. Upload a PDF, and ask questions that are answered from its content. The app splits the PDF into chunks, stores them in PostgreSQL with pgvector, and uses Google Gemini to answer based on the closest chunks.

The app includes a single-page web interface with a pastel game-style design, served by the same Spring Boot app.

## Features

- Upload a PDF (up to 5 MB) from the browser
- Chat with your document and get answers based on its content
- Gemini for chat answers and embeddings
- Vector search with PostgreSQL and pgvector
- Game-style interface: level, score and XP bar that grow as you ask questions

## Tech stack

- Java 25
- Spring Boot 3.5.16
- Spring AI 1.1.4 (Google GenAI chat and embedding starters)
- PostgreSQL with pgvector
- Maven
- Plain HTML, CSS and JavaScript for the frontend (`src/main/resources/static/index.html`)

## How it works

1. You upload a PDF. The app reads it page by page, splits it into chunks, creates an embedding for each chunk with Gemini, and stores them in the `vector_store` table.
2. You ask a question. The app finds the closest chunks to your question and adds them to the prompt along with the last few messages.
3. Gemini answers using only that context.

Chat history is kept in memory, so it resets when the app restarts. Uploaded documents stay in the database.

## Run locally

### Requirements

- JDK 25
- Docker Desktop (for the database)
- A Gemini API key from [Google AI Studio](https://aistudio.google.com/apikey)

### Steps

1. Clone the repository and open the project folder.

2. Set your API key as an environment variable.

   PowerShell:
   ```powershell
   $env:GEMINI_API_KEY="your-key"
   ```

   macOS and Linux:
   ```bash
   export GEMINI_API_KEY="your-key"
   ```

3. Start the app. Spring Boot starts the pgvector database from `compose.yaml` automatically.

   ```powershell
   .\mvnw spring-boot:run
   ```

4. Open [http://localhost:8888](http://localhost:8888), upload a PDF, and ask a question.

## Configuration

Settings are in `src/main/resources/application.properties`. Secrets are never stored in the file. They are read from environment variables.

| Variable | Purpose |
| --- | --- |
| `GEMINI_API_KEY` | Gemini API key, used for chat and embeddings |
| `PORT` | Server port (default 8888) |
| `SPRING_DATASOURCE_URL` | JDBC URL of the Postgres database (optional locally) |
| `SPRING_DATASOURCE_USERNAME` | Database user (optional locally) |
| `SPRING_DATASOURCE_PASSWORD` | Database password (optional locally) |
| `SPRING_DOCKER_COMPOSE_ENABLED` | Set to `false` on a server so the app does not start Docker |

The embedding size is 768. The `spring.ai.google.genai.embedding.text.options.dimensions` and `spring.ai.vectorstore.pgvector.dimensions` properties must always have the same value. If you change it, drop the `vector_store` table so it is recreated:

```sql
DROP TABLE IF EXISTS vector_store;
```

## API

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/ai/upload` | Upload a PDF (multipart form field `file`) |
| `GET` | `/api/ai/chat/messages` | Get the chat history |
| `POST` | `/api/ai/chat/messages` | Send a question with body `{"content": "..."}` |

## Deploy for free

A free setup that works for this app:

- **Database:** [Neon](https://neon.com) (free Postgres with pgvector)
- **App:** [Render](https://render.com) (free Docker web service)

1. In Neon, create a project and run `CREATE EXTENSION IF NOT EXISTS vector;` in the SQL editor.
2. Push the repository to GitHub.
3. In Render, create a **Web Service** from the repo and choose **Docker** as the environment.
4. Add these environment variables in Render:
   ```
   GEMINI_API_KEY=your-key
   SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-host>/<database>?sslmode=require
   SPRING_DATASOURCE_USERNAME=<neon-user>
   SPRING_DATASOURCE_PASSWORD=<neon-password>
   SPRING_DOCKER_COMPOSE_ENABLED=false
   ```
5. Deploy.

Free tiers have limits. Render's free service sleeps after 15 minutes without traffic and takes about a minute to wake up. Neon's free plan has 0.5 GB of storage.

## Notes

- There is no login. Anyone with the link can upload files and use your Gemini quota, so share a deployed copy carefully.
- Everyone who uses the app sees the same chat history.
- Never commit API keys. Keep them in environment variables.

## Project structure

```
src/main/java/pl/mojezapiski/rag/
├── chat/       Chat endpoints and the RAG prompt
├── document/   Vector store search
├── file/       PDF upload and processing
src/main/resources/
├── application.properties
└── static/index.html
```

## License

No license has been specified for this project.
