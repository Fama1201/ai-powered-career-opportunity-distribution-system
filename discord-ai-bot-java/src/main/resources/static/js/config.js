/**
 * Frontend Configuration
 * Centralized configuration for API endpoints and application settings
 */

const CONFIG = {
    // API Base URL - relative path since frontend and backend are on same server
    API_BASE_URL: '/api',
    
    // API Endpoints
    ENDPOINTS: {
        // Authentication
        STUDENT_LOGIN: '/auth/student/login',
        STUDENT_REGISTER: '/auth/student/register',
        HR_LOGIN: '/hr/auth/login',
        HR_REGISTER: '/hr/auth/register',
        REFRESH_TOKEN: '/auth/refresh',
        FORGOT_PASSWORD: '/auth/forgot-password',
        RESET_PASSWORD: '/auth/reset-password',
        VALIDATE_TOKEN: '/auth/validate-token',
        
        // Student
        DASHBOARD_OVERVIEW: '/dashboard/overview',
        DASHBOARD_MATCHES: '/dashboard/matches',
        DASHBOARD_NEW_JOBS: '/dashboard/jobs/new',
        DASHBOARD_STATS: '/dashboard/applications/stats',
        DASHBOARD_PROGRESS: '/dashboard/progress',
        
        // Jobs
        GET_JOBS: '/jobs',
        GET_JOB: '/jobs',
        SEARCH_JOBS: '/jobs/search',
        SAVE_JOB: '/jobs',
        APPLY_JOB: '/jobs',
        
        // HR
        HR_JOBS: '/hr/jobs',
        HR_CANDIDATES: '/hr/candidates',
        HR_ANALYTICS: '/hr/analytics',
        
        // AI
        AI_CHAT: '/ai/chat',
        AI_CHAT_HISTORY: '/ai/chat/history',
        AI_CV_REVIEW: '/ai/cv-review',
        AI_CAREER_ADVICE: '/ai/career-advice',
        AI_CODE_REVIEW: '/ai/code-review',
        
        // Profile
        GET_PROFILE: '/profile',
        UPDATE_PROFILE: '/profile',
        
        // CV Management
        GET_CV: '/cv',
        UPLOAD_CV: '/cv/upload',
        UPDATE_CV: '/cv/update',
        DELETE_CV: '/cv',
        
        // Notifications
        GET_NOTIFICATIONS: '/notifications',
        MARK_NOTIFICATION_READ: '/notifications',
        MARK_ALL_NOTIFICATIONS_READ: '/notifications/mark-all-read',
        
        // Applications
        GET_APPLICATIONS: '/applications',
        GET_APPLICATION_BY_ID: '/applications',
        WITHDRAW_APPLICATION: '/applications',
        
        // Dashboard
        DASHBOARD_APPLICATIONS_STATS: '/dashboard/applications/stats',
        
        // Health
        HEALTH_CHECK: '/status'
    },
    
    // Storage Keys
    STORAGE_KEYS: {
        ACCESS_TOKEN: 'accessToken',
        REFRESH_TOKEN: 'refreshToken',
        USER_TYPE: 'userType',
        USER_EMAIL: 'userEmail',
        USER_ID: 'userId',
        FIRST_NAME: 'firstName',
        LAST_NAME: 'lastName',
        HR_FULL_NAME: 'hrFullName',
        ROLE: 'role'
    },
    
    // Application Settings
    SETTINGS: {
        TOKEN_REFRESH_INTERVAL: 14 * 60 * 1000, // 14 minutes
        MAX_RETRY_ATTEMPTS: 3,
        REQUEST_TIMEOUT: 30000 // 30 seconds
    },
    
    // Error Messages
    ERROR_MESSAGES: {
        NETWORK_ERROR: 'Network error. Please check your connection.',
        UNAUTHORIZED: 'Your session has expired. Please log in again.',
        NOT_FOUND: 'The requested resource was not found.',
        SERVER_ERROR: 'Server error. Please try again later.',
        VALIDATION_ERROR: 'Please check your input and try again.'
    },
    
    // Success Messages
    SUCCESS_MESSAGES: {
        LOGIN_SUCCESS: 'Welcome back!',
        REGISTER_SUCCESS: 'Registration successful!',
        PROFILE_UPDATED: 'Profile updated successfully!',
        APPLICATION_SUBMITTED: 'Application submitted successfully!'
    }
};

// Export for use in other files
if (typeof module !== 'undefined' && module.exports) {
    module.exports = CONFIG;
}

