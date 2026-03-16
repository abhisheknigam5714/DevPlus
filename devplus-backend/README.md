# DevPlus - Team Task & GitHub Contribution Tracker

A comprehensive full-stack application for managing team tasks and tracking GitHub commit activity via webhooks.

## Tech Stack

### Backend
- Java Spring Boot 3.x
- Spring Data JPA (Hibernate)
- Spring Security (JWT-based authentication)
- Spring Mail (for weekly reports)
- MySQL Database
- Maven Build Tool

### Frontend
- React.js (Functional Components + Hooks)
- React Bootstrap + CSS
- Axios for API calls
- React Router DOM v6
- Chart.js for visualizations

## Features

### Role-Based Access Control
- **Manager**: Create/manage projects, add team members, view all stats, receive weekly email reports
- **Team Lead**: Assign tasks to members, view team stats
- **Member**: View assigned tasks, update task status, see own commit history

### Key Features
- JWT-based authentication
- GitHub webhook integration for real-time commit tracking
- Kanban-style task board
- Contribution dashboard with charts
- Weekly automated email reports (every Monday at 9 AM)

## Prerequisites

- Java 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+
- Gmail account (for email reports)

## Setup Instructions

### 1. Database Setup

```sql
CREATE DATABASE devplus_db;
```

### 2. Environment Variables

Set the following environment variables for email functionality:

```bash
# Linux/Mac
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

# Windows (PowerShell)
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="your-app-password"
```

**Note**: For Gmail, you need to use an App Password, not your regular password. Generate one at https://myaccount.google.com/apppasswords

### 3. Configure application.properties

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/devplus_db
spring.datasource.username=root
spring.datasource.password=your_mysql_password

# JWT Secret (change in production)
jwt.secret=your-secure-secret-key
```

### 4. Build and Run

```bash
# Navigate to backend directory
cd devplus-backend

# Build with Maven
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will run on http://localhost:8080

## GitHub Webhook Setup

1. Go to your GitHub repository settings
2. Navigate to Webhooks → Add webhook
3. Set the Payload URL to:
   ```
   http://your-server:8080/api/webhook/github/{projectId}
   ```
   Replace `{projectId}` with your actual project ID
4. Content type: `application/json`
5. Secret: Leave empty (not required)
6. Which events: Select "Just the push event"
7. Click "Add webhook"

**For local testing**, use a service like ngrok:
```bash
ngrok http 8080
# Then use the ngrok URL: https://xxx.ngrok.io/api/webhook/github/{projectId}
```

## API Endpoints

### Authentication (Public)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/auth/me` - Get current user info

### Projects (Protected)
- `GET /api/projects` - List projects
- `POST /api/projects` - Create project (Manager only)
- `GET /api/projects/{id}` - Get project details
- `DELETE /api/projects/{id}` - Delete project (Manager only)
- `POST /api/projects/{id}/members` - Add member
- `GET /api/projects/{id}/members` - List members
- `DELETE /api/projects/{id}/members/{uid}` - Remove member

### Tasks (Protected)
- `GET /api/projects/{id}/tasks` - Get all project tasks
- `POST /api/projects/{id}/tasks` - Create task
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task
- `PATCH /api/tasks/{id}/status` - Update task status
- `GET /api/my-tasks` - Get current user's tasks

### Commits (Protected)
- `GET /api/projects/{id}/commits` - Get project commits
- `GET /api/projects/{id}/commits/stats` - Get member statistics
- `GET /api/projects/{id}/commits/member/{uid}` - Get member commits

### Webhook (Public)
- `POST /api/webhook/github/{projectId}` - Receive GitHub push events

## User Registration

When registering, include these fields:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "MANAGER",
  "githubUsername": "johndoe"
}
```

Roles: `MANAGER`, `TEAM_LEAD`, `MEMBER`

## Weekly Email Reports

The system sends automated weekly reports every Monday at 9:00 AM to all managers. Reports include:
- Project summary with commit counts
- Task completion statistics
- Top contributors
- Inactive members alerts
- Member comparison tables

## Troubleshooting

### Database Connection Issues
- Ensure MySQL is running
- Verify credentials in application.properties
- Check if database exists

### Email Not Sending
- Verify MAIL_USERNAME and MAIL_PASSWORD environment variables
- Ensure Gmail App Password is used (not regular password)
- Check if 2FA is enabled on Gmail account

### Webhook Not Working
- Verify the webhook URL is accessible from the internet
- Use ngrok for local development
- Check server logs for incoming requests

## License

MIT License