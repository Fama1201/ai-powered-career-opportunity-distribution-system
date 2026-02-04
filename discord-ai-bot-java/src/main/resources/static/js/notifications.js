// notifications.js
// Notifications page functionality

let allNotifications = [];

document.addEventListener('DOMContentLoaded', () => {
    initNotifications();
});

async function initNotifications() {
    loadSavedTheme();
    loadSavedLanguage();
    await loadUserProfile();
    setupEventListeners();
    await loadNotifications();
}

function setupEventListeners() {
    // Mark all as read button
    document.getElementById('markAllReadBtn')?.addEventListener('click', handleMarkAllRead);

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

async function loadNotifications() {
    const list = document.getElementById('notificationsList');
    const loadingState = document.getElementById('loadingState');
    const emptyState = document.getElementById('emptyState');

    if (list) {
        showLoading(list);
    }
    if (loadingState) {
        loadingState.style.display = 'block';
    }
    if (emptyState) {
        emptyState.style.display = 'none';
    }

    try {
        const notifications = await StudentAPI.getNotifications();
        allNotifications = Array.isArray(notifications) ? notifications : [];
        
        updateUnreadCount();
        renderNotifications(allNotifications);
    } catch (error) {
        console.error('Failed to load notifications:', error);
        showToast(error.message || 'Failed to load notifications. Please try again.', 'error');
        if (emptyState) {
            emptyState.style.display = 'block';
        }
    } finally {
        if (list) {
            hideLoading(list);
        }
        if (loadingState) {
            loadingState.style.display = 'none';
        }
    }
}

function renderNotifications(notifications) {
    const list = document.getElementById('notificationsList');
    const emptyState = document.getElementById('emptyState');
    const loadingState = document.getElementById('loadingState');

    // Null check for loadingState
    if (loadingState) {
        loadingState.style.display = 'none';
    }

    if (!notifications || notifications.length === 0) {
        if (list) {
            list.innerHTML = '';
        }
        if (emptyState) {
            emptyState.style.display = 'block';
        }
        return;
    }

    if (emptyState) {
        emptyState.style.display = 'none';
    }
    if (list) {
        list.innerHTML = '';
    }

    // Sort: unread first, then by date (newest first)
    const sorted = [...notifications].sort((a, b) => {
        if (a.read !== b.read) {
            return a.read ? 1 : -1; // Unread first
        }
        return new Date(b.createdAt) - new Date(a.createdAt);
    });

    sorted.forEach(notification => {
        const card = createNotificationCard(notification);
        if (list && card) {
            list.appendChild(card);
        }
    });
}

function createNotificationCard(notification) {
    const card = document.createElement('div');
    card.className = `notification-card ${notification.read ? 'read' : 'unread'}`;
    card.setAttribute('data-notification-id', notification.id);

    const date = formatDate(notification.createdAt);

    card.innerHTML = `
        <div class="notification-icon ${notification.read ? '' : 'unread-dot'}">
            <i class="fas fa-bell"></i>
        </div>
        <div class="notification-content">
            <p class="notification-message">${escapeHtml(notification.message || 'No message')}</p>
            <span class="notification-date">${date}</span>
        </div>
        ${!notification.read ? `
        <button class="notification-mark-read-btn" data-notification-id="${notification.id}" title="Mark as read">
            <i class="fas fa-check"></i>
        </button>` : ''}
    `;

    // Add event listener for mark as read button
    const markReadBtn = card.querySelector('.notification-mark-read-btn');
    if (markReadBtn) {
        markReadBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            handleMarkAsRead(notification.id, card);
        });
    }

    return card;
}

async function handleMarkAsRead(notificationId, cardElement) {
    try {
        await StudentAPI.markNotificationRead(notificationId);
        
        // Update local state
        const notification = allNotifications.find(n => n.id === notificationId);
        if (notification) {
            notification.read = true;
        }
        
        // Update UI
        cardElement.classList.remove('unread');
        cardElement.classList.add('read');
        cardElement.querySelector('.notification-icon')?.classList.remove('unread-dot');
        const markReadBtn = cardElement.querySelector('.notification-mark-read-btn');
        if (markReadBtn) {
            markReadBtn.remove();
        }
        
        updateUnreadCount();
        showToast('Notification marked as read', 'success');
    } catch (error) {
        console.error('Failed to mark notification as read:', error);
        showToast(error.message || 'Failed to mark notification as read. Please try again.', 'error');
    }
}

async function handleMarkAllRead() {
    const button = document.getElementById('markAllReadBtn');
    if (!button) return;

    const originalText = button.innerHTML;
    showLoading(button);
    button.disabled = true;

    try {
        await StudentAPI.markAllNotificationsRead();
        
        // Update local state
        if (Array.isArray(allNotifications)) {
            allNotifications.forEach(n => {
                if (n) n.read = true;
            });
        }
        
        // Re-render notifications
        renderNotifications(allNotifications || []);
        updateUnreadCount();
        
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast('All notifications marked as read', 'success');
        } else {
            console.log('All notifications marked as read');
        }
    } catch (error) {
        console.error('Failed to mark all notifications as read:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to mark all notifications as read. Please try again.', 'error');
        } else {
            console.error('Failed to mark all notifications as read:', error);
        }
    } finally {
        hideLoading(button);
        button.disabled = false;
        button.innerHTML = originalText;
    }
}

function updateUnreadCount() {
    const unreadCount = allNotifications.filter(n => !n.read).length;
    const unreadCountElement = document.getElementById('unreadCount');
    const notificationBadge = document.querySelector('.notification-count');
    
    if (unreadCountElement) {
        unreadCountElement.textContent = unreadCount;
    }
    
    if (notificationBadge) {
        notificationBadge.textContent = unreadCount;
        if (unreadCount === 0) {
            notificationBadge.style.display = 'none';
        } else {
            notificationBadge.style.display = 'inline-flex';
        }
    }
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        const now = new Date();
        const diffMs = now - date;
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);

        if (diffMins < 1) {
            return 'Just now';
        } else if (diffMins < 60) {
            return `${diffMins} ${diffMins === 1 ? 'minute' : 'minutes'} ago`;
        } else if (diffHours < 24) {
            return `${diffHours} ${diffHours === 1 ? 'hour' : 'hours'} ago`;
        } else if (diffDays < 7) {
            return `${diffDays} ${diffDays === 1 ? 'day' : 'days'} ago`;
        } else {
            return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
        }
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

