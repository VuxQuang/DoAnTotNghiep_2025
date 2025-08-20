document.addEventListener("DOMContentLoaded", function () {
    const tourCards = document.querySelectorAll(".tour-card:not(.empty)");

    tourCards.forEach(card => {
        card.addEventListener("mouseenter", () => {
            card.classList.add("shadow-lg");
        });
        card.addEventListener("mouseleave", () => {
            card.classList.remove("shadow-lg");
        });
    });
});

document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});
