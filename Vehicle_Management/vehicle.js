const PHOTOS = {
    Car: ["honda-civic.jpg", "honda-vezel.jpg", "mazda-3.jpg", "mitsubishi-outlander.jpg", "nissan-sylphy.jpg",
        "suzuki-swift.jpg", "toyota-axio.jpg", "toyota-prius.jpg", "toyota-rav4.jpg", "toyota-vitz.jpg"],
    Van: ["ford-transit.jpg", "hyundai-h1.jpg", "mitsubishi-delica.jpg", "nissan-nv350.jpg", "nissan-serena.jpg",
        "renault-trafic.jpg", "toyota-granvia.jpg", "toyota-hiace.jpg", "toyota-noah.jpg", "volkswagen-transporter.jpg"],
    Motorcycle: ["bajaj-pulsar-150.jpg", "honda-cb125f.jpg", "honda-dio.jpg", "ktm-duke-200.jpg",
        "royal-enfield-classic-350.jpg", "suzuki-access-125.jpg", "tvs-apache-rtr-160.jpg", "yamaha-fz-s.jpg",
        "yamaha-r15.jpg", "yamaha-ray-zr.jpg"]
};

const PHOTO_FOLDERS = { Car: "cars", Van: "vans", Motorcycle: "motorcycles" };

function money(amount) {
    return "Rs. " + Number(amount).toFixed(2);
}

function escapeHtml(text) {
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}

function showMessage(box, text) {
    box.textContent = text;
    box.hidden = false;
}

function photoHtml(vehicle, className) {
    if (vehicle.imageUrl === "") {
        return '<div class="' + className + ' no-photo">' + vehicle.vehicleType + '</div>';
    }
    return '<img class="' + className + '" src="' + escapeHtml(vehicle.imageUrl) + '" alt="' + escapeHtml(vehicle.brand) + '">';
}

function showTypeFields(type) {
    const groups = { Car: "carFields", Van: "vanFields", Motorcycle: "motorcycleFields" };
    for (const name in groups) {
        const group = document.getElementById(groups[name]);
        group.hidden = name !== type;
        group.disabled = name !== type;
    }
    fillPhotoOptions(type);
}

function fillPhotoOptions(type) {
    const select = document.getElementById("imageName");
    let options = '<option value="">No photo</option>';
    const files = PHOTOS[type];
    for (let i = 0; i < files.length; i++) {
        const value = PHOTO_FOLDERS[type] + "/" + files[i];
        options += '<option value="' + value + '">' + files[i] + '</option>';
    }
    select.innerHTML = options;
    showPhotoPreview();
}

function showPhotoPreview() {
    const select = document.getElementById("imageName");
    const preview = document.getElementById("photoPreview");
    if (select.value === "") {
        preview.hidden = true;
    } else {
        preview.src = "/vehicle/images/" + select.value;
        preview.hidden = false;
    }
}
