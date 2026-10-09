// Sign-up page: checks the form, sends it to Java, then shows the error or goes where Java says.
// /api/signup replies with JSON: { ok, message, redirect }.

const signupForm = document.getElementById("signupForm");
const errTxT = document.getElementById("errTxT");
const signButton = document.getElementById("sign");

signupForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    if (!validateForm(signupForm, errTxT)) return;

    // "true" for Premium, "false" for Regular (the radio buttons carry these values)
    const membership = signupForm.elements["membership"].value;

    const details = new URLSearchParams({
        name: document.getElementById("sign_name").value.trim(),
        password: document.getElementById("sign_password").value,
        contactNo: document.getElementById("sign_contactNo").value.trim(),
        licenceNo: document.getElementById("sign_licenceNo").value.trim(),
        email: document.getElementById("sign_email").value.trim(),
        membership: membership
    });

    signButton.disabled = true;
    signButton.textContent = "Creating account...";

    try {
        // POST keeps the password out of the URL
        const response = await fetch("/api/signup", { method: "POST", body: details });
        const reply = await readReply(response);

        if (response.ok && reply && reply.ok) {
            window.location.href = reply.redirect;
            return;
        }
        showFormError(errTxT, (reply && reply.message) || "Something went wrong on our side. Try again.");
    } catch (err) {
        console.error("fetch failed: ", err);
        showFormError(errTxT, "Can't reach the server. Check your connection and try again.");
    }

    signButton.disabled = false;
    signButton.textContent = "Create account";
});
