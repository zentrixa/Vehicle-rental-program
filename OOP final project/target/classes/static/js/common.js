// Helpers shared by the login and signup pages.

function showFormError(box, message) {
    box.textContent = message;
    box.hidden = false;
}

function clearFormError(box) {
    box.textContent = "";
    box.hidden = true;
}

// Checks every required field. On the first problem it shows a message,
// marks that field as invalid and moves focus to it.
function validateForm(form, errorBox) {
    clearFormError(errorBox);
    const fields = form.querySelectorAll("input[required]");

    fields.forEach(function (field) { field.removeAttribute("aria-invalid"); });

    for (const field of fields) {
        if (!field.checkValidity()) {
            const message = field.validity.valueMissing
                ? "Enter your " + field.dataset.label + "."
                : field.dataset.error || field.validationMessage;

            field.setAttribute("aria-invalid", "true");
            showFormError(errorBox, message);
            field.focus();
            return false;
        }
    }
    return true;
}

// Show/Hide buttons next to password fields: <button data-toggle-password="inputId">
document.querySelectorAll("[data-toggle-password]").forEach(function (button) {
    const input = document.getElementById(button.dataset.togglePassword);

    button.addEventListener("click", function () {
        const reveal = input.type === "password";
        input.type = reveal ? "text" : "password";
        button.textContent = reveal ? "Hide" : "Show";
        button.setAttribute("aria-pressed", String(reveal));
    });
});

// Reads the JSON reply from /api/signup or /api/login. Returns null if the reply isn't JSON.
async function readReply(response) {
    try {
        return await response.json();
    } catch (err) {
        return null;
    }
}
