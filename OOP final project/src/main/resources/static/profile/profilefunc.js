// Profile page: shows the details Java sends, and lets customers edit them.
// GET /api/me gives { username, role, homePage, fields: [{ key, label, value }] };
// POST /api/profile saves the changes and replies { ok, message }.

const viewSection = document.getElementById("viewSection");
const profileList = document.getElementById("profileList");
const editButton = document.getElementById("editButton");
const profileForm = document.getElementById("profileForm");
const profileError = document.getElementById("profileError");
const profileMsg = document.getElementById("profileMsg");
const saveButton = document.getElementById("saveButton");
const cancelButton = document.getElementById("cancelButton");

const emailInput = document.getElementById("edit_email");
const contactInput = document.getElementById("edit_contactNo");
const licenceInput = document.getElementById("edit_licenceNo");
const currentPassInput = document.getElementById("edit_currentPassword");
const newPassInput = document.getElementById("edit_newPassword");

let me = null;

function valueOf(key) {
    const field = me.fields.find(function (f) { return f.key === key; });
    return field ? field.value : "";
}

// one row per field Java sends; the fields differ for customers and admins
function renderProfile() {
    profileList.replaceChildren();
    me.fields.forEach(function (field) {
        const row = document.createElement("div");
        row.className = "profile-row";

        const label = document.createElement("dt");
        label.textContent = field.label;

        const value = document.createElement("dd");
        value.textContent = field.value;
        if (field.key === "licenceNo") value.className = "mono";

        row.append(label, value);
        profileList.append(row);
    });
}

function showForm() {
    emailInput.value = valueOf("email");
    contactInput.value = valueOf("contactNo");
    licenceInput.value = valueOf("licenceNo");
    currentPassInput.value = "";
    newPassInput.value = "";
    profileForm.elements["membership"].value = valueOf("membership") === "Premium" ? "true" : "false";

    clearFormError(profileError);
    profileMsg.hidden = true;
    viewSection.hidden = true;
    profileForm.hidden = false;
    emailInput.focus();
}

function showView() {
    profileForm.hidden = true;
    viewSection.hidden = false;
    editButton.focus();
}

editButton.addEventListener("click", showForm);
cancelButton.addEventListener("click", showView);

profileForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    if (!validateForm(profileForm, profileError)) return;

    const currentPassword = currentPassInput.value;
    const newPassword = newPassInput.value;

    if (newPassword && !currentPassword) {
        showFormError(profileError, "Enter your current password to set a new one.");
        currentPassInput.focus();
        return;
    }
    if (newPassword && newPassword.length < 6) {
        showFormError(profileError, "Use at least 6 characters for your new password.");
        newPassInput.focus();
        return;
    }

    const details = new URLSearchParams({
        email: emailInput.value.trim(),
        contactNo: contactInput.value.trim(),
        licenceNo: licenceInput.value.trim(),
        membership: profileForm.elements["membership"].value,
        currentPassword: currentPassword,
        newPassword: newPassword
    });

    saveButton.disabled = true;
    saveButton.textContent = "Saving...";

    try {
        const response = await fetch("/api/profile", { method: "POST", body: details });
        const reply = await readReply(response);

        if (response.status === 401) {
            window.location.href = "/login&signup/login.html";
            return;
        }

        if (response.ok && reply && reply.ok) {
            me = await requireLogin();
            if (!me) return;
            renderProfile();
            showView();
            profileMsg.textContent = reply.message;
            profileMsg.hidden = false;
        } else {
            showFormError(profileError, (reply && reply.message) || "Something went wrong on our side. Try again.");
        }
    } catch (err) {
        console.error("fetch failed: ", err);
        showFormError(profileError, "Can't reach the server. Check your connection and try again.");
    }

    saveButton.disabled = false;
    saveButton.textContent = "Save changes";
});

requireLogin().then(function (account) {
    if (!account) return;
    me = account;
    if (me.role === "ADMIN") document.getElementById("adminLink").hidden = false;
    renderProfile();
    // only customers have details they can edit
    editButton.hidden = me.role !== "CUSTOMER";
});
