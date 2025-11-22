// Animation JavaScript File

// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeAnimations();
});

function initializeAnimations() {
    // Setup scroll-triggered animations
    setupScrollAnimations();
    
    // Setup parallax effect (optional)
    setupParallaxEffect();
}

// Scroll-triggered Animations using Intersection Observer
function setupScrollAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('active');
                // Stop observing once animated
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);
    
    // Observe all elements with 'reveal' class
    const revealElements = document.querySelectorAll('.reveal');
    revealElements.forEach(el => observer.observe(el));
    
    // Observe all elements with 'stagger-item' class
    const staggerElements = document.querySelectorAll('.stagger-item');
    staggerElements.forEach((el, index) => {
        el.style.transitionDelay = `${index * 0.1}s`;
        observer.observe(el);
    });
}

// Parallax Effect for Background
function setupParallaxEffect() {
    const hero = document.querySelector('.hero');
    const animatedBg = document.querySelector('.animated-background');
    
    if (!hero || !animatedBg) return;
    
    window.addEventListener('scroll', function() {
        const scrolled = window.pageYOffset;
        const rate = scrolled * 0.5;
        
        // Subtle parallax effect
        animatedBg.style.transform = `translateY(${rate}px)`;
    });
}

// Typing Effect (for future use)
function typeWriter(element, text, speed = 50) {
    let i = 0;
    element.textContent = '';
    
    function type() {
        if (i < text.length) {
            element.textContent += text.charAt(i);
            i++;
            setTimeout(type, speed);
        }
    }
    
    type();
}

// Counter Animation (for future use)
function animateCounter(element, target, duration = 2000) {
    let start = 0;
    const increment = target / (duration / 16);
    const timer = setInterval(() => {
        start += increment;
        if (start >= target) {
            element.textContent = Math.floor(target);
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(start);
        }
    }, 16);
}

// Fade In Animation Helper
function fadeIn(element, duration = 500) {
    element.style.opacity = '0';
    element.style.display = 'block';
    
    let start = null;
    function animate(timestamp) {
        if (!start) start = timestamp;
        const progress = timestamp - start;
        
        element.style.opacity = Math.min(progress / duration, 1);
        
        if (progress < duration) {
            requestAnimationFrame(animate);
        }
    }
    
    requestAnimationFrame(animate);
}

// Fade Out Animation Helper
function fadeOut(element, duration = 500) {
    let start = null;
    const startOpacity = parseFloat(window.getComputedStyle(element).opacity);
    
    function animate(timestamp) {
        if (!start) start = timestamp;
        const progress = timestamp - start;
        
        element.style.opacity = Math.max(startOpacity - (progress / duration), 0);
        
        if (progress < duration) {
            requestAnimationFrame(animate);
        } else {
            element.style.display = 'none';
        }
    }
    
    requestAnimationFrame(animate);
}

// Export functions for use in other files (if needed)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        setupScrollAnimations,
        setupParallaxEffect,
        typeWriter,
        animateCounter,
        fadeIn,
        fadeOut
    };
}

