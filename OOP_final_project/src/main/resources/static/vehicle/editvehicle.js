const vehicleForm = document.getElementById("vehicleForm");
const formError = document.getElementById("formError");
const formMsg = document.getElementById("formMsg");
const saveButton = document.getElementById("saveButton");
const pageTitle = document.getElementById("pageTitle");
const pageSub = document.getElementById("pageSub");
const notFound = document.getElementById("notFound");

const vehicleId = new URLSearchParams(window.location.search).get("id") || "";

function showNotFound() {
    pageSub.hidden = true;
    vehicleForm.hidden = true;
    notFound.hidden = false;
}

function showHeading(vehicle) {
    pageTitle.textContent = "Edit " + vehicle.vehicleId;
    pageSub.textContent = vehicle.brand + " " + vehicle.model + " · " + vehicle.vehicleType;
}

function fillForm(vehicle) {
    vehicleForm.elements["type"].value = vehicle.vehicleType;
    showTypeFields(vehicleForm, vehicle.vehicleType);

    document.getElementById("brand").value = vehicle.brand;
    document.getElementById("model").value = vehicle.model;
    document.getElementById("plateNumber").value = vehicle.plateNumber;
    document.getElementById("category").value = vehicle.category;
    document.getElementById("dailyRate").value = vehicle.dailyRate;
    document.getElementById("mileage").value = vehicle.mileage;
    vehicleForm.elements["available"].value = vehicle.available ? "true" : "false";

    if (vehicle.vehicleType === "Car") {
        document.getElementById("seats").value = vehicle.seats;
        document.getElementById("fuelType").value = vehicle.fuelType;
    } else if (vehicle.vehicleType === "Van") {
        document.getElementById("cargoCapacity").value = vehicle.cargoCapacity;
    } else if (vehicle.vehicleType === "Motorcycle") {
        document.getElementById("engineCc").value = vehicle.engineCc;
    }
}

async function loadVehicle() {
    if (!vehicleId) {
        showNotFound();
        return;
    }

    let response;
    try {
        response = await fetch(VEHICLE_API + "/" + encodeURIComponent(vehicleId));
    } catch (err) {
        console.error("fetch failed: ", err);
        pageSub.textContent = "Can't reach the server. Check your connection and reload the page.";
        return;
    }

    if (response.status === 401) {
        window.location.href = "/login&signup/login.html";
        return;
    }
    if (response.status === 404) {
        showNotFound();
        return;
    }
    if (!response.ok) {
        pageSub.textContent = "Something went wrong on our side. Reload the page to try again.";
        return;
    }

    const vehicle = await response.json();
    showHeading(vehicle);
    fillForm(vehicle);
    vehicleForm.hidden = false;
    document.getElementById("brand").focus();
}

vehicleForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    formMsg.hidden = true;
    if (!validateVehicleForm(vehicleForm, formError)) return;

    saveButton.disabled = true;
    saveButton.textContent = "Saving...";

    try {
        const response = await fetch(VEHICLE_API + "/" + encodeURIComponent(vehicleId), {
            method: "PUT",
            body: vehicleFormData(vehicleForm)
        });
        const reply = await readReply(response);

        if (response.status === 401) {
            window.location.href = "/login&signup/login.html";
            return;
        }
        if (response.status === 403) {
            window.location.href = "/vehicle/vehicle-list.html";
            return;
        }
        if (response.status === 404) {
            showNotFound();
            return;
        }

        if (response.ok && reply && reply.ok) {
            pageSub.textContent = document.getElementById("brand").value.trim() + " "
                + document.getElementById("model").value.trim() + " · " + vehicleForm.elements["type"].value;
            showSuccess(formMsg, reply.message, "Back to all vehicles", "/vehicle/vehicle-list.html");
            formMsg.scrollIntoView({ block: "nearest" });
        } else {
            showFormError(formError, (reply && reply.message) || "Something went wrong on our side. Try again.");
        }
    } catch (err) {
        console.error("fetch failed: ", err);
        showFormError(formError, "Can't reach the server. Check your connection and try again.");
    }

    saveButton.disabled = false;
    saveButton.textContent = "Save changes";
});

requireLogin().then(function (me) {
    if (!me) return;
    if (me.role !== "ADMIN") {
        window.location.href = "/vehicle/vehicle-list.html";
        return;
    }
    loadVehicle();
});
