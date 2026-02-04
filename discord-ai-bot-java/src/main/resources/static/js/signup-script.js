/**
 * Handles all logic for the Student Sign Up page.
 * Includes: Gender dropdown toggling and Form Validation.
 * In CSS, .iti is a Class Selector. It stands for International Telephone Input.
 */

document.addEventListener('DOMContentLoaded', function() {
    
    const phoneInput = document.getElementById('phone');
    let iti; // We create this variable here so we can use it later in the Submit logic

    if (phoneInput) {
        // Initialize the library and save it to the 'iti' variable
        iti = window.intlTelInput(phoneInput, {
            separateDialCode: true, //The country code is moved outside the typing box and placed next to the flag. If not The user can accidentally backspace and delete the +420, breaking the number.
            preferredCountries: ["cz"],
            utilsScript: "https://cdn.jsdelivr.net/npm/intl-tel-input@18.2.1/build/js/utils.js"
        });
    }

    // --- PART 1: GENDER DROPDOWN LOGIC ---
    const genderSelect = document.getElementById('gender');
    const otherInput = document.getElementById('otherGenderInput');

    // Only run this if the gender dropdown actually exists on the page
    if (genderSelect) {
        genderSelect.addEventListener('change', function() {
            // Check if the user selected "other"
            if (this.value === 'other') {
                otherInput.style.display = 'block';          // Show the text box
                otherInput.setAttribute('required', 'true'); // Make it mandatory
            } else {
                otherInput.style.display = 'none';           // Hide the text box
                otherInput.removeAttribute('required');      // Make it optional
                otherInput.value = '';                       // Clear any text typed
            }
        });
    }

    // --- PART 2: FORM SUBMISSION LOGIC ---
    const signupForm = document.getElementById('signupForm');

    if (signupForm) {
        signupForm.addEventListener('submit', function(event) {
            // 1. Prevent the page from refreshing
            event.preventDefault();

            // 2. Gather the data
            const firstName = document.getElementById('firstName').value;
            const lastName = document.getElementById('lastName').value;
            const fullName = firstName + " " + lastName;
            
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            // 3. Gather Phone Data
            // We use the 'iti' variable we created at the top
            let fullPhone = "";
            if (iti) {
                // getNumber() automatically combines the Code (+420) and the Number (123...)
                fullPhone = iti.getNumber(); 
            }

            // 3. Gather Gender
            let genderValue = genderSelect ? genderSelect.value : "";
            if (genderValue === 'other') {
                genderValue = otherInput.value;
            }

            // 4. Validation
            if (password !== confirmPassword) {
                alert("Passwords do not match! Please try again.");
                return;
            }

            if (password.length < 6) {
                alert("Password must be at least 6 characters long.");
                return;
            }
            
            // Check if phone number is valid according to the library
            if (iti && !iti.isValidNumber()) {
                alert("Please enter a valid phone number.");
                return;
            }

            // 5. Get email from form
            const email = document.getElementById('email').value;
            
            // 6. Send registration request to backend
            const registrationData = {
                email: email,
                password: password,
                firstName: firstName,
                lastName: lastName
            };
            
            // Show loading state
            const submitButton = signupForm.querySelector('button[type="submit"]');
            const originalButtonText = submitButton.textContent;
            submitButton.disabled = true;
            submitButton.textContent = 'Registering...';
            
            // Make API call
            fetch(`${CONFIG.API_BASE_URL}${CONFIG.ENDPOINTS.STUDENT_REGISTER}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(registrationData)
            })
            .then(response => {
                if (!response.ok) {
                    // Try to parse JSON error response
                    return response.json().then(err => {
                        // Extract error message from response
                        const errorMessage = err.message || err.error || `Registration failed: ${response.status}`;
                        throw new Error(errorMessage);
                    }).catch(parseError => {
                        // If response is not JSON, use status text
                        throw new Error(`Registration failed: ${response.status} ${response.statusText}`);
                    });
                }
                return response.json();
            })
            .then(data => {
                // Success
                console.log("Registration successful:", data);
                showNotification(data.message || 'Registration successful! Redirecting to login...', 'success');
                
                // Redirect to login page after successful registration
                setTimeout(() => {
                    window.location.href = '/pages/auth/login.html';
                }, 2000);
            })
            .catch(error => {
                console.error("Registration error:", error);
                // Show detailed error message
                let errorMessage = error.message || 'Registration failed. Please try again.';
                
                // Check if it's a database/connection error
                if (errorMessage.includes('500') || errorMessage.includes('Internal Server Error')) {
                    errorMessage = 'Database connection error. Please ensure database tables are created. See SETUP_DATABASE.md for instructions.';
                }
                
                showNotification(errorMessage, 'error');
                submitButton.disabled = false;
                submitButton.textContent = originalButtonText;
            });
        });
    }
});

// Notification function (similar to login.js)
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
