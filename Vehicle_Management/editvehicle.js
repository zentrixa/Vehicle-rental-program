const vehicleForm = document.getElementById("vehicleForm");
const errorMsg = document.getElementById("errorMsg");
const successMsg = document.getElementById("successMsg");
const vehicleId = new URLSearchParams(window.location.search).get("id");

document.getElementById("imageName").addEventListener("change", showPhotoPreview);

async function loadVehicle() {
    const response = await fetch("/api/vehicles/" + vehicleId);
    if (!response.ok) {
        document.getElementById("pageTitle").textContent = "Vehicle not found";
        vehicleForm.hidden = true;
        return;
    }
    const v = await response.json();

    document.getElementById("pageTitle").textContent = "Edit " + v.vehicleId + " - " + v.brand + " " + v.model;
    vehicleForm.querySelector('input[value="' + v.vehicleType + '"]').checked = true;
    showTypeFields(v.vehicleType);

    document.getElementById("brand").value = v.brand;
    document.getElementById("model").value = v.model;
    document.getElementById("plateNumber").value = v.plateNumber;
    document.getElementById("category").value = v.category;
    document.getElementById("dailyRate").value = v.dailyRate;
    document.getElementById("mileage").value = v.mileage;
    document.getElementById("imageName").value = v.imageName;
    showPhotoPreview();

    if (v.available) {
        vehicleForm.querySelector('input[name="available"][value="true"]').checked = true;
    } else {
        vehicleForm.querySelector('input[name="available"][value="false"]').checked = true;
    }

    if (v.vehicleType === "Car") {
        document.getElementById("seats").value = v.seats;
        document.getElementById("fuelType").value = v.fuelType;
    } else if (v.vehicleType === "Van") {
        document.getElementById("cargoCapacity").value = v.cargoCapacity;
    } else {
        document.getElementById("engineCc").value = v.engineCc;
    }
}

vehicleForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    errorMsg.hidden = true;
    successMsg.hidden = true;

    const response = await fetch("/api/vehicles/" + vehicleId, {
        method: "PUT",
        body: new URLSearchParams(new FormData(vehicleForm))
    });
    const result = await response.json();

    if (result.ok) {
        showMessage(successMsg, result.message);
        window.scrollTo(0, 0);
    } else {
        showMessage(errorMsg, result.message);
    }
});

loadVehicle();
