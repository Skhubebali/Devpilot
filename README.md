<div align="center">

# 🚀 DevPilot

**AI-powered code intelligence — Chat with your GitHub repositories**

[![Live Demo](https://img.shields.io/badge/Live%20Demo-devpilot--steel.vercel.app-blue?style=for-the-badge&logo=vercel)](https://devpilot-steel.vercel.app)
[![Backend](https://img.shields.io/badge/Backend-Render-46E3B7?style=for-the-badge&logo=render)](https://render.com)
[![Database](https://img.shields.io/badge/Database-Neon%20PostgreSQL-00E599?style=for-the-badge&logo=postgresql)](https://neon.tech)

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot) ![Next.js](https://img.shields.io/badge/Next.js-16.3.4-black?style=for-the-badge&logo=next.js) ![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=java) ![OpenAI](https://img.shields.io/badge/OpenAI-GPT--4o--mini-412991?style=for-the-badge&logo=openai)

</div>

---

## 📖 What is DevPilot?

DevPilot is an **AI-powered code Q&A platform** that lets you chat with your GitHub repositories using natural language. It uses **Retrieval-Augmented Generation (RAG)** to understand your codebase and answer questions like:

- *"How does the authentication flow work in this project?"*
- *"Where is the database connection configured?"*
- *"Explain the indexing pipeline step by step."*
- *"What does the `VectorSearchService` do?"*

DevPilot indexes your repositories by chunking source files into semantic pieces, generating vector embeddings via OpenAI, and storing them in PostgreSQL with the `pgvector` extension. When you ask a question, it retrieves the most relevant code chunks and sends them to GPT-4o-mini for a grounded, accurate answer.

---

## ✨ Features

- 🔐 **GitHub OAuth Login** — Sign in securely with your GitHub account
- 📦 **Repository Sync** — Automatically pulls all your GitHub repositories
- 🧠 **AI Indexing** — Chunks code files and generates semantic embeddings
- 💬 **Chat Interface** — Ask questions in natural language about any indexed repo
- 🔍 **Vector Search** — Uses pgvector cosine similarity for fast semantic retrieval
- 📊 **Real-time Progress** — Live indexing status (Pending → Indexing → Ready)
- 🌐 **Production Ready** — Deployed on Vercel + Render + Neon

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND                             │
│              Next.js 16 · React 19 · Tailwind               │
│           Deployed on Vercel (devpilot-steel.vercel.app)    │
└───────────────────────┬─────────────────────────────────────┘
                        │ HTTPS + credentials: include
                        │ (Session Cookie · SameSite=None)
┌───────────────────────▼─────────────────────────────────────┐
│                        BACKEND                              │
│            Spring Boot 4.1.1 · Java 21 · Spring AI          │
│                Deployed on Render (Docker)                  │
│                                                             │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ AuthCtrl    │  │ RepoCtrl     │  │ IndexingService  │   │
│  │ /api/auth   │  │ /api/repos   │  │ CodeChunker      │   │
│  └─────────────┘  └──────────────┘  │ EmbeddingService │   │
│                                     └──────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              RAG Pipeline                           │   │
│  │  User Question → Embed → pgvector Search →          │   │
│  │  Retrieve Chunks → GPT-4o-mini → Answer             │   │
│  └─────────────────────────────────────────────────────┘   │
└────────────────┬─────────────────────────────┬─────────────┘
                 │                             │
    ┌────────────▼──────────┐    ┌─────────────▼─────────────┐
    │   Neon PostgreSQL     │    │      OpenAI API            │
    │  pgvector · hstore    │    │  gpt-4o-mini (Chat)        │
    │  uuid-ossp            │    │  text-embedding-3-small    │
    └───────────────────────┘    └────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Frontend
| Technology | Version | Purpose |
|:---|:---|:---|
| Next.js | 16.3.4 | React Framework (App Router) |
| React | 19.2.8 | UI Library |
| TanStack Query | 5.x | Server state management, caching |
| Tailwind CSS | 4.x | Utility-first styling |
| Shadcn UI | — | Component library |
| TypeScript | 5.x | Type safety |

### Backend
| Technology | Version | Purpose |
|:---|:---|:---|
| Spring Boot | 4.1.1 | Application framework |
| Java | 21 | Language |
| Spring AI | 2.0.1 | OpenAI / Embedding integration |
| Spring Security | — | OAuth2 GitHub login |
| Spring Data JPA | — | Database ORM |
| Lombok | — | Boilerplate reduction |

### Database & AI
| Technology | Purpose |
|:---|:---|
| PostgreSQL (Neon) | Primary database |
| pgvector | Vector similarity search (`<=>` cosine) |
| OpenAI `text-embedding-3-small` | Code chunk embeddings (1536 dims) |
| OpenAI `gpt-4o-mini` | Chat / answer generation |
| HNSW Index | Fast approximate nearest neighbor search |

### Deployment
| Service | What Runs There |
|:---|:---|
| **Vercel** | Next.js frontend |
| **Render** | Spring Boot backend (Docker) |
| **Neon** | PostgreSQL + pgvector |
| **GitHub OAuth** | Authentication provider |

---

## 📂 Project Structure

```
DevPilot/
├── frontend/                        # Next.js application
│   ├── app/
│   │   ├── page.tsx                 # Landing page
│   │   ├── login/page.tsx           # GitHub login page
│   │   ├── dashboard/page.tsx       # Repository dashboard
│   │   └── chat/[id]/page.tsx       # Chat with a repository
│   ├── hooks/
│   │   ├── use-current-user.ts      # Fetch logged-in user
│   │   └── use-repositories.ts      # Fetch, sync, index repos
│   ├── lib/
│   │   └── api.ts                   # BACKEND_URL constant
│   └── next.config.ts               # API rewrites config
│
├── backend/                         # Spring Boot application
│   ├── src/main/java/com/example/
│   │   ├── controller/
│   │   │   ├── AuthController.java  # /api/auth endpoints
│   │   │   └── RepoController.java  # /api/repositories endpoints
│   │   ├── service/
│   │   │   ├── github/
│   │   │   │   ├── GithubApiClient.java     # GitHub REST API calls
│   │   │   │   └── RepositoryService.java   # Sync + manage repos
│   │   │   ├── indexing/
│   │   │   │   ├── IndexingService.java     # Orchestrates indexing
│   │   │   │   ├── CodeChunker.java         # Splits files into chunks
│   │   │   │   ├── CodeFileFilter.java      # Filters non-code files
│   │   │   │   └── EmbeddingService.java    # Calls OpenAI embeddings
│   │   │   └── rag/
│   │   │       ├── RagService.java          # Builds prompt + calls GPT
│   │   │       └── VectorSearchService.java # Embeds query + searches
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── RepositoryEntity.java
│   │   │   └── CodeChunks.java
│   │   ├── configuration/
│   │   │   ├── SecurityConfig.java   # OAuth2 + session config
│   │   │   └── CorsConfig.java       # Cross-origin config
│   │   └── security/
│   │       ├── gitHubOauth2UserService.java
│   │       └── AppUserPrinciple.java
│   ├── Dockerfile                    # Multi-stage Docker build
│   └── src/main/resources/
│       └── application.yaml          # All configuration
│
├── docker/
│   └── postgres/
│       └── init-extention.sql        # pgvector + hstore + uuid-ossp
├── render.yaml                        # Render Blueprint config
└── docker-compose.yml                 # Local dev PostgreSQL
```

---

## ⚙️ How RAG Works in DevPilot

```
INDEXING (one-time per repo)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
GitHub Repo
    └─→ Fetch all files (GitHub API)
            └─→ Filter: skip binaries, large files (>100KB)
                    └─→ Chunk: split into 800-char pieces, 100-char overlap
                            └─→ Embed: OpenAI text-embedding-3-small → float[1536]
                                    └─→ Store: PostgreSQL code_chunks (vector column)

QUERYING (every chat message)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
User Question
    └─→ Embed question (text-embedding-3-small)
            └─→ pgvector cosine similarity search (topK=5)
                    └─→ Retrieve top 5 relevant code chunks
                            └─→ Build prompt: [system + chunks + question]
                                    └─→ GPT-4o-mini → Answer
```

---

## 🚀 Getting Started Locally

### Prerequisites
- Java 21+
- Node.js 18+ / pnpm
- Docker (for local PostgreSQL with pgvector)
- GitHub OAuth App ([create one here](https://github.com/settings/developers))

### 1. Clone the Repository
```bash
git clone https://github.com/Skhubebali/Devpilot.git
cd DevPilot
```

### 2. Start Local PostgreSQL with pgvector
```bash
docker-compose up -d
```

Then connect to postgres and run:
```sql
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

### 3. Configure Backend Environment

Create `backend/.env` (never commit this!):
```env
DB_URL=jdbc:postgresql://localhost:5433/devpiolet?sslmode=disable
DB_USERNAME=postgres
DB_PASSWORD=postgres

GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

OPENAI_API_KEY=your_openai_api_key

TOKEN_ENCRYPTOR_PASSWORD=any-strong-password-here
TOKEN_ENCRYPTOR_SALT=deadbeefcafebabe

FRONTEND_URL=http://localhost:3000
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### 4. Run the Backend
```bash
cd backend
./mvnw spring-boot:run
# Starts on http://localhost:8081
```

### 5. Configure Frontend Environment

Create `frontend/.env.local`:
```env
NEXT_PUBLIC_BACKEND_URL=http://localhost:8081
```

### 6. Run the Frontend
```bash
cd frontend
pnpm install
pnpm dev
# Starts on http://localhost:3000
```

### 7. Configure GitHub OAuth App (Local)
In [GitHub Developer Settings → OAuth Apps](https://github.com/settings/developers):
- **Homepage URL**: `http://localhost:3000`
- **Authorization callback URL**: `http://localhost:8081/login/oauth2/code/github`

---

## 🌍 Production Deployment

### Step 1: Neon (Database)
1. Create a project at [neon.tech](https://neon.tech)
2. In **SQL Editor**, run:
   ```sql
   CREATE EXTENSION IF NOT EXISTS vector;
   CREATE EXTENSION IF NOT EXISTS hstore;
   CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
   ```
3. Copy the **pooler connection string** (ensure `sslmode=require`)

### Step 2: Render (Backend)
1. New Web Service → connect GitHub repo
2. **Root Directory**: `backend` · **Runtime**: Docker
3. Set environment variables:

   | Key | Value |
   |:---|:---|
   | `PORT` | `8081` |
   | `DB_URL` | `jdbc:postgresql://<neon-host>/devpilot?sslmode=require` |
   | `DB_USERNAME` | `neondb_owner` |
   | `DB_PASSWORD` | `your-neon-password` |
   | `GITHUB_CLIENT_ID` | From GitHub OAuth App |
   | `GITHUB_CLIENT_SECRET` | From GitHub OAuth App |
   | `OPENAI_API_KEY` | Your OpenAI/OpenRouter key |
   | `TOKEN_ENCRYPTOR_PASSWORD` | Any strong secret |
   | `TOKEN_ENCRYPTOR_SALT` | 16-char hex string |
   | `FRONTEND_URL` | `https://your-app.vercel.app` |
   | `CORS_ALLOWED_ORIGINS` | `https://your-app.vercel.app` |

4. Deploy and copy your Render URL

### Step 3: Vercel (Frontend)
1. Import repo at [vercel.com](https://vercel.com) → **Root Directory**: `frontend`
2. Set environment variable:
   - `NEXT_PUBLIC_BACKEND_URL` = `https://your-backend.onrender.com`
3. Deploy and copy your Vercel URL

### Step 4: Update GitHub OAuth App
In [GitHub Developer Settings](https://github.com/settings/developers):
- **Homepage URL**: `https://your-app.vercel.app`
- **Authorization callback URL**: `https://your-backend.onrender.com/login/oauth2/code/github`

### Step 5: Update Render with Vercel URL
Update `FRONTEND_URL` and `CORS_ALLOWED_ORIGINS` on Render with your Vercel domain, then redeploy.

---

## 🔌 API Reference

### Auth
| Method | Endpoint | Auth | Description |
|:---|:---|:---:|:---|
| `GET` | `/api/auth/login-url` | ❌ | Returns GitHub OAuth URL |
| `GET` | `/api/auth/me` | ✅ | Returns current user |

### Repositories
| Method | Endpoint | Auth | Description |
|:---|:---|:---:|:---|
| `GET` | `/api/repositories` | ✅ | List all repos from DB |
| `GET` | `/api/repositories/{id}` | ✅ | Get single repo |
| `POST` | `/api/repositories/sync/{userId}` | ✅ | Sync from GitHub |
| `POST` | `/api/repositories/{id}/index` | ✅ | Start AI indexing |
| `POST` | `/api/repositories/{id}/ask` | ✅ | Ask a question (RAG) |

---

## 🗄️ Database Schema

### `users`
| Column | Type | Description |
|:---|:---|:---|
| `id` | UUID PK | Internal user ID |
| `github_id` | BIGINT UNIQUE | GitHub user ID |
| `uname` | VARCHAR | GitHub username |
| `display_name` | VARCHAR | GitHub display name |
| `avatar_url` | VARCHAR | Avatar URL |
| `access_token` | TEXT | AES-encrypted GitHub token |
| `token_scopes` | VARCHAR | OAuth scopes |

### `repositories`
| Column | Type | Description |
|:---|:---|:---|
| `id` | UUID PK | Internal repo ID |
| `user_id` | UUID FK | Owner user |
| `github_repo_id` | BIGINT UNIQUE | GitHub repo ID |
| `name` | VARCHAR | Repo name |
| `fullname` | VARCHAR | `owner/repo` |
| `index_status` | ENUM | PENDING / INDEXING / READY / FAILED |
| `chunk_count` | INT | Number of indexed chunks |
| `file_processed` | INT | Files indexed so far |
| `total_files` | INT | Total files to index |

### `code_chunks`
| Column | Type | Description |
|:---|:---|:---|
| `id` | UUID PK | Chunk ID |
| `repository_id` | UUID FK | Parent repository |
| `file_path` | VARCHAR | Source file path |
| `chunk_index` | INT | Position within file |
| `content` | TEXT | Raw code content |
| `language` | VARCHAR | Detected language |
| `embedding` | `vector(1536)` | OpenAI embedding vector |

---

## 🔐 Environment Variables Reference

### Backend
| Variable | Required | Description |
|:---|:---:|:---|
| `DB_URL` | ✅ | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | ✅ | Database username |
| `DB_PASSWORD` | ✅ | Database password |
| `GITHUB_CLIENT_ID` | ✅ | GitHub OAuth App Client ID |
| `GITHUB_CLIENT_SECRET` | ✅ | GitHub OAuth App Client Secret |
| `OPENAI_API_KEY` | ✅ | OpenAI or OpenRouter API key |
| `TOKEN_ENCRYPTOR_PASSWORD` | ✅ | Password for AES token encryption |
| `TOKEN_ENCRYPTOR_SALT` | ✅ | Salt for AES token encryption |
| `FRONTEND_URL` | ✅ | Frontend URL for OAuth redirect |
| `CORS_ALLOWED_ORIGINS` | ✅ | Comma-separated allowed origins |
| `PORT` | ⚡ | Server port (default: `8081`) |

### Frontend
| Variable | Required | Description |
|:---|:---:|:---|
| `NEXT_PUBLIC_BACKEND_URL` | ✅ | Full URL of the Spring Boot backend |

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m "Add my feature"`
4. Push to branch: `git push origin feature/my-feature`
5. Open a Pull Request

> ⚠️ **Never commit `.env` files.** Use `.env.example` as a template only.

---

## 📄 License

This project is licensed under the MIT License.

---

<div align="center">

**Built with ❤️ using Spring Boot · Next.js · OpenAI · pgvector**

[🌐 Live Demo](https://devpilot-steel.vercel.app) · [🐛 Report Bug](https://github.com/Skhubebali/Devpilot/issues) · [✨ Request Feature](https://github.com/Skhubebali/Devpilot/issues)

</div>
