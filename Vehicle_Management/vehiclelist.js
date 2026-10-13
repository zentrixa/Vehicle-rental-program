const vehicleRows = document.getElementById("vehicleRows");
const countText = document.getElementById("countText");
const errorMsg = document.getElementById("errorMsg");
const successMsg = document.getElementById("successMsg");

async function loadVehicles() {
    const response = await fetch("/api/vehicles");
    const vehicles = await response.json();

    let rows = "";
    let available = 0;
    for (let i = 0; i < vehicles.length; i++) {
        const v = vehicles[i];
        if (v.available) {
            available++;
        }
        rows += "<tr>";
        rows += '<td class="mono-id">' + v.vehicleId + "</td>";
        rows += '<td><div class="vehicle-cell">' + photoHtml(v, "thumb") + "<div><b>" + escapeHtml(v.brand) + " "
            + escapeHtml(v.model) + '</b><br><span class="small">' + v.vehicleType + " - " + escapeHtml(v.displayDetails)
            + "</span></div></div></td>";
        rows += '<td><span class="plate">' + escapeHtml(v.plateNumber) + "</span></td>";
        rows += "<td>" + v.category + "</td>";
        rows += "<td>" + v.mileage + " km</td>";
        rows += "<td>" + money(v.dailyRate) + "</td>";
        rows += "<td>" + money(v.weeklyCost) + "</td>";
        if (v.available) {
            rows += '<td><span class="status status-yes">Available</span></td>';
        } else {
            rows += '<td><span class="status status-no">Not available</span></td>';
        }
        rows += '<td class="actions-cell"><a class="btn btn-small btn-outline-dark" href="/vehicle/edit-vehicle.html?id='
                + v.vehicleId + '">Edit</a> <button class="btn btn-small btn-outline-danger" onclick="deleteVehicle(\''
                + v.vehicleId + '\')">Delete</button></td>';
        rows += "</tr>";
    }
    vehicleRows.innerHTML = rows;
    countText.textContent = vehicles.length + " vehicles, " + available + " available.";
}

async function deleteVehicle(vehicleId) {
    if (!confirm("Delete vehicle " + vehicleId + "? This can't be undone.")) {
        return;
    }
    errorMsg.hidden = true;
    successMsg.hidden = true;

    const response = await fetch("/api/vehicles/" + vehicleId, { method: "DELETE" });
    const result = await response.json();
    if (result.ok) {
        showMessage(successMsg, result.message);
    } else {
        showMessage(errorMsg, result.message);
    }
    loadVehicles();
}

loadVehicles();
