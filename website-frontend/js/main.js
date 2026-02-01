// Main JavaScript File

// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    // Initialize button click handlers
    setupButtonHandlers();
    
    // Setup smooth scroll for navigation links
    setupSmoothScroll();
    
    // Setup navbar scroll effect
    setupNavbarScroll();
}

// Button Click Handlers
function setupButtonHandlers() {
    const btnStudent = document.getElementById('btn-student');
    const btnHR = document.getElementById('btn-hr');
    
    if (btnStudent) {
        btnStudent.addEventListener('click', function(e) {
            createRippleEffect(e, this);
            // TODO: Navigate to student registration/login page
            console.log('Student button clicked');
            // window.location.href = 'student/login.html';
        });
    }
    
    if (btnHR) {
        btnHR.addEventListener('click', function(e) {
            createRippleEffect(e, this);
            // TODO: Navigate to HR registration/login page
            console.log('HR button clicked');
            // window.location.href = 'hr/login.html';
        });
    }
}

// Ripple Effect on Button Click
function createRippleEffect(event, button) {
    const ripple = document.createElement('span');
    const rect = button.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const x = event.clientX - rect.left - size / 2;
    const y = event.clientY - rect.top - size / 2;
    
    ripple.style.width = ripple.style.height = size + 'px';
    ripple.style.left = x + 'px';
    ripple.style.top = y + 'px';
    ripple.classList.add('ripple');
    
    button.appendChild(ripple);
    
    setTimeout(() => {
        ripple.remove();
    }, 600);
}

// Smooth Scroll for Navigation Links
function setupSmoothScroll() {
    const navLinks = document.querySelectorAll('a[href^="#"]');
    
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            
            if (href === '#') return;
            
            const target = document.querySelector(href);
            if (target) {
                e.preventDefault();
                const offsetTop = target.offsetTop - 80; // Account for sticky navbar
                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth'
                });
            }
        });
    });
}

// Navbar Scroll Effect
function setupNavbarScroll() {
    const navbar = document.querySelector('.navbar');
    let lastScroll = 0;
    
    window.addEventListener('scroll', function() {
        const currentScroll = window.pageYOffset;
        
        if (currentScroll > 100) {
            navbar.style.background = 'rgba(15, 23, 42, 0.95)';
            navbar.style.boxShadow = '0 4px 20px rgba(0, 0, 0, 0.3)';
        } else {
            navbar.style.background = 'rgba(15, 23, 42, 0.8)';
            navbar.style.boxShadow = 'none';
        }
        
        lastScroll = currentScroll;
    });
}

// Utility: Debounce function for performance
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

// Export functions for use in other files (if needed)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        createRippleEffect,
        setupSmoothScroll,
        setupNavbarScroll
    };
}

