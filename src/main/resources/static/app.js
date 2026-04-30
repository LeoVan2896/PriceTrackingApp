const BASE_URL = '/api/products';

// --- Create Product ---
async function createProduct() {
    const name      = document.getElementById('productName').value.trim();
    const brand     = document.getElementById('productBrand').value.trim();
    const category  = document.getElementById('productCategory').value.trim();
    const url       = document.getElementById('productUrl').value.trim();
    const autoTrack = document.getElementById('autoTrack').checked;
    const msg       = document.getElementById('productMsg');
    const supportedSites = ['newegg.com', 'bhphotovideo.com', 'adorama.com', 'microcenter.com'];
    const isSupported = supportedSites.some(site => url.includes(site));
    if (url && !isSupported) {
        showMessage(msg, 'Supported stores: Newegg, B&H Photo, Adorama, Micro Center', 'error');
        return;
    }
    if (!name || !brand || !category) {
        showMessage(msg, 'Name, brand, and category are required', 'error');
        return;
    }

    if (autoTrack && !url) {
        showMessage(msg, 'A URL is required to enable auto tracking', 'error');
        return;
    }

    if (url && !url.includes('newegg.com')) {
        showMessage(msg, 'Only Newegg URLs are currently supported', 'error');
        return;
    }

    try {
        const response = await fetch(BASE_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, brand, category, url, autoTrack })
        });

        const result = await response.json();

        if (!response.ok) {
            showMessage(msg, result.message, 'error');
            return;
        }

        showMessage(
            msg,
            `✅ Created: ${result.data.name} (ID: ${result.data.id})${autoTrack ? ' — auto tracking enabled' : ''}`,
            'success'
        );

        // Clear form
        document.getElementById('productName').value = '';
        document.getElementById('productBrand').value = '';
        document.getElementById('productCategory').value = '';
        document.getElementById('productUrl').value = '';
        document.getElementById('autoTrack').checked = false;

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
            row.style.cursor = 'pointer';
            row.title = 'Click to view price chart';
            row.onclick = () => {
                document.getElementById('chartProductId').value = p.id;
                loadChart();
                document.getElementById('chartContainer').scrollIntoView({ behavior: 'smooth' });
            };
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





// --- Chart ---
let priceChartInstance = null;

async function loadChart() {
    const productId = document.getElementById('chartProductId').value;
    if (!productId) return;

    try {
        // Fetch price history
        const [historyRes, productRes] = await Promise.all([
            fetch(`${BASE_URL}/${productId}/prices`),
            fetch(`${BASE_URL}/${productId}`)
        ]);

        if (!historyRes.ok || !productRes.ok) {
            alert('Product not found');
            return;
        }

        const history = (await historyRes.json()).data;
        const product = (await productRes.json()).data;

        if (history.length === 0) {
            alert('No price history recorded yet for this product');
            return;
        }

        // Sort chronologically (oldest first)
        const sorted = [...history].sort(
            (a, b) => new Date(a.recordedAt) - new Date(b.recordedAt)
        );

        const labels = sorted.map(ph => formatDate(ph.recordedAt));
        const prices = sorted.map(ph => parseFloat(ph.price));
        const storeName = sorted[0].storeName;

        // Show container and set title
        document.getElementById('chartContainer').style.display = 'block';
        document.getElementById('chartTitle').textContent =
            `${product.name} — ${storeName}`;

        // Destroy previous chart if exists
        if (priceChartInstance) {
            priceChartInstance.destroy();
        }

        const ctx = document.getElementById('priceChart').getContext('2d');
        priceChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'Price ($)',
                    data: prices,
                    borderColor: '#1a1a2e',
                    backgroundColor: 'rgba(26, 26, 46, 0.08)',
                    borderWidth: 2,
                    pointRadius: 4,
                    pointBackgroundColor: '#1a1a2e',
                    tension: 0.3,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: ctx => `$${ctx.parsed.y.toFixed(2)}`
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: false,
                        ticks: {
                            callback: val => `$${val}`
                        }
                    },
                    x: {
                        ticks: {
                            maxTicksLimit: 8,
                            maxRotation: 30
                        }
                    }
                }
            }
        });

    } catch (error) {
        console.error('Failed to load chart:', error);
    }
}