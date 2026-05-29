# ? AI Code Review Platform

A production-ready Spring Boot backend that lets developers submit code snippets or GitHub Pull Requests and receive AI-powered code reviews.

## ? Features

- ? JWT-based authentication (register/login)
- ? Code snippet management (create, list, delete)
- ? AI-powered code review via OpenRouter API
- ? GitHub PR review integration
- ? Comment system on reviews
- ?? Tag system for snippets
- ? Pagination support
- ?? User ownership validation
- ? Rate limiting (5 requests/min per IP)
- ? Structured JSON review response
- ? Request logging

## ?? Tech Stack

| Technology | Purpose |
|-----------|---------|
| Java 17 | Programming language |
| Spring Boot 3.5 | Backend framework |
| Spring Security + JWT | Authentication |
| Spring Data JPA + Hibernate | ORM |
| PostgreSQL | Database |
| Docker & Docker Compose | Containerization |
| OpenRouter API | AI Integration |
| Bucket4j | Rate limiting |
| Maven | Build tool |

## ? Getting Started

### Prerequisites

- Java 17+
- Docker & Docker Compose
- OpenRouter API Key (free at [openrouter.ai](https://openrouter.ai))
- GitHub Personal Access Token

### Installation

1. Clone the repository
```bash
git clone https://github.com/yourusername/code-review-ai.git
cd code-review-ai
```

2. Copy environment file
```bash
cp .env.example .env
```

3. Fill in your API keys in `.env`
   OPENROUTER_API_KEY=your_openrouter_api_key
   GITHUB_TOKEN=your_github_token

4. Run with Docker Compose
```bash
docker compose up
```

The application will start at `http://localhost:8080`

## ? API Endpoints

### Auth
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | ? | Register new user |
| POST | `/api/auth/login` | ? | Login and get JWT token |

### Snippets
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/snippets` | ? | Create snippet |
| GET | `/api/snippets?page=0&size=10` | ? | List my snippets (paginated) |
| GET | `/api/snippets/{id}` | ? | Get snippet by id |
| DELETE | `/api/snippets/{id}` | ? | Delete snippet |

### Reviews
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/reviews/snippet/{id}` | ? | Generate AI review |
| GET | `/api/reviews/snippet/{id}` | ? | Get review |
| GET | `/api/reviews/snippet/{id}/parsed` | ? | Get structured JSON review |

### GitHub PR
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/github/review` | ? | Review a GitHub PR |

### Comments
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/comments/snippet/{id}` | ? | Add comment to review |
| GET | `/api/comments/snippet/{id}` | ? | Get comments |

### Tags
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/tags/snippet/{id}` | ? | Add tag to snippet |
| GET | `/api/tags/snippet/{id}` | ? | Get tags |

## ? Example Request/Response

### Register
```json
POST /api/auth/register
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "123456"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1...",
  "username": "johndoe",
  "email": "john@example.com"
}
```

### Create Snippet & Get AI Review
```json
POST /api/snippets
{
  "title": "Bubble Sort",
  "code": "public void bubbleSort(int[] arr) { ... }",
  "language": "Java"
}

POST /api/reviews/snippet/1
Response:
{
  "summary": "The code implements bubble sort correctly...",
  "bugs": [],
  "securityIssues": [],
  "performanceIssues": ["O(n² time complexity"],
  "suggestions": ["Consider using Arrays.sort() for better performance"],
  "improvedCode": "..."
}
```

### GitHub PR Review
```json
POST /api/github/review
{
  "prUrl": "https://github.com/username/repo/pull/1"
}

Response:
{
  "prUrl": "https://github.com/username/repo/pull/1",
  "repository": "username/repo",
  "prNumber": 1,
  "summary": "This PR adds a new feature...",
  "bugs": [],
  "securityIssues": [],
  "performanceIssues": [],
  "suggestions": ["Add unit tests"]
}
```

## ?? Project Structure
src/main/java/com/example/codereviewai/
??? controller/          # REST controllers
??? service/             # Business logic
??? repository/          # Data access layer
??? entity/              # JPA entities
??? dto/
?   ??? request/         # Request DTOs with validation
?   ??? response/        # Response DTOs
??? exception/           # Custom exceptions & global handler
??? security/            # JWT filter & security config
??? config/              # Rate limiting config

## ? Running Tests

```bash
./mvnw test
```

## ? Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `OPENROUTER_API_KEY` | OpenRouter API key for AI reviews | ? |
| `GITHUB_TOKEN` | GitHub personal access token | ? |

## ? Notes

- API keys are never stored in code or committed to Git
- All endpoints except `/api/auth/**` require JWT token
- Rate limit: 5 requests per minute per IP
- Free OpenRouter models may have rate limits; paid models recommended for production