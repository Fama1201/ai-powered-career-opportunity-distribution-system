/**
 * Code Practice Page - JavaScript
 * Handles assignment creation, code submission, and AI feedback
 */

let codeEditor = null;
let currentAssignment = null;
let assignments = [];

// CodeMirror mode mapping
const codeMirrorModes = {
    'c': 'text/x-csrc',
    'cpp': 'text/x-c++src',
    'java': 'text/x-java',
    'python': 'python',
    'javascript': 'javascript',
    'csharp': 'text/x-csharp',
    'go': 'go',
    'rust': 'rust'
};

// Initialize page
document.addEventListener('DOMContentLoaded', () => {
    initializePage();
});

function initializePage() {
    setupEventListeners();
    loadAssignments();
    setupUI();
}

function setupUI() {
    // Setup header dropdowns
    setupHeaderDropdowns();
    
    // Setup sidebar navigation
    setupSidebarNavigation();
    
    // Setup theme toggle
    setupThemeToggle();
    
    // Setup i18n
    if (typeof setupI18n === 'function') {
        setupI18n();
    }
    
    // Load user profile
    loadUserProfile();
    
    // Load notifications
    loadNotifications();
    
    // Load saved theme
    loadSavedTheme();
}

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
    
    // Update CodeMirror theme if editor exists
    if (codeEditor) {
        const isLight = document.body.classList.contains('light-theme');
        codeEditor.setOption('theme', isLight ? 'default' : 'dracula');
    }
    
    // Dispatch event for other components
    document.dispatchEvent(new CustomEvent('themeChanged'));
}

function loadSavedTheme() {
    const savedTheme = localStorage.getItem('theme') || 'dark';
    if (savedTheme === 'light') {
        document.body.classList.add('light-theme');
    }
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.setAttribute('title', savedTheme === 'light' ? 'Switch to dark mode' : 'Switch to light mode');
    }
}

function setupEventListeners() {
    // Create assignment button
    const createBtn = document.getElementById('createAssignmentBtn');
    if (createBtn) {
        createBtn.addEventListener('click', handleCreateAssignment);
    }
    
    // Refresh assignments button
    const refreshBtn = document.getElementById('refreshAssignmentsBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', loadAssignments);
    }
    
    // Setup select elements for placeholder visibility
    setupSelectPlaceholders();
    
    // Modal close buttons
    const closeModal = document.getElementById('closeAssignmentModal');
    if (closeModal) {
        closeModal.addEventListener('click', closeAssignmentModal);
    }
    
    const cancelBtn = document.getElementById('cancelAssignmentBtn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', closeAssignmentModal);
    }
    
    // Submit code button
    const submitBtn = document.getElementById('submitCodeBtn');
    if (submitBtn) {
        submitBtn.addEventListener('click', handleSubmitCode);
    }
    
    // Clear code button
    const clearBtn = document.getElementById('clearCodeBtn');
    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            if (codeEditor) {
                codeEditor.setValue('');
            }
        });
    }
    
    // Close modal on overlay click
    const modal = document.getElementById('assignmentModal');
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeAssignmentModal();
            }
        });
    }
}

async function handleCreateAssignment() {
    const language = document.getElementById('languageSelect').value;
    const level = document.getElementById('levelSelect').value;
    const category = document.getElementById('categorySelect').value || null;
    
    if (!language || !level) {
        showToast('Please select both language and difficulty level', 'error');
        return;
    }
    
    const btn = document.getElementById('createAssignmentBtn');
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<div class="loading-spinner-small"></div> Generating...';
    
    try {
        const newAssignments = await StudentAPI.createAssignments(language, level, category);
        showToast(`Successfully created ${newAssignments.length} assignments!`, 'success');
        
        // Clear form
        document.getElementById('languageSelect').value = '';
        document.getElementById('levelSelect').value = '';
        document.getElementById('categorySelect').value = '';
        
        // Reload assignments
        await loadAssignments();
    } catch (error) {
        console.error('Error creating assignments:', error);
        showToast(error.message || 'Failed to create assignments', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}

async function loadAssignments() {
    const grid = document.getElementById('assignmentsGrid');
    const completedGrid = document.getElementById('completedAssignmentsGrid');
    const completedSection = document.getElementById('completedSection');
    const completedEmptyState = document.getElementById('completedEmptyState');
    const loadingState = document.getElementById('loadingState');
    const emptyState = document.getElementById('emptyState');
    
    if (loadingState) loadingState.style.display = 'block';
    if (emptyState) emptyState.style.display = 'none';
    if (grid) grid.innerHTML = '';
    if (completedGrid) completedGrid.innerHTML = '';
    if (completedSection) completedSection.style.display = 'none';
    if (completedEmptyState) completedEmptyState.style.display = 'none';
    
    try {
        assignments = await StudentAPI.getAssignments();
        
        if (loadingState) loadingState.style.display = 'none';
        
        if (!assignments || assignments.length === 0) {
            if (emptyState) emptyState.style.display = 'block';
            return;
        }
        
        // Separate active and completed assignments
        const activeAssignments = assignments.filter(a => a.status !== 'REVIEWED');
        const completedAssignments = assignments.filter(a => a.status === 'REVIEWED');
        
        // Render active assignments
        if (grid) {
            if (activeAssignments.length === 0) {
                if (emptyState) emptyState.style.display = 'block';
            } else {
                grid.innerHTML = activeAssignments.map(assignment => createAssignmentCard(assignment)).join('');
                // Setup card click handlers
                document.querySelectorAll('#assignmentsGrid .assignment-card').forEach(card => {
                    card.addEventListener('click', () => {
                        const assignmentId = parseInt(card.dataset.assignmentId);
                        openAssignmentModal(assignmentId);
                    });
                });
            }
        }
        
        // Render completed assignments
        if (completedAssignments.length > 0) {
            if (completedSection) completedSection.style.display = 'block';
            if (completedGrid) {
                completedGrid.innerHTML = completedAssignments.map(assignment => createAssignmentCard(assignment)).join('');
                // Setup card click handlers
                document.querySelectorAll('#completedAssignmentsGrid .assignment-card').forEach(card => {
                    card.addEventListener('click', () => {
                        const assignmentId = parseInt(card.dataset.assignmentId);
                        openAssignmentModal(assignmentId);
                    });
                });
            }
        } else {
            if (completedEmptyState) completedEmptyState.style.display = 'block';
        }
    } catch (error) {
        console.error('Error loading assignments:', error);
        if (loadingState) loadingState.style.display = 'none';
        showToast(error.message || 'Failed to load assignments', 'error');
    }
}

function createAssignmentCard(assignment) {
    const statusColors = {
        'ASSIGNED': 'orange',
        'SUBMITTED': 'blue',
        'REVIEWED': 'green'
    };
    
    const statusLabels = {
        'ASSIGNED': 'Not Started',
        'SUBMITTED': 'Submitted',
        'REVIEWED': 'Reviewed'
    };
    
    const levelColors = {
        'beginner': '#10b981',
        'moderate': '#f59e0b',
        'advanced': '#ef4444',
        'expert': '#8b5cf6'
    };
    
    const statusColor = statusColors[assignment.status] || 'gray';
    const statusLabel = statusLabels[assignment.status] || assignment.status;
    const levelColor = levelColors[assignment.level] || '#6b7280';
    
    return `
        <div class="assignment-card" data-assignment-id="${assignment.id}">
            <div class="assignment-header">
                <div class="assignment-title-section">
                    <h3 class="assignment-title">${escapeHtml(assignment.title || 'Untitled Assignment')}</h3>
                    <div class="assignment-meta">
                        <span class="assignment-language">${escapeHtml(assignment.language.toUpperCase())}</span>
                        <span class="assignment-level" style="background: ${levelColor}20; color: ${levelColor};">
                            ${escapeHtml(assignment.level.charAt(0).toUpperCase() + assignment.level.slice(1))}
                        </span>
                        ${assignment.category ? `<span class="assignment-category">${escapeHtml(assignment.category)}</span>` : ''}
                    </div>
                </div>
                <div class="assignment-status">
                    <span class="status-badge" style="background: ${statusColor}20; color: ${statusColor};">
                        ${statusLabel}
                    </span>
                </div>
            </div>
            <div class="assignment-description">
                <p>${escapeHtml(assignment.description || 'No description available')}</p>
            </div>
            <div class="assignment-footer">
                ${assignment.estimatedTime ? `<span class="assignment-time"><i class="fas fa-clock"></i> ${escapeHtml(assignment.estimatedTime)}</span>` : ''}
                <span class="assignment-date">
                    <i class="fas fa-calendar"></i> ${formatDate(assignment.createdAt)}
                </span>
            </div>
        </div>
    `;
}

async function openAssignmentModal(assignmentId) {
    const assignment = assignments.find(a => a.id === assignmentId);
    if (!assignment) {
        showToast('Assignment not found', 'error');
        return;
    }
    
    currentAssignment = assignment;
    const modal = document.getElementById('assignmentModal');
    const title = document.getElementById('assignmentModalTitle');
    const info = document.getElementById('assignmentInfo');
    const feedbackSection = document.getElementById('feedbackSection');
    const feedbackContent = document.getElementById('feedbackContent');
    
    if (title) {
        title.textContent = assignment.title || 'Assignment';
    }
    
    if (info) {
        info.innerHTML = `
            <div class="assignment-detail-item">
                <label>Language:</label>
                <span>${escapeHtml(assignment.language.toUpperCase())}</span>
            </div>
            <div class="assignment-detail-item">
                <label>Level:</label>
                <span>${escapeHtml(assignment.level.charAt(0).toUpperCase() + assignment.level.slice(1))}</span>
            </div>
            ${assignment.category ? `
                <div class="assignment-detail-item">
                    <label>Category:</label>
                    <span>${escapeHtml(assignment.category)}</span>
                </div>
            ` : ''}
            ${assignment.estimatedTime ? `
                <div class="assignment-detail-item">
                    <label>Estimated Time:</label>
                    <span>${escapeHtml(assignment.estimatedTime)}</span>
                </div>
            ` : ''}
            <div class="assignment-description-full">
                <label>Description:</label>
                <p>${escapeHtml(assignment.description || 'No description available')}</p>
            </div>
        `;
    }
    
    // Hide feedback initially
    if (feedbackSection) feedbackSection.style.display = 'none';
    if (feedbackContent) feedbackContent.innerHTML = '';
    
    // Initialize code editor
    initializeCodeEditor(assignment.language);
    
    // Try to load submission from backend
    try {
        const submission = await StudentAPI.getSubmission(assignmentId);
        if (submission && submission.code && codeEditor) {
            codeEditor.setValue(submission.code);
            // Also save to localStorage as backup
            localStorage.setItem(`assignment_code_${assignmentId}`, submission.code);
            
            // If there's feedback, show it
            if (submission.feedback) {
                try {
                    const feedback = JSON.parse(submission.feedback);
                    displayFeedback(feedback);
                } catch (e) {
                    console.warn('Could not parse feedback JSON:', e);
                }
            }
        } else {
            // Try to load from localStorage as fallback
            const savedCode = localStorage.getItem(`assignment_code_${assignmentId}`);
            if (savedCode && codeEditor) {
                codeEditor.setValue(savedCode);
            }
        }
    } catch (error) {
        console.warn('Could not load submission:', error);
        // Try to load from localStorage as fallback
        const savedCode = localStorage.getItem(`assignment_code_${assignmentId}`);
        if (savedCode && codeEditor) {
            codeEditor.setValue(savedCode);
        }
    }
    
    // Show modal
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

function initializeCodeEditor(language) {
    const textarea = document.getElementById('codeEditor');
    if (!textarea) return;
    
    // Destroy existing editor if any
    if (codeEditor) {
        const editorContainer = codeEditor.getWrapperElement();
        editorContainer.parentNode.replaceChild(textarea, editorContainer);
        codeEditor = null;
    }
    
    // Get CodeMirror mode
    const mode = codeMirrorModes[language.toLowerCase()] || 'text/plain';
    
    // Create new editor
    codeEditor = CodeMirror.fromTextArea(textarea, {
        lineNumbers: true,
        mode: mode,
        theme: document.body.classList.contains('light-theme') ? 'default' : 'dracula',
        indentUnit: 4,
        indentWithTabs: false,
        lineWrapping: true,
        autofocus: true
    });
    
    // Set default code template based on language (only if no saved code exists)
    const savedCode = currentAssignment ? localStorage.getItem(`assignment_code_${currentAssignment.id}`) : null;
    if (!savedCode) {
        const templates = {
            'c': '#include <stdio.h>\n\nint main() {\n    // Your code here\n    return 0;\n}',
            'cpp': '#include <iostream>\n\nint main() {\n    // Your code here\n    return 0;\n}',
            'java': 'public class Main {\n    public static void main(String[] args) {\n        // Your code here\n    }\n}',
            'python': '# Your code here\n',
            'javascript': '// Your code here\n',
            'csharp': 'using System;\n\nclass Program {\n    static void Main() {\n        // Your code here\n    }\n}',
            'go': 'package main\n\nimport "fmt"\n\nfunc main() {\n    // Your code here\n}',
            'rust': 'fn main() {\n    // Your code here\n}'
        };
        
        const template = templates[language.toLowerCase()] || '';
        codeEditor.setValue(template);
    }
    
    // Auto-save code to localStorage on change
    if (currentAssignment) {
        codeEditor.on('change', () => {
            const code = codeEditor.getValue();
            localStorage.setItem(`assignment_code_${currentAssignment.id}`, code);
        });
    }
}

async function handleSubmitCode() {
    if (!codeEditor || !currentAssignment) {
        showToast('Please select an assignment first', 'error');
        return;
    }
    
    const code = codeEditor.getValue().trim();
    if (!code) {
        showToast('Please write some code before submitting', 'error');
        return;
    }
    
    if (code.length > 8000) {
        showToast('Code is too long (max 8000 characters)', 'error');
        return;
    }
    
    const btn = document.getElementById('submitCodeBtn');
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<div class="loading-spinner-small"></div> Reviewing...';
    
    try {
        const review = await StudentAPI.submitCodeReview(
            code,
            currentAssignment.language,
            currentAssignment.id
        );
        
        // Show feedback
        displayFeedback(review);
        
        // Save code to localStorage
        if (currentAssignment) {
            localStorage.setItem(`assignment_code_${currentAssignment.id}`, code);
        }
        
        // Reload assignments to update status
        await loadAssignments();
        
        showToast('Code submitted successfully!', 'success');
    } catch (error) {
        console.error('Error submitting code:', error);
        showToast(error.message || 'Failed to submit code for review', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}

function displayFeedback(review) {
    const feedbackSection = document.getElementById('feedbackSection');
    const feedbackContent = document.getElementById('feedbackContent');
    
    if (!feedbackSection || !feedbackContent) return;
    
    let html = '';
    
    // Summary
    if (review.summary) {
        html += `
            <div class="feedback-summary">
                <h4>Summary</h4>
                <p>${escapeHtml(review.summary)}</p>
            </div>
        `;
    }
    
    // Issues
    if (review.issues && review.issues.length > 0) {
        html += `
            <div class="feedback-issues">
                <h4>Issues Found</h4>
                <div class="issues-list">
                    ${review.issues.map(issue => `
                        <div class="issue-item issue-${issue.severity?.toLowerCase() || 'info'}">
                            <div class="issue-header">
                                <span class="issue-severity">${escapeHtml(issue.severity || 'INFO')}</span>
                                <span class="issue-title">${escapeHtml(issue.title || 'Issue')}</span>
                            </div>
                            <p class="issue-description">${escapeHtml(issue.description || '')}</p>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }
    
    // Suggestions
    if (review.suggestions && review.suggestions.length > 0) {
        html += `
            <div class="feedback-suggestions">
                <h4>Suggestions</h4>
                <ul class="suggestions-list">
                    ${review.suggestions.map(suggestion => `
                        <li>${escapeHtml(suggestion)}</li>
                    `).join('')}
                </ul>
            </div>
        `;
    }
    
    feedbackContent.innerHTML = html;
    feedbackSection.style.display = 'block';
    
    // Scroll to feedback
    feedbackSection.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function closeAssignmentModal() {
    const modal = document.getElementById('assignmentModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }
    
    currentAssignment = null;
    if (codeEditor) {
        const editorContainer = codeEditor.getWrapperElement();
        editorContainer.parentNode.replaceChild(
            document.getElementById('codeEditor'),
            editorContainer
        );
        codeEditor = null;
    }
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Setup select placeholders for better visibility
function setupSelectPlaceholders() {
    const selects = document.querySelectorAll('.form-select');
    selects.forEach(select => {
        // Add required attribute to make :invalid work (except for optional fields)
        const isOptional = select.id === 'categorySelect';
        if (!isOptional && !select.hasAttribute('required')) {
            select.setAttribute('required', '');
        }
        
        // Update style on change
        select.addEventListener('change', function() {
            updateSelectStyle(this);
        });
        
        // Update style on focus/blur
        select.addEventListener('focus', function() {
            updateSelectStyle(this);
        });
        
        // Initial update
        updateSelectStyle(select);
    });
}

function updateSelectStyle(select) {
    const isLightTheme = document.body.classList.contains('light-theme');
    const isEmpty = !select.value || select.value === '';
    
    if (isLightTheme) {
        if (isEmpty) {
            // Placeholder state - darker gray for visibility
            select.style.color = '#475569';
            select.style.webkitTextFillColor = '#475569';
            select.style.setProperty('color', '#475569', 'important');
        } else {
            // Selected value - dark
            select.style.color = '#0f172a';
            select.style.webkitTextFillColor = '#0f172a';
            select.style.setProperty('color', '#0f172a', 'important');
        }
    } else {
        // Dark theme - reset to default
        select.style.color = '';
        select.style.webkitTextFillColor = '';
        select.style.removeProperty('color');
    }
}

// Update CodeMirror theme when theme changes
document.addEventListener('themeChanged', () => {
    if (codeEditor) {
        const isLight = document.body.classList.contains('light-theme');
        codeEditor.setOption('theme', isLight ? 'default' : 'dracula');
    }
    
    // Update select styles when theme changes
    document.querySelectorAll('.form-select').forEach(select => {
        updateSelectStyle(select);
    });
});

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

// Setup header dropdowns
function setupHeaderDropdowns() {
    // Language dropdown
    const languageBtn = document.getElementById('languageBtn');
    const languageMenu = document.getElementById('languageMenu');
    
    if (languageBtn && languageMenu) {
        languageBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            languageMenu.classList.toggle('active');
        });
        
        document.addEventListener('click', (e) => {
            if (!languageBtn.contains(e.target) && !languageMenu.contains(e.target)) {
                languageMenu.classList.remove('active');
            }
        });
        
        document.querySelectorAll('.language-option').forEach(option => {
            option.addEventListener('click', (e) => {
                const lang = e.currentTarget.getAttribute('data-lang');
                const langName = e.currentTarget.getAttribute('data-lang-name');
                if (lang && langName) {
                    document.getElementById('currentLanguage').textContent = langName;
                    localStorage.setItem('language', lang);
                    if (typeof setupI18n === 'function') {
                        setupI18n();
                    }
                }
                languageMenu.classList.remove('active');
            });
        });
    }
    
    // User profile dropdown
    const userProfileBtn = document.getElementById('userProfileBtn');
    const userDropdownMenu = document.getElementById('userDropdownMenu');
    
    if (userProfileBtn && userDropdownMenu) {
        userProfileBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            userDropdownMenu.classList.toggle('active');
        });
        
        document.addEventListener('click', (e) => {
            if (!userProfileBtn.contains(e.target) && !userDropdownMenu.contains(e.target)) {
                userDropdownMenu.classList.remove('active');
            }
        });
        
        // Logout handler
        const logoutItem = document.getElementById('logoutItem');
        if (logoutItem) {
            logoutItem.addEventListener('click', (e) => {
                e.preventDefault();
                handleLogout();
            });
        }
    }
}

// Setup sidebar navigation
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
            
            // Navigate using router if available
            if (typeof router !== 'undefined' && router) {
                if (route) {
                    const cleanRoute = route.startsWith('#') ? route.substring(1) : route;
                    router.navigate(cleanRoute);
                } else if (href && href.startsWith('#')) {
                    const cleanRoute = href.substring(1);
                    router.navigate(cleanRoute);
                }
            } else if (href && href !== '#') {
                window.location.href = href;
            }
        });
    });
}

// Logout handler
function handleLogout() {
    // Clear all localStorage data
    localStorage.clear();
    sessionStorage.clear();
    
    // Redirect to login page
    window.location.href = '/login.html';
}

// Load notifications
async function loadNotifications() {
    try {
        const notifications = await StudentAPI.getNotifications();
        const unreadCount = notifications.filter(n => !n.read).length;
        const notificationBadge = document.querySelector('.notification-count');
        if (notificationBadge) {
            notificationBadge.textContent = unreadCount > 0 ? unreadCount : '0';
            notificationBadge.style.display = unreadCount > 0 ? 'flex' : 'none';
        }
    } catch (error) {
        console.error('Error loading notifications:', error);
    }
}

