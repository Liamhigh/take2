// Firebase Project App.js
console.log('Firebase Project initialized');

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('Page loaded successfully');
    
    // Add any interactive features here
    addFeatureCardAnimations();
});

// Add smooth animations to feature cards
function addFeatureCardAnimations() {
    const featureCards = document.querySelectorAll('.feature-card');
    
    featureCards.forEach((card, index) => {
        // Stagger the animation
        setTimeout(() => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            
            requestAnimationFrame(() => {
                card.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                card.style.opacity = '1';
                card.style.transform = 'translateY(0)';
            });
        }, index * 100);
    });
}

// Example Firebase configuration (would be replaced with actual config)
const firebaseConfigExample = {
    apiKey: "YOUR_API_KEY",
    authDomain: "YOUR_PROJECT.firebaseapp.com",
    projectId: "YOUR_PROJECT_ID",
    storageBucket: "YOUR_PROJECT.appspot.com",
    messagingSenderId: "YOUR_SENDER_ID",
    appId: "YOUR_APP_ID"
};

console.log('Firebase config ready for initialization');
