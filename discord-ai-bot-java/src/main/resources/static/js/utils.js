/**
 * Utility Functions
 * Common helper functions used across the application
 */

/**
 * Get stored access token
 */
function getAccessToken() {
    return localStorage.getItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN);
}

/**
 * Get stored refresh token
 */
function getRefreshToken() {
    return localStorage.getItem(CONFIG.STORAGE_KEYS.REFRESH_TOKEN);
}

/**
 * Store authentication tokens
 */
function storeTokens(accessToken, refreshToken, userType, email, userId) {
    localStorage.setItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN, accessToken);
    if (refreshToken) {
        localStorage.setItem(CONFIG.STORAGE_KEYS.REFRESH_TOKEN, refreshToken);
    }
    if (userType) {
        localStorage.setItem(CONFIG.STORAGE_KEYS.USER_TYPE, userType);
    }
    if (email) {
        localStorage.setItem(CONFIG.STORAGE_KEYS.USER_EMAIL, email);
    }
    if (userId) {
        localStorage.setItem(CONFIG.STORAGE_KEYS.USER_ID, userId);
    }
}

/**
 * Clear all stored authentication data
 */
function clearAuthData() {
    localStorage.removeItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.USER_TYPE);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.USER_EMAIL);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.USER_ID);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.FIRST_NAME);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.LAST_NAME);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.HR_FULL_NAME);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.ROLE);
    // Fallback for old keys
    localStorage.removeItem('userToken');
    localStorage.removeItem('userData');
}

/**
 * Check if user is authenticated
 */
function isAuthenticated() {
    return !!getAccessToken();
}

/**
 * Get user type (student or hr)
 */
function getUserType() {
    return localStorage.getItem(CONFIG.STORAGE_KEYS.USER_TYPE);
}

/**
 * Make authenticated API request
 */
async function apiRequest(endpoint, options = {}) {
    const token = getAccessToken();
    
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` })
        }
    };
    
    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...(options.headers || {})
        }
    };
    
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}${endpoint}`, mergedOptions);
        
        // Handle 401 Unauthorized
        if (response.status === 401) {
            // Try to refresh token
            const refreshed = await refreshAccessToken();
            if (!refreshed) {
                // Refresh failed - don't redirect, just throw error
                // Let the calling code decide what to do
                throw new Error(CONFIG.ERROR_MESSAGES.UNAUTHORIZED);
            }
            // Retry request with new token
            mergedOptions.headers['Authorization'] = `Bearer ${getAccessToken()}`;
            const retryResponse = await fetch(`${CONFIG.API_BASE_URL}${endpoint}`, mergedOptions);
            return handleResponse(retryResponse);
        }
        
        return handleResponse(response);
    } catch (error) {
        if (error.message === CONFIG.ERROR_MESSAGES.UNAUTHORIZED) {
            throw error;
        }
        console.error('API Request Error:', error);
        throw new Error(CONFIG.ERROR_MESSAGES.NETWORK_ERROR);
    }
}

/**
 * Handle API response
 */
async function handleResponse(response) {
    if (!response.ok) {
        const error = await response.json().catch(() => ({ message: response.statusText }));
        throw new Error(error.message || CONFIG.ERROR_MESSAGES.SERVER_ERROR);
    }
    
    // Handle empty responses
    const contentType = response.headers.get('content-type');
    if (!contentType || !contentType.includes('application/json')) {
        return null;
    }
    
    return await response.json();
}

/**
 * Refresh access token using refresh token
 */
async function refreshAccessToken() {
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
        return false;
    }
    
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}${CONFIG.ENDPOINTS.REFRESH_TOKEN}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ refreshToken })
        });
        
        if (!response.ok) {
            return false;
        }
        
        const data = await response.json();
        if (data.accessToken) {
            localStorage.setItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN, data.accessToken);
            if (data.refreshToken) {
                localStorage.setItem(CONFIG.STORAGE_KEYS.REFRESH_TOKEN, data.refreshToken);
            }
            return true;
        }
        
        return false;
    } catch (error) {
        console.error('Token refresh error:', error);
        return false;
    }
}

/**
 * Redirect to login page
 */
function redirectToLogin() {
    clearAuthData();
    window.location.href = 'login.html';
}

/**
 * Show notification message
 */
function showNotification(message, type = 'info', duration = 3000) {
    // Remove existing notifications
    const existing = document.querySelector('.notification');
    if (existing) {
        existing.remove();
    }
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    // Add styles
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 1.5rem;
        background: ${type === 'error' ? 'var(--error-red)' : type === 'success' ? 'var(--success-green)' : 'var(--primary-blue)'};
        color: white;
        border-radius: 8px;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        z-index: 10000;
        animation: slideIn 0.3s ease;
    `;
    
    document.body.appendChild(notification);
    
    // Remove after duration
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, duration);
}

/**
 * Format date to readable string
 */
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

/**
 * Format date to relative time (e.g., "2 days ago")
 */
function formatRelativeTime(dateString) {
    if (!dateString) return 'N/A';
    
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) return 'Just now';
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} minutes ago`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} hours ago`;
    if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)} days ago`;
    
    return formatDate(dateString);
}

/**
 * Debounce function
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * Validate email format
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * Validate password strength
 */
function isValidPassword(password) {
    // At least 8 characters, one uppercase, one lowercase, one number
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
    return passwordRegex.test(password);
}

/**
 * Show loading spinner
 */
function showLoading(element) {
    if (!element) return;
    
    element.style.position = 'relative';
    element.style.pointerEvents = 'none';
    element.style.opacity = '0.6';
    
    const spinner = document.createElement('div');
    spinner.className = 'loading-spinner';
    spinner.style.cssText = `
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 40px;
        height: 40px;
        border: 4px solid rgba(255, 255, 255, 0.3);
        border-top-color: var(--primary-blue);
        border-radius: 50%;
        animation: spin 1s linear infinite;
    `;
    
    element.appendChild(spinner);
}

/**
 * Hide loading spinner
 */
function hideLoading(element) {
    if (!element) return;
    
    element.style.pointerEvents = '';
    element.style.opacity = '1';
    
    const spinner = element.querySelector('.loading-spinner');
    if (spinner) {
        spinner.remove();
    }
}

// Add spin animation if not exists
if (!document.querySelector('#loading-spinner-style')) {
    const style = document.createElement('style');
    style.id = 'loading-spinner-style';
    style.textContent = `
        @keyframes spin {
            to { transform: translate(-50%, -50%) rotate(360deg); }
        }
        @keyframes slideIn {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        @keyframes slideOut {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }
    `;
    document.head.appendChild(style);
}

