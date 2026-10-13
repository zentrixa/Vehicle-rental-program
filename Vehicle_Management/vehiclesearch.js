const searchForm = document.getElementById("searchForm");
const results = document.getElementById("results");
const countText = document.getElementById("countText");
const daysInput = document.getElementById("days");

async function search() {
    const brand = document.getElementById("brand").value;
    const type = document.getElementById("type").value;
    const category = document.getElementById("category").value;

    const response = await fetch("/api/vehicles?brand=" + encodeURIComponent(brand) + "&type=" + type + "&category=" + category);
    const vehicles = await response.json();

    let html = "";
    for (let i = 0; i < vehicles.length; i++) {
        const v = vehicles[i];
        let status = '<span class="status status-yes">Available</span>';
        if (!v.available) {
            status = '<span class="status status-no">Not available</span>';
        }
        html += '<div class="vehicle-card">'
            + photoHtml(v, "card-photo")
            + '<div class="card-body">'
            + '<span class="type-tag">' + v.vehicleType + '</span> <span class="mono-id">' + v.vehicleId + '</span>'
            + "<h3>" + escapeHtml(v.brand) + " " + escapeHtml(v.model) + "</h3>"
            + '<p><span class="plate">' + escapeHtml(v.plateNumber) + "</span> " + status + "</p>"
            + "<p>" + v.category + " - " + escapeHtml(v.displayDetails) + "</p>"
            + "<p>" + money(v.dailyRate) + " per day</p>"
            + '<p class="small">' + escapeHtml(v.pricingRule) + "</p>"
            + '<button type="button" class="btn btn-small btn-outline-dark" onclick="showPrice(\'' + v.vehicleId + '\')">Check price</button>'
            + '<p class="price-text" id="price-' + v.vehicleId + '"></p>'
            + "</div></div>";
    }
    results.innerHTML = html;
    countText.textContent = vehicles.length + " vehicles found.";
}

async function showPrice(vehicleId) {
    const premium = document.getElementById("premium").checked;
    const response = await fetch("/api/vehicles/" + vehicleId + "/cost?days=" + daysInput.value + "&premium=" + premium);
    const result = await response.json();
    document.getElementById("price-" + vehicleId).textContent = result.message;
}

searchForm.addEventListener("submit", function (event) {
    event.preventDefault();
    search();
});

search();
