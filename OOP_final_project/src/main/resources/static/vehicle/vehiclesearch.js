const searchForm = document.getElementById("searchForm");
const brandInput = document.getElementById("brand");
const typeSelect = document.getElementById("type");
const categorySelect = document.getElementById("category");
const daysInput = document.getElementById("days");
const clearButton = document.getElementById("clearButton");
const searchError = document.getElementById("searchError");
const resultCount = document.getElementById("resultCount");
const results = document.getElementById("results");
const emptyState = document.getElementById("emptyState");
const emptyTitle = document.getElementById("emptyTitle");
const emptyText = document.getElementById("emptyText");

function daysText() {
    const days = Number(daysInput.value);
    return days + (days === 1 ? " day" : " days");
}

function daysAreValid() {
    const days = Number(daysInput.value);
    return Number.isInteger(days) && days >= 1 && days <= 365;
}

function addFact(list, label, content) {
    const row = document.createElement("div");
    const term = document.createElement("dt");
    term.textContent = label;
    const value = document.createElement("dd");
    if (typeof content === "string") {
        value.textContent = content;
    } else {
        value.append(content);
    }
    row.append(term, value);
    list.append(row);
}

async function showPrice(vehicle, resultBox) {
    if (!daysAreValid()) {
        resultBox.className = "cost-result is-error";
        resultBox.textContent = "Enter between 1 and 365 rental days first.";
        daysInput.focus();
        return;
    }

    resultBox.className = "cost-result";
    resultBox.textContent = "Working it out...";
    try {
        const response = await fetch(VEHICLE_API + "/" + encodeURIComponent(vehicle.vehicleId)
            + "/cost?days=" + encodeURIComponent(daysInput.value));
        const reply = await readReply(response);

        if (response.status === 401) {
            window.location.href = "/login&signup/login.html";
            return;
        }
        if (response.ok && reply && reply.ok) {
            resultBox.textContent = reply.message;
        } else {
            resultBox.className = "cost-result is-error";
            resultBox.textContent = (reply && reply.message) || "Couldn't work out the price. Try again.";
        }
    } catch (err) {
        console.error("fetch failed: ", err);
        resultBox.className = "cost-result is-error";
        resultBox.textContent = "Can't reach the server. Check your connection and try again.";
    }
}

function buildCard(vehicle) {
    const card = document.createElement("article");
    card.className = "vehicle-card";

    const top = document.createElement("div");
    top.className = "card-top";
    const tag = document.createElement("span");
    tag.className = "type-tag";
    tag.textContent = vehicle.vehicleType;
    const id = document.createElement("span");
    id.className = "mono-id";
    id.textContent = vehicle.vehicleId;
    top.append(tag, id);

    const title = document.createElement("h3");
    title.textContent = vehicle.brand + " " + vehicle.model;

    const facts = document.createElement("dl");
    facts.className = "card-facts";
    addFact(facts, "Plate", plateLabel(vehicle.plateNumber));
    addFact(facts, "Category", vehicle.category);
    addFact(facts, "Details", vehicle.displayDetails);
    addFact(facts, "Mileage", formatKm(vehicle.mileage));
    addFact(facts, "Daily rate", formatMoney(vehicle.dailyRate));
    addFact(facts, "Status", statusBadge(vehicle.available));

    const rule = document.createElement("p");
    rule.className = "pricing-rule";
    rule.textContent = vehicle.pricingRule;

    const costBox = document.createElement("div");
    costBox.className = "card-cost";
    const priceButton = document.createElement("button");
    priceButton.type = "button";
    priceButton.className = "btn btn-small btn-outline-dark price-button";
    priceButton.textContent = "Price for " + daysText();
    const resultBox = document.createElement("p");
    resultBox.className = "cost-result";
    resultBox.setAttribute("aria-live", "polite");
    priceButton.addEventListener("click", function () { showPrice(vehicle, resultBox); });
    costBox.append(priceButton, resultBox);

    card.append(top, title, facts, rule, costBox);
    return card;
}

function describeSearch() {
    const parts = [];
    if (typeSelect.value) parts.push(typeSelect.value);
    if (categorySelect.value) parts.push(categorySelect.value);
    if (brandInput.value.trim()) parts.push("“" + brandInput.value.trim() + "”");
    if (parts.length === 0) return "";
    if (parts.length === 1) return " matching " + parts[0];
    return " matching " + parts.slice(0, -1).join(", ") + " and " + parts[parts.length - 1];
}

async function runSearch() {
    clearFormError(searchError);

    const query = new URLSearchParams({
        brand: brandInput.value.trim(),
        type: typeSelect.value,
        category: categorySelect.value
    });

    let response;
    try {
        response = await fetch(VEHICLE_API + "?" + query);
    } catch (err) {
        console.error("fetch failed: ", err);
        showFormError(searchError, "Can't reach the server. Check your connection and try again.");
        return;
    }

    if (response.status === 401) {
        window.location.href = "/login&signup/login.html";
        return;
    }
    if (!response.ok) {
        showFormError(searchError, "Something went wrong on our side. Try again.");
        return;
    }

    const vehicles = await response.json();
    results.replaceChildren();
    vehicles.forEach(function (vehicle) { results.append(buildCard(vehicle)); });

    const count = vehicles.length;
    const filters = describeSearch();
    resultCount.textContent = count + (count === 1 ? " vehicle" : " vehicles") + filters + ".";
    resultCount.hidden = count === 0;
    emptyState.hidden = count !== 0;

    if (count === 0) {
        emptyTitle.textContent = filters ? "No vehicles" + filters : "No vehicles in the fleet yet";
        emptyText.textContent = filters
            ? "Try a different brand, or set Type or Category back to Any."
            : "Vehicles will appear here once an admin adds them.";
    }
}

daysInput.addEventListener("input", function () {
    document.querySelectorAll(".price-button").forEach(function (button) {
        button.textContent = "Price for " + daysText();
    });
});

searchForm.addEventListener("submit", function (event) {
    event.preventDefault();
    runSearch();
});

clearButton.addEventListener("click", function () {
    brandInput.value = "";
    typeSelect.value = "";
    categorySelect.value = "";
    runSearch();
    brandInput.focus();
});

requireLogin().then(function (me) {
    if (!me) return;
    showAdminLinks(me);
    runSearch();
});
