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

            // 5. Success
            console.log("--- New User Signup ---");
            console.log("Name:", fullName);
            console.log("Full Phone:", fullPhone); // This will now print e.g., "+420123456789"
            console.log("Gender:", genderValue);
            

        });
    }
});