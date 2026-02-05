# Domain Model - Jobify CVUT

## Entity Relationships

### Core Entities

1. **User** (`users` table)
   - Primary authentication entity
   - Fields: id, email, password, first_name, last_name, role, is_active, created_at, last_login_at
   - Relationships:
     - 1:N → StudentEntity (via user_id)
     - 1:N → JobApplicationEntity
     - 1:N → SavedJob

2. **StudentEntity** (`student` table)
   - Student profile with CV and skills
   - Fields: id, name, email, skills, career_interest, discord_id, cv_text, user_id (FK)
   - Relationships:
     - N:1 → User (via user_id)
     - 1:N → NotificationEntity
     - 1:N → InteractionEntity

3. **HrUser** (`hr` table)
   - HR recruiter accounts
   - Fields: id, email, password_hash, full_name, company_name, created_at
   - Relationships:
     - 1:N → HrToken

4. **Company** (`company` table)
   - Company information
   - Fields: id, company_name (unique)
   - Relationships:
     - 1:N → Opportunity
     - 1:N → NotificationEntity

5. **Opportunity** (`opportunities` table)
   - Job postings/opportunities
   - Fields: id, title, description, requirements, location, company_id (FK), created_at, job_type, application_deadline, url, wage, home_office, benefits, formal_requirements, technical_requirements, contact_person, company (legacy), opportunity_id, discord_id
   - Relationships:
     - N:1 → Company (via company_id)
     - 1:N → JobApplicationEntity
     - 1:N → SavedJob
     - 1:N → InteractionEntity

6. **JobApplicationEntity** (`job_applications` table)
   - Student job applications
   - Fields: id, user_id (FK), opportunity_id (FK), status, applied_at, notes
   - Relationships:
     - N:1 → User
     - N:1 → Opportunity
   - Unique constraint: (user_id, opportunity_id)

7. **SavedJob** (`saved_jobs` table)
   - Saved opportunities by students
   - Fields: id, user_id (FK), opportunity_id (FK), saved_at
   - Relationships:
     - N:1 → User
     - N:1 → Opportunity
   - Unique constraint: (user_id, opportunity_id)

8. **NotificationEntity** (`notifications` table)
   - User notifications
   - Fields: id, student_id (FK), company_id (FK), message, type, read, created_at
   - Relationships:
     - N:1 → StudentEntity
     - N:1 → Company

9. **InteractionEntity** (`interactions` table)
   - AI chat interactions
   - Fields: id, student_id (FK), opportunity_id (FK), action, prompt, response, created_at
   - Relationships:
     - N:1 → StudentEntity
     - N:1 → Opportunity

10. **Feedback** (`feedback` table)
    - User feedback
    - Fields: id, feedback_text, stars, discord_id

11. **HrToken** (`hr_tokens` table)
    - HR API tokens
    - Fields: id, hr_id (FK), token
    - Relationships:
      - N:1 → HrUser

## Key Business Rules

1. **Matching Algorithm**:
   - Skills overlap between student.skills and opportunity.technical_requirements
   - Career interest match with opportunity.title/description
   - Location preference (if specified)
   - Score: 0-100 based on keyword overlap

2. **Application Flow**:
   - Student uploads CV → parsed → skills extracted
   - System matches opportunities → saved to matches (implicit via search)
   - Student applies → JobApplicationEntity created
   - HR reviews → status updated (PENDING → INTERVIEW → ACCEPTED/REJECTED)

3. **Notifications**:
   - New match found → notification created
   - Application status change → notification created
   - HR action on application → notification created

## Index Recommendations

1. `opportunities.company_id` - FK lookup
2. `opportunities.title` - Search queries
3. `student.user_id` - FK lookup
4. `job_applications.user_id` - User's applications
5. `job_applications.opportunity_id` - Opportunity's applications
6. `job_applications.status` - Filter by status
7. `notifications.student_id` - User notifications
8. `notifications.read` - Unread notifications
9. `saved_jobs.user_id` - User's saved jobs
10. Composite: `(user_id, opportunity_id)` on job_applications and saved_jobs (already unique)

