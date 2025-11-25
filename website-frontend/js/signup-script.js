// document.getElementById('signupForm' is a "Selector." It tells the browser: "Search the whole HTML page (document). Find the one element with id="signupForm"."
//.addEventListener('submit', function(event): Now that we have the form, we attach a "sensor" to it. We are waiting specifically for the 'submit' event (which happens when you click the button or press Enter).

document.getElementById('signupForm').addEventListener('submit', function(event) { 
    // 1. Stop the form from submitting immediately so we can check the password. This is the "Recipe." It says: "When that submit event happens, run the code inside these curly brackets
    event.preventDefault(); //This is one of the most common lines in frontend web dev, the "Stop Sign".

    // 2. Get the values from the password fields. It creates a labeled box in the computer's memory. You are saying, "I want to save something, and I'll name it password
    const password = document.getElementById('password').value;
    //document.getElementById('password') gets the whole input box (the styling, the size, the location). "".value" gets only the text the user typed inside it.
    const confirmPassword = document.getElementById('confirmPassword').value;

    // 3. Simple Validation
    if (password !== confirmPassword) {
        alert("Passwords do not match! Please try again.");
        return; // Stop here
    }

    if (password.length < 6) { // Since password is a String (text), it has a built-in property called .length that counts the characters.
        alert("Password must be at least 6 characters long.");
        return;
    }

    // 4. Success Simulation (Later, you will send this data to your Java backend)
    console.log("Form submitted successfully!");
    console.log("Name:", document.getElementById('fullName').value);
    
    alert("Sign up successful! (Frontend demo)");
    
});

// Note: The Object: Every HTML tag (<input>, <form>, <div>) is an object that JS can pick up, inspect, and change.