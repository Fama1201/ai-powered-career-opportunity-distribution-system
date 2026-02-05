// hr-job-openings.js
// HR Job Openings page functionality

let allJobs = [];
let currentEditingJobId = null;
let currentStatusFilter = '';
let currentSearchQuery = '';

document.addEventListener('DOMContentLoaded', () => {
    initJobOpenings();
});

async function initJobOpenings() {
    loadSavedTheme();
    loadSavedLanguage();
    await loadUserProfile();
    setupEventListeners();
    await loadJobs();
}

function setupEventListeners() {
    // Post new job button - navigate to post job page
    document.getElementById('postNewJobBtn')?.addEventListener('click', () => {
        navigateTo('#/hr/post-job');
    });

    // Status filter (custom dropdown)
    setupStatusFilter();

    // Search
    document.getElementById('searchBtn')?.addEventListener('click', () => {
        performSearch();
    });
    document.getElementById('searchInput')?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            performSearch();
        }
    });

    // Job modal
    document.getElementById('modalCloseBtn')?.addEventListener('click', closeJobModal);
    document.getElementById('cancelJobBtn')?.addEventListener('click', closeJobModal);
    document.getElementById('jobForm')?.addEventListener('submit', handleJobSubmit);
    document.getElementById('jobModalOverlay')?.addEventListener('click', (e) => {
        if (e.target.id === 'jobModalOverlay') {
            closeJobModal();
        }
    });

    // Job detail modal
    document.getElementById('jobDetailCloseBtn')?.addEventListener('click', closeJobDetailModal);
    document.getElementById('jobDetailModalOverlay')?.addEventListener('click', (e) => {
        if (e.target.id === 'jobDetailModalOverlay') {
            closeJobDetailModal();
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
    const grid = document.getElementById('jobCardsGrid');
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
        const jobs = await HrAPI.getJobs(currentStatusFilter || null);
        allJobs = Array.isArray(jobs) ? jobs : [];
        await filterAndRenderJobs();
    } catch (error) {
        console.error('Failed to load jobs:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load jobs. Please try again.', 'error');
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

async function filterAndRenderJobs() {
    let filteredJobs = [...allJobs];

    // Apply search filter
    if (currentSearchQuery) {
        const query = currentSearchQuery.toLowerCase();
        filteredJobs = filteredJobs.filter(job => 
            job.title?.toLowerCase().includes(query) ||
            job.company?.toLowerCase().includes(query) ||
            job.description?.toLowerCase().includes(query)
        );
    }

    await renderJobCards(filteredJobs);
}

async function performSearch() {
    const searchInput = document.getElementById('searchInput');
    currentSearchQuery = searchInput?.value.trim() || '';
    await filterAndRenderJobs();
}

async function renderJobCards(jobs) {
    const grid = document.getElementById('jobCardsGrid');
    const emptyState = document.getElementById('emptyState');
    const loadingState = document.getElementById('loadingState');

    if (loadingState) {
        loadingState.style.display = 'none';
    }

    if (!jobs || jobs.length === 0) {
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

    // Create cards (applicant count will be loaded separately for performance)
    const cardPromises = jobs.map(job => createJobCard(job));
    const cards = await Promise.all(cardPromises);
    
    cards.forEach(card => {
        if (grid && card) {
            grid.appendChild(card);
        }
    });
    
    // Load applicant counts asynchronously after rendering
    loadApplicantCounts(jobs);
}

async function createJobCard(job) {
    const card = document.createElement('div');
    card.className = 'job-card';
    card.setAttribute('data-job-id', job.id);

    // Determine job status
    const status = getJobStatus(job);
    const statusClass = status.toLowerCase();

    // Applicant count will be loaded separately for performance
    const applicantCount = 0; // Will be updated by loadApplicantCounts

    // Get icon based on job type or title
    const iconClass = getJobIcon(job.title || job.jobType);

    card.innerHTML = `
        <div class="job-icon ${iconClass.color}" style="background: ${iconClass.bg}; color: ${iconClass.textColor};">
            <i class="${iconClass.icon}"></i>
        </div>
        <div class="job-content">
            <h3 class="job-title">${escapeHtml(job.title || 'Untitled')}</h3>
            <p class="job-company">${escapeHtml(job.company || 'No company')}</p>
            <p class="job-location">${escapeHtml(job.homeOffice || 'Location not specified')}</p>
            <div class="job-status-badge ${statusClass}">
                <span>${status}</span>
            </div>
        </div>
            <div class="job-footer">
            <span class="job-type">${escapeHtml(job.jobType || 'N/A')}</span>
            <div class="job-salary" id="applicantCount-${job.id}">Loading...</div>
            <div class="job-actions">
                <button class="job-action-btn view-btn" data-action="view" data-job-id="${job.id}" title="View Details">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="job-action-btn edit-btn" data-action="edit" data-job-id="${job.id}" title="Edit">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="job-action-btn applicants-btn" data-action="applicants" data-job-id="${job.id}" title="View Applicants">
                    <i class="fas fa-users"></i>
                </button>
                <div class="job-status-dropdown">
                    <button class="job-action-btn status-btn" data-action="status" data-job-id="${job.id}" title="Change Status">
                        <i class="fas fa-ellipsis-v"></i>
                    </button>
                    <div class="status-dropdown-menu" id="statusMenu-${job.id}">
                        ${status !== 'OPEN' ? '<button class="status-option" data-status="OPEN">Mark as Open</button>' : ''}
                        ${status !== 'CLOSED' ? '<button class="status-option" data-status="CLOSED">Mark as Closed</button>' : ''}
                        ${status !== 'ARCHIVED' ? '<button class="status-option" data-status="ARCHIVED">Archive</button>' : ''}
                        <button class="status-option delete-option" data-action="delete" data-job-id="${job.id}">Delete</button>
                    </div>
                </div>
            </div>
        </div>
    `;

    // Add event listeners
    const viewBtn = card.querySelector('[data-action="view"]');
    const editBtn = card.querySelector('[data-action="edit"]');
    const applicantsBtn = card.querySelector('[data-action="applicants"]');
    const statusBtn = card.querySelector('[data-action="status"]');
    const statusMenu = card.querySelector(`#statusMenu-${job.id}`);
    const statusOptions = card.querySelectorAll('.status-option');

    viewBtn?.addEventListener('click', () => viewJobDetails(job.id));
    editBtn?.addEventListener('click', () => editJob(job.id));
    applicantsBtn?.addEventListener('click', () => viewJobApplicants(job.id));
    
    statusBtn?.addEventListener('click', (e) => {
        e.stopPropagation();
        statusMenu?.classList.toggle('active');
    });

    statusOptions.forEach(option => {
        option.addEventListener('click', (e) => {
            e.stopPropagation();
            const status = option.getAttribute('data-status');
            const action = option.getAttribute('data-action');
            const jobId = option.getAttribute('data-job-id');
            
            if (action === 'delete') {
                handleDeleteJob(parseInt(jobId));
            } else if (status) {
                handleStatusChange(parseInt(jobId), status);
            }
            statusMenu?.classList.remove('active');
        });
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', (e) => {
        if (!card.contains(e.target)) {
            statusMenu?.classList.remove('active');
        }
    });

    return card;
}

function getJobStatus(job) {
    if (!job.applicationDeadline) {
        return 'ARCHIVED';
    }
    const deadline = new Date(job.applicationDeadline);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (deadline < today) {
        return 'CLOSED';
    }
    return 'OPEN';
}

async function loadApplicantCounts(jobs) {
    // Load applicant counts in parallel but with a small delay to avoid overwhelming the server
    const batchSize = 5;
    for (let i = 0; i < jobs.length; i += batchSize) {
        const batch = jobs.slice(i, i + batchSize);
        await Promise.all(batch.map(async (job) => {
            try {
                const applications = await HrAPI.getJobApplications(job.id);
                const count = Array.isArray(applications) ? applications.length : 0;
                const countElement = document.getElementById(`applicantCount-${job.id}`);
                if (countElement) {
                    countElement.textContent = `${count} ${count === 1 ? 'Applicant' : 'Applicants'}`;
                }
            } catch (error) {
                console.warn('Failed to load applicant count for job', job.id, error);
                const countElement = document.getElementById(`applicantCount-${job.id}`);
                if (countElement) {
                    countElement.textContent = '0 Applicants';
                }
            }
        }));
        // Small delay between batches
        if (i + batchSize < jobs.length) {
            await new Promise(resolve => setTimeout(resolve, 100));
        }
    }
}

function getJobIcon(title) {
    const titleLower = (title || '').toLowerCase();
    if (titleLower.includes('frontend') || titleLower.includes('ui') || titleLower.includes('react')) {
        return { icon: 'fas fa-code', color: 'purple', bg: 'rgba(147, 51, 234, 0.2)', textColor: '#9333ea' };
    } else if (titleLower.includes('backend') || titleLower.includes('server') || titleLower.includes('api')) {
        return { icon: 'fas fa-server', color: 'blue', bg: 'rgba(37, 99, 235, 0.2)', textColor: '#2563eb' };
    } else if (titleLower.includes('data') || titleLower.includes('analyst') || titleLower.includes('scientist')) {
        return { icon: 'fas fa-chart-bar', color: 'green', bg: 'rgba(16, 185, 129, 0.2)', textColor: '#10b981' };
    } else if (titleLower.includes('design') || titleLower.includes('ux') || titleLower.includes('ui')) {
        return { icon: 'fas fa-paint-brush', color: 'orange', bg: 'rgba(245, 158, 11, 0.2)', textColor: '#f59e0b' };
    } else if (titleLower.includes('devops') || titleLower.includes('cloud')) {
        return { icon: 'fas fa-cloud', color: 'cyan', bg: 'rgba(6, 182, 212, 0.2)', textColor: '#06b6d4' };
    }
    return { icon: 'fas fa-briefcase', color: 'default', bg: 'rgba(100, 116, 139, 0.2)', textColor: '#64748b' };
}

function openJobModal(jobId = null) {
    currentEditingJobId = jobId;
    const modal = document.getElementById('jobModalOverlay');
    const modalTitle = document.getElementById('modalTitle');
    const form = document.getElementById('jobForm');

    if (modalTitle) {
        modalTitle.textContent = jobId ? 'Edit Job' : 'Post New Job';
    }

    if (jobId) {
        loadJobForEdit(jobId);
    } else {
        form?.reset();
    }

    if (modal) {
        modal.style.display = 'flex';
    }
}

function closeJobModal() {
    const modal = document.getElementById('jobModalOverlay');
    const form = document.getElementById('jobForm');
    
    if (modal) {
        modal.style.display = 'none';
    }
    if (form) {
        form.reset();
    }
    currentEditingJobId = null;
}

async function loadJobForEdit(jobId) {
    try {
        const job = await HrAPI.getJob(jobId);
        populateJobForm(job);
    } catch (error) {
        console.error('Failed to load job:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load job details.', 'error');
        }
    }
}

function populateJobForm(job) {
    document.getElementById('jobTitle').value = job.title || '';
    document.getElementById('jobCompany').value = job.company || '';
    document.getElementById('jobType').value = job.jobType || '';
    document.getElementById('jobWage').value = job.wage || '';
    document.getElementById('jobHomeOffice').value = job.homeOffice || '';
    document.getElementById('jobDescription').value = job.description || '';
    document.getElementById('jobTechnicalRequirements').value = job.technicalRequirements || '';
    document.getElementById('jobFormalRequirements').value = job.formalRequirements || '';
    document.getElementById('jobBenefits').value = job.benefits || '';
    document.getElementById('jobContactPerson').value = job.contactPerson || '';
    document.getElementById('jobUrl').value = job.url || '';

    // Set deadline if exists
    if (job.applicationDeadline) {
        const deadline = new Date(job.applicationDeadline);
        // Convert to local datetime string format
        const year = deadline.getFullYear();
        const month = String(deadline.getMonth() + 1).padStart(2, '0');
        const day = String(deadline.getDate()).padStart(2, '0');
        const hours = String(deadline.getHours()).padStart(2, '0');
        const minutes = String(deadline.getMinutes()).padStart(2, '0');
        document.getElementById('jobDeadline').value = `${year}-${month}-${day}T${hours}:${minutes}`;
    }
}

async function handleJobSubmit(e) {
    e.preventDefault();
    
    const saveBtn = document.getElementById('saveJobBtn');
    const saveBtnText = document.getElementById('saveJobBtnText');
    const saveBtnLoading = document.getElementById('saveJobBtnLoading');

    const formData = {
        title: document.getElementById('jobTitle').value.trim(),
        company: document.getElementById('jobCompany').value.trim(),
        jobType: document.getElementById('jobType').value,
        wage: document.getElementById('jobWage').value.trim(),
        homeOffice: document.getElementById('jobHomeOffice').value,
        description: document.getElementById('jobDescription').value.trim(),
        technicalRequirements: document.getElementById('jobTechnicalRequirements').value.trim(),
        formalRequirements: document.getElementById('jobFormalRequirements').value.trim(),
        benefits: document.getElementById('jobBenefits').value.trim(),
        contactPerson: document.getElementById('jobContactPerson').value.trim(),
        url: document.getElementById('jobUrl').value.trim()
    };

    // Handle deadline
    const deadlineInput = document.getElementById('jobDeadline').value;
    if (deadlineInput) {
        formData.applicationDeadline = deadlineInput; // Will be sent as ISO string
    }

    // Show loading state
    saveBtn.disabled = true;
    saveBtnText.style.display = 'none';
    saveBtnLoading.style.display = 'inline';

    try {
        if (currentEditingJobId) {
            await HrAPI.updateJob(currentEditingJobId, formData);
            if (typeof UI !== 'undefined' && UI.toast) {
                UI.toast('Job updated successfully!', 'success');
            }
        } else {
            await HrAPI.createJob(formData);
            if (typeof UI !== 'undefined' && UI.toast) {
                UI.toast('Job posted successfully!', 'success');
            }
        }
        
        closeJobModal();
        await loadJobs();
    } catch (error) {
        console.error('Failed to save job:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to save job. Please try again.', 'error');
        }
    } finally {
        saveBtn.disabled = false;
        saveBtnText.style.display = 'inline';
        saveBtnLoading.style.display = 'none';
    }
}

async function editJob(jobId) {
    openJobModal(jobId);
}

async function viewJobDetails(jobId) {
    try {
        const job = await HrAPI.getJob(jobId);
        showJobDetailModal(job);
    } catch (error) {
        console.error('Failed to load job details:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load job details.', 'error');
        }
    }
}

function showJobDetailModal(job) {
    const modal = document.getElementById('jobDetailModalOverlay');
    const content = document.getElementById('jobDetailContent');
    const title = document.getElementById('jobDetailTitle');

    if (title) {
        title.textContent = job.title || 'Job Details';
    }

    if (content) {
        const status = getJobStatus(job);
        content.innerHTML = `
            <div class="job-detail-section">
                <h3>Basic Information</h3>
                <div class="detail-row">
                    <span class="detail-label">Company:</span>
                    <span class="detail-value">${escapeHtml(job.company || 'N/A')}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Job Type:</span>
                    <span class="detail-value">${escapeHtml(job.jobType || 'N/A')}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Work Location:</span>
                    <span class="detail-value">${escapeHtml(job.homeOffice || 'N/A')}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Wage/Salary:</span>
                    <span class="detail-value">${escapeHtml(job.wage || 'N/A')}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Status:</span>
                    <span class="detail-value status-badge ${status.toLowerCase()}">${status}</span>
                </div>
                ${job.applicationDeadline ? `
                <div class="detail-row">
                    <span class="detail-label">Application Deadline:</span>
                    <span class="detail-value">${formatDate(job.applicationDeadline)}</span>
                </div>
                ` : ''}
            </div>
            ${job.description ? `
            <div class="job-detail-section">
                <h3>Description</h3>
                <p>${escapeHtml(job.description)}</p>
            </div>
            ` : ''}
            ${job.technicalRequirements ? `
            <div class="job-detail-section">
                <h3>Technical Requirements</h3>
                <p>${escapeHtml(job.technicalRequirements)}</p>
            </div>
            ` : ''}
            ${job.formalRequirements ? `
            <div class="job-detail-section">
                <h3>Formal Requirements</h3>
                <p>${escapeHtml(job.formalRequirements)}</p>
            </div>
            ` : ''}
            ${job.benefits ? `
            <div class="job-detail-section">
                <h3>Benefits</h3>
                <p>${escapeHtml(job.benefits)}</p>
            </div>
            ` : ''}
            ${job.contactPerson ? `
            <div class="job-detail-section">
                <h3>Contact</h3>
                <div class="detail-row">
                    <span class="detail-label">Contact Person:</span>
                    <span class="detail-value">${escapeHtml(job.contactPerson)}</span>
                </div>
            </div>
            ` : ''}
            ${job.url ? `
            <div class="job-detail-section">
                <div class="detail-row">
                    <span class="detail-label">Application URL:</span>
                    <a href="${escapeHtml(job.url)}" target="_blank" class="detail-value link">${escapeHtml(job.url)}</a>
                </div>
            </div>
            ` : ''}
            <div class="job-detail-actions">
                <button class="btn btn-secondary" onclick="closeJobDetailModal()">Close</button>
                <button class="btn btn-primary" onclick="editJob(${job.id}); closeJobDetailModal();">Edit Job</button>
            </div>
        `;
    }

    if (modal) {
        modal.style.display = 'flex';
    }
}

function closeJobDetailModal() {
    const modal = document.getElementById('jobDetailModalOverlay');
    if (modal) {
        modal.style.display = 'none';
    }
}

async function viewJobApplicants(jobId) {
    // Navigate to candidates page with job filter
    if (typeof router !== 'undefined' && router.navigate) {
        router.navigate(`/hr/candidates?jobId=${jobId}`);
    } else {
        window.location.href = `/pages/hr/hr-candidates.html?jobId=${jobId}`;
    }
}

async function handleStatusChange(jobId, status) {
    try {
        await HrAPI.updateJobStatus(jobId, status);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(`Job status updated to ${status}`, 'success');
        }
        await loadJobs();
    } catch (error) {
        console.error('Failed to update job status:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to update job status.', 'error');
        }
    }
}

async function handleDeleteJob(jobId) {
    if (!confirm('Are you sure you want to delete this job? This action cannot be undone.')) {
        return;
    }

    try {
        await HrAPI.deleteJob(jobId);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast('Job deleted successfully', 'success');
        }
        await loadJobs();
    } catch (error) {
        console.error('Failed to delete job:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to delete job.', 'error');
        }
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

function setupStatusFilter() {
    const statusFilterBtn = document.getElementById('statusFilterBtn');
    const statusFilterDropdown = document.getElementById('statusFilterDropdown');
    const statusFilterOptions = document.querySelectorAll('.custom-select-option');
    const statusFilterText = document.getElementById('statusFilterText');
    const statusFilterHidden = document.getElementById('statusFilter');
    const statusFilterWrapper = statusFilterBtn?.closest('.custom-select-wrapper');

    console.log('Setting up status filter:', { statusFilterBtn, statusFilterDropdown, statusFilterOptions: statusFilterOptions.length });

    if (!statusFilterBtn || !statusFilterDropdown) {
        console.error('Status filter elements not found!', { statusFilterBtn, statusFilterDropdown });
        return;
    }

    // Toggle dropdown
    statusFilterBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        const isActive = statusFilterDropdown.classList.toggle('active');
        if (statusFilterWrapper) {
            if (isActive) {
                statusFilterWrapper.classList.add('active');
            } else {
                statusFilterWrapper.classList.remove('active');
            }
        }
    });

    // Select option
    statusFilterOptions.forEach(option => {
        option.addEventListener('click', (e) => {
            e.stopPropagation();
            const value = option.getAttribute('data-value');
            const text = option.textContent;

            // Update hidden input
            if (statusFilterHidden) {
                statusFilterHidden.value = value;
            }

            // Update button text
            if (statusFilterText) {
                statusFilterText.textContent = text;
            }

            // Update active state
            statusFilterOptions.forEach(opt => opt.classList.remove('active'));
            option.classList.add('active');

            // Close dropdown
            statusFilterDropdown.classList.remove('active');
            if (statusFilterWrapper) {
                statusFilterWrapper.classList.remove('active');
            }

            // Apply filter
            currentStatusFilter = value;
            filterAndRenderJobs().catch(err => {
                console.error('Error filtering jobs:', err);
            });
        });
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', (e) => {
        if (!statusFilterBtn.contains(e.target) && !statusFilterDropdown.contains(e.target)) {
            statusFilterDropdown.classList.remove('active');
            if (statusFilterWrapper) {
                statusFilterWrapper.classList.remove('active');
            }
        }
    });

    // Set initial active option
    statusFilterOptions.forEach(option => {
        if (option.getAttribute('data-value') === '') {
            option.classList.add('active');
        }
    });
}

