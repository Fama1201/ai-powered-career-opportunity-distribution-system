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
            
            // Simulate login (in production, this would send to a backend API)
            setTimeout(() => {
                // In a real application, you would send this data to your backend API
                console.log('Login attempt:', {
                    userType: formData.userType,
                    email: formData.email,
                    rememberMe: formData.rememberMe
                });
                
                // Show success message
                showNotification('Login successful! Redirecting...', 'success');
                
                // In production, redirect based on user type
                setTimeout(() => {
                    if (formData.userType === 'student') {
                        // Redirect to student dashboard
                        window.location.href = 'student-dashboard.html'; // This page doesn't exist yet
                    } else if (formData.userType === 'hr') {
                        // Redirect to HR dashboard
                        window.location.href = 'hr-dashboard.html'; // This page doesn't exist yet
                    }
                }, 1500);
            }, 1500);
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

