// hr-profile.js
// HR Profile page functionality

let originalProfileData = null;

document.addEventListener('DOMContentLoaded', () => {
    initProfile();
});

async function initProfile() {
    loadSavedTheme();
    loadSavedLanguage();
    await loadUserProfile();
    setupEventListeners();
    await loadProfileData();
}

function setupEventListeners() {
    // Form submission
    document.getElementById('profileForm')?.addEventListener('submit', handleSaveProfile);
    
    // Cancel button
    document.getElementById('cancelBtn')?.addEventListener('click', () => {
        if (originalProfileData) {
            populateForm(originalProfileData);
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

async function loadProfileData() {
    try {
        // Try to get HR profile from localStorage first
        const email = localStorage.getItem(CONFIG.STORAGE_KEYS.USER_EMAIL);
        const firstName = localStorage.getItem(CONFIG.STORAGE_KEYS.FIRST_NAME);
        const lastName = localStorage.getItem(CONFIG.STORAGE_KEYS.LAST_NAME);
        const companyName = localStorage.getItem('hr_company_name') || '';

        // For now, use localStorage data since HR profile API endpoint may not exist
        const profile = {
            fullName: firstName && lastName ? `${firstName} ${lastName}` : (firstName || email?.split('@')[0] || 'HR Professional'),
            email: email || '',
            companyName: companyName
        };

        originalProfileData = { ...profile };
        populateForm(profile);
    } catch (error) {
        console.error('Failed to load profile:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load profile. Please try again.', 'error');
        }
    }
}

function populateForm(profile) {
    document.getElementById('profileName').value = profile.fullName || '';
    document.getElementById('profileEmail').value = profile.email || '';
    document.getElementById('profileCompany').value = profile.companyName || '';
}

async function handleSaveProfile(e) {
    e.preventDefault();
    
    const saveBtn = document.getElementById('saveBtn');
    const saveBtnText = document.getElementById('saveBtnText');
    const saveBtnLoading = document.getElementById('saveBtnLoading');
    
    const formData = {
        fullName: document.getElementById('profileName').value.trim(),
        companyName: document.getElementById('profileCompany').value.trim()
    };

    // Show loading state
    saveBtn.disabled = true;
    saveBtnText.style.display = 'none';
    saveBtnLoading.style.display = 'inline';

    try {
        // For now, save to localStorage since HR profile API endpoint may not exist
        // TODO: Implement HR profile API endpoint: PUT /api/hr/profile
        const fullNameParts = formData.fullName.split(' ');
        const firstName = fullNameParts[0] || '';
        const lastName = fullNameParts.slice(1).join(' ') || '';

        localStorage.setItem(CONFIG.STORAGE_KEYS.FIRST_NAME, firstName);
        localStorage.setItem(CONFIG.STORAGE_KEYS.LAST_NAME, lastName);
        localStorage.setItem('hr_company_name', formData.companyName);

        originalProfileData = {
            fullName: formData.fullName,
            email: document.getElementById('profileEmail').value,
            companyName: formData.companyName
        };
        
        // Update header profile display
        await loadUserProfile();
        
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast('Profile updated successfully!', 'success');
        }
    } catch (error) {
        console.error('Failed to update profile:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to update profile. Please try again.', 'error');
        }
    } finally {
        saveBtn.disabled = false;
        saveBtnText.style.display = 'inline';
        saveBtnLoading.style.display = 'none';
    }
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

