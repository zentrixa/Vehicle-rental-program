// Admin dashboard: lists every customer, searches by username or member number, deletes customers.
// GET    /api/admin/customers?q=...  -> [{ id, username, email, contactNo, licenceNo, membership }]
// DELETE /api/admin/customers/{id}   -> { ok, message }
// Java checks that the caller is an admin on every request; this page only handles the display.

const searchForm = document.getElementById("searchForm");
const searchInput = document.getElementById("searchInput");
const clearButton = document.getElementById("clearButton");
const adminMsg = document.getElementById("adminMsg");
const adminError = document.getElementById("adminError");
const resultCount = document.getElementById("resultCount");
const customerTable = document.getElementById("customerTable");
const customerRows = document.getElementById("customerRows");
const emptyState = document.getElementById("emptyState");
const emptyTitle = document.getElementById("emptyTitle");
const emptyText = document.getElementById("emptyText");
const deleteDialog = document.getElementById("deleteDialog");
const deleteText = document.getElementById("deleteText");
const cancelDelete = document.getElementById("cancelDelete");
const confirmDelete = document.getElementById("confirmDelete");

let currentQuery = "";
let pendingDelete = null; // the customer the dialog is asking about

function addCell(row, label, text, className) {
    const cell = document.createElement("td");
    cell.dataset.label = label; // shown as the row label on small screens
    cell.textContent = text;
    if (className) cell.className = className;
    row.append(cell);
}

function renderCustomers(customers) {
    customerRows.replaceChildren();

    customers.forEach(function (customer) {
        const row = document.createElement("tr");
        addCell(row, "Member no.", customer.id);
        addCell(row, "Username", customer.username);
        addCell(row, "Email", customer.email);
        addCell(row, "Contact", customer.contactNo);
        addCell(row, "Licence", customer.licenceNo, "mono");
        addCell(row, "Membership", customer.membership);

        const actionCell = document.createElement("td");
        actionCell.className = "row-action";
        const button = document.createElement("button");
        button.type = "button";
        button.className = "btn btn-small btn-outline-danger";
        button.textContent = "Delete";
        button.setAttribute("aria-label", "Delete " + customer.username);
        button.addEventListener("click", function () { askToDelete(customer); });
        actionCell.append(button);
        row.append(actionCell);

        customerRows.append(row);
    });

    const count = customers.length;
    const noun = count === 1 ? "customer" : "customers";
    resultCount.textContent = currentQuery
        ? count + " " + noun + (count === 1 ? " matches" : " match") + " \u201c" + currentQuery + "\u201d."
        : count + " " + noun + ".";

    customerTable.hidden = count === 0;
    emptyState.hidden = count !== 0;
    if (count === 0) {
        if (currentQuery) {
            emptyTitle.textContent = "No customers match \u201c" + currentQuery + "\u201d";
            emptyText.textContent = "Check the spelling, or search by member number.";
        } else {
            emptyTitle.textContent = "No customers yet";
            emptyText.textContent = "Customers appear here after they register.";
        }
    }
    clearButton.hidden = !currentQuery;
}

async function loadCustomers(query) {
    currentQuery = query;
    clearFormError(adminError);

    let response;
    try {
        response = await fetch("/api/admin/customers?q=" + encodeURIComponent(query));
    } catch (err) {
        console.error("fetch failed: ", err);
        showFormError(adminError, "Can't reach the server. Check your connection and try again.");
        return false;
    }

    if (response.status === 401) {
        window.location.href = "/login&signup/login.html";
        return false;
    }
    if (response.status === 403) {
        window.location.href = "/listingpage/listingpage.html"; // customers don't belong here
        return false;
    }
    if (!response.ok) {
        showFormError(adminError, "Something went wrong on our side. Try again.");
        return false;
    }

    renderCustomers(await response.json());
    return true;
}

function askToDelete(customer) {
    pendingDelete = customer;
    deleteText.textContent = "Delete " + customer.username + " (member number " + customer.id
        + ")? Their account and details will be removed. This can't be undone.";
    confirmDelete.disabled = false;
    confirmDelete.textContent = "Delete customer";
    deleteDialog.showModal(); // focus starts on Cancel, the safe choice
}

cancelDelete.addEventListener("click", function () { deleteDialog.close(); });
deleteDialog.addEventListener("close", function () { pendingDelete = null; });

confirmDelete.addEventListener("click", async function () {
    if (!pendingDelete) return;
    const customer = pendingDelete;

    confirmDelete.disabled = true;
    confirmDelete.textContent = "Deleting...";

    let message = null;
    let errorMessage = null;
    try {
        const response = await fetch("/api/admin/customers/" + encodeURIComponent(customer.id), { method: "DELETE" });
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

    // refresh the list either way: if the customer was already gone, the list should show that
    adminMsg.hidden = true;
    const loaded = await loadCustomers(currentQuery);
    if (message && loaded) {
        adminMsg.textContent = message;
        adminMsg.hidden = false;
    }
    if (errorMessage) showFormError(adminError, errorMessage);
});

searchForm.addEventListener("submit", function (event) {
    event.preventDefault();
    adminMsg.hidden = true;
    loadCustomers(searchInput.value.trim());
});

clearButton.addEventListener("click", function () {
    searchInput.value = "";
    adminMsg.hidden = true;
    loadCustomers("");
    searchInput.focus();
});

requireLogin().then(function (me) {
    if (!me) return;
    // customers have no business here: send them to their own landing page
    if (me.role !== "ADMIN") {
        window.location.href = me.homePage;
        return;
    }
    document.getElementById("welcome").textContent = "Signed in as " + me.username + ".";
    loadCustomers(""); // nothing searched yet, so show everyone
});
