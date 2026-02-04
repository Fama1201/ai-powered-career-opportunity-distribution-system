// career-advisor.js
// Career Advisor page functionality

let chatHistory = [];

document.addEventListener('DOMContentLoaded', () => {
    initCareerAdvisor();
});

async function initCareerAdvisor() {
    loadSavedTheme();
    loadSavedLanguage();
    await loadUserProfile();
    setupEventListeners();
    await loadChatHistory();
}

function setupEventListeners() {
    // Chat input
    const chatInput = document.getElementById('chatInput');
    const sendBtn = document.getElementById('sendChatBtn');
    
    sendBtn?.addEventListener('click', sendMessage);
    chatInput?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // Auto-resize textarea
    chatInput?.addEventListener('input', function() {
        this.style.height = 'auto';
        this.style.height = Math.min(this.scrollHeight, 120) + 'px';
    });

    // Quick action buttons
    document.querySelectorAll('.quick-action-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const action = e.currentTarget.getAttribute('data-action');
            handleQuickAction(action);
        });
    });

    // Load history button
    document.getElementById('loadHistoryBtn')?.addEventListener('click', loadChatHistory);

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

async function sendMessage() {
    const chatInput = document.getElementById('chatInput');
    const message = chatInput.value.trim();
    
    if (!message) return;

    // Add user message to chat
    addMessageToChat('user', message);
    chatInput.value = '';
    chatInput.style.height = 'auto';

    // Show loading indicator
    const loadingId = addMessageToChat('ai', '...', true);

    try {
        const response = await StudentAPI.sendAiChat(message);
        removeLoadingMessage(loadingId);
        displayAiResponse(response);
    } catch (error) {
        console.error('Failed to send message:', error);
        removeLoadingMessage(loadingId);
        addMessageToChat('ai', `Error: ${error.message || 'Failed to get response. Please try again.'}`, false, true);
    }
}

function displayAiResponse(response) {
    const messagesContainer = document.getElementById('chatMessages');
    
    // Main reply
    if (response.reply) {
        addMessageToChat('ai', response.reply);
    } else if (response.summary) {
        addMessageToChat('ai', response.summary);
    }

    // CV Score
    if (response.cvScore !== null && response.cvScore !== undefined) {
        const scoreColor = response.cvScore >= 70 ? 'var(--success-green)' : 
                          response.cvScore >= 50 ? 'var(--warning-yellow)' : 
                          'var(--error-red)';
        const scoreMessage = `
            <div class="cv-score-card" style="border-left: 4px solid ${scoreColor};">
                <div class="cv-score-header">
                    <strong>CV Score: <span style="color: ${scoreColor};">${response.cvScore}/100</span></strong>
                </div>
            </div>
        `;
        addMessageToChat('ai', scoreMessage, false, false, true);
    }

    // Missing Skills
    if (response.missingSkills && response.missingSkills.length > 0) {
        const skillsMessage = `
            <div class="ai-suggestion-card">
                <h4>🔍 Missing Skills:</h4>
                <ul>
                    ${response.missingSkills.map(skill => `<li>${escapeHtml(skill)}</li>`).join('')}
                </ul>
            </div>
        `;
        addMessageToChat('ai', skillsMessage, false, false, true);
    }

    // Recommendations
    if (response.recommendations && response.recommendations.length > 0) {
        const recommendationsMessage = `
            <div class="ai-suggestion-card">
                <h4>💡 Recommendations:</h4>
                <ul>
                    ${response.recommendations.map(rec => `<li>${escapeHtml(rec)}</li>`).join('')}
                </ul>
            </div>
        `;
        addMessageToChat('ai', recommendationsMessage, false, false, true);
    }

    // Top Matches
    if (response.topMatches && response.topMatches.length > 0) {
        const matchesMessage = `
            <div class="ai-suggestion-card">
                <h4>🎯 Top Job Matches:</h4>
                <div class="top-matches-list">
                    ${response.topMatches.map(match => `
                        <div class="match-item">
                            <div class="match-header">
                                <strong>${escapeHtml(match.title || 'N/A')}</strong>
                                <span class="match-score">${match.score || 0}%</span>
                            </div>
                            <p class="match-company">${escapeHtml(match.company || 'N/A')}</p>
                            ${match.matchReason ? `<p class="match-reason">${escapeHtml(match.matchReason)}</p>` : ''}
                            ${match.url ? `<a href="${escapeHtml(match.url)}" target="_blank" class="match-link">View Job →</a>` : ''}
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
        addMessageToChat('ai', matchesMessage, false, false, true);
    }

    // Assignments
    if (response.assignments && response.assignments.length > 0) {
        const assignmentsMessage = `
            <div class="ai-suggestion-card">
                <h4>📚 Suggested Assignments:</h4>
                <div class="assignments-list">
                    ${response.assignments.map(assignment => `
                        <div class="assignment-item">
                            <div class="assignment-header">
                                <strong>${escapeHtml(assignment.title || 'Assignment')}</strong>
                                ${assignment.estimatedTime ? `<span class="assignment-time">⏱️ ${escapeHtml(assignment.estimatedTime)}</span>` : ''}
                            </div>
                            ${assignment.description ? `<p>${escapeHtml(assignment.description)}</p>` : ''}
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
        addMessageToChat('ai', assignmentsMessage, false, false, true);
    }

    scrollToBottom();
}

function handleQuickAction(action) {
    const chatInput = document.getElementById('chatInput');
    
    switch(action) {
        case 'cv-review':
            chatInput.value = 'Please review my CV and provide feedback on how to improve it.';
            break;
        case 'career-advice':
            chatInput.value = 'What career advice do you have for me based on my profile?';
            break;
        case 'skill-gaps':
            chatInput.value = 'What skills am I missing? What should I learn to improve my job prospects?';
            break;
    }
    
    chatInput.focus();
    chatInput.dispatchEvent(new Event('input'));
}

async function loadChatHistory() {
    try {
        const history = await StudentAPI.getAiChatHistory();
        chatHistory = Array.isArray(history) ? history : [];
        
        // Clear current messages (except welcome)
        const messagesContainer = document.getElementById('chatMessages');
        const welcomeMessage = messagesContainer.querySelector('.welcome-message');
        messagesContainer.innerHTML = '';
        if (welcomeMessage) {
            messagesContainer.appendChild(welcomeMessage);
        }
        
        // Display history
        if (chatHistory.length > 0) {
            chatHistory.forEach(item => {
                if (item.prompt) {
                    addMessageToChat('user', item.prompt);
                }
                if (item.response) {
                    try {
                        const response = JSON.parse(item.response);
                        displayAiResponse(response);
                    } catch (e) {
                        // If response is not JSON, display as plain text
                        addMessageToChat('ai', item.response);
                    }
                }
            });
            scrollToBottom();
        }
    } catch (error) {
        console.error('Failed to load chat history:', error);
        showToast('Failed to load chat history', 'error');
    }
}

function addMessageToChat(type, content, isLoading = false, isError = false, isHTML = false) {
    const messagesContainer = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    const messageId = `msg-${Date.now()}-${Math.random()}`;
    messageDiv.id = messageId;
    messageDiv.className = `${type}-message-bubble ${isLoading ? 'loading' : ''} ${isError ? 'error' : ''}`;
    
    if (isHTML) {
        messageDiv.innerHTML = content;
    } else {
        messageDiv.textContent = content;
    }
    
    messagesContainer.appendChild(messageDiv);
    scrollToBottom();
    
    return messageId;
}

function removeLoadingMessage(messageId) {
    const message = document.getElementById(messageId);
    if (message) {
        message.remove();
    }
}

function scrollToBottom() {
    const messagesContainer = document.getElementById('chatMessages');
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
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

