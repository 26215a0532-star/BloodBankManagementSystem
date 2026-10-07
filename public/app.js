const API_BASE = '/api';
let authToken = '';

const app = {
    init() {
        // Login Form
        document.getElementById('login-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            await this.login();
        });

        // Navigation
        document.querySelectorAll('.nav-links a').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                document.querySelectorAll('.nav-links a').forEach(l => l.classList.remove('active'));
                e.target.classList.add('active');
                this.switchView(e.target.dataset.target);
            });
        });

        // Logout
        document.getElementById('logout-btn').addEventListener('click', () => this.logout());

        // Add Donor Form
        document.getElementById('add-donor-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            await this.addDonor();
        });

        // Request Blood Form
        document.getElementById('request-blood-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            await this.requestBlood();
        });

        // Record Donation Form
        document.getElementById('record-donation-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            await this.recordDonation();
        });

        // Theme Toggle
        document.getElementById('theme-toggle').addEventListener('click', () => {
            document.body.classList.toggle('light-mode');
            const isLight = document.body.classList.contains('light-mode');
            document.getElementById('theme-toggle').textContent = isLight ? '☀️' : '🌙';
        });
    },

    async request(endpoint, options = {}) {
        const headers = { 'Content-Type': 'application/json' };
        if (authToken) headers['Authorization'] = authToken;
        
        try {
            const res = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
            const data = await res.json().catch(() => ({}));
            
            if (!res.ok) {
                if (res.status === 401) this.logout();
                throw new Error(data.message || 'Request failed');
            }
            return data;
        } catch (err) {
            alert(err.message);
            throw err;
        }
    },

    async login() {
        const user = document.getElementById('username').value;
        const pass = document.getElementById('password').value;
        const errDiv = document.getElementById('login-error');
        
        try {
            const data = await this.request('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ username: user, password: pass })
            });
            
            authToken = data.token;
            errDiv.textContent = '';
            
            // Switch to dashboard
            document.getElementById('login-screen').classList.remove('active');
            document.getElementById('dashboard-screen').classList.add('active');
            this.switchView('inventory');
            
        } catch (err) {
            errDiv.textContent = err.message;
        }
    },

    logout() {
        authToken = '';
        document.getElementById('dashboard-screen').classList.remove('active');
        document.getElementById('login-screen').classList.add('active');
    },

    switchView(view) {
        document.querySelectorAll('.view-section').forEach(el => el.classList.remove('active'));
        document.getElementById(`view-${view}`).classList.add('active');
        
        const titles = {
            'inventory': 'Inventory Dashboard',
            'donors': 'Donors Directory',
            'requests': 'Blood Requests'
        };
        document.getElementById('page-title').textContent = titles[view];

        // Load data
        if (view === 'inventory') this.loadInventory();
        else if (view === 'donors') this.loadDonors();
        else if (view === 'requests') this.loadRequests();
    },

    async loadInventory() {
        const grid = document.getElementById('inventory-grid');
        grid.innerHTML = '<div style="color: #cbd5e1">Loading...</div>';
        
        try {
            const inventory = await this.request('/inventory');
            grid.innerHTML = inventory.map(item => `
                <div class="blood-card glass">
                    <div class="blood-type">${item.bloodGroup}</div>
                    <div class="blood-stats">
                        <span>Available: <b>${item.available} Units</b></span>
                    </div>
                    <div class="blood-stats" style="margin-top: 0.5rem; font-size: 0.75rem; color: #94a3b8;">
                        <span>Reserved: ${item.reserved}</span>
                    </div>
                </div>
            `).join('');
        } catch (e) {
            grid.innerHTML = '<div style="color: #ef4444">Failed to load inventory.</div>';
        }
    },

    async loadDonors() {
        const tbody = document.getElementById('donors-table-body');
        try {
            const donors = await this.request('/donors');
            tbody.innerHTML = donors.map(d => `
                <tr>
                    <td>#${d.id}</td>
                    <td><b>${d.fullName}</b><br><small style="color:#94a3b8">${d.email}</small></td>
                    <td><span class="status-badge status-approved">${d.bloodGroup}</span></td>
                    <td>${d.age}</td>
                    <td>${d.phone}</td>
                    <td>
                        ${d.lastDonationDate || 'Never'}
                        <button class="btn-outline btn-small" style="margin-left: 10px;" onclick="app.showRecordDonationModal(${d.id})">+ Donate</button>
                    </td>
                </tr>
            `).join('');
        } catch (e) {}
    },

    async loadRequests() {
        const tbody = document.getElementById('requests-table-body');
        try {
            const reqs = await this.request('/blood-requests');
            tbody.innerHTML = reqs.map(r => `
                <tr>
                    <td>#${r.id}</td>
                    <td>${r.patientName}</td>
                    <td>${r.hospitalName}</td>
                    <td><b>${r.bloodGroup}</b></td>
                    <td>${r.units} Units</td>
                    <td>
                        <span class="status-badge status-${r.status.toLowerCase()}">${r.status}</span>
                    </td>
                    <td>
                        ${r.status === 'Pending' ? `
                            <button class="btn-primary btn-small" style="background: var(--success); margin-right: 0.5rem;" onclick="app.updateRequest(${r.id}, 'approve')">Approve</button>
                            <button class="btn-primary btn-small" onclick="app.updateRequest(${r.id}, 'reject')">Reject</button>
                        ` : '-'}
                    </td>
                </tr>
            `).join('');
        } catch (e) {}
    },

    async updateRequest(id, action) {
        if (!confirm(`Are you sure you want to ${action} this request?`)) return;
        try {
            await this.request(`/blood-requests/${id}/${action}`, { method: 'PUT' });
            this.loadRequests();
            this.loadInventory(); // Refresh stats
        } catch (e) {}
    },

    showAddDonorModal() {
        document.getElementById('add-donor-modal').classList.add('active');
    },

    showRequestBloodModal() {
        document.getElementById('request-blood-modal').classList.add('active');
    },

    showRecordDonationModal(donorId) {
        document.getElementById('donation-donor-id').value = donorId;
        document.getElementById('record-donation-modal').classList.add('active');
    },

    closeModal(id) {
        document.getElementById(id).classList.remove('active');
        const form = document.querySelector(`#${id} form`);
        if (form) form.reset();
    },

    async addDonor() {
        const donor = {
            fullName: document.getElementById('donor-name').value,
            age: parseInt(document.getElementById('donor-age').value),
            bloodGroup: document.getElementById('donor-bg').value,
            phone: document.getElementById('donor-phone').value,
            email: document.getElementById('donor-email').value,
            gender: 'Unknown'
        };

        try {
            await this.request('/donors', {
                method: 'POST',
                body: JSON.stringify(donor)
            });
            this.closeModal('add-donor-modal');
            this.loadDonors();
        } catch (e) {}
    },

    async requestBlood() {
        const requestData = {
            patientName: document.getElementById('req-patient').value,
            hospitalName: document.getElementById('req-hospital').value,
            bloodGroup: document.getElementById('req-bg').value,
            units: parseInt(document.getElementById('req-units').value),
            reason: document.getElementById('req-reason').value
        };

        try {
            await this.request('/blood-requests', {
                method: 'POST',
                body: JSON.stringify(requestData)
            });
            this.closeModal('request-blood-modal');
            this.loadRequests();
        } catch (e) {}
    },

    async recordDonation() {
        const donationData = {
            donorId: parseInt(document.getElementById('donation-donor-id').value),
            units: parseInt(document.getElementById('donation-units').value),
            collectionCenter: "Main Branch"
        };

        try {
            await this.request('/donations', {
                method: 'POST',
                body: JSON.stringify(donationData)
            });
            this.closeModal('record-donation-modal');
            this.loadDonors(); // refresh last donation date
            this.loadInventory(); // refresh inventory stats in background
            alert('Donation recorded successfully! Inventory updated.');
        } catch (e) {}
    }
};

app.init();
