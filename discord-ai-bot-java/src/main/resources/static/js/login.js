// Login Form Handler
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const passwordToggle = document.getElementById('passwordToggle');
    const passwordInput = document.getElementById('password');
    const eyeIcon = passwordToggle.querySelector('.eye-icon');
    const eyeOffIcon = passwordToggle.querySelector('.eye-off-icon');

    // Password visibility toggle
    if (passwordToggle && passwordInput) {
        passwordToggle.addEventListener('click', function() {
            const isPassword = passwordInput.type === 'password';
            passwordInput.type = isPassword ? 'text' : 'password';
            
            if (isPassword) {
                eyeIcon.style.display = 'none';
                eyeOffIcon.style.display = 'block';
            } else {
                eyeIcon.style.display = 'block';
                eyeOffIcon.style.display = 'none';
            }
        });
    }

    // Form submission
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Get form data
            const formData = {
                userType: document.getElementById('userType').value,
                email: document.getElementById('email').value.trim(),
                password: document.getElementById('password').value,
                rememberMe: document.getElementById('rememberMe').checked
            };
            
            // Validate form
            if (!formData.userType) {
                showNotification('Please select your account type.', 'error');
                return;
            }
            
            if (!formData.email) {
                showNotification('Please enter your email or username.', 'error');
                return;
            }
            
            if (!formData.password) {
                showNotification('Please enter your password.', 'error');
                return;
            }
            
            // Validate email format (if it looks like an email)
            if (formData.email.includes('@')) {
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!emailRegex.test(formData.email)) {
                    showNotification('Please enter a valid email address.', 'error');
                    return;
                }
            }
            
            // Show loading state
            const submitButton = loginForm.querySelector('.btn-submit');
            const originalText = submitButton.innerHTML;
            submitButton.disabled = true;
            submitButton.innerHTML = '<span>Signing in...</span>';
            
            // Determine API endpoint based on user type
            // Fallback if CONFIG is not defined
            const apiBaseUrl = (typeof CONFIG !== 'undefined' && CONFIG.API_BASE_URL) ? CONFIG.API_BASE_URL : '/api';
            const studentEndpoint = (typeof CONFIG !== 'undefined' && CONFIG.ENDPOINTS && CONFIG.ENDPOINTS.STUDENT_LOGIN) 
                ? CONFIG.ENDPOINTS.STUDENT_LOGIN 
                : '/auth/student/login';
            const hrEndpoint = (typeof CONFIG !== 'undefined' && CONFIG.ENDPOINTS && CONFIG.ENDPOINTS.HR_LOGIN) 
                ? CONFIG.ENDPOINTS.HR_LOGIN 
                : '/hr/auth/login';
            
            const loginEndpoint = formData.userType === 'student' 
                ? `${apiBaseUrl}${studentEndpoint}`
                : `${apiBaseUrl}${hrEndpoint}`;
            
            console.log('Login endpoint:', loginEndpoint);
            
            // Prepare login request
            const loginRequest = {
                email: formData.email,
                password: formData.password
            };
            
            // Send login request to backend
            fetch(loginEndpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(loginRequest)
            })
            .then(async response => {
                console.log('Login response status:', response.status);
                console.log('Login response headers:', response.headers);
                
                if (!response.ok) {
                    // Try to get error message from response
                    let errorMessage = `Login failed: ${response.status}`;
                    
                    try {
                        const contentType = response.headers.get('content-type');
                        console.log('Response content-type:', contentType);
                        
                        if (contentType && contentType.includes('application/json')) {
                            const err = await response.json();
                            console.log('Login error response (JSON):', JSON.stringify(err, null, 2));
                            errorMessage = err.message || err.error || errorMessage;
                        } else {
                            const text = await response.text();
                            console.log('Login error response (text):', text);
                            // Try to parse as JSON if it looks like JSON
                            try {
                                const parsed = JSON.parse(text);
                                errorMessage = parsed.message || parsed.error || errorMessage;
                            } catch (e) {
                                errorMessage = text || errorMessage;
                            }
                        }
                    } catch (parseError) {
                        console.error('Error parsing error response:', parseError);
                        try {
                            const text = await response.text();
                            console.error('Raw response text:', text);
                            errorMessage = text || errorMessage;
                        } catch (textError) {
                            console.error('Could not read response text:', textError);
                        }
                    }
                    
                    console.error('Final error message:', errorMessage);
                    throw new Error(errorMessage);
                }
                
                return response.json();
            })
            .then(data => {
                console.log('Login response data:', data);
                
                // Success - save auth data using CONFIG.STORAGE_KEYS
                const STORAGE_KEYS = (typeof CONFIG !== 'undefined' && CONFIG.STORAGE_KEYS) ? CONFIG.STORAGE_KEYS : {
                    ACCESS_TOKEN: 'accessToken',
                    REFRESH_TOKEN: 'refreshToken',
                    USER_TYPE: 'userType',
                    USER_EMAIL: 'userEmail',
                    USER_ID: 'userId',
                    FIRST_NAME: 'firstName',
                    LAST_NAME: 'lastName',
                    HR_FULL_NAME: 'hrFullName',
                    ROLE: 'role'
                };
                
                if (data.accessToken) {
                    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, data.accessToken);
                }
                if (data.refreshToken) {
                    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, data.refreshToken);
                }
                if (data.userId) {
                    localStorage.setItem(STORAGE_KEYS.USER_ID, data.userId);
                }
                if (data.email) {
                    localStorage.setItem(STORAGE_KEYS.USER_EMAIL, data.email);
                }
                if (data.role) {
                    localStorage.setItem(STORAGE_KEYS.ROLE, data.role);
                }
                // Save fullName for HR users
                if (data.fullName) {
                    localStorage.setItem(STORAGE_KEYS.HR_FULL_NAME, data.fullName);
                }
                // Ensure role is set for HR users
                if (data.fullName && !data.role) {
                    localStorage.setItem(STORAGE_KEYS.ROLE, 'HR');
                }
                // Save firstName and lastName for Student users
                if (data.firstName) {
                    localStorage.setItem(STORAGE_KEYS.FIRST_NAME, data.firstName);
                }
                if (data.lastName) {
                    localStorage.setItem(STORAGE_KEYS.LAST_NAME, data.lastName);
                }
                // Save user type
                localStorage.setItem(STORAGE_KEYS.USER_TYPE, formData.userType);
                
                // Also use storeTokens if available
                if (typeof storeTokens === 'function') {
                    storeTokens(
                        data.accessToken,
                        data.refreshToken,
                        formData.userType,
                        data.email,
                        data.userId
                    );
                }
                
                console.log('Login successful:', data);
                showNotification(data.message || 'Login successful! Redirecting...', 'success');
                
                // Redirect based on user type (always redirect on successful login)
                setTimeout(() => {
                    let dashboardPath;
                    if (formData.userType === 'student') {
                        dashboardPath = '/pages/student/student-dashboard.html';
                    } else if (formData.userType === 'hr') {
                        dashboardPath = '/pages/hr/hr-dashboard.html';
                    } else {
                        // Fallback to login if user type is unknown
                        dashboardPath = '/pages/auth/login.html';
                    }
                    
                    console.log('Redirecting to:', dashboardPath);
                    window.location.href = dashboardPath;
                }, 1500);
            })
            .catch(error => {
                console.error('Login error:', error);
                showNotification(error.message || 'Login failed. Please try again.', 'error');
                submitButton.disabled = false;
                submitButton.innerHTML = originalText;
            });
        });
    }
});

// Notification function (reused from contact.js)
function showNotification(message, type = 'info') {
    // Remove existing notification if any
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    // Add styles
    notification.style.cssText = `
        position: fixed;
        top: 100px;
        right: 2rem;
        background: ${type === 'success' ? 'rgba(16, 185, 129, 0.9)' : 'rgba(239, 68, 68, 0.9)'};
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 12px;
        box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
        z-index: 10000;
        animation: slideInRight 0.3s ease;
        max-width: 400px;
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.2);
    `;
    
    // Add animation keyframes if not already added
    if (!document.querySelector('#notification-styles')) {
        const style = document.createElement('style');
        style.id = 'notification-styles';
        style.textContent = `
            @keyframes slideInRight {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
            @keyframes slideOutRight {
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
    
    // Append to body
    document.body.appendChild(notification);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 5000);
}

