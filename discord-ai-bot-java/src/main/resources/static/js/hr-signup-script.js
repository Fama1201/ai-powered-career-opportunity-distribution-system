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

            // 4. Prepare data for API call
            const hrData = {
                firstName: firstName,
                lastName: lastName,
                email: email,
                password: password,
                companyName: companyName,
                industry: industry,
                position: positionValue,
                phone: fullPhone || null
            };

            // 5. Success - Log data (will be replaced with API call later)
            console.log("--- New HR User Signup ---");
            console.log("Name:", fullName);
            console.log("Email:", email);
            console.log("Company:", companyName);
            console.log("Industry:", industry);
            console.log("Position:", positionValue);
            console.log("Phone:", fullPhone || "Not provided");
            console.log("Full Data:", hrData);

            // TODO: Replace with actual API call
            // Example:
            // fetch('http://localhost:8080/api/hr/auth/register', {
            //     method: 'POST',
            //     headers: {
            //         'Content-Type': 'application/json'
            //     },
            //     body: JSON.stringify(hrData)
            // })
            // .then(response => response.json())
            // .then(data => {
            //     if (data.success) {
            //         // Store token
            //         localStorage.setItem('hrToken', data.data.token);
            //         // Redirect to HR dashboard
            //         window.location.href = 'hr-dashboard.html';
            //     } else {
            //         alert(data.message || 'Registration failed. Please try again.');
            //     }
            // })
            // .catch(error => {
            //     console.error('Error:', error);
            //     alert('An error occurred. Please try again later.');
            // });

        });
    }
});

