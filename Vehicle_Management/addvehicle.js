const vehicleForm = document.getElementById("vehicleForm");
const errorMsg = document.getElementById("errorMsg");
const successMsg = document.getElementById("successMsg");

function chosenType() {
    return vehicleForm.querySelector('input[name="type"]:checked').value;
}

vehicleForm.querySelectorAll('input[name="type"]').forEach(function (radio) {
    radio.addEventListener("change", function () {
        showTypeFields(chosenType());
    });
});

document.getElementById("imageName").addEventListener("change", showPhotoPreview);

vehicleForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    errorMsg.hidden = true;
    successMsg.hidden = true;

    const response = await fetch("/api/vehicles", {
        method: "POST",
        body: new URLSearchParams(new FormData(vehicleForm))
    });
    const result = await response.json();

    if (result.ok) {
        showMessage(successMsg, result.message);
        const type = chosenType();
        vehicleForm.reset();
        vehicleForm.querySelector('input[value="' + type + '"]').checked = true;
        showTypeFields(type);
        window.scrollTo(0, 0);
    } else {
        showMessage(errorMsg, result.message);
    }
});

showTypeFields(chosenType());
