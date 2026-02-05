// hr-dashboard.js
// HR Dashboard page functionality

let allJobs = [];
let allApplications = [];
let dashboardStats = {
    totalJobs: 0,
    newCandidates: 0,
    totalHires: 0,
    hiresThisMonth: 0,
    totalInterviews: 0
};

document.addEventListener('DOMContentLoaded', () => {
    initDashboard();
});

async function initDashboard() {
    loadSavedTheme();
    loadSavedLanguage();
    await loadUserProfile();
    setupEventListeners();
    await loadDashboardData();
    updateDashboardDisplay();
}

function setupEventListeners() {
    // Sidebar navigation
    document.querySelectorAll('.sidebar-item').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const route = item.getAttribute('data-route');
            if (route) {
                navigateTo(route);
            }
        });
    });

    // Profile and Notifications dropdown items
    document.getElementById('profileItem')?.addEventListener('click', (e) => {
        e.preventDefault();
        const route = e.currentTarget.getAttribute('data-route');
        if (route) {
            navigateTo(route);
        }
    });
    document.getElementById('notificationsItem')?.addEventListener('click', (e) => {
        e.preventDefault();
        const route = e.currentTarget.getAttribute('data-route');
        if (route) {
            navigateTo(route);
        }
    });

    // Logout
    document.getElementById('logoutItem')?.addEventListener('click', handleLogout);

    // Theme toggle
    document.getElementById('themeToggle')?.addEventListener('click', toggleTheme);

    // Language dropdown
    document.getElementById('languageBtn')?.addEventListener('click', toggleLanguageMenu);
    document.querySelectorAll('.language-option').forEach(btn => {
        btn.addEventListener('click', (e) => handleLanguageChange(e.currentTarget));
    });

    // User profile dropdown
    document.getElementById('userProfileBtn')?.addEventListener('click', toggleUserProfileMenu);

    // See all link
    document.querySelector('.see-all-link')?.addEventListener('click', (e) => {
        e.preventDefault();
        const route = e.currentTarget.getAttribute('data-route');
        if (route) {
            navigateTo(route);
        }
    });

    // Assistant buttons
    document.getElementById('askAssistantBtn')?.addEventListener('click', () => {
        navigateTo('#/hr/analytics'); // Navigate to Analytics or Career Advisor
    });
    document.getElementById('assistantAskBtn')?.addEventListener('click', () => {
        navigateTo('#/hr/analytics');
    });

    // Click outside to close dropdowns
    document.addEventListener('click', (e) => {
        const userDropdownMenu = document.getElementById('userDropdownMenu');
        const languageMenu = document.getElementById('languageMenu');

        if (userDropdownMenu && !e.target.closest('.user-profile-dropdown')) {
            userDropdownMenu.classList.remove('active');
        }
        if (languageMenu && !e.target.closest('.language-dropdown')) {
            languageMenu.classList.remove('active');
        }
    });
}

async function loadDashboardData() {
    try {
        // Load all jobs
        allJobs = await HrAPI.getJobs();
        allJobs = Array.isArray(allJobs) ? allJobs : [];

        // Load applications for all jobs
        allApplications = [];
        for (const job of allJobs) {
            try {
                const applications = await HrAPI.getJobApplications(job.id);
                const appsWithJob = applications.map(app => ({
                    ...app,
                    jobId: job.id,
                    jobTitle: job.title || `Job #${job.id}`
                }));
                allApplications.push(...appsWithJob);
            } catch (error) {
                console.error(`Failed to load applications for job ${job.id}:`, error);
                // Continue with other jobs
            }
        }

        // Calculate statistics
        calculateStatistics();
    } catch (error) {
        console.error('Failed to load dashboard data:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load dashboard data.', 'error');
        }
    }
}

function calculateStatistics() {
    // Total jobs
    dashboardStats.totalJobs = allJobs.length;

    // New candidates (APPLIED status)
    dashboardStats.newCandidates = allApplications.filter(app => {
        const status = (app.status || '').toUpperCase();
        return status === 'APPLIED';
    }).length;

    // Total hires
    dashboardStats.totalHires = allApplications.filter(app => {
        const status = (app.status || '').toUpperCase();
        return status === 'HIRED';
    }).length;

    // Hires this month
    const now = new Date();
    dashboardStats.hiresThisMonth = allApplications.filter(app => {
        const status = (app.status || '').toUpperCase();
        if (status !== 'HIRED') return false;
        if (!app.appliedAt) return false;
        const hireDate = new Date(app.appliedAt);
        return hireDate.getMonth() === now.getMonth() && 
               hireDate.getFullYear() === now.getFullYear();
    }).length;

    // Total interviews
    dashboardStats.totalInterviews = allApplications.filter(app => {
        const status = (app.status || '').toUpperCase();
        return status === 'INTERVIEW';
    }).length;
}

function updateDashboardDisplay() {
    // Update statistics cards
    document.getElementById('totalJobOpenings').textContent = dashboardStats.totalJobs;
    document.getElementById('newCandidates').textContent = dashboardStats.newCandidates;
    document.getElementById('totalHires').textContent = dashboardStats.totalHires;
    document.getElementById('hiresThisMonth').textContent = `${dashboardStats.hiresThisMonth} Hired this month`;
    document.getElementById('totalInterviews').textContent = dashboardStats.totalInterviews;

    // Update recent jobs
    renderRecentJobs();

    // Update assistant message
    updateAssistantMessage();
}

function renderRecentJobs() {
    const grid = document.getElementById('recentJobsGrid');
    const loadingState = document.getElementById('loadingState');
    const emptyState = document.getElementById('emptyState');

    if (loadingState) {
        loadingState.style.display = 'none';
    }

    if (!allJobs || allJobs.length === 0) {
        if (grid) {
            grid.innerHTML = '';
        }
        if (emptyState) {
            emptyState.style.display = 'block';
        }
        return;
    }

    if (emptyState) {
        emptyState.style.display = 'none';
    }
    if (grid) {
        grid.innerHTML = '';
    }

    // Sort by ID (most recent first) and take first 2
    const recentJobs = [...allJobs]
        .sort((a, b) => (b.id || 0) - (a.id || 0))
        .slice(0, 2);

    recentJobs.forEach(job => {
        const card = createJobCard(job);
        if (grid && card) {
            grid.appendChild(card);
        }
    });
}

function createJobCard(job) {
    const card = document.createElement('div');
    card.className = 'job-card';
    card.setAttribute('data-job-id', job.id);

    // Get job icon color based on status or random
    const iconColors = ['purple', 'green', 'blue', 'orange', 'red'];
    const iconColor = iconColors[job.id % iconColors.length] || 'purple';

    // Format wage
    const wage = job.wage || job.salary || 'N/A';
    const wageDisplay = typeof wage === 'number' ? `€ ${wage.toFixed(2)}` : wage;

    // Get job type
    const jobType = job.type || 'Full-Time';

    card.innerHTML = `
        <div class="job-icon ${iconColor}">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none"
                xmlns="http://www.w3.org/2000/svg">
                <path
                    d="M4 4H20C21.1 4 22 4.9 22 6V18C22 19.1 21.1 20 20 20H4C2.9 20 2 19.1 2 18V6C2 4.9 2.9 4 4 4Z"
                    stroke="currentColor" stroke-width="2" stroke-linecap="round"
                    stroke-linejoin="round" />
                <path d="M22 6L12 13L2 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                    stroke-linejoin="round" />
            </svg>
        </div>
        <div class="job-content">
            <h3 class="job-title">${escapeHtml(job.title || 'Untitled Job')}</h3>
            <p class="job-company">${escapeHtml(job.company || 'Company')}</p>
            <p class="job-location">${escapeHtml(job.location || 'Location')}</p>
        </div>
        <div class="job-footer">
            <span class="job-type">${escapeHtml(jobType)}</span>
            <div class="job-salary">${escapeHtml(wageDisplay)}</div>
            <button class="job-arrow" data-job-id="${job.id}">></button>
        </div>
    `;

    // Add event listeners
    card.addEventListener('click', () => {
        navigateTo(`#/hr/job-openings?jobId=${job.id}`);
    });

    const arrowBtn = card.querySelector('.job-arrow');
    arrowBtn?.addEventListener('click', (e) => {
        e.stopPropagation();
        navigateTo(`#/hr/job-openings?jobId=${job.id}`);
    });

    return card;
}

function updateAssistantMessage() {
    const messageEl = document.getElementById('assistantMessage');
    if (!messageEl) return;

    if (dashboardStats.newCandidates > 0) {
        messageEl.textContent = `Reviewing candidate pools... ${dashboardStats.newCandidates} new candidates found across ${dashboardStats.totalJobs} job postings.`;
    } else if (dashboardStats.totalJobs > 0) {
        messageEl.textContent = `You have ${dashboardStats.totalJobs} active job postings. Start receiving applications soon!`;
    } else {
        messageEl.textContent = `Welcome! Create your first job posting to start receiving applications.`;
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// User profile functions
async function loadUserProfile() {
    const firstName = localStorage.getItem(CONFIG.STORAGE_KEYS.FIRST_NAME);
    const lastName = localStorage.getItem(CONFIG.STORAGE_KEYS.LAST_NAME);
    const email = localStorage.getItem(CONFIG.STORAGE_KEYS.USER_EMAIL);
    const role = localStorage.getItem(CONFIG.STORAGE_KEYS.ROLE) || 'HR';

    let fullName = '';
    if (firstName && lastName) {
        fullName = `${firstName} ${lastName}`;
    } else if (firstName) {
        fullName = firstName;
    } else if (email) {
        fullName = email.split('@')[0];
    } else {
        fullName = 'HR Professional';
    }

    updateUserProfileDisplay(fullName, role);
}

function updateUserProfileDisplay(fullName, role) {
    const profileNameElement = document.querySelector('.profile-name');
    const profileRoleElement = document.querySelector('.profile-role');
    const profileInitialsElement = document.querySelector('.profile-initials');
    const welcomeUserNameElement = document.querySelector('.user-name');

    if (profileNameElement) {
        profileNameElement.textContent = fullName;
    }

    if (profileRoleElement) {
        profileRoleElement.textContent = role === 'HR' ? 'HR Professional' : role;
    }

    if (profileInitialsElement && fullName) {
        const names = fullName.trim().split(' ');
        let initials = '';
        if (names.length >= 2) {
            initials = (names[0].charAt(0) + names[names.length - 1].charAt(0)).toUpperCase();
        } else if (names.length === 1) {
            initials = names[0].substring(0, 2).toUpperCase();
        }
        profileInitialsElement.textContent = initials || 'HR';
    }

    if (welcomeUserNameElement && fullName) {
        const firstName = fullName.trim().split(' ')[0];
        welcomeUserNameElement.textContent = firstName || 'HR Manager';
    }
}

function loadSavedTheme() {
    const savedTheme = localStorage.getItem('theme') || 'dark';
    if (savedTheme === 'light') {
        document.body.classList.add('light-theme');
    } else {
        document.body.classList.remove('light-theme');
    }
}

function loadSavedLanguage() {
    const savedLang = localStorage.getItem('language') || 'en';
    if (typeof I18n !== 'undefined' && I18n.setLanguage) {
        I18n.setLanguage(savedLang);
    }
    const langButtons = document.querySelectorAll('.language-option');
    langButtons.forEach(btn => {
        const lang = btn.getAttribute('data-lang');
        const langName = btn.getAttribute('data-lang-name');
        if (lang === savedLang) {
            btn.classList.add('active');
            const currentLangEl = document.getElementById('currentLanguage');
            if (currentLangEl) {
                currentLangEl.textContent = langName || 'English';
            }
        } else {
            btn.classList.remove('active');
        }
    });
}

function toggleTheme() {
    const body = document.body;
    const isLightTheme = body.classList.contains('light-theme');
    if (isLightTheme) {
        body.classList.remove('light-theme');
        localStorage.setItem('theme', 'dark');
    } else {
        body.classList.add('light-theme');
        localStorage.setItem('theme', 'light');
    }
}

function toggleLanguageMenu() {
    document.getElementById('languageMenu')?.classList.toggle('active');
}

function handleLanguageChange(button) {
    const lang = button.getAttribute('data-lang');
    const langName = button.getAttribute('data-lang-name');
    document.getElementById('currentLanguage').textContent = langName;
    document.querySelectorAll('.language-option').forEach(opt => opt.classList.remove('active'));
    button.classList.add('active');
    localStorage.setItem('language', lang);
    if (typeof I18n !== 'undefined' && I18n.setLanguage) {
        I18n.setLanguage(lang);
    }
    document.getElementById('languageMenu')?.classList.remove('active');
}

function toggleUserProfileMenu() {
    document.getElementById('userDropdownMenu')?.classList.toggle('active');
}

async function handleLogout(e) {
    e.preventDefault();
    if (confirm('Are you sure you want to logout?')) {
        try {
            await apiRequest('/auth/logout', { method: 'POST' });
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            clearAuthData();
            window.location.href = '/pages/auth/login.html';
        }
    }
}

function navigateTo(route) {
    if (typeof router !== 'undefined' && router.navigate) {
        router.navigate(route);
    } else {
        window.location.hash = route;
    }
}
