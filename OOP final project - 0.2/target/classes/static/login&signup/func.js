

// lisenting to the pressing of submit in the sign-up page
document.getElementById("sign").addEventListener('click', function () {
    console.log("submission working")
    // getting the values from the HTML input boxes
    var name = document.getElementById("sign_name").value;
    var password = document.getElementById("sign_password").value;
    var contactNo = document.getElementById("sign_contactNo").value;
    var licenceNo = document.getElementById("sign_licenceNo").value;
    var email = document.getElementById("sign_email").value;

    fetch("/api/signup?name="+encodeURIComponent(name)
        + "&password=" + encodeURIComponent(password)
        + "&contactNo=" + encodeURIComponent(contactNo)
        + "&licenceNo=" + encodeURIComponent(licenceNo)
        + "&email=" + encodeURIComponent(email)
    )


})
