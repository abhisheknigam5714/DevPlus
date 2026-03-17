# ⚡ DevPlus — Team Task & GitHub Contribution Tracker

> A full stack team productivity platform where Managers assign tasks to developers and track their real GitHub commit activity in real-time via webhooks.

---

## 🧰 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.x, Spring Security, Spring Data JPA |
| Frontend | React.js, React Bootstrap, Chart.js, Axios |
| Database | MySQL 8.0 |
| Auth | JWT (JSON Web Token) |
| Webhooks | GitHub Webhooks → Spring Boot endpoint |
| Scheduler | Spring `@Scheduled` + JavaMailSender |
| Build Tool | Maven (Backend), npm (Frontend) |

---

## 🚀 Features

- ✅ **Role-Based Access Control** — Manager, Team Lead, Member with different permissions
- ✅ **Project Management** — Create projects linked to GitHub repos, add/remove members
- ✅ **Task Management** — Assign tasks with priority, due date, and Kanban status tracking
- ✅ **GitHub Webhook Integration** — Captures every commit in real-time on push event
- ✅ **Contribution Dashboard** — Per-member commit stats with Chart.js bar charts
- ✅ **Live Commit Feed** — Shows commit message, branch, author, and timestamp
- ✅ **Webhook Setup Page** — Manager copies webhook URL directly to paste in GitHub
- ✅ **Automated Weekly Email Report** — Every Monday 9 AM, Manager receives HTML team summary via Spring Scheduler

---

## 📁 Project Structure

```
DevPlus/
├── devplus-backend/
│   └── src/main/java/com/devplus/
│       ├── controller/        → REST API endpoints
│       ├── model/             → JPA entities (User, Project, Task, CommitLog)
│       ├── repository/        → Spring Data JPA repositories
│       ├── service/           → Business logic + EmailService + WeeklyReportScheduler
│       ├── security/          → JwtUtil, JwtFilter, SecurityConfig
│       ├── dto/               → Request/Response DTOs
│       └── DevPlusApplication.java
│
└── devplus-frontend/
    └── src/
        ├── components/        → Navbar, Sidebar, TaskCard, CommitFeed, ContributionChart
        ├── pages/             → Login, Register, Dashboard, ProjectDetail, TaskBoard, MemberStats, WebhookSetup
        ├── services/          → Axios API calls (auth, project, task, commit)
        ├── context/           → AuthContext (global JWT state)
        └── App.jsx
```

---

## ⚙️ Backend Setup

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.8+

### Step 1 — Create MySQL database
```sql
CREATE DATABASE devplus_db;
```

### Step 2 — Configure `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/devplus_db
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
jwt.secret=devplus_secret_key_2024
jwt.expiration=86400000
server.port=8080

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Step 3 — Set environment variables for email

```bash
# Windows
set MAIL_USERNAME=your_gmail@gmail.com
set MAIL_PASSWORD=your_gmail_app_password

# Mac/Linux
export MAIL_USERNAME=your_gmail@gmail.com
export MAIL_PASSWORD=your_gmail_app_password
```

> ⚠️ Use a **Gmail App Password** — not your regular password.
> Go to: **Google Account → Security → 2-Step Verification → App Passwords → Generate**

### Step 4 — Run the backend
```bash
cd devplus-backend
mvn spring-boot:run
```

Backend runs at: `http://localhost:8080`

---

## 🖥️ Frontend Setup

### Prerequisites
- Node.js 18+
- npm 9+
- Backend running on `http://localhost:8080`

### Step 1 — Install dependencies
```bash
cd devplus-frontend
npm install
```

### Step 2 — Start the frontend
```bash
npm start
```

Frontend runs at: `http://localhost:3000`

---

## 🔗 API Endpoints

### Auth (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register with role selection |
| POST | `/api/auth/login` | Login → returns JWT token |

### Projects (Protected)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/projects` | Get all manager projects |
| POST | `/api/projects` | Create project |
| GET | `/api/projects/{id}` | Project detail |
| DELETE | `/api/projects/{id}` | Delete project |
| POST | `/api/projects/{id}/members` | Add member to project |
| GET | `/api/projects/{id}/members` | List project members |
| DELETE | `/api/projects/{id}/members/{uid}` | Remove member |

### Tasks (Protected)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/projects/{id}/tasks` | All tasks in project |
| POST | `/api/projects/{id}/tasks` | Create task |
| PUT | `/api/tasks/{id}` | Update task or status |
| DELETE | `/api/tasks/{id}` | Delete task |

### Commits (Protected)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/projects/{id}/commits` | All commits in project |
| GET | `/api/projects/{id}/commits/stats` | Per-member commit stats |
| GET | `/api/projects/{id}/commits/member/{uid}` | Single member commits |

### Webhook (Public — called by GitHub)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/webhook/github/{projectId}` | Receives GitHub push event |

---

## 🔔 GitHub Webhook Setup

1. Go to your GitHub repository
2. Click **Settings → Webhooks → Add webhook**
3. Set **Payload URL**:
   ```
   http://your-server-url/api/webhook/github/{projectId}
   ```
   > For local testing, use [ngrok](https://ngrok.com) to expose localhost:
   > ```bash
   > ngrok http 8080
   > ```
   > Use the generated ngrok URL as the payload URL
4. Set **Content type** → `application/json`
5. Select **Just the push event**
6. Click **Add webhook** ✅

> ⚠️ Make sure each member's **GitHub username** is entered correctly during registration — this is how commits are matched to users automatically.

---

## 👤 Roles & Permissions

| Role | What They Can Do |
|------|-----------------|
| `MANAGER` | Create projects, add/remove members, assign tasks, view all stats, receive weekly email |
| `TEAM_LEAD` | Assign tasks to members, view their team's commit stats |
| `MEMBER` | View own tasks, update own task status, view own commit history |

---

## 🗺️ Frontend Pages

| Route | Page | Access |
|-------|------|--------|
| `/login` | Login | Public |
| `/register` | Register with role | Public |
| `/dashboard` | Role-based home | All |
| `/projects` | Project list | Manager |
| `/projects/:id` | Project detail — 3 tabs (Tasks, Members, Commits) | Manager, Team Lead |
| `/projects/:id/tasks` | Kanban board | Manager, Team Lead |
| `/projects/:id/stats` | Contribution bar charts | Manager, Team Lead |
| `/projects/:id/webhook` | Webhook URL copy page | Manager |
| `/my-tasks` | My assigned tasks | Member |

---

## 📧 Weekly Email Report

- Fires automatically every **Monday at 9:00 AM** via Spring `@Scheduled`
- Sent to every Manager for each of their projects
- HTML email includes:
  - Per-member commit count for the past 7 days
  - Tasks assigned vs completed per member
  - Top contributor of the week
  - Inactive members alert (0 commits)

---

## 🤝 Author

**Abhishek Nigam**
- GitHub: [@abhisheknigam5714](https://github.com/abhisheknigam5714)
- LinkedIn: [abhishek-nigam100](https://linkedin.com/in/abhishek-nigam100)
- Email: an5714170@gmail.com
