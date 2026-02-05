// hr-post-job.js
// HR Post New Job page functionality

document.addEventListener('DOMContentLoaded', () => {
    initPostJob();
});

async function initPostJob() {
    loadSavedTheme();
    loadSavedLanguage();
    await loadUserProfile();
    setupEventListeners();
}

function setupEventListeners() {
    // Form submission
    document.getElementById('postJobForm')?.addEventListener('submit', handleJobSubmit);

    // Cancel button
    document.getElementById('cancelBtn')?.addEventListener('click', () => {
        if (confirm('Are you sure you want to cancel? All unsaved changes will be lost.')) {
            navigateTo('#/hr/job-openings');
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

async function handleJobSubmit(e) {
    e.preventDefault();
    
    const submitBtn = document.getElementById('submitBtn');
    const submitBtnText = document.getElementById('submitBtnText');
    const submitBtnLoading = document.getElementById('submitBtnLoading');

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

    // Validate required fields
    if (!formData.title || !formData.company || !formData.jobType || !formData.description) {
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast('Please fill in all required fields', 'error');
        }
        return;
    }

    // Show loading state
    submitBtn.disabled = true;
    submitBtnText.style.display = 'none';
    submitBtnLoading.style.display = 'inline';

    try {
        const job = await HrAPI.createJob(formData);
        
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast('Job posted successfully!', 'success');
        }
        
        // Redirect to job openings page after a short delay
        setTimeout(() => {
            navigateTo('#/hr/job-openings');
        }, 1500);
    } catch (error) {
        console.error('Failed to post job:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to post job. Please try again.', 'error');
        }
        
        // Re-enable button on error
        submitBtn.disabled = false;
        submitBtnText.style.display = 'inline';
        submitBtnLoading.style.display = 'none';
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

