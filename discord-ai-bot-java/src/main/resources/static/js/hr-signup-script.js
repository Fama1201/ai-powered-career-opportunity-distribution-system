document.addEventListener('DOMContentLoaded', function() {
    
    const phoneInput = document.getElementById('hrPhone');
    let iti; // We create this variable here so we can use it later in the Submit logic

    if (phoneInput) {
        // Initialize the library and save it to the 'iti' variable
        iti = window.intlTelInput(phoneInput, {
            separateDialCode: true, // The country code is moved outside the typing box and placed next to the flag.
            preferredCountries: ["cz"],
            utilsScript: "https://cdn.jsdelivr.net/npm/intl-tel-input@18.2.1/build/js/utils.js"
        });
    }

    // --- PART 1: POSITION DROPDOWN LOGIC ---
    const positionSelect = document.getElementById('position');
    const otherPositionInput = document.getElementById('otherPositionInput');

    // Only run this if the position dropdown actually exists on the page
    if (positionSelect) {
        positionSelect.addEventListener('change', function() {
            // Check if the user selected "other"
            if (this.value === 'Other') {
                otherPositionInput.style.display = 'block';          // Show the text box
                otherPositionInput.setAttribute('required', 'true'); // Make it mandatory
            } else {
                otherPositionInput.style.display = 'none';           // Hide the text box
                otherPositionInput.removeAttribute('required');      // Make it optional
                otherPositionInput.value = '';                       // Clear any text typed
            }
        });
    }

    // --- PART 2: FORM SUBMISSION LOGIC ---
    const hrSignupForm = document.getElementById('hrSignupForm');

    if (hrSignupForm) {
        hrSignupForm.addEventListener('submit', function(event) {
            // 1. Prevent the page from refreshing
            event.preventDefault();

            // 2. Gather the data
            const firstName = document.getElementById('hrFirstName').value;
            const lastName = document.getElementById('hrLastName').value;
            const fullName = firstName + " " + lastName;
            
            const email = document.getElementById('hrEmail').value;
            const companyName = document.getElementById('companyName').value;
            const industry = document.getElementById('industry').value;
            
            // Gather Position
            let positionValue = positionSelect ? positionSelect.value : "";
            if (positionValue === 'Other') {
                positionValue = otherPositionInput.value;
            }

            // Gather Phone Data (Optional)
            let fullPhone = "";
            if (iti && phoneInput.value.trim() !== '') {
                if (iti.isValidNumber()) {
                    fullPhone = iti.getNumber();
                } else {
                    alert("Please enter a valid phone number or leave it empty.");
                    return;
                }
            }

            const password = document.getElementById('hrPassword').value;
            const confirmPassword = document.getElementById('hrConfirmPassword').value;

            // 3. Validation
            if (password !== confirmPassword) {
                alert("Passwords do not match! Please try again.");
                return;
            }

            if (password.length < 6) {
                alert("Password must be at least 6 characters long.");
                return;
            }

            // 4. Prepare data for API call (matching HrRegisterRequest DTO)
            const hrData = {
                fullName: fullName,  // Backend expects fullName, not firstName/lastName
                email: email,
                password: password,
                companyName: companyName
                // Note: industry, position, phone are not in HrRegisterRequest DTO
            };

            // 5. Show loading state
            const submitButton = hrSignupForm.querySelector('button[type="submit"]');
            const originalButtonText = submitButton.innerHTML;
            submitButton.disabled = true;
            submitButton.innerHTML = '<span>Signing up...</span>';

            // 6. Determine API endpoint
            const apiBaseUrl = (typeof CONFIG !== 'undefined' && CONFIG.API_BASE_URL) ? CONFIG.API_BASE_URL : '/api';
            const hrRegisterEndpoint = (typeof CONFIG !== 'undefined' && CONFIG.ENDPOINTS && CONFIG.ENDPOINTS.HR_REGISTER) 
                ? CONFIG.ENDPOINTS.HR_REGISTER 
                : '/hr/auth/register';
            
            const registerUrl = `${apiBaseUrl}${hrRegisterEndpoint}`;
            console.log('Registering HR user:', hrData);
            console.log('Register endpoint:', registerUrl);

            // 7. Send registration request to backend
            fetch(registerUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(hrData)
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => {
                        const errorMessage = err.message || err.error || `Registration failed: ${response.status}`;
                        throw new Error(errorMessage);
                    }).catch(parseError => {
                        throw new Error(`Registration failed: ${response.status} ${response.statusText}`);
                    });
                }
                return response.json();
            })
            .then(data => {
                console.log('Registration successful:', data);
                showNotification('Registration successful! Logging you in...', 'success');

                // Auto-login after successful registration
                const loginData = {
                    email: hrData.email,
                    password: hrData.password
                };
                
                const apiBaseUrl = (typeof CONFIG !== 'undefined' && CONFIG.API_BASE_URL) ? CONFIG.API_BASE_URL : '/api';
                const hrLoginEndpoint = (typeof CONFIG !== 'undefined' && CONFIG.ENDPOINTS && CONFIG.ENDPOINTS.HR_LOGIN) 
                    ? CONFIG.ENDPOINTS.HR_LOGIN 
                    : '/hr/auth/login';
                
                const loginUrl = `${apiBaseUrl}${hrLoginEndpoint}`;
                
                // Auto-login
                fetch(loginUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(loginData)
                })
                .then(loginResponse => {
                    if (!loginResponse.ok) {
                        throw new Error('Auto-login failed');
                    }
                    return loginResponse.json();
                })
                .then(loginData => {
                    // Save auth data
                    if (loginData.accessToken) {
                        localStorage.setItem('accessToken', loginData.accessToken);
                    }
                    if (loginData.refreshToken) {
                        localStorage.setItem('refreshToken', loginData.refreshToken);
                    }
                    if (loginData.email) {
                        localStorage.setItem('email', loginData.email);
                    }
                    // HR users have role "HR"
                    localStorage.setItem('role', loginData.role || 'HR');
                    // Save fullName for HR users
                    if (loginData.fullName) {
                        localStorage.setItem('hrFullName', loginData.fullName);
                    }
                    
                    // Use storeTokens if available
                    if (typeof storeTokens === 'function') {
                        storeTokens(
                            loginData.accessToken,
                            loginData.refreshToken,
                            'hr',
                            loginData.email,
                            null // HR doesn't have userId in the same way
                        );
                    }
                    
                    console.log('Auto-login successful:', loginData);
                    showNotification('Welcome! Redirecting to your dashboard...', 'success');
                    
                    // Redirect to HR dashboard
                    setTimeout(() => {
                        window.location.href = '/pages/hr/hr-dashboard.html';
                    }, 1500);
                })
                .catch(loginError => {
                    console.error('Auto-login error:', loginError);
                    // If auto-login fails, redirect to login page
                    showNotification('Registration successful! Please log in.', 'success');
                    setTimeout(() => {
                        window.location.href = '/pages/auth/login.html';
                    }, 2000);
                });
            })
            .catch(error => {
                console.error('Registration error:', error);
                let errorMessage = error.message || 'Registration failed. Please try again.';

                // Check if it's a database/connection error
                if (errorMessage.includes('500') || errorMessage.includes('Internal Server Error')) {
                    errorMessage = 'Database connection error. Please ensure database tables are created. See SETUP_DATABASE.md for instructions.';
                } else if (error.message.includes('400')) {
                    try {
                        const errorObj = JSON.parse(error.message.substring(error.message.indexOf('{')));
                        errorMessage = errorObj.message || errorMessage;
                    } catch (e) {
                        // Fallback if not a JSON error
                    }
                }

                showNotification(errorMessage, 'error');
                submitButton.disabled = false;
                submitButton.innerHTML = originalButtonText;
            });

        });
    }
});

// Notification function (similar to login.js and signup-script.js)
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

