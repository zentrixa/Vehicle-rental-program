// Shared by the pages you see after logging in (listing, profile, admin).

// Coming back with the browser's Back button must not show a page from before logging out.
window.addEventListener("pageshow", function (event) {
    if (event.persisted) window.location.reload();
});

// Asks Java who is logged in. Returns { username, role, homePage, fields },
// or sends the visitor to the login page and returns null.
async function requireLogin() {
    try {
        const response = await fetch("/api/me");
        if (response.ok) return await response.json();
    } catch (err) {
        console.error("session check failed: ", err);
    }
    window.location.href = "/login&signup/login.html";
    return null;
}

document.querySelectorAll("[data-logout]").forEach(function (button) {
    button.addEventListener("click", async function () {
        try {
            await fetch("/api/logout", { method: "POST" });
        } catch (err) {
            console.error("logout failed: ", err);
        }
        window.location.href = "/index.html";
    });
});
