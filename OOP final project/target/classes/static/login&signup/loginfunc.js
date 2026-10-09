// Login page: checks the form, sends it to Java, then shows the error or goes where Java says.
// /api/login replies with JSON: { ok, message, redirect }. Customers are sent to the
// listing page and admins to the admin page; Java decides, this file just follows.

const loginForm = document.getElementById("loginForm");
const errorMSG = document.getElementById("ErrorMSG");
const loginButton = document.getElementById("login");

loginForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    if (!validateForm(loginForm, errorMSG)) return;

    const details = new URLSearchParams({
        name: document.getElementById("log_name").value.trim(),
        password: document.getElementById("log_pass").value
    });

    loginButton.disabled = true;
    loginButton.textContent = "Logging in...";

    try {
        const response = await fetch("/api/login", { method: "POST", body: details });
        const reply = await readReply(response);

        if (response.ok && reply && reply.ok) {
            window.location.href = reply.redirect;
            return;
        }
        showFormError(errorMSG, (reply && reply.message) || "Couldn't log you in. Try again.");
    } catch (err) {
        console.error("fetch failed: ", err);
        showFormError(errorMSG, "Can't reach the server. Check your connection and try again.");
    }

    loginButton.disabled = false;
    loginButton.textContent = "Log in";
});
