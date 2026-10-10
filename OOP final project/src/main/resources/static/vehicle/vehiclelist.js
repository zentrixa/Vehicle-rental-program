// Vehicle Listing Page (Member 2): every vehicle in the fleet, in a table.
// Admins also get Edit and Delete buttons on each row.
//
// GET    /api/vehicles       -> [ { vehicleId, vehicleType, brand, model, plateNumber, category,
//                                   dailyRate, mileage, available, displayDetails, weeklyCost, ... } ]
// DELETE /api/vehicles/{id}  -> { ok, message }
// Java checks that the caller is an admin before deleting; this page only handles the display.

const listMsg = document.getElementById("listMsg");
const listError = document.getElementById("listError");
const resultCount = document.getElementById("resultCount");
const vehicleTable = document.getElementById("vehicleTable");
const vehicleRows = document.getElementById("vehicleRows");
const actionsHeader = document.getElementById("actionsHeader");
const emptyState = document.getElementById("emptyState");
const emptyText = document.getElementById("emptyText");
const deleteDialog = document.getElementById("deleteDialog");
const deleteText = document.getElementById("deleteText");
const cancelDelete = document.getElementById("cancelDelete");
const confirmDelete = document.getElementById("confirmDelete");

let isAdmin = false;
let pendingDelete = null; // the vehicle the dialog is asking about

// content can be text or an element
function addCell(row, label, content, className) {
    const cell = document.createElement("td");
    cell.dataset.label = label; // shown as the row label on small screens
    if (typeof content === "string") {
        cell.textContent = content;
    } else {
        cell.append(content);
    }
    if (className) cell.className = className;
    row.append(cell);
}

function renderVehicles(vehicles) {
    vehicleRows.replaceChildren();

    vehicles.forEach(function (vehicle) {
        const row = document.createElement("tr");

        addCell(row, "ID", vehicle.vehicleId, "mono-id");

        // name on top, then the type and the details only that type has (polymorphism in Java)
        const name = document.createElement("div");
        const strong = document.createElement("strong");
        strong.textContent = vehicle.brand + " " + vehicle.model;
        const sub = document.createElement("span");
        sub.className = "cell-sub";
        sub.textContent = vehicle.vehicleType + " · " + vehicle.displayDetails;
        name.append(strong, sub);
        addCell(row, "Vehicle", name, "vehicle-cell");

        addCell(row, "Plate", plateLabel(vehicle.plateNumber));
        addCell(row, "Category", vehicle.category);
        addCell(row, "Mileage", formatKm(vehicle.mileage), "num");
        addCell(row, "Daily rate", formatMoney(vehicle.dailyRate), "num");
        addCell(row, "7-day price", formatMoney(vehicle.weeklyCost), "num");
        addCell(row, "Status", statusBadge(vehicle.available));

        if (isAdmin) {
            const actionCell = document.createElement("td");
            actionCell.className = "row-action";

            const editLink = document.createElement("a");
            editLink.className = "btn btn-small btn-outline-dark";
            editLink.href = "/vehicle/edit-vehicle.html?id=" + encodeURIComponent(vehicle.vehicleId);
            editLink.textContent = "Edit";
            editLink.setAttribute("aria-label", "Edit " + vehicle.vehicleId);

            const deleteButton = document.createElement("button");
            deleteButton.type = "button";
            deleteButton.className = "btn btn-small btn-outline-danger";
            deleteButton.textContent = "Delete";
            deleteButton.setAttribute("aria-label", "Delete " + vehicle.vehicleId);
            deleteButton.addEventListener("click", function () { askToDelete(vehicle); });

            actionCell.append(editLink, deleteButton);
            row.append(actionCell);
        }

        vehicleRows.append(row);
    });

    const count = vehicles.length;
    let available = 0;
    vehicles.forEach(function (vehicle) { if (vehicle.available) available++; });
    resultCount.textContent = count + (count === 1 ? " vehicle" : " vehicles") + ", " + available + " available.";

    vehicleTable.hidden = count === 0;
    emptyState.hidden = count !== 0;
    resultCount.hidden = count === 0;
    emptyText.textContent = isAdmin
        ? "Use Add a vehicle to put the first one in the fleet."
        : "Vehicles will appear here once they are added.";
}

async function loadVehicles() {
    clearFormError(listError);

    let response;
    try {
        response = await fetch(VEHICLE_API);
    } catch (err) {
        console.error("fetch failed: ", err);
        showFormError(listError, "Can't reach the server. Check your connection and try again.");
        return false;
    }

    if (response.status === 401) {
        window.location.href = "/login&signup/login.html";
        return false;
    }
    if (!response.ok) {
        showFormError(listError, "Something went wrong on our side. Try again.");
        return false;
    }

    renderVehicles(await response.json());
    return true;
}

function askToDelete(vehicle) {
    pendingDelete = vehicle;
    deleteText.textContent = "Delete " + vehicle.brand + " " + vehicle.model + " (" + vehicle.vehicleId
        + ", plate " + vehicle.plateNumber + ")? Only do this when it is no longer in service. This can't be undone.";
    confirmDelete.disabled = false;
    confirmDelete.textContent = "Delete vehicle";
    deleteDialog.showModal(); // focus starts on Cancel, the safe choice
}

cancelDelete.addEventListener("click", function () { deleteDialog.close(); });
deleteDialog.addEventListener("close", function () { pendingDelete = null; });

confirmDelete.addEventListener("click", async function () {
    if (!pendingDelete) return;
    const vehicle = pendingDelete;

    confirmDelete.disabled = true;
    confirmDelete.textContent = "Deleting...";

    let message = null;
    let errorMessage = null;
    try {
        const response = await fetch(VEHICLE_API + "/" + encodeURIComponent(vehicle.vehicleId), { method: "DELETE" });
        const reply = await readReply(response);
        if (response.ok && reply && reply.ok) {
            message = reply.message;
        } else if (response.status === 401) {
            window.location.href = "/login&signup/login.html";
            return;
        } else {
            errorMessage = (reply && reply.message) || "Something went wrong on our side. Try again.";
        }
    } catch (err) {
        console.error("fetch failed: ", err);
        errorMessage = "Can't reach the server. Check your connection and try again.";
    }

    deleteDialog.close();

    // reload either way: if the vehicle was already gone, the list should show that
    listMsg.hidden = true;
    const loaded = await loadVehicles();
    if (message && loaded) {
        listMsg.textContent = message;
        listMsg.hidden = false;
    }
    if (errorMessage) showFormError(listError, errorMessage);
});

requireLogin().then(function (me) {
    if (!me) return;
    isAdmin = me.role === "ADMIN";
    showAdminLinks(me);
    actionsHeader.hidden = !isAdmin;
    document.getElementById("welcome").textContent = isAdmin
        ? "Signed in as " + me.username + ". Add, edit or remove vehicles in the fleet."
        : "Signed in as " + me.username + ". Here is every vehicle we rent out.";
    loadVehicles();
});
