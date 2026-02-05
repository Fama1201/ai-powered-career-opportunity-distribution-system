# 🤖 AI-Powered Career Opportunity Distribution System (Jobify CVUT)

**Jobify CVUT** is a comprehensive ecosystem connecting **FIT ČVUT** students with career opportunities.  
The system integrates an intelligent **Discord Bot** for real-time interaction and a robust **Web Platform** for in-depth management.

Powered by **Spring Boot**, **OpenAI (GPT)**, and **PostgreSQL**, it serves two main user groups:
- **Students** seeking jobs and internships
- **HR / Recruiters** posting and managing opportunities

---

## 📌 Unified Features

### 🌐 Web Platform
- Student Dashboard with application tracking and profile completeness
- HR Portal for job posting and candidate management
- Smart CV Builder (skills, experience, education)
- AI-based Job Matching Engine
- Analytics and charts using Chart.js

### 🤖 Discord Bot
- Interactive onboarding directly in Discord
- Instant notifications for new job matches
- PDF resume parsing and automatic data extraction
- GPT-powered career advisor via chat commands

### 🧠 Core Technology
- AI matching using EXPERTS.AI data and OpenAI
- Centralized PostgreSQL database shared by bot and web app
- Feedback loop based on user interactions

---

## 📁 Project Structure

The project follows a Spring Boot monolith architecture (Backend + Static Frontend):

```text
src/
├── main/
│   ├── java/
│   │   └── com/jobifycvut/
│   │       ├── backend/
│   │       │   ├── controller/
│   │       │   ├── service/
│   │       │   ├── model/
│   │       │   ├── repository/
│   │       │   └── config/
│   │       └── bot/
│   └── resources/
│       ├── application.properties
│       └── static/
│           ├── css/
│           ├── js/
│           ├── pages/
│           └── index.html
```

---

## ⚙️ Installation & Setup

### 1. Requirements
- Java JDK 17+
- PostgreSQL 14+
- Discord Bot Token
- OpenAI API Key

### 2. Environment Configuration
Configure `src/main/resources/application.properties` or environment variables:

```properties
DB_URL=jdbc:postgresql://localhost:5432/jobify_db
DB_USER=postgres
DB_PASSWORD=your_password

DISCORD_TOKEN=your_discord_bot_token
OPENAI_API_KEY=sk-proj-...
```

### 3. Database Initialization
```bash
psql -U postgres -d jobify_db -f database-schema.sql
```

### 4. Running the Application
```bash
./gradlew bootRun
# or
java -jar build/libs/discord-ai-bot-java-0.0.1-SNAPSHOT.jar
```

---

## 🚀 Usage Guide

### 🌐 Web Portal
- Landing Page: http://localhost:8080/
- Student Login: /pages/auth/login.html
- HR Login: /pages/auth/hr-signup.html

### 🤖 Discord Commands

| Command | Description |
|-------|------------|
| !start | Begin onboarding |
| !ask <text> | Ask GPT for career advice |
| !fetch | Fetch new job matches |
| !status | Check system status |

---

## 📌 Roadmap
- WebSockets for real-time updates
- Localization (EN / CZ)
- Mobile responsiveness
- OAuth2 login (Google / GitHub)

---

## 🙋‍♂️ Support & Contact

- francisco.molina.antonio@gmail.com
- ErdemYusufEmre@gmail.com
- emreyuce228@gmail.com

---

## 👨‍💻 Authors

- Francisco Antonio Molina Alava – 🧠 Team Leader & Main Developer Led architecture, backend/frontend integration, database design, and CI/CD pipeline.

- Yunus Emre Yuce – 💻 Main Backend Developer & Database Spearheaded core backend development, designed the initial database schema, and managed project documentation.

- Yusuf Emre Erdem – 🖥️ Main Frontend Developer & Backend Logic Led the frontend architecture and implementation, contributed to backend logic, unit testing, and documentation support.

- Emir Orhan – 🖥️ Frontend Programmer & Research Developed frontend components, assisted in data validation, and conducted initial requirements gathering.

- Abdul Rahman Asaad Mourad – ⚙️ Backend Developer & Quality Assurance Implemented backend features, performed static code analysis, and ensured code quality.

- Karim Gamal Aziz Georgy Habib – 📄 Documentation Official project documentation formatting and structuring.

- Maya Hussein Abdulhalem Elkadi – 📄 Documentation Supported the documentation effort across various project components.

---

## 📄 License

This project is licensed under the MIT License.
