Claro Cisco. Aquí tienes el README.md actualizado con los nuevos roles que me pediste.He integrado los cambios de forma profesional en la sección de "Authors", manteniendo el resto de la documentación técnica intacta.Markdown# 🤖 AI-Powered Career Opportunity Distribution System (Jobify CVUT)

**Jobify CVUT** is a comprehensive ecosystem connecting **FIT ČVUT** students with career opportunities. The system integrates an intelligent **Discord Bot** for real-time interaction and a robust **Web Platform** for in-depth management. 

Powered by **Spring Boot**, **OpenAI (GPT)**, and **PostgreSQL**, it serves two main user groups: **Students** (seeking jobs/internships) and **HR/Recruiters** (posting opportunities).

---

## 📌 Unified Features

### 🌐 Web Platform (New!)
- **Student Dashboard:** Visual progress tracking, job application history, and profile completeness stats.
- **HR Portal:** Companies can post new jobs, manage candidate pipelines, and view analytics.
- **Smart CV Builder:** Web interface to edit skills, experience, and education details.
- **Job Matching Engine:** View matched opportunities filtered by the AI algorithm.
- **Analytics:** Charts and insights on application trends (via `Chart.js`).

### 🤖 Discord Bot
- **Interactive Onboarding:** Seamless sign-up flow directly within Discord.
- **Instant Notifications:** Receive alerts for new job matches via DM.
- **PDF Resume Parsing:** Upload a CV file in Discord, and the system extracts data automatically.
- **GPT Career Advisor:** Ask questions like *"How can I improve my CV?"* directly in chat.

### 🧠 Core Technology
- **AI Matching:** Uses EXPERTS.AI data and OpenAI to match profiles with job descriptions.
- **Persistent Data:** PostgreSQL database shared between the Bot and the Web App.
- **Feedback Loop:** System learns from user ratings and interactions.

---

## 📁 Extended Project Structure

The project follows a standard Spring Boot Monolith structure (Backend + Static Frontend).

```text
src/
├── main/
│   ├── java/
│   │   └── com/jobifycvut/
│   │       ├── backend/
│   │       │   ├── controller/      # Web API & Page Controllers (Dashboard, HR, Auth)
│   │       │   ├── service/         # Business Logic (MatchService, AiService)
│   │       │   ├── model/           # JPA Entities (Student, Opportunity, HrUser)
│   │       │   ├── repository/      # Spring Data JPA Repositories
│   │       │   └── config/          # Security & App Config
│   │       └── bot/                 # Discord Bot Logic (Listeners, Commands)
│   │
│   └── resources/
│       ├── application.properties   # Main configuration
│       └── static/                  # Web Frontend
│           ├── css/                 # Stylesheets
│           ├── js/                  # JavaScript logic (Charts, API calls)
│           ├── pages/               # HTML Views
│           │   ├── student/         # Student Dashboard Views
│           │   ├── hr/              # HR/Company Views
│           │   └── auth/            # Login/Register Pages
│           └── index.html           # Landing Page
⚙️ Installation & Setup1. RequirementsJava JDK 17+PostgreSQL 14+Discord Bot TokenOpenAI API Key2. Environment ConfigurationConfigure src/main/resources/application.properties or set environment variables:Properties# Database
DB_URL=jdbc:postgresql://localhost:5432/jobify_db
DB_USER=postgres
DB_PASSWORD=your_password

# External APIs
DISCORD_TOKEN=your_discord_bot_token
OPENAI_API_KEY=sk-proj-...
3. Database InitializationCreate the database and run the schema script:Bashpsql -U postgres -d jobify_db -f database-schema.sql
4. Running the ApplicationThe command below starts both the Web Server (Port 8080) and the Discord Bot.Bash# Using Gradle Wrapper
./gradlew bootRun

# Or using the built JAR
java -jar build/libs/discord-ai-bot-java-0.0.1-SNAPSHOT.jar
🚀 Usage Guide🌐 Accessing the Web PortalOnce the application is running, open your browser:Landing Page: http://localhost:8080/Student Login: http://localhost:8080/pages/auth/login.htmlHR Login: http://localhost:8080/pages/auth/hr-signup.htmlTip: You can log in using the same credentials created via the Discord Bot or sign up directly on the web.🤖 Discord CommandsCommandDescription!startBegins the onboarding process (creates profile).!ask <text>Ask GPT for career advice (e.g., "What skills do I lack?").!fetchForce the system to look for new job matches immediately.!statusCheck if the Bot and Backend are online.📌 Roadmap & Future Plans🔔 Real-time Web Sockets: Push notifications on the web dashboard.🌍 Localization: Toggle between English (EN) and Czech (CZ).📱 Mobile Responsiveness: Improved UI for mobile browsers.🔐 OAuth2: Login with Google or GitHub.🙋‍♂️ Support & ContactFor help, bug reports, or feature requests, please contact the development team:Email: francisco.molina.antonio@gmail.comEmail: ErdemYusufEmre@gmail.comEmail: emreyuce228@gmail.comGitLab Issues: Open a ticket👨‍💻 Authors and AcknowledgmentThis project was developed by a collaborative team of students from FIT ČVUT.Francisco Antonio Molina Alava – 🧠 Team Leader & Main DeveloperLed architecture, backend/frontend integration, database design, and CI/CD pipeline.Yunus Emre Yuce – 💻 Main Backend Developer & DatabaseSpearheaded core backend development, designed the initial database schema, and managed project documentation.Yusuf Emre Erdem – 🎨 Main Frontend Developer & Backend LogicLed the frontend architecture and implementation, contributed to backend logic, unit testing, and documentation support.Emir Orhan – 🖥️ Frontend Programmer & ResearchDeveloped frontend components, assisted in data validation, and conducted initial requirements gathering.Abdul Rahman Asaad Mourad – ⚙️ Backend Developer & Quality AssuranceImplemented backend features, performed static code analysis, and ensured code quality.Karim Gamal Aziz Georgy Habib – 📄 DocumentationOfficial project documentation formatting and structuring.Maya Hussein Abdulhalem Elkadi – 📄 DocumentationSupported the documentation effort across various project components.We thank all contributors for their collaboration in building this AI-powered ecosystem.📄 LicenseThis project is licensed under the MIT License. See the LICENSE file for details.
