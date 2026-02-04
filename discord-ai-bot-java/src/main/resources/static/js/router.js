/**
 * Hash-based Router for Student Dashboard
 * Handles navigation between different pages/sections
 */

class Router {
    constructor() {
        this.routes = new Map();
        this.currentRoute = null;
        this.beforeRouteChange = null;
        this.afterRouteChange = null;
        this.init();
    }

    init() {
        // Listen for hash changes
        window.addEventListener('hashchange', () => this.handleRoute());
        
        // Handle initial route
        this.handleRoute();
    }

    /**
     * Register a route
     * @param {string} path - Route path (e.g., '/student/dashboard')
     * @param {Function} handler - Route handler function
     */
    register(path, handler) {
        this.routes.set(path, handler);
    }

    /**
     * Navigate to a route
     * @param {string} path - Route path
     * @param {Object} params - Route parameters
     */
    navigate(path, params = {}) {
        const queryString = Object.keys(params).length > 0 
            ? '?' + new URLSearchParams(params).toString() 
            : '';
        window.location.hash = path + queryString;
    }

    /**
     * Get current route
     */
    getCurrentRoute() {
        const hash = window.location.hash.slice(1);
        // If no hash, check if we're on a specific page
        if (!hash || hash === '') {
            const pathname = window.location.pathname;
            if (pathname.includes('student-dashboard.html')) {
                return { path: '/student/dashboard', params: {} };
            }
            if (pathname.includes('cv.html')) {
                return { path: '/student/cv', params: {} };
            }
            // Default to dashboard
            return { path: '/student/dashboard', params: {} };
        }
        const [path, query] = hash.split('?');
        const params = query ? Object.fromEntries(new URLSearchParams(query)) : {};
        return { path, params };
    }

    /**
     * Handle route change
     */
    async handleRoute() {
        const { path, params } = this.getCurrentRoute();
        
        // Call before route change hook
        if (this.beforeRouteChange) {
            const shouldContinue = await this.beforeRouteChange(path, params);
            if (shouldContinue === false) return;
        }

        // Find and execute route handler
        const handler = this.routes.get(path);
        if (handler) {
            try {
                await handler(params);
                this.currentRoute = path;
            } catch (error) {
                console.error('Route handler error:', error);
                this.navigate('/student/dashboard');
            }
        } else {
            // If route not found and we're not already on a page, try to navigate
            // But don't show warning for empty routes or if we're already on a page
            if (path && path !== '' && path !== '/') {
                console.warn(`Route not found: ${path}`);
                // Only redirect if we're not on a specific page already
                if (!window.location.pathname.includes('.html')) {
                    this.navigate('/student/dashboard');
                }
            }
        }

        // Call after route change hook
        if (this.afterRouteChange) {
            this.afterRouteChange(path, params);
        }
    }

    /**
     * Set before route change hook
     */
    beforeRoute(hook) {
        this.beforeRouteChange = hook;
    }

    /**
     * Set after route change hook
     */
    afterRoute(hook) {
        this.afterRouteChange = hook;
    }
}

// Create global router instance
const router = new Router();

// Register default routes
router.register('/student/dashboard', () => {
    // If we're already on dashboard, just update active state
    if (window.location.pathname.includes('student-dashboard.html') || window.location.pathname.includes('dashboard.html')) {
        document.querySelectorAll('.sidebar-item').forEach(item => {
            item.classList.remove('active');
            if (item.getAttribute('href')?.includes('dashboard') || item.getAttribute('data-route')?.includes('dashboard') || item.textContent.includes('Dashboard')) {
                item.classList.add('active');
            }
        });
    } else {
        // Navigate to dashboard page
        window.location.href = '/pages/student/student-dashboard.html';
    }
});

router.register('/student/job-matches', () => {
    // If we're already on job-matches.html, just update active state
    if (window.location.pathname.includes('job-matches.html')) {
        document.querySelectorAll('.sidebar-item').forEach(item => {
            item.classList.remove('active');
            if (item.getAttribute('href')?.includes('job-matches') || item.textContent.includes('Job Matches')) {
                item.classList.add('active');
            }
        });
    } else {
        window.location.href = '/pages/student/job-matches.html';
    }
});

router.register('/student/applications', (params) => {
    // If we're already on applications.html, just update active state
    if (window.location.pathname.includes('applications.html')) {
        document.querySelectorAll('.sidebar-item').forEach(item => {
            item.classList.remove('active');
            if (item.getAttribute('href')?.includes('applications') || item.textContent.includes('Applications')) {
                item.classList.add('active');
            }
        });
        // Apply status filter if provided
        if (params.status) {
            const filterBtn = document.querySelector(`[data-status="${params.status}"]`);
            if (filterBtn) {
                document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
                filterBtn.classList.add('active');
                // Trigger filter
                if (typeof handleStatusFilter === 'function') {
                    handleStatusFilter(params.status);
                }
            }
        }
    } else {
        const url = '/pages/student/applications.html' + (params.status ? `?status=${params.status}` : '');
        window.location.href = url;
    }
});

router.register('/student/cv', () => {
    // If we're already on cv.html, just update active state
    if (window.location.pathname.includes('cv.html')) {
        document.querySelectorAll('.sidebar-item').forEach(item => {
            item.classList.remove('active');
            if (item.getAttribute('href')?.includes('cv.html') || item.textContent.includes('CV Management')) {
                item.classList.add('active');
            }
        });
    } else {
        window.location.href = '/pages/student/cv.html';
    }
});

router.register('/student/advisor', () => {
    window.location.href = '/pages/student/advisor.html';
});

router.register('/student/profile', () => {
    // If we're already on profile.html, just update active state
    if (window.location.pathname.includes('profile.html')) {
        document.querySelectorAll('.sidebar-item').forEach(item => {
            item.classList.remove('active');
        });
        const profileItem = document.getElementById('profileItem');
        if (profileItem) {
            profileItem.classList.add('active');
        }
    } else {
        window.location.href = '/pages/student/profile.html';
    }
});

router.register('/student/notifications', () => {
    // If we're already on notifications.html, just update active state
    if (window.location.pathname.includes('notifications.html')) {
        document.querySelectorAll('.sidebar-item').forEach(item => {
            item.classList.remove('active');
        });
        const notificationsItem = document.getElementById('notificationsItem');
        if (notificationsItem) {
            notificationsItem.classList.add('active');
        }
    } else {
        window.location.href = '/pages/student/notifications.html';
    }
});

router.register('/student/login', () => {
    window.location.href = '/pages/auth/login.html';
});

// HR Routes
router.register('/hr/dashboard', () => {
    // If we're already on hr-dashboard.html, just update active state
    if (window.location.pathname.includes('hr-dashboard.html')) {
        document.querySelectorAll('.sidebar-item').forEach(item => {
            item.classList.remove('active');
            if (item.getAttribute('href')?.includes('dashboard') || item.getAttribute('data-route')?.includes('dashboard') || item.textContent.includes('Dashboard')) {
                item.classList.add('active');
            }
        });
    } else {
        window.location.href = '/pages/hr/hr-dashboard.html';
    }
});

router.register('/hr/job-openings', () => {
    window.location.href = '/pages/hr/hr-job-openings.html';
});

router.register('/hr/candidates', () => {
    window.location.href = '/pages/hr/hr-candidates.html';
});

router.register('/hr/hires', () => {
    window.location.href = '/pages/hr/hr-hires.html';
});

router.register('/hr/analytics', () => {
    window.location.href = '/pages/hr/hr-analytics.html';
});

router.register('/hr/profile', () => {
    // If we're already on hr-profile.html, just update active state
    if (window.location.pathname.includes('hr-profile.html')) {
        document.querySelectorAll('.sidebar-item').forEach(item => {
            item.classList.remove('active');
        });
        const profileItem = document.getElementById('profileItem');
        if (profileItem) {
            profileItem.classList.add('active');
        }
    } else {
        window.location.href = '/pages/hr/hr-profile.html';
    }
});

router.register('/hr/notifications', () => {
    // If we're already on hr-notifications.html, just update active state
    if (window.location.pathname.includes('hr-notifications.html')) {
        document.querySelectorAll('.sidebar-item').forEach(item => {
            item.classList.remove('active');
        });
        const notificationsItem = document.getElementById('notificationsItem');
        if (notificationsItem) {
            notificationsItem.classList.add('active');
        }
    } else {
        window.location.href = '/pages/hr/hr-notifications.html';
    }
});

// Export for use in other files
if (typeof module !== 'undefined' && module.exports) {
    module.exports = router;
}

