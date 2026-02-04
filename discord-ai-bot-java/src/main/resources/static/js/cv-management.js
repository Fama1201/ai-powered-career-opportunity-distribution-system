// cv-management.js
// Note: This file uses global functions from utils.js, ui.js, config.js, and i18n.js
// Make sure these scripts are loaded before this one in the HTML

let selectedFile = null;

document.addEventListener('DOMContentLoaded', () => {
    initCvManagement();
});

async function initCvManagement() {
    loadSavedTheme();
    loadSavedLanguage();
    await loadUserProfile(); // Load user profile first
    setupEventListeners();
    await loadCurrentCv();
}

/**
 * Load saved theme from localStorage
 */
function loadSavedTheme() {
    const savedTheme = localStorage.getItem('theme') || 'dark';
    if (savedTheme === 'light') {
        document.body.classList.add('light-theme');
    } else {
        document.body.classList.remove('light-theme');
    }
}

/**
 * Load saved language from localStorage
 */
function loadSavedLanguage() {
    const savedLang = localStorage.getItem('language') || 'en';
    if (typeof I18n !== 'undefined' && I18n.setLanguage) {
        I18n.setLanguage(savedLang);
    }
    // Update language dropdown
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

/**
 * Load user profile information
 */
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
        fullName = 'Student'; // Fallback
    }

    updateUserProfileDisplay(fullName, role);
}

/**
 * Update user profile display in header
 */
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

function setupEventListeners() {
    // File input
    const fileInput = document.getElementById('cvFileInput');
    const uploadArea = document.getElementById('cvUploadArea');
    const uploadPlaceholder = document.getElementById('uploadPlaceholder');
    const uploadFileInfo = document.getElementById('uploadFileInfo');
    const fileRemoveBtn = document.getElementById('fileRemoveBtn');
    const cancelUploadBtn = document.getElementById('cancelUploadBtn');
    const uploadCvBtn = document.getElementById('uploadCvBtn');

    // Click to select file
    uploadArea.addEventListener('click', () => {
        if (!selectedFile) {
            fileInput.click();
        }
    });

    // File input change
    fileInput.addEventListener('change', (e) => {
        handleFileSelect(e.target.files[0]);
    });

    // Drag and drop
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('drag-over');
    });

    uploadArea.addEventListener('dragleave', () => {
        uploadArea.classList.remove('drag-over');
    });

    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('drag-over');
        const file = e.dataTransfer.files[0];
        if (file) {
            handleFileSelect(file);
        }
    });

    // Remove file
    fileRemoveBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        clearFileSelection();
    });

    // Cancel upload
    cancelUploadBtn.addEventListener('click', () => {
        clearFileSelection();
    });

    // Upload CV
    uploadCvBtn.addEventListener('click', async () => {
        if (selectedFile) {
            await uploadCv();
        }
    });

    // Delete CV
    document.getElementById('deleteCvBtn')?.addEventListener('click', handleDeleteCv);

    // Download CV (as text file)
    document.getElementById('downloadCvBtn')?.addEventListener('click', handleDownloadCv);

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

function handleFileSelect(file) {
    if (!file) return;

    // Validate file type
    const validTypes = ['application/pdf', 'text/plain'];
    const validExtensions = ['.pdf', '.txt'];
    const fileExtension = '.' + file.name.split('.').pop().toLowerCase();

    if (!validTypes.includes(file.type) && !validExtensions.includes(fileExtension)) {
        showToast('Please select a PDF or TXT file.', 'error');
        return;
    }

    // Validate file size (5MB max)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
        showToast('File size exceeds 5MB limit.', 'error');
        return;
    }

    selectedFile = file;
    displayFileInfo(file);
    updateUploadButton();
}

function displayFileInfo(file) {
    const uploadPlaceholder = document.getElementById('uploadPlaceholder');
    const uploadFileInfo = document.getElementById('uploadFileInfo');
    const fileName = document.getElementById('fileName');
    const fileSize = document.getElementById('fileSize');

    uploadPlaceholder.style.display = 'none';
    uploadFileInfo.style.display = 'flex';

    fileName.textContent = file.name;
    fileSize.textContent = formatFileSize(file.size);

    // Update icon based on file type
    const fileIcon = uploadFileInfo.querySelector('.file-icon i');
    if (file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')) {
        fileIcon.className = 'fas fa-file-pdf';
    } else {
        fileIcon.className = 'fas fa-file-alt';
    }

    document.getElementById('cancelUploadBtn').style.display = 'inline-block';
}

function clearFileSelection() {
    selectedFile = null;
    document.getElementById('cvFileInput').value = '';
    document.getElementById('uploadPlaceholder').style.display = 'flex';
    document.getElementById('uploadFileInfo').style.display = 'none';
    document.getElementById('cancelUploadBtn').style.display = 'none';
    updateUploadButton();
}

function updateUploadButton() {
    const uploadCvBtn = document.getElementById('uploadCvBtn');
    const uploadBtnText = document.getElementById('uploadBtnText');
    
    if (selectedFile) {
        uploadCvBtn.disabled = false;
        uploadBtnText.textContent = 'Upload CV';
    } else {
        uploadCvBtn.disabled = true;
        uploadBtnText.textContent = 'Select a file to upload';
    }
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}

async function uploadCv() {
    if (!selectedFile) {
        showToast('Please select a file first.', 'error');
        return;
    }

    const uploadCvBtn = document.getElementById('uploadCvBtn');
    const uploadIcon = document.getElementById('uploadIcon');
    const originalText = uploadCvBtn.innerHTML;

    showLoading(uploadCvBtn);
    uploadCvBtn.disabled = true;

    try {
        const formData = new FormData();
        formData.append('file', selectedFile);

        const token = getAccessToken();
        if (!token) {
            throw new Error('Not authenticated. Please log in again.');
        }

        const response = await fetch(`${CONFIG.API_BASE_URL}${CONFIG.ENDPOINTS.UPLOAD_CV}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
                // Don't set Content-Type header - browser will set it with boundary for FormData
            },
            body: formData
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: response.statusText }));
            throw new Error(error.message || 'Upload failed');
        }

        const data = await response.json();
        
        showToast('CV uploaded successfully!', 'success');
        clearFileSelection();
        await loadCurrentCv(); // Reload to show the new CV

    } catch (error) {
        console.error('CV upload error:', error);
        showToast(error.message || 'Failed to upload CV. Please try again.', 'error');
    } finally {
        hideLoading(uploadCvBtn);
        uploadCvBtn.disabled = false;
        uploadCvBtn.innerHTML = originalText;
    }
}

async function loadCurrentCv() {
    const cvPreviewSection = document.getElementById('cvPreviewSection');
    const cvEmptyState = document.getElementById('cvEmptyState');
    const cvTextPreview = document.getElementById('cvTextPreview');

    try {
        const response = await apiRequest(CONFIG.ENDPOINTS.GET_CV);
        
        if (response && response.hasCv && response.cvText) {
            // Show preview section
            cvPreviewSection.style.display = 'block';
            cvEmptyState.style.display = 'none';
            
            // Display CV text (truncate if too long)
            const cvText = response.cvText;
            const maxLength = 2000;
            if (cvText.length > maxLength) {
                cvTextPreview.innerHTML = `
                    <pre>${escapeHtml(cvText.substring(0, maxLength))}...</pre>
                    <p class="cv-truncated-note">(Content truncated for preview. Full CV is saved in database.)</p>
                `;
            } else {
                cvTextPreview.innerHTML = `<pre>${escapeHtml(cvText)}</pre>`;
            }
        } else {
            // Show empty state
            cvPreviewSection.style.display = 'none';
            cvEmptyState.style.display = 'block';
        }
    } catch (error) {
        console.error('Failed to load CV:', error);
        // Show empty state on error
        cvPreviewSection.style.display = 'none';
        cvEmptyState.style.display = 'block';
    }
}

async function handleDeleteCv() {
    if (!confirm('Are you sure you want to delete your CV? This action cannot be undone.')) {
        return;
    }

    const deleteBtn = document.getElementById('deleteCvBtn');
    showLoading(deleteBtn);
    deleteBtn.disabled = true;

    try {
        await apiRequest(CONFIG.ENDPOINTS.DELETE_CV, { method: 'DELETE' });
        showToast('CV deleted successfully.', 'success');
        await loadCurrentCv(); // Reload to show empty state
    } catch (error) {
        console.error('Failed to delete CV:', error);
        showToast(error.message || 'Failed to delete CV. Please try again.', 'error');
    } finally {
        hideLoading(deleteBtn);
        deleteBtn.disabled = false;
    }
}

async function handleDownloadCv() {
    try {
        const response = await apiRequest(CONFIG.ENDPOINTS.GET_CV);
        
        if (!response || !response.hasCv || !response.cvText) {
            showToast('No CV available to download.', 'error');
            return;
        }

        // Create a blob and download as text file
        const blob = new Blob([response.cvText], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'my-cv.txt';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        showToast('CV downloaded successfully.', 'success');
    } catch (error) {
        console.error('Failed to download CV:', error);
        showToast(error.message || 'Failed to download CV. Please try again.', 'error');
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
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
            // Use clearAuthData from utils.js if available, otherwise use local function
            if (typeof clearAuthData === 'function') {
                clearAuthData();
            } else {
                Object.values(CONFIG.STORAGE_KEYS).forEach(key => {
                    localStorage.removeItem(key);
                });
            }
            window.location.href = '/pages/auth/login.html';
        }
    }
}

