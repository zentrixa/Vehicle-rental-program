// Vehicle Addition Form (Member 2). Admins only.
// POST /api/vehicles with the form fields -> { ok, message }
// Java builds a Car, Van or Motorcycle from the chosen type and saves it to vehicles.txt.

const vehicleForm = document.getElementById("vehicleForm");
const formError = document.getElementById("formError");
const formMsg = document.getElementById("formMsg");
const saveButton = document.getElementById("saveButton");

function chosenType() {
    return vehicleForm.elements["type"].value;
}

// show the Car, Van or Motorcycle box when the type changes
vehicleForm.querySelectorAll('input[name="type"]').forEach(function (radio) {
    radio.addEventListener("change", function () {
        showTypeFields(vehicleForm, chosenType());
        clearFormError(formError);
    });
});

vehicleForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    formMsg.hidden = true;
    if (!validateVehicleForm(vehicleForm, formError)) return;

    saveButton.disabled = true;
    saveButton.textContent = "Adding...";

    try {
        const response = await fetch(VEHICLE_API, { method: "POST", body: vehicleFormData(vehicleForm) });
        const reply = await readReply(response);

        if (response.status === 401) {
            window.location.href = "/login&signup/login.html";
            return;
        }
        if (response.status === 403) {
            window.location.href = "/vehicle/vehicle-list.html"; // customers can't add vehicles
            return;
        }

        if (response.ok && reply && reply.ok) {
            // ready for the next one: clear the form but keep the same type
            const type = chosenType();
            vehicleForm.reset();
            vehicleForm.elements["type"].value = type;
            showTypeFields(vehicleForm, type);
            showSuccess(formMsg, reply.message, "See all vehicles", "/vehicle/vehicle-list.html");
            document.getElementById("brand").focus();
        } else {
            showFormError(formError, (reply && reply.message) || "Something went wrong on our side. Try again.");
        }
    } catch (err) {
        console.error("fetch failed: ", err);
        showFormError(formError, "Can't reach the server. Check your connection and try again.");
    }

    saveButton.disabled = false;
    saveButton.textContent = "Add vehicle";
});

showTypeFields(vehicleForm, chosenType());

requireLogin().then(function (me) {
    if (!me) return;
    // only admins manage the fleet; customers go back to the list
    if (me.role !== "ADMIN") {
        window.location.href = "/vehicle/vehicle-list.html";
    }
});
