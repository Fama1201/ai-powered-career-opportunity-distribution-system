/**
 * API Layer - Real Backend Integration
 * Uses actual backend endpoints from Spring Boot controllers
 */

const StudentAPI = {
    /**
     * Make authenticated API request
     */
    async request(endpoint, options = {}) {
        const token = getAccessToken();
        if (!token) {
            throw new Error('Not authenticated');
        }

        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        };

        const mergedOptions = {
            ...defaultOptions,
            ...options,
            headers: {
                ...defaultOptions.headers,
                ...(options.headers || {})
            }
        };

        try {
            const response = await fetch(`${CONFIG.API_BASE_URL}${endpoint}`, mergedOptions);
            
            // Don't auto-redirect on 401/403 - let the calling code handle it
            // This prevents unwanted redirects when the page is loading
            if (response.status === 401 || response.status === 403) {
                const error = await response.json().catch(() => ({ message: 'Unauthorized' }));
                throw new Error(error.message || 'Unauthorized');
            }

            if (!response.ok) {
                const error = await response.json().catch(() => ({ message: response.statusText }));
                throw new Error(error.message || `HTTP ${response.status}`);
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            }
            return null;
        } catch (error) {
            console.error('API Request Error:', error);
            throw error;
        }
    },

    // ========== DASHBOARD ==========

    /**
     * GET /api/dashboard/overview
     * Returns: { studentName, totalApplications, savedJobsCount, recentMatches[] }
     */
    async getDashboardOverview() {
        return this.request(CONFIG.ENDPOINTS.DASHBOARD_OVERVIEW);
    },

    /**
     * GET /api/dashboard/matches
     * Returns: OpportunityListResponse[]
     */
    async getJobMatches() {
        return this.request(CONFIG.ENDPOINTS.DASHBOARD_MATCHES);
    },

    /**
     * GET /api/dashboard/jobs/new
     * Returns: OpportunityListResponse[]
     */
    async getNewJobs() {
        return this.request(CONFIG.ENDPOINTS.DASHBOARD_NEW_JOBS);
    },

    /**
     * GET /api/dashboard/applications/stats
     * Returns: { totalApplied, interviews, rejected }
     */
    async getApplicationStats() {
        return this.request(CONFIG.ENDPOINTS.DASHBOARD_STATS);
    },

    /**
     * GET /api/dashboard/progress
     * Returns: { score, message }
     */
    async getProgress() {
        return this.request(CONFIG.ENDPOINTS.DASHBOARD_PROGRESS);
    },

    // ========== JOBS/OPPORTUNITIES ==========

    /**
     * GET /api/jobs
     * Query params: page, size, sort
     * Returns: Page<OpportunityListResponse>
     */
    async getJobs(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`${CONFIG.ENDPOINTS.GET_JOBS}${queryString ? '?' + queryString : ''}`);
    },

    /**
     * GET /api/jobs/{id}
     * Returns: OpportunityDetailResponse
     */
    async getJobById(id) {
        return this.request(`${CONFIG.ENDPOINTS.GET_JOB}/${id}`);
    },

    /**
     * GET /api/jobs/search?keyword=...
     * Returns: Page<OpportunityListResponse>
     */
    async searchJobs(keyword, params = {}) {
        const queryParams = { keyword, ...params };
        const queryString = new URLSearchParams(queryParams).toString();
        return this.request(`${CONFIG.ENDPOINTS.SEARCH_JOBS}?${queryString}`);
    },

    /**
     * POST /api/jobs/{id}/save
     * Returns: { message: "Job saved successfully" }
     */
    async saveJob(jobId) {
        return this.request(`${CONFIG.ENDPOINTS.SAVE_JOB}/${jobId}/save`, {
            method: 'POST'
        });
    },

    /**
     * POST /api/jobs/{id}/apply
     * Returns: { message: "Job applied successfully" }
     */
    async applyToJob(jobId) {
        return this.request(`${CONFIG.ENDPOINTS.APPLY_JOB}/${jobId}/apply`, {
            method: 'POST'
        });
    },

    // ========== APPLICATIONS ==========

    /**
     * GET /api/applications?status=...&opportunityId=...
     * Returns: ApplicationResponse[]
     */
    async getApplications(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/applications${queryString ? '?' + queryString : ''}`);
    },

    /**
     * GET /api/applications/{id}
     * Returns: ApplicationResponse
     */
    async getApplicationById(id) {
        return this.request(`/applications/${id}`);
    },

    /**
     * PUT /api/applications/{id}/withdraw
     * Returns: ApplicationResponse
     */
    async withdrawApplication(id) {
        return this.request(`/applications/${id}/withdraw`, {
            method: 'PUT'
        });
    },

    // ========== NOTIFICATIONS ==========

    /**
     * GET /api/notifications
     * Returns: NotificationResponse[]
     */
    async getNotifications() {
        return this.request('/notifications');
    },

    /**
     * PUT /api/notifications/{id}/read
     * Returns: 204 No Content
     */
    async markNotificationRead(id) {
        return this.request(`/notifications/${id}/read`, {
            method: 'PUT'
        });
    },

    /**
     * PUT /api/notifications/mark-all-read
     * Returns: 204 No Content
     */
    async markAllNotificationsRead() {
        return this.request('/notifications/mark-all-read', {
            method: 'PUT'
        });
    },

    // ========== PROFILE ==========

    /**
     * GET /api/profile
     * Returns: StudentProfileResponse { id, name, email, skills, careerInterest }
     */
    async getProfile() {
        return this.request(CONFIG.ENDPOINTS.GET_PROFILE);
    },

    /**
     * PUT /api/profile
     * Body: { name?, skills?, careerInterest? }
     * Returns: StudentProfileResponse
     */
    async updateProfile(data) {
        return this.request(CONFIG.ENDPOINTS.UPDATE_PROFILE, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    // ========== AI ==========

    /**
     * POST /api/ai/chat
     * Body: { message: string }
     * Returns: ChatResponse { reply, ... }
     */
    async sendAiChat(message) {
        return this.request(CONFIG.ENDPOINTS.AI_CHAT, {
            method: 'POST',
            body: JSON.stringify({ message })
        });
    },

    /**
     * GET /api/ai/chat/history
     * Returns: HistoryItemResponse[]
     */
    async getAiChatHistory() {
        return this.request(CONFIG.ENDPOINTS.AI_CHAT_HISTORY);
    },

    /**
     * POST /api/ai/cv-review
     * Body: { message: string }
     * Returns: ChatResponse
     */
    async getCvReview(message) {
        return this.request(CONFIG.ENDPOINTS.AI_CV_REVIEW, {
            method: 'POST',
            body: JSON.stringify({ message })
        });
    },

    /**
     * POST /api/ai/career-advice
     * Body: { message: string }
     * Returns: ChatResponse
     */
    async getCareerAdvice(message) {
        return this.request(CONFIG.ENDPOINTS.AI_CAREER_ADVICE, {
            method: 'POST',
            body: JSON.stringify({ message })
        });
    },

    // ========== CV ==========

    /**
     * GET /api/cv
     * Returns: CvResponse { hasCv: boolean, cvText: string }
     */
    async getCv() {
        return this.request('/cv');
    },

    /**
     * POST /api/cv/upload
     * Body: FormData with file or extractedText
     * Returns: CvResponse
     */
    async uploadCv(formData) {
        const token = getAccessToken();
        const response = await fetch(`${CONFIG.API_BASE_URL}/cv/upload`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        if (response.status === 401 || response.status === 403) {
            clearAuthData();
            window.location.href = '/pages/auth/login.html';
            throw new Error('Unauthorized');
        }

        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: response.statusText }));
            throw new Error(error.message || `HTTP ${response.status}`);
        }

        return await response.json();
    },

    /**
     * PUT /api/cv/update
     * Body: { cvText: string }
     * Returns: CvResponse
     */
    async updateCv(cvText) {
        return this.request('/cv/update', {
            method: 'PUT',
            body: JSON.stringify({ cvText })
        });
    },

    /**
     * DELETE /api/cv
     * Returns: CvResponse
     */
    async deleteCv() {
        return this.request('/cv', {
            method: 'DELETE'
        });
    },

    // ========== AUTH ==========

    /**
     * Logout (if endpoint exists, otherwise just clear local storage)
     */
    async logout() {
        try {
            await this.request('/auth/logout', { method: 'POST' });
        } catch (error) {
            // If logout endpoint doesn't exist, just clear local storage
            console.warn('Logout endpoint not available, clearing local storage');
        }
        clearAuthData();
    }
};

// Export for use in other files
if (typeof module !== 'undefined' && module.exports) {
    module.exports = StudentAPI;
}

