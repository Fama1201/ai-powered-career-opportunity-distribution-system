/**
 * Student Dashboard - Full Integration
 * All buttons and interactions connected to real backend APIs
 */

// Global state
let dashboardData = null;
let jobMatches = [];
let applicationStats = null;
let notifications = [];
let aiChatDrawer = null;

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', function() {
    if (!isAuthenticated()) {
        window.location.href = '/pages/auth/login.html';
        return;
    }

    initializeDashboard();
    loadSavedTheme();
    loadSavedLanguage();
    loadUserProfile();
    loadDashboardData();
    loadNotifications();
});

/**
 * Initialize all dashboard components
 */
function initializeDashboard() {
    setupSidebarNavigation();
    setupUserProfile();
    setupThemeToggle();
    setupLanguageDropdown();
    setupDashboardsButton();
    setupStatusCards();
    setupJobCards();
    setupAssistantCard();
    setupClickOutside();
}

/**
 * Load dashboard data from API
 */
async function loadDashboardData() {
    try {
        UI.loading(document.querySelector('.dashboard-main'), true);
        
        // Load overview, matches, and stats in parallel
        const [overview, matches, stats] = await Promise.all([
            StudentAPI.getDashboardOverview().catch(() => null),
            StudentAPI.getJobMatches().catch(() => []),
            StudentAPI.getApplicationStats().catch(() => null)
        ]);

        dashboardData = overview;
        jobMatches = Array.isArray(matches) ? matches : [];
        applicationStats = stats;

        // Update UI
        updateWelcomeMessage(overview);
        updateStatusCards(stats);
        renderJobMatches(jobMatches);
        
    } catch (error) {
        console.error('Error loading dashboard data:', error);
        UI.toast(I18n.t('error') + ': ' + error.message, 'error');
    } finally {
        UI.loading(document.querySelector('.dashboard-main'), false);
    }
}

/**
 * Load notifications
 */
async function loadNotifications() {
    try {
        notifications = await StudentAPI.getNotifications();
        updateNotificationBadge();
    } catch (error) {
        console.error('Error loading notifications:', error);
    }
}

/**
 * Update welcome message
 */
function updateWelcomeMessage(overview) {
    const userNameEl = document.querySelector('.welcome-title .user-name');
    if (userNameEl && overview) {
        const firstName = overview.studentName ? overview.studentName.split(' ')[0] : 
                         localStorage.getItem('firstName') || 'Student';
        userNameEl.textContent = firstName;
    }
}

/**
 * Update status cards with real data
 */
function updateStatusCards(stats) {
    if (!stats) {
        // Calculate from applications if stats not available
        calculateStatsFromApplications();
        return;
    }

    // Update Applied card
    const appliedNumber = document.querySelector('.status-card:first-child .status-number');
    if (appliedNumber) {
        appliedNumber.textContent = stats.totalApplied || 0;
    }

    // Update Interview card
    const interviewNumber = document.querySelector('.status-card:nth-child(2) .status-number');
    if (interviewNumber) {
        interviewNumber.textContent = stats.interviews || 0;
    }

    // Update detailed Applied card (the large one with "Applied • Interview")
    // This should show only "Applied" status count, not including Interview or Rejected
    const detailedApplied = document.querySelector('.status-card.detailed:first-of-type .status-number-large');
    if (detailedApplied) {
        // Calculate correct Applied count from applications (async, will update when ready)
        calculateAppliedCountForDetailedCard();
    }

    // Update Rejected card
    const rejectedNumber = document.querySelector('.status-card.detailed:last-of-type .status-number-large');
    if (rejectedNumber) {
        rejectedNumber.textContent = stats.rejected || 0;
    }
}

/**
 * Calculate and update the detailed Applied card count
 * This ensures only "Applied" status applications are counted (excluding Interview/Rejected)
 */
async function calculateAppliedCountForDetailedCard() {
    try {
        const applications = await StudentAPI.getApplications();
        // Filter only "Applied" status (case-insensitive, excluding Interview and Rejected)
        const appliedCount = applications.filter(app => {
            const status = (app.status || '').toLowerCase();
            return status.includes('applied') && !status.includes('interview') && !status.includes('reject');
        }).length;
        
        const detailedApplied = document.querySelector('.status-card.detailed:first-of-type .status-number-large');
        if (detailedApplied) {
            detailedApplied.textContent = appliedCount;
        }
    } catch (error) {
        console.error('Error calculating applied count for detailed card:', error);
    }
}

/**
 * Calculate stats from applications list
 */
async function calculateStatsFromApplications() {
    try {
        const applications = await StudentAPI.getApplications();
        const stats = {
            totalApplied: applications.length,
            interviews: applications.filter(a => a.status === 'Interview').length,
            rejected: applications.filter(a => a.status === 'Rejected').length
        };
        updateStatusCards(stats);
    } catch (error) {
        console.error('Error calculating stats:', error);
    }
}

/**
 * Render job matches
 */
function renderJobMatches(matches) {
    const grid = document.querySelector('.job-cards-grid');
    if (!grid) return;

    if (!matches || matches.length === 0) {
        grid.innerHTML = `<div class="empty-state">${I18n.t('noMatches')}</div>`;
        return;
    }

    // Helper function to escape HTML
    const escapeHtml = (text) => {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    };

    // Show first 2-3 matches
    const displayMatches = matches.slice(0, 3);
    grid.innerHTML = displayMatches.map(job => {
        const matchScore = job.matchScore || job.match_score || 0;
        const matchScoreColor = matchScore >= 70 ? '#22c55e' : matchScore >= 50 ? '#f59e0b' : '#64748b';
        
        return `
        <div class="job-card dashboard-job-card" data-job-id="${job.id}">
            <div class="job-card-header-dashboard">
                <div class="job-icon-dashboard purple">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M4 4H20C21.1 4 22 4.9 22 6V18C22 19.1 21.1 20 20 20H4C2.9 20 2 19.1 2 18V6C2 4.9 2.9 4 4 4Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M22 6L12 13L2 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                ${matchScore > 0 ? `
                <div class="match-score-badge-dashboard" style="background: ${matchScoreColor}20; color: ${matchScoreColor}; border: 1px solid ${matchScoreColor}40;">
                    ${matchScore}%
                </div>
                ` : ''}
            </div>
            <div class="job-content-dashboard">
                <h3 class="job-title-dashboard">${escapeHtml(job.title || 'N/A')}</h3>
                <p class="job-company-dashboard">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M3 21H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M5 21V7L13 2L21 7V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M9 9V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M15 9V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    ${escapeHtml(job.company || 'N/A')}
                </p>
                <div class="job-meta-dashboard">
                    <span class="job-location-dashboard">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M12 13C13.6569 13 15 11.6569 15 10C15 8.34315 13.6569 7 12 7C10.3431 7 9 8.34315 9 10C9 11.6569 10.3431 13 12 13Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        ${escapeHtml(job.homeOffice || job.location || 'Remote')}
                    </span>
                    <span class="job-type-dashboard">${escapeHtml(job.jobType || I18n.t('fullTime'))}</span>
                </div>
            </div>
            <div class="job-footer-dashboard">
                ${job.wage ? `<div class="job-salary-dashboard">💰 ${escapeHtml(job.wage)}</div>` : ''}
                <button class="job-arrow-dashboard" data-job-id="${job.id}" aria-label="View job details">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M12 5L19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </button>
            </div>
        </div>
    `;
    }).join('');

    // Re-attach event listeners
    setupJobCards();
}

/**
 * Setup status cards - make them clickable
 */
function setupStatusCards() {
    const statusCards = document.querySelectorAll('.status-card');
    statusCards.forEach(card => {
        card.style.cursor = 'pointer';
        card.addEventListener('click', function() {
            const label = this.querySelector('.status-label, .status-title')?.textContent;
            if (label) {
                const status = label.toLowerCase();
                if (status.includes('applied')) {
                    router.navigate('/student/applications', { status: 'Applied' });
                } else if (status.includes('interview')) {
                    router.navigate('/student/applications', { status: 'Interview' });
                } else if (status.includes('rejected')) {
                    router.navigate('/student/applications', { status: 'Rejected' });
                }
            }
        });
    });
}

/**
 * Setup job cards - arrow button opens job detail modal
 */
function setupJobCards() {
    const jobCards = document.querySelectorAll('.job-card');
    const arrowButtons = document.querySelectorAll('.job-arrow');
    const seeAllLink = document.querySelector('.see-all-link');

    // Arrow buttons
    arrowButtons.forEach(btn => {
        btn.addEventListener('click', async function(e) {
            e.stopPropagation();
            const jobId = this.getAttribute('data-job-id') || 
                         this.closest('.job-card')?.getAttribute('data-job-id');
            if (jobId) {
                await showJobDetailModal(jobId);
            }
        });
    });

    // See all link
    if (seeAllLink) {
        seeAllLink.addEventListener('click', function(e) {
            e.preventDefault();
            router.navigate('/student/job-matches');
        });
    }
}

/**
 * Show job detail modal
 */
async function showJobDetailModal(jobId) {
    try {
        UI.loading(document.body, true);
        const job = await StudentAPI.getJobById(jobId);
        
        const content = `
            <div class="job-detail-modal">
                <div class="job-detail-header">
                    <h3>${job.title || 'N/A'}</h3>
                    <p class="job-detail-company">${job.company || 'N/A'}</p>
                </div>
                <div class="job-detail-info">
                    <div class="info-row">
                        <span class="info-label">${I18n.t('location')}:</span>
                        <span>${job.homeOffice || 'N/A'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">${I18n.t('jobType')}:</span>
                        <span>${job.jobType || 'N/A'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">${I18n.t('salary')}:</span>
                        <span>${job.wage || 'N/A'}</span>
                    </div>
                    ${job.applicationDeadline ? `
                    <div class="info-row">
                        <span class="info-label">${I18n.t('deadline')}:</span>
                        <span>${new Date(job.applicationDeadline).toLocaleDateString()}</span>
                    </div>` : ''}
                </div>
                ${job.description ? `
                <div class="job-detail-section">
                    <h4>${I18n.t('description')}</h4>
                    <p>${job.description}</p>
                </div>` : ''}
                ${job.requirements ? `
                <div class="job-detail-section">
                    <h4>${I18n.t('requirements')}</h4>
                    <p>${job.requirements}</p>
                </div>` : ''}
                ${job.benefits ? `
                <div class="job-detail-section">
                    <h4>${I18n.t('benefits')}</h4>
                    <p>${job.benefits}</p>
                </div>` : ''}
            </div>
        `;

        const footer = `
            <button class="btn btn-secondary" id="saveJobBtn" data-job-id="${jobId}">${I18n.t('save')}</button>
            <button class="btn btn-primary" id="applyJobBtn" data-job-id="${jobId}" ${job.applied ? 'disabled' : ''}>
                ${job.applied ? I18n.t('applied') : I18n.t('apply')}
            </button>
        `;

        const modal = UI.modal({
            title: I18n.t('jobDetails'),
            content: content,
            footer: footer,
            size: 'large'
        });

        // Attach event listeners
        document.getElementById('saveJobBtn')?.addEventListener('click', async () => {
            await handleSaveJob(jobId, modal);
        });

        document.getElementById('applyJobBtn')?.addEventListener('click', async () => {
            await handleApplyJob(jobId, modal);
        });

    } catch (error) {
        console.error('Error loading job details:', error);
        UI.toast(I18n.t('error') + ': ' + error.message, 'error');
    } finally {
        UI.loading(document.body, false);
    }
}

/**
 * Handle save job
 */
async function handleSaveJob(jobId, modal) {
    try {
        UI.loading(document.getElementById('saveJobBtn'), true);
        await StudentAPI.saveJob(jobId);
        UI.toast(I18n.t('saveSuccess'), 'success');
        modal.close();
    } catch (error) {
        console.error('Error saving job:', error);
        UI.toast(I18n.t('error') + ': ' + error.message, 'error');
    } finally {
        UI.loading(document.getElementById('saveJobBtn'), false);
    }
}

/**
 * Handle apply to job
 */
async function handleApplyJob(jobId, modal) {
    try {
        UI.loading(document.getElementById('applyJobBtn'), true);
        await StudentAPI.applyToJob(jobId);
        UI.toast(I18n.t('applySuccess'), 'success');
        
        // Refresh dashboard data
        await loadDashboardData();
        
        modal.close();
    } catch (error) {
        console.error('Error applying to job:', error);
        UI.toast(I18n.t('error') + ': ' + error.message, 'error');
    } finally {
        UI.loading(document.getElementById('applyJobBtn'), false);
    }
}

/**
 * Setup sidebar navigation
 */
function setupSidebarNavigation() {
    const sidebarItems = document.querySelectorAll('.sidebar-item');
    
    sidebarItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const route = this.getAttribute('data-route');
            const href = this.getAttribute('href');
            
            // Remove active class from all items
            sidebarItems.forEach(i => i.classList.remove('active'));
            this.classList.add('active');
            
            // Navigate using data-route if available, otherwise use href
            if (route) {
                // Remove # from route if present
                const cleanRoute = route.startsWith('#') ? route.substring(1) : route;
                router.navigate(cleanRoute);
            } else if (href && href !== '#' && href !== 'student-dashboard.html') {
                // Fallback to href parsing
                if (href.startsWith('#')) {
                    const cleanRoute = href.substring(1);
                    router.navigate(cleanRoute);
                } else if (href.includes('job-matches') || href.includes('job')) {
                    router.navigate('/student/job-matches');
                } else if (href.includes('applications')) {
                    router.navigate('/student/applications');
                } else if (href.includes('cv')) {
                    router.navigate('/student/cv');
                } else if (href.includes('advisor')) {
                    router.navigate('/student/advisor');
                } else {
                    window.location.href = href;
                }
            }
        });
    });
}

/**
 * Setup user profile dropdown
 */
function setupUserProfile() {
    const userProfileBtn = document.getElementById('userProfileBtn');
    const userDropdownMenu = document.getElementById('userDropdownMenu');
    const profileItem = document.getElementById('profileItem');
    const notificationsItem = document.getElementById('notificationsItem');
    const logoutItem = document.getElementById('logoutItem');
    
    if (userProfileBtn && userDropdownMenu) {
        userProfileBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            userDropdownMenu.classList.toggle('active');
        });
    }
    
    if (profileItem) {
        profileItem.addEventListener('click', function(e) {
            e.preventDefault();
            userDropdownMenu.classList.remove('active');
            router.navigate('/student/profile');
        });
    }
    
    if (notificationsItem) {
        notificationsItem.addEventListener('click', function(e) {
            e.preventDefault();
            userDropdownMenu.classList.remove('active');
            router.navigate('/student/notifications');
        });
    }
    
    if (logoutItem) {
        logoutItem.addEventListener('click', function(e) {
            e.preventDefault();
            handleLogout();
        });
    }
}

/**
 * Handle logout
 */
async function handleLogout() {
    const confirmed = confirm(I18n.t('confirmLogout'));
    if (!confirmed) return;

    try {
        await StudentAPI.logout();
        clearAuthData();
        UI.toast('Logged out successfully', 'success');
        setTimeout(() => {
            window.location.href = '/pages/auth/login.html';
        }, 500);
    } catch (error) {
        console.error('Logout error:', error);
        clearAuthData();
        window.location.href = '/pages/auth/login.html';
    }
}

/**
 * Setup theme toggle
 */
function setupThemeToggle() {
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', function() {
            toggleTheme();
        });
    }
}

function toggleTheme() {
    const body = document.body;
    const themeToggle = document.getElementById('themeToggle');
    const isLightTheme = body.classList.contains('light-theme');
    
    if (isLightTheme) {
        body.classList.remove('light-theme');
        localStorage.setItem('theme', 'dark');
        if (themeToggle) themeToggle.setAttribute('title', 'Switch to light mode');
    } else {
        body.classList.add('light-theme');
        localStorage.setItem('theme', 'light');
        if (themeToggle) themeToggle.setAttribute('title', 'Switch to dark mode');
    }
}

function loadSavedTheme() {
    const savedTheme = localStorage.getItem('theme') || 'dark';
    if (savedTheme === 'light') {
        document.body.classList.add('light-theme');
    }
}

/**
 * Setup language dropdown
 */
function setupLanguageDropdown() {
    const languageBtn = document.getElementById('languageBtn');
    const languageMenu = document.getElementById('languageMenu');
    const languageOptions = document.querySelectorAll('.language-option');
    
    if (languageBtn && languageMenu) {
        languageBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            languageMenu.classList.toggle('active');
        });
    }
    
    languageOptions.forEach(option => {
        option.addEventListener('click', function() {
            const lang = this.getAttribute('data-lang');
            const langName = this.getAttribute('data-lang-name');
            
            document.getElementById('currentLanguage').textContent = langName;
            languageOptions.forEach(opt => opt.classList.remove('active'));
            this.classList.add('active');
            
            I18n.setLanguage(lang);
            languageMenu.classList.remove('active');
        });
    });
}

function loadSavedLanguage() {
    const savedLang = localStorage.getItem('language') || 'en';
    const langOption = document.querySelector(`.language-option[data-lang="${savedLang}"]`);
    
    if (langOption) {
        const langName = langOption.getAttribute('data-lang-name');
        document.getElementById('currentLanguage').textContent = langName;
        langOption.classList.add('active');
        I18n.setLanguage(savedLang);
    }
}

/**
 * Setup dashboards button (dropdown menu)
 */
function setupDashboardsButton() {
    const dashboardsBtn = document.getElementById('dashboardsBtn');
    if (dashboardsBtn) {
        dashboardsBtn.addEventListener('click', function() {
            // For now, just navigate to dashboard
            router.navigate('/student/dashboard');
        });
    }
}

/**
 * Setup assistant card - AI chat
 */
function setupAssistantCard() {
    const assistantAskBtn = document.getElementById('assistantAskBtn') || document.querySelector('.assistant-ask-btn');
    
    if (assistantAskBtn) {
        assistantAskBtn.addEventListener('click', function() {
            // Navigate to Career Advisor page
            router.navigate('/student/advisor');
        });
    }
}

/**
 * Open AI chat drawer
 */
function openAiChatDrawer() {
    const content = `
        <div class="ai-chat-drawer">
            <div class="ai-chat-messages" id="aiChatMessages">
                <div class="ai-message ai-message-bot">
                    <div class="ai-avatar">AI</div>
                    <div class="ai-text">${I18n.t('askPlaceholder')}</div>
                </div>
            </div>
            <div class="ai-chat-input-container">
                <input type="text" id="aiChatInput" class="ai-chat-input" placeholder="${I18n.t('askPlaceholder')}" />
                <button id="aiChatSend" class="ai-chat-send">${I18n.t('send')}</button>
            </div>
        </div>
    `;

    aiChatDrawer = UI.drawer({
        title: I18n.t('yourCareerAssistant'),
        content: content,
        position: 'bottom'
    });

    // Load chat history
    loadAiChatHistory();

    // Setup send button
    const sendBtn = document.getElementById('aiChatSend');
    const input = document.getElementById('aiChatInput');
    
    const sendMessage = async () => {
        const message = input.value.trim();
        if (!message) return;

        // Add user message
        addChatMessage(message, 'user');
        input.value = '';

        // Show loading
        addChatMessage(I18n.t('loading'), 'bot', true);

        try {
            const response = await StudentAPI.sendAiChat(message);
            // Remove loading message
            const messagesEl = document.getElementById('aiChatMessages');
            const loadingMsg = messagesEl.querySelector('.ai-message-loading');
            if (loadingMsg) loadingMsg.remove();

            // Add bot response
            addChatMessage(response.reply || response.message || 'No response', 'bot');
        } catch (error) {
            console.error('AI chat error:', error);
            const messagesEl = document.getElementById('aiChatMessages');
            const loadingMsg = messagesEl.querySelector('.ai-message-loading');
            if (loadingMsg) loadingMsg.remove();
            addChatMessage(I18n.t('error') + ': ' + error.message, 'bot');
        }
    };

    sendBtn.addEventListener('click', sendMessage);
    input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });
}

/**
 * Add chat message to drawer
 */
function addChatMessage(text, type = 'bot', isLoading = false) {
    const messagesEl = document.getElementById('aiChatMessages');
    if (!messagesEl) return;

    const messageEl = document.createElement('div');
    messageEl.className = `ai-message ai-message-${type} ${isLoading ? 'ai-message-loading' : ''}`;
    messageEl.innerHTML = `
        <div class="ai-avatar">${type === 'bot' ? 'AI' : 'You'}</div>
        <div class="ai-text">${text}</div>
    `;

    messagesEl.appendChild(messageEl);
    messagesEl.scrollTop = messagesEl.scrollHeight;
}

/**
 * Load AI chat history
 */
async function loadAiChatHistory() {
    try {
        const history = await StudentAPI.getAiChatHistory();
        const messagesEl = document.getElementById('aiChatMessages');
        if (!messagesEl || !history || history.length === 0) return;

        // Clear initial message
        messagesEl.innerHTML = '';

        history.forEach(item => {
            if (item.prompt) addChatMessage(item.prompt, 'user');
            if (item.response) addChatMessage(item.response, 'bot');
        });
    } catch (error) {
        console.error('Error loading chat history:', error);
    }
}

/**
 * Update notification badge
 */
function updateNotificationBadge() {
    const badge = document.querySelector('.notification-count');
    if (badge) {
        const unreadCount = notifications.filter(n => !n.read).length;
        badge.textContent = unreadCount;
        badge.style.display = unreadCount > 0 ? 'block' : 'none';
    }
}

/**
 * Load user profile
 */
function loadUserProfile() {
    const firstName = localStorage.getItem('firstName');
    const lastName = localStorage.getItem('lastName');
    const email = localStorage.getItem('userEmail');
    const role = localStorage.getItem('role') || 'STUDENT';
    
    let fullName = '';
    if (firstName && lastName) {
        fullName = `${firstName} ${lastName}`;
    } else if (firstName) {
        fullName = firstName;
    } else if (email) {
        fullName = email.split('@')[0];
    }
    
    if (fullName) {
        updateUserProfileDisplay(fullName, role);
    }
}

function updateUserProfileDisplay(fullName, role) {
    const profileNameElement = document.querySelector('.profile-name');
    const profileRoleElement = document.querySelector('.profile-role');
    const profileInitialsElement = document.querySelector('.profile-initials');
    
    if (profileNameElement) {
        profileNameElement.textContent = fullName || 'Student User';
    }
    
    if (profileRoleElement) {
        profileRoleElement.textContent = 'Student';
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

/**
 * Click outside to close dropdowns
 */
function setupClickOutside() {
    document.addEventListener('click', function(e) {
        const userDropdown = document.getElementById('userDropdownMenu');
        const languageMenu = document.getElementById('languageMenu');
        
        if (userDropdown && !e.target.closest('.user-profile-dropdown')) {
            userDropdown.classList.remove('active');
        }
        
        if (languageMenu && !e.target.closest('.language-dropdown')) {
            languageMenu.classList.remove('active');
        }
    });
}

// Inject job detail modal styles
if (!document.getElementById('job-detail-modal-styles')) {
    const style = document.createElement('style');
    style.id = 'job-detail-modal-styles';
    style.textContent = `
        .job-detail-modal { }
        .job-detail-header { margin-bottom: 20px; }
        .job-detail-header h3 { margin: 0 0 8px 0; font-size: 24px; }
        .job-detail-company { color: var(--text-secondary, #6b7280); margin: 0 0 12px 0; }
        .match-score { display: inline-block; padding: 4px 12px; background: #10b981; color: white; border-radius: 12px; font-size: 14px; }
        .job-detail-info { margin-bottom: 20px; }
        .info-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--border-color, #e5e7eb); }
        .info-label { font-weight: 600; }
        .job-detail-section { margin-top: 20px; }
        .job-detail-section h4 { margin: 0 0 8px 0; font-size: 18px; }
        .job-detail-section p { color: var(--text-secondary, #6b7280); line-height: 1.6; }
        .ai-chat-drawer { display: flex; flex-direction: column; height: 100%; }
        .ai-chat-messages { flex: 1; overflow-y: auto; padding: 16px; max-height: 400px; }
        .ai-message { display: flex; gap: 12px; margin-bottom: 16px; }
        .ai-message-user { flex-direction: row-reverse; }
        .ai-avatar { width: 32px; height: 32px; border-radius: 50%; background: var(--primary-blue, #3b82f6); color: white; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold; flex-shrink: 0; }
        .ai-message-user .ai-avatar { background: var(--text-secondary, #6b7280); }
        .ai-text { flex: 1; padding: 12px; background: var(--bg-secondary, #f3f4f6); border-radius: 8px; }
        .ai-message-user .ai-text { background: var(--primary-blue, #3b82f6); color: white; }
        .ai-chat-input-container { display: flex; gap: 8px; padding: 16px; border-top: 1px solid var(--border-color, #e5e7eb); }
        .ai-chat-input { flex: 1; padding: 12px; border: 1px solid var(--border-color, #e5e7eb); border-radius: 8px; }
        .ai-chat-send { padding: 12px 24px; background: var(--primary-blue, #3b82f6); color: white; border: none; border-radius: 8px; cursor: pointer; }
        .btn { padding: 10px 20px; border: none; border-radius: 6px; cursor: pointer; font-size: 14px; font-weight: 500; }
        .btn-primary { background: var(--primary-blue, #3b82f6); color: white; }
        .btn-secondary { background: var(--bg-secondary, #f3f4f6); color: var(--text-primary, #1f2937); }
        .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    `;
    document.head.appendChild(style);
}
