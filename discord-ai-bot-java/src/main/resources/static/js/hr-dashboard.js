// HR Dashboard JavaScript

document.addEventListener('DOMContentLoaded', function () {
    initializeDashboard();
    loadSavedTheme();
    loadSavedLanguage();
    loadUserProfile();
});

function initializeDashboard() {
    setupSidebarNavigation();
    setupUserProfile();
    setupThemeToggle();
    setupLanguageDropdown();
    setupDashboardsButton();
    setupJobCards();
    setupAssistantCard();
    setupClickOutside();
}

// Load user profile information from backend or localStorage
function loadUserProfile() {
    // Try to get from localStorage first (from login)
    const fullName = localStorage.getItem('hrFullName');
    const email = localStorage.getItem('email');
    const role = localStorage.getItem('role');
    
    // Ensure role is HR for HR dashboard
    const hrRole = (role === 'HR' || role === 'hr') ? 'HR' : 'HR';
    
    console.log('Loading HR profile - fullName:', fullName, 'email:', email, 'role:', role);
    
    if (fullName) {
        updateUserProfileDisplay(fullName, hrRole);
    } else if (email) {
        // If fullName not in localStorage, try to get from backend
        // For now, use email as fallback
        updateUserProfileDisplay(email, hrRole);
    } else {
        // Fallback if nothing is available
        updateUserProfileDisplay('HR User', hrRole);
    }
}

// Update user profile display in header
function updateUserProfileDisplay(fullName, role) {
    const profileNameElement = document.querySelector('.profile-name');
    const profileRoleElement = document.querySelector('.profile-role');
    const profileInitialsElement = document.querySelector('.profile-initials');
    
    if (profileNameElement) {
        profileNameElement.textContent = fullName || 'HR User';
    }
    
    if (profileRoleElement) {
        // Always show HR Professional for HR dashboard
        profileRoleElement.textContent = 'HR Professional';
    }
    
    if (profileInitialsElement && fullName) {
        // Extract initials from full name
        const names = fullName.trim().split(' ');
        let initials = '';
        if (names.length >= 2) {
            initials = (names[0].charAt(0) + names[names.length - 1].charAt(0)).toUpperCase();
        } else if (names.length === 1) {
            initials = names[0].substring(0, 2).toUpperCase();
        }
        profileInitialsElement.textContent = initials || 'HR';
    }
    
    // Update welcome message with user's first name
    const welcomeUserNameElement = document.querySelector('.user-name');
    if (welcomeUserNameElement && fullName) {
        // Extract first name from full name
        const firstName = fullName.trim().split(' ')[0];
        welcomeUserNameElement.textContent = firstName || 'HR Manager';
    }
}

// Sidebar Navigation
function setupSidebarNavigation() {
    const sidebarItems = document.querySelectorAll('.sidebar-item');

    sidebarItems.forEach(item => {
        item.addEventListener('click', function (e) {
            e.preventDefault();
            
            // Get route from data-route attribute or href
            const route = this.getAttribute('data-route') || this.getAttribute('href');
            
            if (route && route.startsWith('#/')) {
                // Use router if available
                if (typeof router !== 'undefined' && router.navigate) {
                    router.navigate(route.substring(1)); // Remove # prefix
                } else {
                    // Fallback: use hash navigation
                    window.location.hash = route;
                }
            } else if (route && !route.startsWith('#')) {
                // Direct href navigation
                window.location.href = route;
            }
            
            // Remove active class from all items
            sidebarItems.forEach(i => i.classList.remove('active'));
            // Add active class to clicked item
            this.classList.add('active');
        });
    });
}

// User Profile Dropdown
function setupUserProfile() {
    const userProfileBtn = document.getElementById('userProfileBtn');
    const userDropdownMenu = document.getElementById('userDropdownMenu');

    if (userProfileBtn && userDropdownMenu) {
        userProfileBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            userDropdownMenu.classList.toggle('active');
        });
    }

    // Profile menu items
    const profileItem = document.getElementById('profileItem');
    const notificationsItem = document.getElementById('notificationsItem');
    const logoutItem = document.getElementById('logoutItem');

    if (profileItem) {
        profileItem.addEventListener('click', function (e) {
            e.preventDefault();
            userDropdownMenu.classList.remove('active');
            // Navigate to profile page using router
            if (typeof router !== 'undefined' && router.navigate) {
                router.navigate('/hr/profile');
            } else {
                // Fallback: use hash navigation
                window.location.hash = '#/hr/profile';
            }
        });
    }

    if (notificationsItem) {
        notificationsItem.addEventListener('click', function (e) {
            e.preventDefault();
            userDropdownMenu.classList.remove('active');
            // Navigate to notifications page using router
            if (typeof router !== 'undefined' && router.navigate) {
                router.navigate('/hr/notifications');
            } else {
                // Fallback: use hash navigation
                window.location.hash = '#/hr/notifications';
            }
        });
    }

    if (logoutItem) {
        logoutItem.addEventListener('click', function (e) {
            e.preventDefault();
            // Clear all authentication data
            if (typeof clearAuthData === 'function') {
                clearAuthData();
            } else {
                // Fallback: clear all possible auth keys
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('userId');
                localStorage.removeItem('email');
                localStorage.removeItem('role');
                localStorage.removeItem('userToken');
                localStorage.removeItem('userData');
            }
            // Redirect to login page (absolute path)
            window.location.href = '/pages/auth/login.html';
        });
    }
}

// Theme Toggle
function setupThemeToggle() {
    const themeToggle = document.getElementById('themeToggle');

    if (themeToggle) {
        themeToggle.addEventListener('click', function () {
            toggleTheme();
        });
    }
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

function loadSavedTheme() {
    const savedTheme = localStorage.getItem('theme') || 'dark';
    if (savedTheme === 'light') {
        document.body.classList.add('light-theme');
    }
}

// Language Dropdown
function setupLanguageDropdown() {
    const languageBtn = document.getElementById('languageBtn');
    const languageMenu = document.getElementById('languageMenu');
    const languageOptions = document.querySelectorAll('.language-option');

    if (languageBtn && languageMenu) {
        languageBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            languageMenu.classList.toggle('active');
        });
    }

    // Language options
    languageOptions.forEach(option => {
        option.addEventListener('click', function () {
            const lang = this.getAttribute('data-lang');
            const langName = this.getAttribute('data-lang-name');

            // Update current language display
            document.getElementById('currentLanguage').textContent = langName;

            // Remove active class from all options
            languageOptions.forEach(opt => opt.classList.remove('active'));
            // Add active class to selected option
            this.classList.add('active');

            // Save language preference
            localStorage.setItem('language', lang);

            // Close menu
            languageMenu.classList.remove('active');

            // Apply language translation
            applyLanguage(lang);
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
        applyLanguage(savedLang);
    }
}

// Language translations
const translations = {
    en: {
        profile: 'Profile',
        notifications: 'Notifications',
        logout: 'Logout',
        welcome: 'Welcome HR Manager',
        dashboard: 'Dashboard',
        jobMatches: 'Job Openings',
        myApplications: 'Candidates',
        cvManagement: 'Hires',
        careerAdvisor: 'Analytics',
        applied: 'Job Openings',
        interview: 'New Candidates',
        rejected: 'Interviews',
        newJobMatches: 'Recent Job Postings',
        seeAll: 'See all',
        yourCareerAssistant: 'HR Assistant',
        ask: 'Ask...',
        fullTime: 'Full-Time'
    },
    cs: {
        profile: 'Profil',
        notifications: 'Notifikace',
        logout: 'Odhlásit se',
        welcome: 'Vítejte v Jobify',
        dashboard: 'Nástěnka',
        jobMatches: 'Shody práce',
        myApplications: 'Moje přihlášky',
        cvManagement: 'Správa CV',
        careerAdvisor: 'Kariérní poradce',
        applied: 'Přihlášeno',
        interview: 'Pohovor',
        rejected: 'Odmítnuto',
        newJobMatches: 'Nové shody práce',
        seeAll: 'Zobrazit vše',
        yourCareerAssistant: 'Váš kariérní asistent',
        ask: 'Zeptat se...',
        fullTime: 'Plný úvazek'
    },
    tr: {
        profile: 'Profil',
        notifications: 'Bildirimler',
        logout: 'Çıkış Yap',
        welcome: 'Hoş Geldiniz İK Yöneticisi',
        dashboard: 'Kontrol Paneli',
        jobMatches: 'Açık Pozisyonlar',
        myApplications: 'Adaylar',
        cvManagement: 'İşe Alımlar',
        careerAdvisor: 'Analizler',
        applied: 'Açık Pozisyonlar',
        interview: 'Yeni Adaylar',
        rejected: 'Mülakatlar',
        newJobMatches: 'Son İş İlanları',
        seeAll: 'Tümünü Gör',
        yourCareerAssistant: 'İK Asistanı',
        ask: 'Sor...',
        fullTime: 'Tam Zamanlı'
    },
    es: {
        profile: 'Perfil',
        notifications: 'Notificaciones',
        logout: 'Cerrar sesión',
        welcome: 'Bienvenido a Jobify',
        dashboard: 'Panel',
        jobMatches: 'Coincidencias de trabajo',
        myApplications: 'Mis solicitudes',
        cvManagement: 'Gestión de CV',
        careerAdvisor: 'Asesor de carrera',
        applied: 'Aplicado',
        interview: 'Entrevista',
        rejected: 'Rechazado',
        newJobMatches: 'Nuevas coincidencias',
        seeAll: 'Ver todo',
        yourCareerAssistant: 'Tu asistente de carrera',
        ask: 'Preguntar...',
        fullTime: 'Tiempo completo'
    }
};

function applyLanguage(lang) {
    const t = translations[lang] || translations.en;

    // Update all elements with data-i18n attribute
    document.querySelectorAll('[data-i18n]').forEach(element => {
        const key = element.getAttribute('data-i18n');
        if (t[key]) {
            element.textContent = t[key];
        }
    });

    // Update sidebar items
    const sidebarItems = document.querySelectorAll('.sidebar-item span');
    if (sidebarItems.length >= 5) {
        sidebarItems[0].textContent = t.dashboard;
        sidebarItems[1].textContent = t.jobMatches;
        sidebarItems[2].textContent = t.myApplications;
        sidebarItems[3].textContent = t.cvManagement;
        sidebarItems[4].textContent = t.careerAdvisor;
    }

    // Update section titles
    const welcomeTitle = document.querySelector('.welcome-title');
    if (welcomeTitle) {
        const userName = document.querySelector('.user-name').textContent;
        welcomeTitle.innerHTML = `${t.welcome} <span class="user-name">${userName}</span>!`;
    }

    // Update other text elements
    const seeAllLinks = document.querySelectorAll('.see-all-link');
    seeAllLinks.forEach(link => {
        link.textContent = t.seeAll + ' >';
    });

    const sectionTitles = document.querySelectorAll('.section-title');
    sectionTitles.forEach((title, index) => {
        if (index === 0) title.textContent = t.newJobMatches;
        if (index === 1) title.textContent = t.yourCareerAssistant;
    });

    const askButtons = document.querySelectorAll('.ask-button, .assistant-ask-btn');
    askButtons.forEach(btn => {
        if (btn.classList.contains('ask-button')) {
            btn.textContent = t.ask;
        } else {
            btn.textContent = t.ask + ' >';
        }
    });
}

// Dashboards Button
function setupDashboardsButton() {
    const dashboardsBtn = document.getElementById('dashboardsBtn');

    if (dashboardsBtn) {
        dashboardsBtn.addEventListener('click', function () {
            // TODO: Open dashboards menu or navigate
            console.log('Dashboards clicked');
            // Could open a dropdown menu with different dashboard views
        });
    }
}


// Job Cards Interaction
function setupJobCards() {
    const jobCards = document.querySelectorAll('.job-card');

    jobCards.forEach(card => {
        card.addEventListener('click', function () {
            // TODO: Navigate to job details page
            const jobTitle = this.querySelector('.job-title').textContent;
            console.log('Job card clicked:', jobTitle);
            // window.location.href = `job-details.html?id=${jobId}`;
        });

        // Arrow button click
        const arrowBtn = card.querySelector('.job-arrow');
        if (arrowBtn) {
            arrowBtn.addEventListener('click', function (e) {
                e.stopPropagation(); // Prevent card click
                // TODO: Navigate to job details
                const jobTitle = card.querySelector('.job-title').textContent;
                console.log('Job arrow clicked:', jobTitle);
            });
        }
    });
}

// Career Assistant Card
function setupAssistantCard() {
    const askButton = document.querySelector('.ask-button');
    const assistantAskBtn = document.querySelector('.assistant-ask-btn');

    if (askButton) {
        askButton.addEventListener('click', function () {
            // TODO: Open AI chat interface
            console.log('Ask button clicked');
            openAssistantChat();
        });
    }

    if (assistantAskBtn) {
        assistantAskBtn.addEventListener('click', function () {
            // TODO: Open AI chat interface
            console.log('Assistant ask button clicked');
            openAssistantChat();
        });
    }
}

// Open Assistant Chat
function openAssistantChat() {
    // TODO: Implement AI chat modal or redirect to chat page
    alert('AI Career Assistant chat will open here. This feature will be implemented soon!');
    // You can create a modal or redirect to a chat page
}

// Click outside to close dropdowns
function setupClickOutside() {
    document.addEventListener('click', function (e) {
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

// Utility: Get user data from localStorage or API
function getUserData() {
    // TODO: Fetch user data from API or localStorage
    const userData = {
        name: 'Emre Erdem',
        email: 'emre@example.com',
        role: 'Student'
    };

    return userData;
}

// Update welcome message with user name
function updateWelcomeMessage() {
    const userData = getUserData();
    const userNameElement = document.querySelector('.user-name');

    if (userNameElement && userData.name) {
        const firstName = userData.name.split(' ')[0];
        userNameElement.textContent = firstName;
    }
}

// Initialize welcome message
updateWelcomeMessage();
