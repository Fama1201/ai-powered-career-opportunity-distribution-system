// job-matches.js
// Job Matches page functionality

let allJobMatches = [];
let filteredMatches = [];

document.addEventListener('DOMContentLoaded', () => {
    initJobMatches();
});

async function initJobMatches() {
    loadSavedTheme();
    loadSavedLanguage();
    await loadUserProfile();
    setupEventListeners();
    await loadJobMatches();
}

function setupEventListeners() {
    // Search functionality
    const searchInput = document.getElementById('jobSearchInput');
    const searchBtn = document.getElementById('searchBtn');
    
    searchBtn?.addEventListener('click', handleSearch);
    searchInput?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    });

    // Filter functionality
    document.getElementById('jobTypeFilter')?.addEventListener('change', handleFilter);
    document.getElementById('locationFilter')?.addEventListener('change', handleFilter);

    // Refresh matches button
    document.getElementById('refreshMatchesBtn')?.addEventListener('click', handleRefreshMatches);

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

async function loadJobMatches() {
    const grid = document.getElementById('jobMatchesGrid');
    const loadingState = document.getElementById('loadingState');
    const emptyState = document.getElementById('emptyState');

    if (grid) {
        showLoading(grid);
    }
    if (loadingState) {
        loadingState.style.display = 'block';
    }
    if (emptyState) {
        emptyState.style.display = 'none';
    }

    try {
        // Use StudentAPI to get job matches from backend
        const matches = await StudentAPI.getJobMatches();
        allJobMatches = Array.isArray(matches) ? matches : [];
        filteredMatches = [...allJobMatches];
        
        updateMatchesCount();
        renderJobMatches(filteredMatches);
    } catch (error) {
        console.error('Failed to load job matches:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load job matches. Please try again.', 'error');
        } else {
            alert(error.message || 'Failed to load job matches. Please try again.');
        }
        if (emptyState) {
            emptyState.style.display = 'block';
        }
    } finally {
        if (grid) {
            hideLoading(grid);
        }
        if (loadingState) {
            loadingState.style.display = 'none';
        }
    }
}

function renderJobMatches(matches) {
    const grid = document.getElementById('jobMatchesGrid');
    const emptyState = document.getElementById('emptyState');
    const loadingState = document.getElementById('loadingState');

    loadingState.style.display = 'none';

    if (!matches || matches.length === 0) {
        grid.innerHTML = '';
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';
    grid.innerHTML = '';

    matches.forEach(job => {
        const card = createJobCard(job);
        grid.appendChild(card);
    });
}

function createJobCard(job) {
    const card = document.createElement('div');
    card.className = 'job-card';
    card.setAttribute('data-job-id', job.id);

    // Get match score from backend response (matchScore field)
    const matchScore = job.matchScore || job.match_score || 0;
    const matchScoreColor = matchScore >= 70 ? '#22c55e' : matchScore >= 50 ? '#f59e0b' : '#64748b';

    // Format wage - could be string or number
    let wageDisplay = '';
    if (job.wage) {
        if (typeof job.wage === 'number') {
            wageDisplay = `€ ${job.wage.toFixed(2)}`;
        } else {
            wageDisplay = job.wage;
        }
    }

    card.innerHTML = `
        <div class="job-card-header">
            <div class="job-icon purple">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M4 4H20C21.1 4 22 4.9 22 6V18C22 19.1 21.1 20 20 20H4C2.9 20 2 19.1 2 18V6C2 4.9 2.9 4 4 4Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M22 6L12 13L2 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </div>
            ${matchScore > 0 ? `
            <div class="match-score-badge" style="background: ${matchScoreColor}20; color: ${matchScoreColor}; border: 1px solid ${matchScoreColor}40;">
                ${matchScore}% Match
            </div>
            ` : ''}
        </div>
        <div class="job-content">
            <h3 class="job-title">${escapeHtml(job.title || 'N/A')}</h3>
            <p class="job-company">${escapeHtml(job.company || 'N/A')}</p>
            <div class="job-meta">
                <div class="job-meta-row">
                    <span class="job-location">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M12 13C13.6569 13 15 11.6569 15 10C15 8.34315 13.6569 7 12 7C10.3431 7 9 8.34315 9 10C9 11.6569 10.3431 13 12 13Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        ${escapeHtml(job.homeOffice || job.location || 'Remote')}
                    </span>
                    <span class="job-type">${escapeHtml(job.jobType || 'N/A')}</span>
                </div>
            </div>
            ${wageDisplay ? `<p class="job-salary">💰 ${escapeHtml(wageDisplay)}</p>` : ''}
            ${job.applicationDeadline ? `<p class="job-deadline">📅 Deadline: ${formatDate(job.applicationDeadline)}</p>` : ''}
        </div>
        <div class="job-card-footer">
            <button class="btn btn-secondary btn-sm" data-action="view-details" data-job-id="${job.id}">
                View Details
            </button>
            <button class="btn btn-primary btn-sm" data-action="apply-job" data-job-id="${job.id}" ${job.applied ? 'disabled' : ''}>
                ${job.applied ? 'Applied' : 'Apply'}
            </button>
        </div>
    `;

    // Add event listeners
    card.querySelector('[data-action="view-details"]')?.addEventListener('click', (e) => {
        e.stopPropagation();
        viewJobDetail(job.id);
    });

    card.querySelector('[data-action="apply-job"]')?.addEventListener('click', (e) => {
        e.stopPropagation();
        handleApplyJob(job.id, card);
    });

    // Make entire card clickable
    card.addEventListener('click', () => {
        viewJobDetail(job.id);
    });

    return card;
}

let currentModal = null;

async function viewJobDetail(jobId) {
    try {
        // Use StudentAPI to get job details from backend
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
                ${job.formalRequirements ? `
                <div class="job-detail-section">
                    <h4>Formal Requirements</h4>
                    <p>${escapeHtml(job.formalRequirements)}</p>
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
                ${job.contactPerson ? `
                <div class="job-detail-section">
                    <h4>Contact Person</h4>
                    <p>${escapeHtml(job.contactPerson)}</p>
                </div>` : ''}
                ${job.url ? `
                <div class="job-detail-section">
                    <h4>Application URL</h4>
                    <p><a href="${escapeHtml(job.url)}" target="_blank" rel="noopener noreferrer">${escapeHtml(job.url)}</a></p>
                </div>` : ''}
            </div>
        `;

        const modalFooter = `
            <button class="btn btn-secondary" data-action="save-job" data-job-id="${jobId}" ${job.saved ? 'disabled' : ''}>
                ${job.saved ? 'Saved' : 'Save Job'}
            </button>
            <button class="btn btn-primary" data-action="apply-job-modal" data-job-id="${jobId}" ${job.applied ? 'disabled' : ''}>
                ${job.applied ? 'Applied' : 'Apply Now'}
            </button>
        `;

        if (typeof UI !== 'undefined' && UI.modal) {
            currentModal = UI.modal({
                title: 'Job Details',
                content: modalContent,
                footer: modalFooter,
                size: 'large',
                onClose: () => {
                    currentModal = null;
                }
            });

            // Add event listeners to modal buttons after modal is created
            setTimeout(() => {
                document.querySelector('[data-action="save-job"]')?.addEventListener('click', (e) => {
                    e.stopPropagation();
                    handleSaveJob(jobId);
                });

                document.querySelector('[data-action="apply-job-modal"]')?.addEventListener('click', async (e) => {
                    e.stopPropagation();
                    await handleApplyJob(jobId);
                    if (currentModal && currentModal.close) {
                        currentModal.close();
                    }
                });
            }, 100);
        } else {
            // Fallback: use alert
            alert(`Job: ${job.title}\nCompany: ${job.company}\nLocation: ${job.homeOffice || job.location || 'Remote'}`);
        }

    } catch (error) {
        console.error('Failed to load job details:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load job details. Please try again.', 'error');
        } else {
            alert(error.message || 'Failed to load job details. Please try again.');
        }
    }
}

async function handleApplyJob(jobId, cardElement = null) {
    const button = cardElement 
        ? cardElement.querySelector(`[data-action="apply-job"][data-job-id="${jobId}"]`)
        : document.querySelector(`[data-action="apply-job-modal"][data-job-id="${jobId}"]`);

    if (!button || button.disabled) return;

    const originalText = button.innerHTML;
    button.disabled = true;
    button.style.opacity = '0.6';
    button.style.cursor = 'not-allowed';
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Applying...';

    try {
        // Use StudentAPI to apply for job
        await StudentAPI.applyToJob(jobId);
        
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast('Application submitted successfully!', 'success');
        } else {
            alert('Application submitted successfully!');
        }
        
        // Update button state
        button.textContent = 'Applied';
        button.disabled = true;
        button.classList.remove('btn-primary');
        button.classList.add('btn-secondary');
        button.style.opacity = '1';

        // Update job in arrays
        const jobIndex = allJobMatches.findIndex(j => j.id === jobId);
        if (jobIndex !== -1) {
            allJobMatches[jobIndex].applied = true;
        }
        const filteredIndex = filteredMatches.findIndex(j => j.id === jobId);
        if (filteredIndex !== -1) {
            filteredMatches[filteredIndex].applied = true;
        }

    } catch (error) {
        console.error('Failed to apply for job:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to apply for job. Please try again.', 'error');
        } else {
            alert(error.message || 'Failed to apply for job. Please try again.');
        }
        button.disabled = false;
        button.innerHTML = originalText;
        button.style.opacity = '1';
        button.style.cursor = 'pointer';
    }
}

async function handleSaveJob(jobId) {
    const button = document.querySelector(`[data-action="save-job"][data-job-id="${jobId}"]`);
    if (!button || button.disabled) return;

    const originalText = button.innerHTML;
    button.disabled = true;
    button.style.opacity = '0.6';
    button.style.cursor = 'not-allowed';
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';

    try {
        // Use StudentAPI to save job
        await StudentAPI.saveJob(jobId);
        
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast('Job saved successfully!', 'success');
        } else {
            alert('Job saved successfully!');
        }
        button.textContent = 'Saved';
        button.disabled = true;
        button.style.opacity = '1';
    } catch (error) {
        console.error('Failed to save job:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to save job. Please try again.', 'error');
        } else {
            alert(error.message || 'Failed to save job. Please try again.');
        }
        button.disabled = false;
        button.innerHTML = originalText;
        button.style.opacity = '1';
        button.style.cursor = 'pointer';
    }
}

function handleSearch() {
    const searchTerm = document.getElementById('jobSearchInput').value.toLowerCase().trim();
    applyFilters(searchTerm);
}

function handleFilter() {
    const searchTerm = document.getElementById('jobSearchInput').value.toLowerCase().trim();
    applyFilters(searchTerm);
}

function applyFilters(searchTerm = '') {
    const jobType = document.getElementById('jobTypeFilter').value;
    const location = document.getElementById('locationFilter').value;

    filteredMatches = allJobMatches.filter(job => {
        // Search filter
        if (searchTerm) {
            const searchableText = `${job.title || ''} ${job.company || ''} ${job.description || ''}`.toLowerCase();
            if (!searchableText.includes(searchTerm)) {
                return false;
            }
        }

        // Job type filter
        if (jobType && job.jobType !== jobType) {
            return false;
        }

        // Location filter
        if (location) {
            const jobLocation = (job.homeOffice || job.location || '').toLowerCase();
            if (location === 'Remote' && !jobLocation.includes('remote')) {
                return false;
            }
            if (location === 'On-site' && jobLocation.includes('remote')) {
                return false;
            }
            if (location === 'Hybrid' && !jobLocation.includes('hybrid')) {
                return false;
            }
        }

        return true;
    });

    updateMatchesCount();
    renderJobMatches(filteredMatches);
}

function updateMatchesCount() {
    const countElement = document.getElementById('matchesCount');
    if (countElement) {
        const count = filteredMatches.length;
        countElement.textContent = `${count} ${count === 1 ? 'match' : 'matches'} found`;
    }
}

async function handleRefreshMatches() {
    const refreshBtn = document.getElementById('refreshMatchesBtn');
    if (!refreshBtn) return;

    // Disable button and show loading state
    refreshBtn.disabled = true;
    const originalHTML = refreshBtn.innerHTML;
    refreshBtn.innerHTML = `
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="spinning">
            <path d="M1 4V10H7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M23 20V14H17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10M23 14L18.36 18.36A9 9 0 0 1 3.51 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>Refreshing...</span>
    `;

    try {
        // Reload matches from backend
        await loadJobMatches();
        
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast('Matches refreshed successfully!', 'success');
        }
    } catch (error) {
        console.error('Failed to refresh matches:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to refresh matches. Please try again.', 'error');
        }
    } finally {
        // Re-enable button
        refreshBtn.disabled = false;
        refreshBtn.innerHTML = originalHTML;
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
            // Use StudentAPI or direct API call
            if (typeof StudentAPI !== 'undefined' && StudentAPI.request) {
                await StudentAPI.request('/auth/logout', { method: 'POST' });
            } else if (typeof apiRequest !== 'undefined') {
                await apiRequest('/auth/logout', { method: 'POST' });
            }
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            if (typeof clearAuthData === 'function') {
                clearAuthData();
            }
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

