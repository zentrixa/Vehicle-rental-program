const VEHICLE_API = "/api/vehicles";

function formatMoney(amount) {
    return "Rs. " + Number(amount).toLocaleString("en-US", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function formatKm(km) {
    return Number(km).toLocaleString("en-US") + " km";
}

function showAdminLinks(me) {
    if (me.role !== "ADMIN") return;
    document.querySelectorAll("[data-admin-only]").forEach(function (element) {
        element.hidden = false;
    });
}

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

function showTypeFields(form, type) {
    form.querySelectorAll("[data-type-fields]").forEach(function (group) {
        const matches = group.dataset.typeFields === type;
        group.hidden = !matches;
        group.disabled = !matches;
    });
}

function vehicleFormData(form) {
    return new URLSearchParams(new FormData(form));
}

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
