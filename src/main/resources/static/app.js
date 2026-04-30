const BASE_URL = '/api/products';

// --- Create Product ---
async function createProduct() {
    const name     = document.getElementById('productName').value.trim();
    const brand    = document.getElementById('productBrand').value.trim();
    const category = document.getElementById('productCategory').value.trim();
    const msg      = document.getElementById('productMsg');

    if (!name || !brand || !category) {
        showMessage(msg, 'All fields are required', 'error');
        return;
    }

    try {
        const response = await fetch(BASE_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, brand, category })
        });

        const result = await response.json();

        if (!response.ok) {
            showMessage(msg, result.message, 'error');
            return;
        }

        showMessage(msg, `Created: ${result.data.name} (ID: ${result.data.id})`, 'success');
        document.getElementById('productName').value = '';
        document.getElementById('productBrand').value = '';
        document.getElementById('productCategory').value = '';
        loadProducts();

    } catch (error) {
        showMessage(msg, 'Network error — is the server running?', 'error');
    }
}

// --- Load All Products ---
async function loadProducts() {
    const tbody = document.getElementById('productTableBody');

    try {
        const response = await fetch(BASE_URL);
        const result   = await response.json();
        const products = result.data;

        tbody.innerHTML = '';

        const lowestPrices = await Promise.all(
            products.map(p => fetchLowestPrice(p.id))
        );

        products.forEach((p, index) => {
            const row = document.createElement('tr');
            row.innerHTML = `
        <td>${p.id}</td>
        <td>${p.name}</td>
        <td>${p.brand}</td>
        <td>${p.category}</td>
        <td>${p.autoTrack ? '🟢 Auto' : '⚪ Manual'}</td>
        <td>${formatDate(p.createdAt)}</td>
        <td>${lowestPrices[index]}</td>
      `;
            tbody.appendChild(row);
        });

    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="7">Failed to load products</td></tr>';
    }
}
// --- Fetch Lowest Price for a Product ---
async function fetchLowestPrice(productId) {
    try {
        const response = await fetch(`${BASE_URL}/${productId}/prices/lowest`);
        if (!response.ok) return 'No prices yet';
        const result = await response.json();
        return `$${result.data.price} @ ${result.data.storeName}`;
    } catch {
        return 'N/A';
    }
}

// --- Add Price ---
async function addPrice() {
    const productId = document.getElementById('priceProductId').value;
    const storeName = document.getElementById('storeName').value.trim();
    const price     = document.getElementById('priceValue').value;
    const msg       = document.getElementById('priceMsg');

    if (!productId || !storeName || !price) {
        showMessage(msg, 'All fields are required', 'error');
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/prices`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                productId: Number(productId),
                storeName,
                price: Number(price)
            })
        });

        const result = await response.json();

        if (!response.ok) {
            showMessage(msg, result.message, 'error');
            return;
        }

        showMessage(msg, `Recorded $${result.data.price} at ${result.data.storeName}`, 'success');
        loadProducts();

    } catch (error) {
        showMessage(msg, 'Network error', 'error');
    }
}

// --- Load Price History ---
async function loadPriceHistory() {
    const productId = document.getElementById('historyProductId').value;
    const tbody     = document.getElementById('historyTableBody');

    if (!productId) {
        tbody.innerHTML = '<tr><td colspan="3">Enter a product ID</td></tr>';
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/${productId}/prices`);
        const result   = await response.json();

        if (!response.ok) {
            tbody.innerHTML = `<tr><td colspan="3">${result.message}</td></tr>`;
            return;
        }

        tbody.innerHTML = result.data.map(ph => `
      <tr>
        <td>${ph.storeName}</td>
        <td>$${ph.price}</td>
        <td>${formatDate(ph.recordedAt)}</td>
      </tr>
    `).join('');

    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="3">Failed to load history</td></tr>';
    }
}

// --- Helpers ---
function showMessage(el, text, type) {
    el.textContent = text;
    el.className   = `msg ${type}`;
}

function formatDate(dateStr) {
    return new Date(dateStr).toLocaleString();
}

// Load products on page start
loadProducts();