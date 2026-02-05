// hr-hires.js
// HR Hires page functionality

let allJobs = [];
let allHires = [];
let currentJobFilter = null;
let currentSearchQuery = '';

document.addEventListener('DOMContentLoaded', () => {
    initHires();
});

async function initHires() {
    loadSavedTheme();
    loadSavedLanguage();
    await loadUserProfile();
    setupEventListeners();
    await loadJobs();
    await loadAllHires();
}

function setupEventListeners() {
    // Job filter
    setupJobFilter();
    
    // Search
    document.getElementById('searchBtn')?.addEventListener('click', () => {
        performSearch();
    });
    document.getElementById('searchInput')?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            performSearch();
        }
    });

    // Hire detail modal
    document.getElementById('hireDetailCloseBtn')?.addEventListener('click', closeHireDetailModal);
    document.getElementById('hireDetailModalOverlay')?.addEventListener('click', (e) => {
        if (e.target.id === 'hireDetailModalOverlay') {
            closeHireDetailModal();
        }
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

async function loadJobs() {
    try {
        const jobs = await HrAPI.getJobs();
        allJobs = Array.isArray(jobs) ? jobs : [];
        populateJobFilter();
    } catch (error) {
        console.error('Failed to load jobs:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load jobs.', 'error');
        }
    }
}

function populateJobFilter() {
    const dropdown = document.getElementById('jobFilterDropdown');
    if (!dropdown) return;

    // Clear existing options except "All Jobs"
    const allJobsOption = dropdown.querySelector('[data-value=""]');
    dropdown.innerHTML = '';
    if (allJobsOption) {
        dropdown.appendChild(allJobsOption);
    } else {
        const allOption = document.createElement('button');
        allOption.type = 'button';
        allOption.className = 'custom-select-option';
        allOption.setAttribute('data-value', '');
        allOption.textContent = 'All Jobs';
        dropdown.appendChild(allOption);
    }

    // Add job options
    allJobs.forEach(job => {
        const option = document.createElement('button');
        option.type = 'button';
        option.className = 'custom-select-option';
        option.setAttribute('data-value', job.id);
        option.textContent = job.title || `Job #${job.id}`;
        dropdown.appendChild(option);
    });

    // Re-setup event listeners
    setupJobFilter();
}

function setupJobFilter() {
    const jobFilterBtn = document.getElementById('jobFilterBtn');
    const jobFilterDropdown = document.getElementById('jobFilterDropdown');
    const jobFilterOptions = jobFilterDropdown?.querySelectorAll('.custom-select-option');
    const jobFilterText = document.getElementById('jobFilterText');
    const jobFilterHidden = document.getElementById('jobFilter');
    const jobFilterWrapper = jobFilterBtn?.closest('.custom-select-wrapper');

    if (!jobFilterBtn || !jobFilterDropdown) return;

    // Remove existing listeners by cloning
    const newBtn = jobFilterBtn.cloneNode(true);
    jobFilterBtn.parentNode.replaceChild(newBtn, jobFilterBtn);
    const newDropdown = jobFilterDropdown.cloneNode(true);
    jobFilterDropdown.parentNode.replaceChild(newDropdown, jobFilterDropdown);

    // Re-get elements
    const btn = document.getElementById('jobFilterBtn');
    const dropdown = document.getElementById('jobFilterDropdown');
    const options = dropdown?.querySelectorAll('.custom-select-option');
    const wrapper = btn?.closest('.custom-select-wrapper');

    // Toggle dropdown
    btn?.addEventListener('click', (e) => {
        e.stopPropagation();
        const isActive = dropdown.classList.toggle('active');
        if (wrapper) {
            if (isActive) {
                wrapper.classList.add('active');
            } else {
                wrapper.classList.remove('active');
            }
        }
    });

    // Select option
    options?.forEach(option => {
        option.addEventListener('click', (e) => {
            e.stopPropagation();
            const value = option.getAttribute('data-value');
            const text = option.textContent;

            // Update hidden input
            if (jobFilterHidden) {
                jobFilterHidden.value = value;
            }

            // Update button text
            if (jobFilterText) {
                jobFilterText.textContent = text;
            }

            // Update active state
            options.forEach(opt => opt.classList.remove('active'));
            option.classList.add('active');

            // Close dropdown
            dropdown.classList.remove('active');
            if (wrapper) {
                wrapper.classList.remove('active');
            }

            // Apply filter
            currentJobFilter = value ? parseInt(value) : null;
            filterAndRenderHires();
        });
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', (e) => {
        if (!btn.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.classList.remove('active');
            if (wrapper) {
                wrapper.classList.remove('active');
            }
        }
    });

    // Set initial active option
    options?.forEach(option => {
        if (option.getAttribute('data-value') === '') {
            option.classList.add('active');
        }
    });
}

async function loadAllHires() {
    const grid = document.getElementById('hiresGrid');
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
        allHires = [];
        
        // Load applications for all jobs
        for (const job of allJobs) {
            try {
                const applications = await HrAPI.getJobApplications(job.id);
                const hiredApplications = applications.filter(app => 
                    app.status === 'HIRED' || app.status === 'hired'
                );
                
                // Add job title to each hire
                hiredApplications.forEach(hire => {
                    hire.jobTitle = job.title || `Job #${job.id}`;
                    hire.jobId = job.id;
                });
                
                allHires.push(...hiredApplications);
            } catch (error) {
                console.error(`Failed to load applications for job ${job.id}:`, error);
                // Continue with other jobs
            }
        }

        updateStatistics();
        filterAndRenderHires();
    } catch (error) {
        console.error('Failed to load hires:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load hires. Please try again.', 'error');
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

function updateStatistics() {
    const totalHires = allHires.length;
    const uniqueJobIds = new Set(allHires.map(hire => hire.jobId));
    const totalJobs = uniqueJobIds.size;
    
    // Count hires this month
    const now = new Date();
    const thisMonthHires = allHires.filter(hire => {
        if (!hire.appliedAt) return false;
        const hireDate = new Date(hire.appliedAt);
        return hireDate.getMonth() === now.getMonth() && 
               hireDate.getFullYear() === now.getFullYear();
    }).length;

    // Update DOM
    const totalHiresEl = document.getElementById('totalHires');
    const totalJobsEl = document.getElementById('totalJobs');
    const thisMonthHiresEl = document.getElementById('thisMonthHires');

    if (totalHiresEl) {
        totalHiresEl.textContent = totalHires;
    }
    if (totalJobsEl) {
        totalJobsEl.textContent = totalJobs;
    }
    if (thisMonthHiresEl) {
        thisMonthHiresEl.textContent = thisMonthHires;
    }
}

function filterAndRenderHires() {
    let filteredHires = [...allHires];

    // Apply job filter
    if (currentJobFilter) {
        filteredHires = filteredHires.filter(hire => hire.jobId === currentJobFilter);
    }

    // Apply search filter
    if (currentSearchQuery) {
        const query = currentSearchQuery.toLowerCase();
        filteredHires = filteredHires.filter(hire => 
            hire.studentName?.toLowerCase().includes(query) ||
            hire.studentEmail?.toLowerCase().includes(query) ||
            hire.jobTitle?.toLowerCase().includes(query)
        );
    }

    renderHires(filteredHires);
}

function performSearch() {
    const searchInput = document.getElementById('searchInput');
    currentSearchQuery = searchInput?.value.trim() || '';
    filterAndRenderHires();
}

function renderHires(hires) {
    const grid = document.getElementById('hiresGrid');
    const emptyState = document.getElementById('emptyState');
    const loadingState = document.getElementById('loadingState');
    const emptyStateMessage = document.getElementById('emptyStateMessage');

    if (loadingState) {
        loadingState.style.display = 'none';
    }

    if (!hires || hires.length === 0) {
        if (grid) {
            grid.innerHTML = '';
        }
        if (emptyState) {
            emptyState.style.display = 'block';
        }
        if (emptyStateMessage) {
            if (currentJobFilter || currentSearchQuery) {
                emptyStateMessage.textContent = 'No hires match your filters. Try adjusting your search criteria.';
            } else {
                emptyStateMessage.textContent = 'You haven\'t hired any candidates yet. Hired candidates will appear here.';
            }
        }
        return;
    }

    if (emptyState) {
        emptyState.style.display = 'none';
    }
    if (grid) {
        grid.innerHTML = '';
    }

    hires.forEach(hire => {
        const card = createHireCard(hire);
        if (grid && card) {
            grid.appendChild(card);
        }
    });
}

function createHireCard(hire) {
    const card = document.createElement('div');
    card.className = 'candidate-card hire-card';
    card.setAttribute('data-application-id', hire.applicationId);

    // Get initials
    const initials = getInitials(hire.studentName || hire.studentEmail || 'H');

    // Applied date
    const appliedDate = formatDate(hire.appliedAt);

    card.innerHTML = `
        <div class="candidate-header">
            <div class="candidate-avatar hired-avatar">${initials}</div>
            <div class="candidate-info">
                <h3 class="candidate-name">${escapeHtml(hire.studentName || 'Unknown')}</h3>
                <p class="candidate-email">${escapeHtml(hire.studentEmail || 'No email')}</p>
            </div>
            <span class="match-score high">Hired</span>
        </div>
        <div class="candidate-body">
            <div class="info-row">
                <i class="fas fa-briefcase"></i>
                <span>${escapeHtml(hire.jobTitle || 'Unknown Job')}</span>
            </div>
            <div class="info-row">
                <i class="fas fa-calendar"></i>
                <span>Hired: ${appliedDate}</span>
            </div>
            <div class="status-badge hired">Hired</div>
        </div>
        <div class="candidate-footer">
            <button class="btn btn-sm btn-primary view-detail-btn" data-application-id="${hire.applicationId}">
                <i class="fas fa-eye"></i> View Details
            </button>
        </div>
    `;

    // Add event listener
    const viewBtn = card.querySelector('.view-detail-btn');
    viewBtn?.addEventListener('click', () => {
        viewHireDetails(hire.applicationId);
    });

    return card;
}

function getInitials(name) {
    if (!name) return '?';
    const names = name.trim().split(' ');
    if (names.length >= 2) {
        return (names[0].charAt(0) + names[names.length - 1].charAt(0)).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
}

async function viewHireDetails(applicationId) {
    try {
        const detail = await HrAPI.getApplicationDetail(applicationId);
        showHireDetailModal(detail);
    } catch (error) {
        console.error('Failed to load hire details:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load hire details.', 'error');
        }
    }
}

function showHireDetailModal(detail) {
    const modal = document.getElementById('hireDetailModalOverlay');
    const content = document.getElementById('hireDetailContent');
    const title = document.getElementById('hireDetailTitle');

    if (title) {
        title.textContent = `${detail.studentName || 'Hire'} - Details`;
    }

    if (content) {
        const appliedDate = formatDate(detail.appliedAt);
        const initials = getInitials(detail.studentName || detail.studentEmail || 'H');
        const jobTitle = allJobs.find(job => job.id === detail.jobId)?.title || `Job #${detail.jobId}`;

        content.innerHTML = `
            <div class="candidate-detail-header">
                <div class="candidate-detail-avatar hired-avatar">${initials}</div>
                <div class="candidate-detail-info">
                    <h3>${escapeHtml(detail.studentName || 'Unknown')}</h3>
                    <p class="candidate-detail-email">${escapeHtml(detail.studentEmail || 'No email')}</p>
                    <div class="status-badge hired">Hired</div>
                </div>
            </div>

            <div class="candidate-detail-section">
                <h4>Hire Information</h4>
                <div class="detail-row">
                    <span class="detail-label">Job Position:</span>
                    <span class="detail-value">${escapeHtml(jobTitle)}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Hired Date:</span>
                    <span class="detail-value">${appliedDate}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Status:</span>
                    <span class="detail-value status-badge hired">Hired</span>
                </div>
            </div>

            ${detail.studentSkills ? `
            <div class="candidate-detail-section">
                <h4>Skills</h4>
                <p class="candidate-skills">${escapeHtml(detail.studentSkills)}</p>
            </div>
            ` : ''}

            ${detail.studentCareerInterest ? `
            <div class="candidate-detail-section">
                <h4>Career Interest</h4>
                <p class="candidate-career-interest">${escapeHtml(detail.studentCareerInterest)}</p>
            </div>
            ` : ''}

            ${detail.studentCvText ? `
            <div class="candidate-detail-section">
                <h4>CV Preview</h4>
                <div class="cv-preview">
                    <pre class="cv-text">${escapeHtml(detail.studentCvText)}</pre>
                </div>
            </div>
            ` : ''}

            ${detail.notes ? `
            <div class="candidate-detail-section">
                <h4>Notes</h4>
                <div class="notes-preview">
                    <pre class="notes-text">${escapeHtml(detail.notes)}</pre>
                </div>
            </div>
            ` : ''}

            <div class="candidate-detail-actions">
                <button class="btn btn-primary" id="viewCvBtn" data-student-id="${detail.studentUserId}">
                    <i class="fas fa-file-pdf"></i> View Full CV
                </button>
            </div>
        `;

        // Add event listener
        document.getElementById('viewCvBtn')?.addEventListener('click', async () => {
            await viewFullCv(detail.studentUserId);
        });
    }

    if (modal) {
        modal.style.display = 'flex';
    }
}

async function viewFullCv(studentUserId) {
    try {
        const cv = await HrAPI.getStudentCv(studentUserId);
        if (cv && cv.cvText) {
            // Open CV in a new window or modal
            const cvWindow = window.open('', '_blank');
            cvWindow.document.write(`
                <html>
                    <head>
                        <title>CV - ${studentUserId}</title>
                        <style>
                            body { font-family: Arial, sans-serif; padding: 2rem; line-height: 1.6; }
                            pre { white-space: pre-wrap; word-wrap: break-word; }
                        </style>
                    </head>
                    <body>
                        <h1>Curriculum Vitae</h1>
                        <pre>${escapeHtml(cv.cvText)}</pre>
                    </body>
                </html>
            `);
        } else {
            if (typeof UI !== 'undefined' && UI.toast) {
                UI.toast('CV not available for this hire.', 'info');
            }
        }
    } catch (error) {
        console.error('Failed to load CV:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load CV.', 'error');
        }
    }
}

function closeHireDetailModal() {
    const modal = document.getElementById('hireDetailModalOverlay');
    if (modal) {
        modal.style.display = 'none';
    }
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
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

function showLoading(element) {
    if (element) {
        element.style.opacity = '0.5';
        element.style.pointerEvents = 'none';
    }
}

function hideLoading(element) {
    if (element) {
        element.style.opacity = '1';
        element.style.pointerEvents = 'auto';
    }
}

