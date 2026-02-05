// hr-analytics.js
// HR Analytics page functionality

let allJobs = [];
let allApplications = [];
let charts = {};

document.addEventListener('DOMContentLoaded', () => {
    initAnalytics();
});

async function initAnalytics() {
    loadSavedTheme();
    loadSavedLanguage();
    await loadUserProfile();
    setupEventListeners();
    await loadAllData();
    calculateAndDisplayAnalytics();
}

function setupEventListeners() {
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

async function loadAllData() {
    try {
        // Load all jobs
        allJobs = await HrAPI.getJobs();
        allJobs = Array.isArray(allJobs) ? allJobs : [];

        // Load applications for all jobs
        allApplications = [];
        for (const job of allJobs) {
            try {
                const applications = await HrAPI.getJobApplications(job.id);
                const appsWithJob = applications.map(app => ({
                    ...app,
                    jobId: job.id,
                    jobTitle: job.title || `Job #${job.id}`,
                    jobStatus: job.status
                }));
                allApplications.push(...appsWithJob);
            } catch (error) {
                console.error(`Failed to load applications for job ${job.id}:`, error);
                // Continue with other jobs
            }
        }
    } catch (error) {
        console.error('Failed to load analytics data:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load analytics data.', 'error');
        }
    }
}

function calculateAndDisplayAnalytics() {
    // Calculate overview statistics
    const totalJobs = allJobs.length;
    const totalApplications = allApplications.length;
    const totalHires = allApplications.filter(app => 
        app.status === 'HIRED' || app.status === 'hired'
    ).length;
    const conversionRate = totalApplications > 0 
        ? ((totalHires / totalApplications) * 100).toFixed(1)
        : '0.0';

    // Update overview stats
    document.getElementById('totalJobs').textContent = totalJobs;
    document.getElementById('totalApplications').textContent = totalApplications;
    document.getElementById('totalHires').textContent = totalHires;
    document.getElementById('conversionRate').textContent = `${conversionRate}%`;

    // Create charts
    createStatusChart();
    createTimeSeriesChart();
    createFunnelChart();
    createTopJobsChart();
    renderJobPerformanceTable();
}

function createStatusChart() {
    const ctx = document.getElementById('statusChart');
    if (!ctx) return;

    // Destroy existing chart if it exists
    if (charts.statusChart) {
        charts.statusChart.destroy();
    }

    // Count applications by status
    const statusCounts = {
        APPLIED: 0,
        SHORTLISTED: 0,
        INTERVIEW: 0,
        REJECTED: 0,
        HIRED: 0,
        WITHDRAWN: 0
    };

    allApplications.forEach(app => {
        const status = (app.status || 'APPLIED').toUpperCase();
        if (statusCounts.hasOwnProperty(status)) {
            statusCounts[status]++;
        }
    });

    const isLightTheme = document.body.classList.contains('light-theme');
    const textColor = isLightTheme ? '#0f172a' : '#e2e8f0';
    const gridColor = isLightTheme ? '#e2e8f0' : 'rgba(226, 232, 240, 0.1)';

    charts.statusChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Applied', 'Shortlisted', 'Interview', 'Rejected', 'Hired', 'Withdrawn'],
            datasets: [{
                data: [
                    statusCounts.APPLIED,
                    statusCounts.SHORTLISTED,
                    statusCounts.INTERVIEW,
                    statusCounts.REJECTED,
                    statusCounts.HIRED,
                    statusCounts.WITHDRAWN
                ],
                backgroundColor: [
                    'rgba(37, 99, 235, 0.8)',
                    'rgba(16, 185, 129, 0.8)',
                    'rgba(245, 158, 11, 0.8)',
                    'rgba(239, 68, 68, 0.8)',
                    'rgba(34, 197, 94, 0.8)',
                    'rgba(100, 116, 139, 0.8)'
                ],
                borderColor: [
                    '#2563eb',
                    '#10b981',
                    '#f59e0b',
                    '#ef4444',
                    '#22c55e',
                    '#64748b'
                ],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: textColor,
                        padding: 15,
                        font: {
                            size: 12
                        }
                    }
                },
                tooltip: {
                    backgroundColor: isLightTheme ? 'rgba(255, 255, 255, 0.95)' : 'rgba(15, 23, 42, 0.95)',
                    titleColor: textColor,
                    bodyColor: textColor,
                    borderColor: gridColor,
                    borderWidth: 1
                }
            }
        }
    });
}

function createTimeSeriesChart() {
    const ctx = document.getElementById('timeSeriesChart');
    if (!ctx) return;

    // Destroy existing chart if it exists
    if (charts.timeSeriesChart) {
        charts.timeSeriesChart.destroy();
    }

    // Group applications by month
    const monthlyData = {};
    allApplications.forEach(app => {
        if (!app.appliedAt) return;
        const date = new Date(app.appliedAt);
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        if (!monthlyData[monthKey]) {
            monthlyData[monthKey] = 0;
        }
        monthlyData[monthKey]++;
    });

    // Sort by date
    const sortedMonths = Object.keys(monthlyData).sort();
    const last6Months = sortedMonths.slice(-6);

    const isLightTheme = document.body.classList.contains('light-theme');
    const textColor = isLightTheme ? '#0f172a' : '#e2e8f0';
    const gridColor = isLightTheme ? '#e2e8f0' : 'rgba(226, 232, 240, 0.1)';
    const primaryColor = isLightTheme ? '#2563eb' : '#60a5fa';

    charts.timeSeriesChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: last6Months.map(month => {
                const [year, monthNum] = month.split('-');
                const date = new Date(year, parseInt(monthNum) - 1);
                return date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
            }),
            datasets: [{
                label: 'Applications',
                data: last6Months.map(month => monthlyData[month] || 0),
                borderColor: primaryColor,
                backgroundColor: isLightTheme 
                    ? 'rgba(37, 99, 235, 0.1)' 
                    : 'rgba(96, 165, 250, 0.1)',
                fill: true,
                tension: 0.4,
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: isLightTheme ? 'rgba(255, 255, 255, 0.95)' : 'rgba(15, 23, 42, 0.95)',
                    titleColor: textColor,
                    bodyColor: textColor,
                    borderColor: gridColor,
                    borderWidth: 1
                }
            },
            scales: {
                x: {
                    ticks: {
                        color: textColor
                    },
                    grid: {
                        color: gridColor
                    }
                },
                y: {
                    beginAtZero: true,
                    ticks: {
                        color: textColor,
                        stepSize: 1
                    },
                    grid: {
                        color: gridColor
                    }
                }
            }
        }
    });
}

function createFunnelChart() {
    const ctx = document.getElementById('funnelChart');
    if (!ctx) return;

    // Destroy existing chart if it exists
    if (charts.funnelChart) {
        charts.funnelChart.destroy();
    }

    const applied = allApplications.length;
    const shortlisted = allApplications.filter(app => 
        (app.status || '').toUpperCase() === 'SHORTLISTED'
    ).length;
    const interview = allApplications.filter(app => 
        (app.status || '').toUpperCase() === 'INTERVIEW'
    ).length;
    const hired = allApplications.filter(app => 
        (app.status || '').toUpperCase() === 'HIRED'
    ).length;

    const isLightTheme = document.body.classList.contains('light-theme');
    const textColor = isLightTheme ? '#0f172a' : '#e2e8f0';
    const gridColor = isLightTheme ? '#e2e8f0' : 'rgba(226, 232, 240, 0.1)';

    charts.funnelChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Applied', 'Shortlisted', 'Interview', 'Hired'],
            datasets: [{
                label: 'Count',
                data: [applied, shortlisted, interview, hired],
                backgroundColor: [
                    'rgba(37, 99, 235, 0.8)',
                    'rgba(16, 185, 129, 0.8)',
                    'rgba(245, 158, 11, 0.8)',
                    'rgba(34, 197, 94, 0.8)'
                ],
                borderColor: [
                    '#2563eb',
                    '#10b981',
                    '#f59e0b',
                    '#22c55e'
                ],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            indexAxis: 'y',
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: isLightTheme ? 'rgba(255, 255, 255, 0.95)' : 'rgba(15, 23, 42, 0.95)',
                    titleColor: textColor,
                    bodyColor: textColor,
                    borderColor: gridColor,
                    borderWidth: 1
                }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    ticks: {
                        color: textColor,
                        stepSize: 1
                    },
                    grid: {
                        color: gridColor
                    }
                },
                y: {
                    ticks: {
                        color: textColor
                    },
                    grid: {
                        color: gridColor
                    }
                }
            }
        }
    });
}

function createTopJobsChart() {
    const ctx = document.getElementById('topJobsChart');
    if (!ctx) return;

    // Destroy existing chart if it exists
    if (charts.topJobsChart) {
        charts.topJobsChart.destroy();
    }

    // Count applications per job
    const jobApplicationCounts = {};
    allApplications.forEach(app => {
        const jobId = app.jobId;
        if (!jobApplicationCounts[jobId]) {
            jobApplicationCounts[jobId] = {
                title: app.jobTitle || `Job #${jobId}`,
                count: 0
            };
        }
        jobApplicationCounts[jobId].count++;
    });

    // Sort by count and get top 5
    const topJobs = Object.entries(jobApplicationCounts)
        .sort((a, b) => b[1].count - a[1].count)
        .slice(0, 5)
        .map(([jobId, data]) => data);

    const isLightTheme = document.body.classList.contains('light-theme');
    const textColor = isLightTheme ? '#0f172a' : '#e2e8f0';
    const gridColor = isLightTheme ? '#e2e8f0' : 'rgba(226, 232, 240, 0.1)';
    const primaryColor = isLightTheme ? '#2563eb' : '#60a5fa';

    charts.topJobsChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: topJobs.map(job => {
                // Truncate long titles
                return job.title.length > 20 ? job.title.substring(0, 20) + '...' : job.title;
            }),
            datasets: [{
                label: 'Applications',
                data: topJobs.map(job => job.count),
                backgroundColor: primaryColor,
                borderColor: isLightTheme ? '#2563eb' : '#60a5fa',
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: isLightTheme ? 'rgba(255, 255, 255, 0.95)' : 'rgba(15, 23, 42, 0.95)',
                    titleColor: textColor,
                    bodyColor: textColor,
                    borderColor: gridColor,
                    borderWidth: 1
                }
            },
            scales: {
                x: {
                    ticks: {
                        color: textColor,
                        maxRotation: 45,
                        minRotation: 45
                    },
                    grid: {
                        color: gridColor
                    }
                },
                y: {
                    beginAtZero: true,
                    ticks: {
                        color: textColor,
                        stepSize: 1
                    },
                    grid: {
                        color: gridColor
                    }
                }
            }
        }
    });
}

function renderJobPerformanceTable() {
    const tbody = document.getElementById('jobPerformanceTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    // Calculate stats per job
    const jobStats = {};
    allJobs.forEach(job => {
        jobStats[job.id] = {
            title: job.title || `Job #${job.id}`,
            status: job.status || 'UNKNOWN',
            applications: 0,
            shortlisted: 0,
            interview: 0,
            hired: 0
        };
    });

    allApplications.forEach(app => {
        const jobId = app.jobId;
        if (!jobStats[jobId]) return;

        jobStats[jobId].applications++;
        const status = (app.status || '').toUpperCase();
        if (status === 'SHORTLISTED') {
            jobStats[jobId].shortlisted++;
        } else if (status === 'INTERVIEW') {
            jobStats[jobId].interview++;
        } else if (status === 'HIRED') {
            jobStats[jobId].hired++;
        }
    });

    // Sort by applications count (descending)
    const sortedJobs = Object.entries(jobStats)
        .sort((a, b) => b[1].applications - a[1].applications);

    sortedJobs.forEach(([jobId, stats]) => {
        const conversionRate = stats.applications > 0
            ? ((stats.hired / stats.applications) * 100).toFixed(1)
            : '0.0';

        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${escapeHtml(stats.title)}</td>
            <td><span class="status-badge ${getStatusClassForJob(stats.status)}">${escapeHtml(stats.status)}</span></td>
            <td>${stats.applications}</td>
            <td>${stats.shortlisted}</td>
            <td>${stats.interview}</td>
            <td>${stats.hired}</td>
            <td>${conversionRate}%</td>
        `;
        tbody.appendChild(row);
    });
}

function getStatusClassForJob(status) {
    const statusUpper = (status || '').toUpperCase();
    switch (statusUpper) {
        case 'OPEN': return 'shortlisted';
        case 'CLOSED': return 'rejected';
        case 'ARCHIVED': return 'withdrawn';
        default: return 'applied';
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
    
    // Recreate charts with new theme
    if (allApplications.length > 0) {
        createStatusChart();
        createTimeSeriesChart();
        createFunnelChart();
        createTopJobsChart();
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

