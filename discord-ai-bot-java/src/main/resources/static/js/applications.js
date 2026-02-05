// applications.js
// My Applications page functionality

let allApplications = [];
let filteredApplications = [];
let currentStatusFilter = '';

document.addEventListener('DOMContentLoaded', () => {
    initApplications();
});

async function initApplications() {
    loadSavedTheme();
    loadSavedLanguage();
    await loadUserProfile();
    setupEventListeners();
    
    // Check URL params for status filter
    const urlParams = new URLSearchParams(window.location.search);
    const statusParam = urlParams.get('status');
    if (statusParam) {
        currentStatusFilter = statusParam;
        document.querySelector(`[data-status="${statusParam}"]`)?.classList.add('active');
        document.querySelectorAll('.filter-btn').forEach(btn => {
            if (btn.getAttribute('data-status') !== statusParam) {
                btn.classList.remove('active');
            }
        });
    }
    
    await loadApplications();
}

function setupEventListeners() {
    // Status filter buttons
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const status = e.currentTarget.getAttribute('data-status') || '';
            handleStatusFilter(status);
        });
    });

    // Theme toggle
    document.getElementById('themeToggle')?.addEventListener('click', toggleTheme);

    // Language dropdown
    document.getElementById('languageBtn')?.addEventListener('click', toggleLanguageMenu);
    document.querySelectorAll('.language-option').forEach(btn => {
        btn.addEventListener('click', (e) => handleLanguageChange(e.currentTarget));
    });

    // User profile dropdown
    document.getElementById('userProfileBtn')?.addEventListener('click', toggleUserProfileMenu);

    // Logout
    document.getElementById('logoutItem')?.addEventListener('click', handleLogout);

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

async function loadApplications() {
    const list = document.getElementById('applicationsList');
    const loadingState = document.getElementById('loadingState');
    const emptyState = document.getElementById('emptyState');

    showLoading(list);
    loadingState.style.display = 'block';
    emptyState.style.display = 'none';

    try {
        const params = currentStatusFilter ? { status: currentStatusFilter } : {};
        const applications = await StudentAPI.getApplications(params);
        allApplications = Array.isArray(applications) ? applications : [];
        
        // Load job details for each application
        await enrichApplicationsWithJobDetails();
        
        filteredApplications = [...allApplications];
        updateApplicationCounts();
        renderApplications(filteredApplications);
    } catch (error) {
        console.error('Failed to load applications:', error);
        showToast(error.message || 'Failed to load applications. Please try again.', 'error');
        emptyState.style.display = 'block';
    } finally {
        hideLoading(list);
        loadingState.style.display = 'none';
    }
}

async function enrichApplicationsWithJobDetails() {
    // Fetch job details for each application
    for (let app of allApplications) {
        try {
            if (app.opportunityId) {
                const job = await StudentAPI.getJobById(app.opportunityId);
                app.jobDetails = job;
            }
        } catch (error) {
            console.error(`Failed to load job details for application ${app.id}:`, error);
            app.jobDetails = null;
        }
    }
}

function handleStatusFilter(status) {
    currentStatusFilter = status;
    
    // Update active button
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
        if (btn.getAttribute('data-status') === status) {
            btn.classList.add('active');
        }
    });
    
    // Filter applications
    if (status) {
        filteredApplications = allApplications.filter(app => 
            app.status && app.status.toLowerCase() === status.toLowerCase()
        );
    } else {
        filteredApplications = [...allApplications];
    }
    
    renderApplications(filteredApplications);
    updateApplicationsCount();
}

function renderApplications(applications) {
    const list = document.getElementById('applicationsList');
    const emptyState = document.getElementById('emptyState');
    const loadingState = document.getElementById('loadingState');

    loadingState.style.display = 'none';

    if (!applications || applications.length === 0) {
        list.innerHTML = '';
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';
    list.innerHTML = '';

    applications.forEach(application => {
        const card = createApplicationCard(application);
        list.appendChild(card);
    });
}

function createApplicationCard(application) {
    const card = document.createElement('div');
    card.className = 'application-card';
    card.setAttribute('data-application-id', application.id);

    const job = application.jobDetails || {};
    const status = application.status || 'Applied';
    const statusClass = getStatusClass(status);
    const appliedDate = application.appliedAt ? formatDate(application.appliedAt) : 'N/A';

    card.innerHTML = `
        <div class="application-card-header">
            <div class="application-icon ${statusClass}">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </div>
            <div class="application-status-badge ${statusClass}">
                ${escapeHtml(status)}
            </div>
        </div>
        <div class="application-content">
            <h3 class="application-job-title">${escapeHtml(job.title || 'Job Title N/A')}</h3>
            <p class="application-company">${escapeHtml(job.company || 'Company N/A')}</p>
            <div class="application-meta">
                <div class="application-meta-item">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <polyline points="12 6 12 12 16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <span>Applied: ${appliedDate}</span>
                </div>
                ${job.jobType ? `
                <div class="application-meta-item">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M12 13C13.6569 13 15 11.6569 15 10C15 8.34315 13.6569 7 12 7C10.3431 7 9 8.34315 9 10C9 11.6569 10.3431 13 12 13Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <span>${escapeHtml(job.jobType)}</span>
                </div>` : ''}
                ${job.homeOffice ? `
                <div class="application-meta-item">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M12 13C13.6569 13 15 11.6569 15 10C15 8.34315 13.6569 7 12 7C10.3431 7 9 8.34315 9 10C9 11.6569 10.3431 13 12 13Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <span>${escapeHtml(job.homeOffice)}</span>
                </div>` : ''}
            </div>
            ${application.notes ? `
            <div class="application-notes">
                <p><strong>Notes:</strong> ${escapeHtml(application.notes)}</p>
            </div>` : ''}
        </div>
        <div class="application-card-footer">
            <button class="btn btn-secondary btn-sm" data-action="view-job-details" data-application-id="${application.id}" data-job-id="${application.opportunityId}">
                ${I18n.t('viewDetails')}
            </button>
            ${status.toLowerCase() !== 'rejected' ? `
            <button class="btn btn-danger btn-sm" data-action="withdraw-application" data-application-id="${application.id}">
                ${I18n.t('withdrawn')}
            </button>` : ''}
        </div>
    `;

    // Add event listeners
    card.querySelector('[data-action="view-job-details"]')?.addEventListener('click', (e) => {
        e.stopPropagation();
        viewJobDetails(application.opportunityId);
    });

    card.querySelector('[data-action="withdraw-application"]')?.addEventListener('click', (e) => {
        e.stopPropagation();
        handleWithdrawApplication(application.id, card);
    });

    return card;
}

async function viewJobDetails(jobId) {
    if (!jobId) {
        showToast('Job details not available', 'error');
        return;
    }

    showLoading(document.body);
    try {
        const job = await StudentAPI.getJobById(jobId);
        
        const modalContent = `
            <div class="job-detail-content">
                <div class="job-detail-header">
                    <h2>${escapeHtml(job.title || 'N/A')}</h2>
                    <p class="job-detail-company">${escapeHtml(job.company || 'N/A')}</p>
                </div>
                <div class="job-detail-info">
                    <div class="info-row">
                        <span class="info-label">Location:</span>
                        <span>${escapeHtml(job.homeOffice || job.location || 'Remote')}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Job Type:</span>
                        <span>${escapeHtml(job.jobType || 'N/A')}</span>
                    </div>
                    ${job.wage ? `
                    <div class="info-row">
                        <span class="info-label">Salary:</span>
                        <span>${escapeHtml(job.wage)}</span>
                    </div>` : ''}
                    ${job.applicationDeadline ? `
                    <div class="info-row">
                        <span class="info-label">Deadline:</span>
                        <span>${formatDate(job.applicationDeadline)}</span>
                    </div>` : ''}
                </div>
                ${job.description ? `
                <div class="job-detail-section">
                    <h4>Description</h4>
                    <p>${escapeHtml(job.description)}</p>
                </div>` : ''}
                ${job.technicalRequirements ? `
                <div class="job-detail-section">
                    <h4>Technical Requirements</h4>
                    <p>${escapeHtml(job.technicalRequirements)}</p>
                </div>` : ''}
                ${job.benefits ? `
                <div class="job-detail-section">
                    <h4>Benefits</h4>
                    <p>${escapeHtml(job.benefits)}</p>
                </div>` : ''}
            </div>
        `;

        showModal(modalContent, 'Job Details');

    } catch (error) {
        console.error('Failed to load job details:', error);
        showToast(error.message || 'Failed to load job details. Please try again.', 'error');
    } finally {
        hideLoading(document.body);
    }
}

async function handleWithdrawApplication(applicationId, cardElement) {
    if (!confirm('Are you sure you want to withdraw this application? This action cannot be undone.')) {
        return;
    }

    const button = cardElement.querySelector(`[data-action="withdraw-application"][data-application-id="${applicationId}"]`);
    if (!button) return;

    const originalText = button.innerHTML;
    showLoading(button);
    button.disabled = true;

    try {
        await StudentAPI.withdrawApplication(applicationId);
        showToast('Application withdrawn successfully', 'success');
        
        // Remove from lists
        allApplications = allApplications.filter(app => app.id !== applicationId);
        filteredApplications = filteredApplications.filter(app => app.id !== applicationId);
        
        // Remove card from DOM
        cardElement.remove();
        
        // Update counts
        updateApplicationCounts();
        updateApplicationsCount();
        
    } catch (error) {
        console.error('Failed to withdraw application:', error);
        showToast(error.message || 'Failed to withdraw application. Please try again.', 'error');
        button.disabled = false;
        button.innerHTML = originalText;
    } finally {
        hideLoading(button);
    }
}

function getStatusClass(status) {
    if (!status) return 'applied';
    const statusLower = status.toLowerCase();
    if (statusLower.includes('interview')) return 'interview';
    if (statusLower.includes('reject')) return 'rejected';
    return 'applied';
}

function updateApplicationCounts() {
    const counts = {
        all: allApplications.length,
        applied: allApplications.filter(app => {
            const status = (app.status || '').toLowerCase();
            return status.includes('applied') && !status.includes('interview') && !status.includes('reject');
        }).length,
        interview: allApplications.filter(app => (app.status || '').toLowerCase().includes('interview')).length,
        rejected: allApplications.filter(app => (app.status || '').toLowerCase().includes('reject')).length
    };

    document.getElementById('allCount').textContent = counts.all;
    document.getElementById('appliedCount').textContent = counts.applied;
    document.getElementById('interviewCount').textContent = counts.interview;
    document.getElementById('rejectedCount').textContent = counts.rejected;
}

function updateApplicationsCount() {
    const countElement = document.getElementById('applicationsCount');
    if (countElement) {
        const count = filteredApplications.length;
        countElement.textContent = `${count} ${count === 1 ? 'application' : 'applications'}`;
    }
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
    } catch (e) {
        return dateString;
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
    const role = localStorage.getItem(CONFIG.STORAGE_KEYS.ROLE) || 'STUDENT';

    let fullName = '';
    if (firstName && lastName) {
        fullName = `${firstName} ${lastName}`;
    } else if (firstName) {
        fullName = firstName;
    } else if (email) {
        fullName = email.split('@')[0];
    } else {
        fullName = 'Student';
    }

    updateUserProfileDisplay(fullName, role);
}

function updateUserProfileDisplay(fullName, role) {
    const profileNameElement = document.querySelector('.profile-name');
    const profileRoleElement = document.querySelector('.profile-role');
    const profileInitialsElement = document.querySelector('.profile-initials');

    if (profileNameElement) {
        profileNameElement.textContent = fullName;
    }

    if (profileRoleElement) {
        profileRoleElement.textContent = role === 'STUDENT' ? 'Student' : role;
    }

    if (profileInitialsElement && fullName) {
        const names = fullName.trim().split(' ');
        let initials = '';
        if (names.length >= 2) {
            initials = (names[0].charAt(0) + names[names.length - 1].charAt(0)).toUpperCase();
        } else if (names.length === 1) {
            initials = names[0].substring(0, 2).toUpperCase();
        }
        profileInitialsElement.textContent = initials || 'ST';
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

