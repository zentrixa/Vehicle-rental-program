/* =====================================================================
   MEMBER 2 - Vehicle Management (front end)
   Pages: search.html (public search), cars.html / vans.html / motorcycles.html (one type each),
          add.html (Create), edit.html (Update/Delete), list.html (admin listing + quick status update)
   Every vehicle can have a photo (imageUrl). The photo picker reads /images/vehicles/manifest.json.
   Talks to VehicleController: /api/vehicles
   ===================================================================== */

(function () {
  "use strict";

  const $ = (id) => document.getElementById(id);
  const API = "/api/vehicles";
  let branchNames = {};

  async function loadBranches(select) {
    try {
      const branches = await api.get("/api/branches");
      branchNames = {};
      branches.forEach((b) => { branchNames[b.id] = b.name; });
      if (select) {
        select.insertAdjacentHTML("beforeend", branches.map((b) => '<option value="' + b.id + '">' + ui.escape(b.name) + " (" + ui.escape(b.city) + ")</option>").join(""));
      }
    } catch (e) { /* branch module not available - keep going */ }
  }

  const PRICE_RULES = {
    CAR: "Daily rate x days. Rentals of 7 days or more get 10% off.",
    VAN: "Daily rate x days, plus a one-time Rs. 2,000 cleaning fee.",
    MOTORCYCLE: "Daily rate x days, plus Rs. 150/day if no helmet is included. 3 days or more get 5% off."
  };

  function card(v, i) {
    return '<article class="vehicle-card' + (v.status !== "AVAILABLE" ? " unavailable" : "") + '" data-id="' + v.id + '" style="animation-delay:' + Math.min(i, 10) * 50 + 'ms;cursor:pointer" tabindex="0">' +
      '<div class="art' + (v.imageUrl ? " photo" : "") + '">' + ui.vehicleStatusBadge(v.status) + '<span class="type-tag">' + ui.escape(v.category) + "</span>" + ui.vehicleMedia(v) + "</div>" +
      '<div class="body"><div class="meta"><h3>' + ui.escape(v.brand + " " + v.model) + "</h3>" + ui.plate(v.plateNumber) + "</div>" +
      '<div class="small muted">' + v.year + " &middot; " + ui.escape(branchNames[v.branchId] || "Unassigned") + "</div>" +
      '<div class="features">' + v.features.map((f) => "<span>" + ui.escape(f) + "</span>").join("") + "</div>" +
      '<div class="foot"><div class="price">' + ui.money(v.dailyRate) + " <small>per day</small></div>" +
      '<span class="btn btn-sm btn-ghost">Details</span></div></div></article>';
  }

  // ================================================================ search page
  const filters = { type: "", q: "", maxRate: "", branchId: "", onlyAvailable: true };
  let lastResults = [];

  async function runSearch() {
    const box = $("results");
    try {
      const list = await api.get(api.url(API, {
        q: filters.q, type: filters.type, branchId: filters.branchId, maxRate: filters.maxRate,
        status: filters.onlyAvailable ? "AVAILABLE" : ""
      }));
      lastResults = list;
      renderResults();
    } catch (err) {
      box.innerHTML = '<div class="empty"><h3>Could not load the fleet</h3><p>' + ui.escape(err.message) + "</p></div>";
    }
  }

  function renderResults() {
    const sort = $("sortBy").value;
    const list = lastResults.slice().sort((a, b) => {
      if (sort === "rate-asc") return a.dailyRate - b.dailyRate;
      if (sort === "rate-desc") return b.dailyRate - a.dailyRate;
      if (sort === "year-desc") return b.year - a.year;
      return (a.brand + a.model).localeCompare(b.brand + b.model);
    });
    ui.countUp($("resultCount"), list.length, { duration: 400 });
    $("results").innerHTML = list.length
      ? list.map(card).join("")
      : '<div class="empty" style="grid-column:1/-1"><h3>No vehicles match</h3><p>Try a higher price limit, another branch, or untick "only show vehicles I can book now".</p></div>';
  }

  async function showDetails(id) {
    const v = lastResults.find((x) => x.id === id) || await api.get(API + "/" + id);
    const s = Session.get();
    const discount = s && s.kind === "customer" && s.tier === "PREMIUM" ? 0.1 : 0;
    const specs = {
      CAR: [["Seats", v.seats], ["Transmission", v.transmission]],
      VAN: [["Seats", v.seats], ["Cargo", v.cargoCapacityKg + " kg"]],
      MOTORCYCLE: [["Engine", v.engineCc + " cc"], ["Helmet", v.helmetIncluded ? "Included" : "Rs. 150/day"]]
    }[v.type] || [];
    const start = ui.param("start");
    const end = ui.param("end");
    let days = 3;
    if (start && end) days = Math.max(1, Math.round((new Date(end) - new Date(start)) / 86400000) + 1);

    const m = openModal(
      '<div class="detail-art' + (v.imageUrl ? " photo" : "") + '">' + ui.vehicleMedia(v) + "</div>" +
      '<div class="panel-head" style="margin-bottom:6px"><h3 style="margin:0">' + ui.escape(v.displayName) + "</h3>" + ui.plate(v.plateNumber) + "</div>" +
      '<p class="muted small">' + ui.escape(v.category) + " &middot; " + ui.escape(branchNames[v.branchId] || "Unassigned branch") + " &middot; " + ui.vehicleStatusBadge(v.status) + "</p>" +
      '<dl class="spec-list">' + specs.concat([["Fuel", v.fuelType], ["Colour", v.colour], ["Mileage", v.mileage.toLocaleString() + " km"], ["Daily rate", ui.money(v.dailyRate)]])
        .map((p) => "<div><dt>" + p[0] + "</dt><dd>" + ui.escape(p[1]) + "</dd></div>").join("") + "</dl>" +
      '<div class="quote-box"><div class="days-row"><label for="qDays" class="small">Rental days</label><input type="range" id="qDays" min="1" max="30" value="' + days + '"><output id="qDaysOut"></output></div>' +
      '<p class="small muted" style="margin:8px 0 0">' + PRICE_RULES[v.type] + (discount ? " Your Premium 10% discount is applied." : "") + "</p>" +
      '<div class="total"><span id="qStandard" class="small muted"></span><strong id="qFinal">-</strong></div></div>' +
      '<div class="form-actions"><button class="btn btn-ghost" data-close>Close</button>' +
      (v.status === "AVAILABLE" ? '<a class="btn btn-dark" id="qBook" href="#">Book this vehicle</a>' : '<span class="badge badge-amber">Not bookable right now</span>') + "</div>",
      true
    );
    const range = m.el.querySelector("#qDays");
    const update = ui.debounce(async () => {
      const d = Number(range.value);
      m.el.querySelector("#qDaysOut").textContent = d + (d === 1 ? " day" : " days");
      try {
        const q = await api.get(api.url(API + "/" + v.id + "/quote", { days: d, discount }));
        m.el.querySelector("#qFinal").textContent = ui.money(q.finalCost);
        m.el.querySelector("#qStandard").textContent = discount ? "Standard " + ui.money(q.standardCost) : "Total for " + d + " days";
      } catch (err) { m.el.querySelector("#qFinal").textContent = "-"; }
      const book = m.el.querySelector("#qBook");
      if (book) {
        const s0 = start || ui.today();
        book.href = api.url("/bookings/book.html", { vehicleId: v.id, start: s0, end: ui.addDays(s0, d - 1) });
      }
    }, 120);
    range.addEventListener("input", () => { m.el.querySelector("#qDaysOut").textContent = range.value + " days"; update(); });
    update();
  }

  async function initSearch() {
    await loadBranches($("branchFilter"));
    filters.type = (ui.param("type") || "").toUpperCase();
    filters.branchId = ui.param("branch") || "";
    $("branchFilter").value = filters.branchId;
    document.querySelectorAll("#typeChips .chip").forEach((c) => c.classList.toggle("on", c.dataset.type === filters.type));

    $("typeChips").addEventListener("click", (e) => {
      const chip = e.target.closest(".chip");
      if (!chip) return;
      document.querySelectorAll("#typeChips .chip").forEach((c) => c.classList.toggle("on", c === chip));
      filters.type = chip.dataset.type;
      runSearch();
    });
    $("q").addEventListener("input", ui.debounce((e) => { filters.q = e.target.value; runSearch(); }, 250));
    $("maxRate").addEventListener("input", (e) => {
      const max = Number(e.target.value);
      filters.maxRate = max >= 20000 ? "" : max;
      $("maxRateText").textContent = filters.maxRate ? "Up to " + ui.money(max) : "Any price";
    });
    $("maxRate").addEventListener("change", runSearch);
    $("branchFilter").addEventListener("change", (e) => { filters.branchId = e.target.value; runSearch(); });
    $("onlyAvailable").addEventListener("change", (e) => { filters.onlyAvailable = e.target.checked; runSearch(); });
    $("sortBy").addEventListener("change", renderResults);
    $("resetFilters").addEventListener("click", () => {
      Object.assign(filters, { type: "", q: "", maxRate: "", branchId: "", onlyAvailable: true });
      $("q").value = ""; $("maxRate").value = 20000; $("maxRateText").textContent = "Any price"; $("branchFilter").value = ""; $("onlyAvailable").checked = true;
      document.querySelectorAll("#typeChips .chip").forEach((c) => c.classList.toggle("on", c.dataset.type === ""));
      runSearch();
    });
    $("results").addEventListener("click", (e) => { const c = e.target.closest(".vehicle-card"); if (c) showDetails(c.dataset.id); });
    $("results").addEventListener("keydown", (e) => { const c = e.target.closest(".vehicle-card"); if (c && e.key === "Enter") showDetails(c.dataset.id); });

    try {
      const stats = await api.get(API + "/stats");
      $("fleetSummary").textContent = stats.total + " vehicles in the fleet: " + (stats.byType.CAR || 0) + " cars, " + (stats.byType.VAN || 0) +
        " vans and " + (stats.byType.MOTORCYCLE || 0) + " motorcycles. " + (stats.byStatus.AVAILABLE || 0) + " are ready to book now.";
    } catch (e) { /* keep default text */ }
    runSearch();
  }

  // ================================================================ category pages (cars / vans / motorcycles)
  const catFilter = { q: "", onlyAvailable: false };

  async function loadCategory(type) {
    const list = await api.get(api.url(API, { type, q: catFilter.q, status: catFilter.onlyAvailable ? "AVAILABLE" : "" }));
    lastResults = list;
    renderResults();
  }

  async function initCategory() {
    const type = document.body.dataset.type;
    await loadBranches();
    try {
      const all = await api.get(api.url(API, { type }));
      const available = all.filter((v) => v.status === "AVAILABLE");
      ui.countUp($("cTotal"), all.length);
      ui.countUp($("cAvailable"), available.length);
      ui.countUp($("cFrom"), all.length ? Math.min.apply(null, all.map((v) => v.dailyRate)) : 0, { prefix: "Rs. " });
      ui.countUp($("cBranches"), new Set(all.map((v) => v.branchId)).size);
    } catch (e) { /* stats are optional */ }
    $("q").addEventListener("input", ui.debounce((e) => { catFilter.q = e.target.value; loadCategory(type); }, 250));
    $("availChips").addEventListener("click", (e) => {
      const chip = e.target.closest(".chip");
      if (!chip) return;
      document.querySelectorAll("#availChips .chip").forEach((c) => c.classList.toggle("on", c === chip));
      catFilter.onlyAvailable = chip.dataset.avail === "1";
      loadCategory(type);
    });
    $("sortBy").addEventListener("change", renderResults);
    $("results").addEventListener("click", (e) => { const c = e.target.closest(".vehicle-card"); if (c) showDetails(c.dataset.id); });
    $("results").addEventListener("keydown", (e) => { const c = e.target.closest(".vehicle-card"); if (c && e.key === "Enter") showDetails(c.dataset.id); });
    loadCategory(type);
  }

  // ================================================================ add / edit form (shared)
  function selectedType(form) {
    const checked = form.querySelector('input[name="type"]:checked');
    return checked ? checked.value : form.querySelector('input[name="type"]').value;
  }

  function showTypeFields(form, type) {
    form.querySelectorAll(".type-fields").forEach((box) => {
      const on = box.dataset.for === type;
      box.classList.toggle("hidden", !on);
      box.querySelectorAll("input,select").forEach((el) => { el.disabled = !on; });
    });
  }

  function previewFrom(form) {
    const d = ui.formData(form);
    const type = selectedType(form);
    const features = {
      CAR: [(d.seats || 5) + " seats", d.transmission || "Automatic", d.fuelType],
      VAN: [(d.seats || 12) + " seats", (d.cargoCapacityKg || 0) + " kg cargo", d.fuelType],
      MOTORCYCLE: [(d.engineCc || 125) + " cc", d.helmetIncluded ? "Helmet included" : "Helmet Rs. 150/day", d.fuelType]
    }[type];
    const fake = { type, colour: d.colour, imageUrl: d.imageUrl, brand: d.brand, model: d.model };
    $("previewCard").innerHTML =
      '<article class="vehicle-card"><div class="art' + (d.imageUrl ? " photo" : "") + '">' + ui.vehicleStatusBadge(d.status || "AVAILABLE") + '<span class="type-tag">' + ui.typeLabel(type) + "</span>" +
      ui.vehicleMedia(fake) + '</div><div class="body"><div class="meta"><h3>' + ui.escape((d.brand || "Brand") + " " + (d.model || "Model")) + "</h3>" +
      ui.plate((d.plateNumber || "ABC-0000").toUpperCase()) + '</div><div class="small muted">' + ui.escape(d.year || "") + " &middot; " +
      ui.escape(branchNames[d.branchId] || "Unassigned") + '</div><div class="features">' + features.map((f) => "<span>" + ui.escape(f) + "</span>").join("") +
      '</div><div class="foot"><div class="price">' + ui.money(d.dailyRate || 0) + " <small>per day</small></div></div></div></article>";
  }

  // ---------------------------------------------------------------- photo picker (add + edit)
  const TYPE_FOLDER = { CAR: "cars", VAN: "vans", MOTORCYCLE: "motorcycles" };
  let manifest = null;

  async function loadManifest() {
    if (manifest) return manifest;
    try { manifest = await api.get("/images/vehicles/manifest.json"); } catch (e) { manifest = { cars: [], vans: [], motorcycles: [] }; }
    return manifest;
  }

  function showPhoto(form) {
    const url = $("imageUrl").value;
    $("photoBox").classList.toggle("has", !!url);
    $("photoBox").innerHTML = url
      ? '<img src="' + ui.escape(url) + '" alt="Selected photo"><span class="small">' + ui.escape(url.split("/").pop()) + "</span>"
      : '<span class="small muted">No photo yet. The site will draw the vehicle instead.</span>';
    $("clearPhoto").disabled = !url;
  }

  function setPhoto(form, url) {
    $("imageUrl").value = url || "";
    showPhoto(form);
    form.dispatchEvent(new Event("input"));
  }

  /** When the brand + model typed match a photo in the gallery, use it automatically. */
  async function autoMatchPhoto(form) {
    if ($("imageUrl").value) return;
    const d = ui.formData(form);
    const wanted = ((d.brand || "") + " " + (d.model || "")).toLowerCase().replace(/[^a-z0-9]/g, "");
    if (wanted.length < 5) return;
    const m = await loadManifest();
    const all = [].concat(m.cars, m.vans, m.motorcycles);
    const hit = all.find((p) => p.label.toLowerCase().replace(/[^a-z0-9]/g, "") === wanted);
    if (hit) { setPhoto(form, hit.file); toast("Found a matching photo: " + hit.label, "success"); }
  }

  async function openPicker(form) {
    const m = await loadManifest();
    let tab = TYPE_FOLDER[selectedType(form)] || "cars";
    const modal = openModal(
      '<h3>Choose a photo</h3><p class="small muted">Photos are stored in <code>/images/vehicles/</code>. Click one to use it.</p>' +
      '<div class="chips" id="pickTabs"><button class="chip" data-tab="cars">Cars</button><button class="chip" data-tab="vans">Vans</button><button class="chip" data-tab="motorcycles">Motorcycles</button></div>' +
      '<div class="pick-grid" id="pickGrid"></div>' +
      '<div class="field" style="margin-top:14px"><label for="pickPath">Or type an image path</label><input id="pickPath" placeholder="/images/vehicles/cars/my-car.jpg"></div>' +
      '<div class="form-actions"><button class="btn btn-ghost" data-close>Cancel</button><button class="btn" id="pickUsePath">Use this path</button></div>', true);
    const draw = () => {
      modal.el.querySelectorAll("#pickTabs .chip").forEach((c) => c.classList.toggle("on", c.dataset.tab === tab));
      modal.el.querySelector("#pickGrid").innerHTML = (m[tab] || []).map((p) =>
        '<button type="button" class="pick-item' + (p.file === $("imageUrl").value ? " on" : "") + '" data-file="' + p.file + '"><img src="' + p.file + '" alt="" loading="lazy"><span>' + ui.escape(p.label) + "</span></button>").join("");
    };
    draw();
    modal.el.querySelector("#pickTabs").addEventListener("click", (e) => { const c = e.target.closest(".chip"); if (c) { tab = c.dataset.tab; draw(); } });
    modal.el.querySelector("#pickGrid").addEventListener("click", (e) => {
      const item = e.target.closest(".pick-item");
      if (!item) return;
      setPhoto(form, item.dataset.file);
      modal.close();
    });
    modal.el.querySelector("#pickUsePath").addEventListener("click", () => {
      const path = modal.el.querySelector("#pickPath").value.trim();
      if (!path) { toast("Type an image path first", "error"); return; }
      setPhoto(form, path);
      modal.close();
    });
  }

  function setupPhotoPicker(form) {
    $("pickPhoto").addEventListener("click", () => openPicker(form));
    $("clearPhoto").addEventListener("click", () => setPhoto(form, ""));
    showPhoto(form);
  }

  async function initAdd() {
    if (!Session.requireAdmin()) return;
    const form = $("vehicleForm");
    await loadBranches($("branchId"));
    form.querySelectorAll("[data-art]").forEach((el) => { el.innerHTML = ui.vehicleArt(el.dataset.art, "silver"); });
    $("year").value = new Date().getFullYear();
    $("year").max = new Date().getFullYear() + 1;
    const refresh = () => {
      const type = selectedType(form);
      $("previewQuote").innerHTML = "<dt>Rule</dt><dd style='text-align:left;font-weight:500'>" + PRICE_RULES[type] + "</dd>";
      previewFrom(form);
    };
    form.querySelectorAll('input[name="type"]').forEach((r) => r.addEventListener("change", () => { showTypeFields(form, r.value); refresh(); }));
    form.addEventListener("input", refresh);
    $("brand").addEventListener("change", () => autoMatchPhoto(form));
    $("model").addEventListener("change", () => autoMatchPhoto(form));
    setupPhotoPicker(form);
    showTypeFields(form, "CAR");
    refresh();

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      try {
        const v = await ui.busy($("saveVehicle"), () => api.post(API, ui.formData(form)));
        Session.logAdminAction("Added vehicle " + v.id + " " + v.displayName + " (" + v.plateNumber + ")");
        const m = openModal('<div style="text-align:center"><div class="detail-art' + (v.imageUrl ? " photo" : "") + '">' + ui.vehicleMedia(v) + '</div><span class="stamp">ADDED</span>' +
          "<h3 style='margin-top:16px'>" + ui.escape(v.displayName) + '</h3><p class="muted">Saved as ' + v.id + " with plate " + ui.escape(v.plateNumber) + ".</p>" +
          '<div class="form-actions" style="justify-content:center"><a class="btn btn-ghost" href="/vehicles/add.html">Add another</a><a class="btn btn-dark" href="/vehicles/list.html">Go to fleet register</a></div></div>');
        m.el.querySelector(".btn-ghost").addEventListener("click", (ev) => { ev.preventDefault(); m.close(); form.reset(); $("imageUrl").value = ""; showPhoto(form); showTypeFields(form, "CAR"); refresh(); });
      } catch (err) { toast(err.message, "error"); ui.showFieldError(form, err.message); }
    });
  }

  async function initEdit() {
    if (!Session.requireAdmin()) return;
    const id = ui.param("id");
    const form = $("vehicleForm");
    await loadBranches($("branchId"));
    let v;
    try { v = await api.get(API + "/" + id); } catch (err) { toast(err.message, "error"); return; }

    $("typeHidden").value = v.type;
    $("typeBadge").textContent = ui.typeLabel(v.type) + " " + v.id;
    $("editTitle").textContent = "Edit " + v.displayName;
    showTypeFields(form, v.type);
    ui.fillForm(form, v);
    setupPhotoPicker(form);

    const quote = ui.debounce(async () => {
      try {
        const rows = await Promise.all([1, 3, 7].map((d) => api.get(api.url(API + "/" + v.id + "/quote", { days: d }))));
        $("previewQuote").innerHTML = rows.map((q) => "<dt>" + q.days + (q.days === 1 ? " day" : " days") + "</dt><dd>" + ui.money(q.finalCost) + "</dd>").join("") +
          "<dt>Rule</dt><dd style='text-align:left;font-weight:500'>" + PRICE_RULES[v.type] + "</dd>";
      } catch (e) { /* ignore */ }
    }, 200);
    form.addEventListener("input", () => previewFrom(form));
    previewFrom(form);
    quote();

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      try {
        const before = v.status;
        v = await ui.busy($("saveVehicle"), () => api.put(API + "/" + v.id, ui.formData(form)));
        Session.logAdminAction("Updated vehicle " + v.id + " " + v.displayName + (before !== v.status ? " (status: " + v.statusLabel + ")" : ""));
        toast("Vehicle saved", "success");
        ui.fillForm(form, v);
        form.querySelector('[name="correctMileage"]').checked = false;
        showPhoto(form);
        previewFrom(form);
        quote();
      } catch (err) { toast(err.message, "error"); ui.showFieldError(form, err.message); }
    });

    $("deleteVehicle").addEventListener("click", async () => {
      const ok = await confirmDialog({ title: "Remove " + v.displayName + "?", message: "The vehicle " + v.plateNumber + " will be deleted from vehicles.txt.", confirmText: "Remove", danger: true });
      if (!ok) return;
      try {
        await api.del(API + "/" + v.id);
        Session.logAdminAction("Removed vehicle " + v.id + " " + v.displayName + " (" + v.plateNumber + ")");
        toast("Vehicle removed", "success");
        setTimeout(() => { location.href = "/vehicles/list.html"; }, 700);
      } catch (err) { toast(err.message, "error"); }
    });
  }

  // ================================================================ admin list page
  const listFilter = { status: "", type: "", q: "" };

  async function loadList() {
    const rows = $("vehicleRows");
    try {
      const [list, stats] = await Promise.all([api.get(api.url(API, listFilter)), api.get(API + "/stats")]);
      ui.countUp($("vTotal"), stats.total);
      ui.countUp($("vAvailable"), stats.byStatus.AVAILABLE || 0);
      ui.countUp($("vRented"), stats.byStatus.RENTED || 0);
      ui.countUp($("vMaintenance"), stats.byStatus.MAINTENANCE || 0);
      if (!list.length) { rows.innerHTML = '<tr><td colspan="8"><div class="empty" style="border:0">No vehicles match. <a href="/vehicles/add.html">Add a vehicle</a></div></td></tr>'; return; }
      rows.innerHTML = list.map((v) =>
        '<tr data-id="' + v.id + '"><td><div class="v-cell"><div class="v-thumb">' + ui.vehicleMedia(v) + "</div><div><strong>" + ui.escape(v.brand + " " + v.model) +
        '</strong><div class="small muted">' + v.id + " &middot; " + v.year + "</div></div></div></td>" +
        "<td>" + ui.plate(v.plateNumber) + "</td><td>" + ui.typeLabel(v.type) + '<div class="small muted">' + ui.escape(v.category) + "</div></td>" +
        "<td>" + ui.escape(branchNames[v.branchId] || "-") + "</td><td>" + ui.money(v.dailyRate) + "</td><td>" + v.mileage.toLocaleString() + " km</td>" +
        '<td><select class="select-sm status-select ' + v.status + '" data-status aria-label="Status">' +
        ["AVAILABLE", "RENTED", "MAINTENANCE"].map((s) => '<option value="' + s + '"' + (s === v.status ? " selected" : "") + ">" + { AVAILABLE: "Available", RENTED: "On rent", MAINTENANCE: "Maintenance" }[s] + "</option>").join("") +
        "</select></td>" +
        '<td><div class="actions"><a class="btn btn-ghost btn-sm" href="/vehicles/edit.html?id=' + v.id + '">Edit</a>' +
        '<button class="btn btn-ghost btn-sm" data-delete style="color:var(--stop)">Delete</button></div></td></tr>'
      ).join("");
      rows.querySelectorAll("tr").forEach((tr) => { tr.__data = list.find((x) => x.id === tr.dataset.id); });
    } catch (err) { rows.innerHTML = '<tr><td colspan="8">' + ui.escape(err.message) + "</td></tr>"; }
  }

  async function initList() {
    if (!Session.requireAdmin()) return;
    await loadBranches();
    loadList();
    $("listSearch").addEventListener("input", ui.debounce((e) => { listFilter.q = e.target.value; loadList(); }, 250));
    $("typeSelect").addEventListener("change", (e) => { listFilter.type = e.target.value; loadList(); });
    $("statusChips").addEventListener("click", (e) => {
      const chip = e.target.closest(".chip");
      if (!chip) return;
      document.querySelectorAll("#statusChips .chip").forEach((c) => c.classList.toggle("on", c === chip));
      listFilter.status = chip.dataset.status;
      loadList();
    });
    $("vehicleRows").addEventListener("change", async (e) => {
      if (!e.target.matches("[data-status]")) return;
      const tr = e.target.closest("tr");
      const v = tr.__data;
      try {
        const updated = await api.patch(API + "/" + v.id + "/status", { status: e.target.value });
        Session.logAdminAction("Set " + updated.displayName + " (" + updated.plateNumber + ") to " + updated.statusLabel);
        toast(updated.displayName + " is now " + updated.statusLabel.toLowerCase(), "success");
        tr.classList.remove("arriving"); void tr.offsetWidth; tr.classList.add("arriving");
        loadList();
      } catch (err) { toast(err.message, "error"); e.target.value = v.status; }
    });
    $("vehicleRows").addEventListener("click", async (e) => {
      if (!e.target.closest("[data-delete]")) return;
      const tr = e.target.closest("tr");
      const v = tr.__data;
      const ok = await confirmDialog({ title: "Remove " + v.displayName + "?", message: "Plate " + v.plateNumber + " will be deleted from the fleet.", confirmText: "Remove", danger: true });
      if (!ok) return;
      try {
        await api.del(API + "/" + v.id);
        Session.logAdminAction("Removed vehicle " + v.id + " " + v.displayName);
        tr.classList.add("leaving");
        setTimeout(loadList, 400);
        toast("Vehicle removed", "success");
      } catch (err) { toast(err.message, "error"); }
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    const page = document.body.dataset.page;
    if (page === "search") initSearch();
    if (page === "category") initCategory();
    if (page === "add") initAdd();
    if (page === "edit") initEdit();
    if (page === "list") initList();
  });
})();
