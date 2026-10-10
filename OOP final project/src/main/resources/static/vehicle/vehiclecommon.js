// Helpers shared by the four vehicle pages (Member 2).
// Uses showFormError, clearFormError and readReply from Member 1's js/common.js,
// and requireLogin from Member 1's js/session.js, so load those two files first.

const VEHICLE_API = "/api/vehicles";

// 6500 -> "Rs. 6,500.00"
function formatMoney(amount) {
    return "Rs. " + Number(amount).toLocaleString("en-US", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

// 45000 -> "45,000 km"
function formatKm(km) {
    return Number(km).toLocaleString("en-US") + " km";
}

// Admins see extra links in the header (Add vehicle, Admin page).
function showAdminLinks(me) {
    if (me.role !== "ADMIN") return;
    document.querySelectorAll("[data-admin-only]").forEach(function (element) {
        element.hidden = false;
    });
}

// A small "Available" / "Not available" label.
function statusBadge(available) {
    const badge = document.createElement("span");
    badge.className = available ? "status status-available" : "status status-unavailable";
    badge.textContent = available ? "Available" : "Not available";
    return badge;
}

function plateLabel(plateNumber) {
    const plate = document.createElement("span");
    plate.className = "plate";
    plate.textContent = plateNumber;
    return plate;
}

// Works like Member 1's validateForm, but with wording for vehicles, and it checks <select>s too.
// Fields inside a disabled fieldset (the other vehicle types) always pass.
function validateVehicleForm(form, errorBox) {
    clearFormError(errorBox);
    const fields = form.querySelectorAll("input[required], select[required]");

    fields.forEach(function (field) { field.removeAttribute("aria-invalid"); });

    for (const field of fields) {
        if (!field.checkValidity()) {
            const message = field.validity.valueMissing
                ? "Enter the " + field.dataset.label + "."
                : field.dataset.error || field.validationMessage;

            field.setAttribute("aria-invalid", "true");
            showFormError(errorBox, message);
            field.focus();
            return false;
        }
    }
    return true;
}

// Shows the extra details for one vehicle type (Car, Van or Motorcycle) and hides the rest.
// Hidden groups are disabled, so they are not checked and not sent to Java.
function showTypeFields(form, type) {
    form.querySelectorAll("[data-type-fields]").forEach(function (group) {
        const matches = group.dataset.typeFields === type;
        group.hidden = !matches;
        group.disabled = !matches;
    });
}

// Every enabled field with a name, in the format Java's @RequestParam reads.
function vehicleFormData(form) {
    return new URLSearchParams(new FormData(form));
}

// Shows a success message, optionally followed by a link.
function showSuccess(box, message, linkText, linkHref) {
    box.replaceChildren(document.createTextNode(message + " "));
    if (linkText) {
        const link = document.createElement("a");
        link.href = linkHref;
        link.textContent = linkText;
        box.append(link);
    }
    box.hidden = false;
}
